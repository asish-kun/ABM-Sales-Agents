
package org.joone.edit;

import java.beans.*;

class EditedAdaptor implements PropertyChangeListener {

    EditedAdaptor(PropertySheetPanel t) {
	sink = t;
    }	

    public void propertyChange(PropertyChangeEvent evt) {
	sink.wasModified(evt);
    }

    PropertySheetPanel sink;
}
