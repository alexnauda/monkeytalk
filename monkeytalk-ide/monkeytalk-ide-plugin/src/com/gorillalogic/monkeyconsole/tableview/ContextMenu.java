/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2012 Gorilla Logic, Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */
package com.gorillalogic.monkeyconsole.tableview;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;

import com.gorillalogic.monkeyconsole.preferences.PreferenceConstants;

/**
 * The menu that happens on right click of a table cell
 * @author digitalogic8
 *
 */
public class ContextMenu {
	MonkeyTalkTabularEditor dmconsole;
	TableItem item;
	Event event;
	Point point;

	public ContextMenu(MonkeyTalkTabularEditor dmconsole, Event event)
	{
		this.dmconsole = dmconsole;
		this.event = event;
		
		point = dmconsole.getTv().getTable().toControl(event.x, event.y);
		
		item =  dmconsole.getTv().getTable().getItem(point);
		
	}
	
	public void show()
	{
		Menu menu = new Menu(dmconsole.getSite().getShell(), SWT.POP_UP); 
		MenuItem appendItem = new MenuItem (menu, SWT.PUSH);
		appendItem.setText("Append Row"); 
		appendItem.addSelectionListener(new SelectionAdapter()
		{ 
			public void widgetSelected(SelectionEvent event)
			{ 
				dmconsole.appendRow();
			}
		});
		if (item != null){
		final int row =  dmconsole.getCommands().indexOf(item.getData());
		
		int num = dmconsole.getTv().getTable().getSelectionCount();
		
		
		if(num > 0){
			//insert
			MenuItem insertItem = new MenuItem (menu, SWT.PUSH);
			insertItem.setText("Insert Row Above"); 
			insertItem.addSelectionListener(new SelectionAdapter()
			{ 
				public void widgetSelected(SelectionEvent event)
				{ 
					dmconsole.insertRow(row);
				}
			});	
	    //Delete Row
		MenuItem menuItem = new MenuItem (menu, SWT.PUSH);
		menuItem.setText("Delete Row" + (num > 1 ? "s" : "")); 
		menuItem.addSelectionListener(new SelectionAdapter()
		{ 
			public void widgetSelected(SelectionEvent event)
			{ 
				dmconsole.deleteRows(dmconsole.getTv().getTable().getSelectionIndices());
			}
		});
		
		
		//PlaySelected
		MenuItem playMenuItem = new MenuItem (menu, SWT.PUSH);
		playMenuItem.setText("Play Row" + (num > 1 ? "s" : "")); 
		playMenuItem.setEnabled(dmconsole.getMonkeyControls().isCurrentlyConnected() && !dmconsole.getMonkeyControls().isRecordingON() && dmconsole.getLimitedComponentSet() == null);
		playMenuItem.addSelectionListener(new SelectionAdapter()
		{ 
			public void widgetSelected(SelectionEvent event)
			{ 
				dmconsole.getMonkeyControls().startReplayRange(dmconsole.getTv().getTable().getSelectionIndex(), dmconsole.getTv().getTable().getSelectionIndex() + dmconsole.getTv().getTable().getSelectionCount());
			}
		});
		
			
		}
		}

		
		menu.setLocation(event.x, event.y);
		menu.setVisible(true);
	}
}