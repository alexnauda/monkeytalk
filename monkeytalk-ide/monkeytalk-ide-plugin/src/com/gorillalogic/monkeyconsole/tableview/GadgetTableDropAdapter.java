package com.gorillalogic.monkeyconsole.tableview;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;

public class GadgetTableDropAdapter implements DropTargetListener {
    TableViewer tv;
	public GadgetTableDropAdapter(TableViewer tv) {
		this.tv = tv;
	}

	@Override
	public void dragEnter(DropTargetEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragLeave(DropTargetEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOperationChanged(DropTargetEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOver(DropTargetEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drop(DropTargetEvent arg0) {
		System.out.println("dropxdrdsfg");

	}

	@Override
	public void dropAccept(DropTargetEvent arg0) {
		// TODO Auto-generated method stub

	}

}
