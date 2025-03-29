
package org.joone.edit;

import java.beans.*;
import java.awt.*;
import javax.swing.*;
import org.joone.net.*;
import CH.ifa.draw.util.*;
import CH.ifa.draw.framework.*;

public class PropertySheet extends JFrame {
    private PropertySheetPanel panel;
    private boolean started;
    protected Wrapper target;
    protected NeuralNet nNet;
    private static final long serialVersionUID = -3520314914318159702L;
    
    private boolean enabled;
    
    PropertySheet(int x, int y) {
        this(null, x, y, null);
    }
    
    PropertySheet(Wrapper wr, int x, int y) {
        this(wr, x, y, null);
    }
    
    PropertySheet(Wrapper wr, int x, int y, NeuralNet nn) {
        super("Properties - <initializing...>");
        target = wr;
        nNet = nn;
        if (target != null)
            setTarget(wr);
        initialize(x, y);
    }
    
    protected void initialize(int x, int y) {
        getContentPane().setLayout(new BorderLayout());
        setBackground(Color.gray);
        setLocation(x,y);
        getContentPane().add(getContents(), BorderLayout.NORTH);
        setTitle("Properties window");
        Iconkit kit = Iconkit.instance();
        if (kit == null)
            throw new HJDError("Iconkit instance isn't set");
        final Image img = kit.loadImageResource(JoonEdit.DIAGRAM_IMAGES + "JooneIcon.gif");
        this.setIconImage(img);
        started = true;
    }
    
    protected JComponent getContents() {
        JPanel activePanel = new JPanel();
        activePanel.setAlignmentX(LEFT_ALIGNMENT);
        activePanel.setAlignmentY(TOP_ALIGNMENT);
        activePanel.setLayout(new BorderLayout());
        activePanel.add(getPanel(), BorderLayout.NORTH);
        return activePanel;
    }
    
    public void setTarget(Wrapper targ) {
        target = targ;
        String displayName = target.getBeanName();
        getPanel().setTarget(target);
        setTitle("Properties - " + displayName);
    }
    
    
    void setCustomizer(Customizer c) {
        getPanel().setCustomizer(c);
    }
    
    void wasModified(PropertyChangeEvent evt) {
        getPanel().wasModified(evt);
    }
    
    /** Getter for property panel.
     * @return Value of property panel.
     */
    protected PropertySheetPanel getPanel() {
        if (panel == null)
            panel = new PropertySheetPanel(this);
        return panel;
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
        getPanel().setControlsEnabled(enabled);
    }
    
    public void update() {
        getPanel().update();
    }
}

