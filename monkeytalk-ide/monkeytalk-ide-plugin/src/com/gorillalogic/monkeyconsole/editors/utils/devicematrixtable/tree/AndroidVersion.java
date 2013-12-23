package com.gorillalogic.monkeyconsole.editors.utils.devicematrixtable.tree;

import java.util.ArrayList;
import java.util.List;

public class AndroidVersion {
	private String name;
	private int sort;
	private List<Resolution> todos = new ArrayList<Resolution>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getSort() {
		return sort;
	}

	public void setSort(int sort) {
		this.sort = sort;
	}

	public List<Resolution> getTodos() {
		return todos;
	}
	
	@Override
	public String toString() {
		return "AndroidVersion: name=" + getName() + " children=" + todos;
	}
}