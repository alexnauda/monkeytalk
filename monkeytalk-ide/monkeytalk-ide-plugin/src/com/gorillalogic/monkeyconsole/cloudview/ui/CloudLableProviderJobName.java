package com.gorillalogic.monkeyconsole.cloudview.ui;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.forms.widgets.Hyperlink;

public class 	CloudLableProviderJobName extends StyledCellLabelProvider {
    MyHyperlink2 m_control;

    public CloudLableProviderJobName( Composite parent ) {
        m_control = new MyHyperlink2( parent, SWT.WRAP );
    }

    @Override 
    protected void paint( Event event, Object element ) {
        String sValue = ((JobRow)element).getJobName();
        m_control.setText( sValue );

        GC gc = event.gc;
        Rectangle cellRect = new Rectangle( event.x, event.y, event.width, event.height );
        cellRect.width = 4000;

        m_control.paintText( gc, cellRect);
    }
}

class MyHyperlink4 extends Hyperlink {
	
    public MyHyperlink4(Composite parent, int style) {
        super(parent, style);
        this.setUnderlined(false);
    }

    @Override
    public void paintText(GC gc, Rectangle bounds) {
        super.paintText(gc, bounds);
    }
}

