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
package com.gorillalogic.monkeyconsole.actions;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

public class DropDownMenuAction extends Action implements IMenuCreator {
	private Menu menu;
	private List<Action> actions;
	private Action selectedAction;

	public DropDownMenuAction(List<Action> actions) {
		super("", IAction.AS_DROP_DOWN_MENU | SWT.NONE);
		this.actions = actions;
		setMenuCreator(this);
		if (actions.size() > 0) {
			this.setSelectedAction(actions.get(0));
		}

	}

	public Action getSelectedAction() {
		return selectedAction;
	}

	public void setSelectedAction(Action selectedAction) {
		this.selectedAction = selectedAction;
		this.setToolTipText(selectedAction.getToolTipText());
		this.setImageDescriptor(selectedAction.getImageDescriptor());
		this.setText(selectedAction.getText());

	}

	public void dispose() {
		if (menu != null) {
			menu.dispose();
			menu = null;
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (menu != null)
			menu.dispose();
		menu = new Menu(parent.getShell(), SWT.POP_UP | SWT.NONE);
		for (Action t : actions)
			addActionToMenu(menu, t);
		return menu;
	}

	protected void addActionToMenu(Menu parent, Action action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	@Override
	public void setEnabled(boolean enabled) {
		for (Action a : actions) {
			a.setEnabled(enabled);
		}
		if(selectedAction != null)
		selectedAction.setEnabled(enabled);
		super.setEnabled(enabled);
	}

	@Override
	public void run() {
		if (selectedAction!=null) {
			selectedAction.run();
		}
	}

	void clear() {
		dispose();
	}
}