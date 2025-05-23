/* Generated by Together */

package org.joone.edit;

import java.awt.*;
import java.util.*;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import CH.ifa.draw.figures.*;
import org.joone.engine.*;

public class ChartHandleLayerFigure extends OutputLayerFigure {
    
    static final long serialVersionUID = 5413936904067056132L;
    
    public ChartHandleLayerFigure() {
    }
    
    protected Vector addHandles(Vector handles) {
        handles.addElement(new ConnectionHandle(this, RelativeLocator.east(),
        new ChartHandleConnection()));
        return handles;
    }
    
    protected void initContent() {
        fPostConn = new Vector();
        fPreConn = new Vector();
        
        Font f = new Font("Helvetica", Font.PLAIN, 12);
        Font fb = new Font("Helvetica", Font.BOLD, 12);
        
        myOutputLayer = (OutputPatternListener)createLayer();
        
        TextFigure name = new UpdatableTextFigure() {
            public void setText(String newText) {
                super.setText(newText);
                getOutputLayer().setName(newText);
            }
            public void update() {
                setText(getOutputLayer().getName());
            }
        };
        
        name.setFont(fb);
        name.setText("Chart Handle " + ++numLayers);
        name.setAttribute("TextColor", Color.blue);
        add(name);
    }
    
    public boolean canConnect(GenericFigure start, ConnectionFigure conn) {
        return super.canConnect(start, conn);
    }
    
    public void addPostConn(LayerFigure layerFigure, OutputPatternListener synapse) {
        ChartingHandle handle = (ChartingHandle)getOutputLayer();
        handle.setChartSynapse((ChartInterface)synapse);
    }
    
    public void removePostConn(LayerFigure figure, OutputPatternListener conn) {
        this.removePostConn(figure);
        ChartingHandle handle = (ChartingHandle)getOutputLayer();
        handle.setChartSynapse(null);
    }
    
}
