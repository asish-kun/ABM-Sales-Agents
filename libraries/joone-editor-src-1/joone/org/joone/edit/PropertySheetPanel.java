package org.joone.edit;

import java.beans.*;
import java.lang.reflect.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

class PropertySheetPanel extends JPanel {

    private JFrame frame;
    private Wrapper targetWrapper;
    private Object target;
    private PropertyDescriptor[] properties;
    private PropertyEditor[] editors;
    private Object[] values;
    private JComponent[] views;
    private JLabel[] labels;
    private boolean processEvents;
    private boolean skipNextValidation = false;
    private static int hPad =4;
    private static int vPad =4;
    private GridLayout gl;

    private static final long serialVersionUID = -3252002549205917685L;

    private boolean enabled;

    PropertySheetPanel(JFrame frame) {
        this.frame = frame;
        gl = new GridLayout();
        gl.setColumns(2);
        gl.setHgap(5);
        gl.setVgap(5);
        setLayout(gl);
    }

    synchronized void setTarget(Wrapper targ) {

        removeAll();
        targetWrapper = targ;
        target = targ.getBean();

        try {
            BeanInfo bi = Introspector.getBeanInfo(target.getClass());
            PropertyDescriptor[] pd = bi.getPropertyDescriptors();

            // Add properties to SortedMap with lower case name as the key.
            TreeMap tm = new TreeMap();
            for (int i = 0; i < pd.length; i++) {
                tm.put(pd[i].getName().toLowerCase(), pd[i]);
            }

            // Get the properties in name order.
            int i = 0;
            properties = new PropertyDescriptor[pd.length];
            while (!tm.isEmpty()) {
                properties[i++] = (PropertyDescriptor)tm.remove(tm.firstKey());
            }
        } catch (IntrospectionException ex) {
            error("PropertySheet: Couldn't introspect", ex);
            return;
        }

        editors = new PropertyEditor[properties.length];
        values = new Object[properties.length];
        views = new JComponent[properties.length];
        labels = new JLabel[properties.length];

        // Create an event adaptor.
        EditedAdaptor adaptor = new EditedAdaptor(this);
        gl.setRows(properties.length);
        gl.setColumns(2);
        int addedProperties = 0;
        for (int i = 0; i < properties.length; i++) {

            // Don't display hidden or expert properties.
            if (properties[i].isHidden() || properties[i].isExpert()) {
                continue;
            }

            String name = properties[i].getDisplayName();
            Class type = properties[i].getPropertyType();
            Method getter = properties[i].getReadMethod();
            Method setter = properties[i].getWriteMethod();

            // Only display read/write properties.
            if (getter == null || setter == null) {
                continue;
            }

            JComponent view = null;

            try {
                Object args[] = { };
                Object value = getter.invoke(target, args);
                values[i] = value;

                PropertyEditor editor = null;
                Class pec = properties[i].getPropertyEditorClass();
                if (pec != null) {
                    try {
                        editor = (PropertyEditor)pec.newInstance();
                    } catch (Exception ex) {
                        // Drop through.
                    }
                }
                if (editor == null) {
                    editor = PropertyEditorManager.findEditor(type);
                }
                editors[i] = editor;

                // If we can't edit this component, skip it.
                if (editor == null) {
                    // If it's a user-defined property we give a warning.
                    String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();
                    if (getterClass.indexOf("java.") != 0) {
                        System.err.println("Warning: Can't find public property editor for property \""
                        + name + "\".  Skipping.");
                    }
                    continue;
                }

                // Don't try to set null values:
                if (value == null) {
                    // If it's a user-defined property we give a warning.
                    String getterClass = properties[i].getReadMethod().getDeclaringClass().getName();
                    if (getterClass.indexOf("java.") != 0) {
                        System.err.println("Warning: Property \"" + name
                        + "\" has null initial value.  Skipping.");
                    }
                    continue;
                }

                editor.setValue(value);
                editor.addPropertyChangeListener(adaptor);

                // Now figure out how to display it...
                if (editor.isPaintable() && editor.supportsCustomEditor()) {
                    view = new PropertyCanvas(frame, editor);
                } else if (editor.getTags() != null) {
                    view = new PropertySelector(editor);
                } else if (editor.getAsText() != null) {
                    String init = editor.getAsText();
                    PropertyText pt = new PropertyText(init);
                    pt.setEditor(editor);
                    view = pt;
                } else {
                    System.err.println("Warning: Property \"" + name
                    + "\" has non-displayabale editor.  Skipping.");
                    continue;
                }

            } catch (InvocationTargetException ex) {
                System.err.println("Skipping property " + name + " ; exception on target: " + ex.getTargetException());
                ex.getTargetException().printStackTrace();
                continue;
            } catch (Exception ex) {
                System.err.println("Skipping property " + name + " ; exception: " + ex);
                ex.printStackTrace();
                continue;
            }
            Font f = new Font("Monospaced", Font.PLAIN, 12);
            labels[i] = new JLabel(name, JLabel.RIGHT);
            labels[i].setFont(f);
            add(labels[i]);

            views[i] = view;
            view.setFont(f);
            add(views[i]);
            addedProperties++;
        }
        processEvents = true;
        gl.setRows(addedProperties);
    }

    synchronized void setCustomizer(Customizer c) {
        if (c != null) {
            c.addPropertyChangeListener(new EditedAdaptor(this));
        }
    }

    synchronized void wasModified(PropertyChangeEvent evt) {

        if (!processEvents) {
            return;
        }

        // Skip validation if we have just returned focus from an error.
        if (skipNextValidation) {
            skipNextValidation = false;
            return;
        }

        if (evt.getSource() instanceof PropertyEditor) {
            PropertyEditor editor = (PropertyEditor) evt.getSource();
            for (int i = 0 ; i < editors.length; i++) {
                if (editors[i] == editor) {
                    PropertyDescriptor property = properties[i];
                    Object value = editor.getValue();
                    values[i] = value;
                    Method setter = property.getWriteMethod();
                    try {
                        Object args[] = { value };
                        args[0] = value;
                        setter.invoke(target, args);

                        // We add the changed property to the targets wrapper
                        // so that we know precisely what bean properties have
                        // changed for the target bean and we're able to
                        // generate initialization statements for only those
                        // modified properties at code generation time.
                        targetWrapper.getChangedProperties().addElement(properties[i]);
                        targetWrapper.updateFigure();

                    } catch (InvocationTargetException ex) {
                        if (ex.getTargetException()
                        instanceof PropertyVetoException) {
                            //warning("Vetoed; reason is: "
                            //        + ex.getTargetException().getMessage());
                            // temp dealock fix...I need to remove the deadlock.
                            System.err.println("WARNING: Vetoed; reason is: "
                            + ex.getTargetException().getMessage());
                            return;
                        }
                        else {
                            error("InvocationTargetException while updating "
                            + property.getName(), ex.getTargetException());
                            return;
                        }
                    }
                    catch (Exception ex) {
                        error("Unexpected exception while updating "
                        + property.getName(), ex);
                        return;
                    }
                    if (views[i] != null && views[i] instanceof PropertyCanvas) {
                        views[i].repaint();
                    }
                    break;
                }
            }
        }

        // Now re-read all the properties and update the editors
        // for any other properties that have changed.
        this.update();

        // Make sure the target bean gets repainted.
        if (Beans.isInstanceOf(target, JComponent.class)) {
            ((JComponent)(Beans.getInstanceOf(target, JComponent.class))).repaint();
        }
    }

    public void update() {
        for (int i = 0; i < properties.length; i++) {
            Object o;
            try {
                Method getter = properties[i].getReadMethod();
                Object args[] = { };
                o = getter.invoke(target, args);
            } catch (Exception ex) {
                o = null;
            }
            if ((o == values[i]) || (o != null && o.equals(values[i]))) {
                // The property is equal to its old value.
                continue;
            }
            values[i] = o;
            // Make sure we have an editor for this property...
            if (editors[i] == null) {
                continue;
            }
            // The property has changed!  Update the editor.
            editors[i].setValue(o);
            if (views[i] != null) {
                if (views[i] instanceof PropertyText) {
                    PropertyText pt = (PropertyText)views[i];
                    pt.setText(editors[i].getAsText());
                }
                   
                views[i].repaint();
            }
        }
    }

    private void warning(String s) {
        // Validation will be attempted on return from focus of the ErrorDialog if this is not set.
        skipNextValidation = true;
        new ErrorDialog(frame, "Warning: " + s);
    }

    private void error(String message, Throwable th) {
        // Validation will be attempted on return from focus of the ErrorDialog if this is not set.
        skipNextValidation = true;
        String mess = message + ":\n" + th;
        System.err.println(message);
        th.printStackTrace();
        // Popup an ErrorDialog with the given error message.
        new ErrorDialog(frame, mess);

    }

    /** Getter for property enabled.
     * @return Value of property enabled.
     */
    public boolean isControlsEnabled() {
        return enabled;
    }

    /** Setter for property enabled.
     * @param enabled New value of property enabled.
     */
    public void setControlsEnabled(boolean enabled) {
        this.enabled = enabled;
        for (int i=0; i < views.length; ++i)
            if (views[i] != null)
                views[i].setEnabled(enabled);
    }
}

