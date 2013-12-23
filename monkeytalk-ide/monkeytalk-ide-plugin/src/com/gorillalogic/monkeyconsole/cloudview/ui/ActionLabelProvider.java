package com.gorillalogic.monkeyconsole.cloudview.ui;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class 	ActionLabelProvider extends StyledCellLabelProvider {
    MyHyperlink m_control;

    public ActionLabelProvider( Composite parent ) {
        m_control = new MyHyperlink( parent, SWT.WRAP );
        m_control.addListener(SWT.Selection, new Listener() {
        	public void handleEvent(Event event) {
        		System.out.println("You have selected: "+event.text);
        		}
        		});
    }

    @Override 
    protected void paint( Event event, Object element ) {
        String sValue = ((JobRow)element).getActions();
        m_control.setText( sValue );

        GC gc = event.gc;
        Rectangle cellRect = new Rectangle( event.x, event.y, event.width, event.height );
        cellRect.width = 4000;
        
        m_control.paintText( gc, cellRect);
    }
}

class MyHyperlink extends Hyperlink {
	
    public MyHyperlink(Composite parent, int style) {
        super(parent, style);
        this.setUnderlined(true);
    }

    @Override
    public void paintText(GC gc, Rectangle bounds) {
        super.paintText(gc, bounds);
    }
}

