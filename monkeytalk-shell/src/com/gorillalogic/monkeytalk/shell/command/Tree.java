/*  MonkeyTalk - a cross-platform functional testing tool
    Copyright (C) 2013 Gorilla Logic, Inc.

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
package com.gorillalogic.monkeytalk.shell.command;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.shell.Print;
import com.gorillalogic.monkeytalk.verify.Verify;

public class Tree extends BaseCommand {
	public Tree(String line, ScriptProcessor processor) {
		super(line, processor);
	}

	@Override
	public void run() throws JSONException {
		String[] parts = line.split("\\s+");
		String filter = (parts.length > 1 ? parts[1].toLowerCase() : null);

		Response resp = processor.getAgent().getCommandSender().dumpTree();
		JSONObject json = resp.getBodyAsJSON();

		if (json.has("message")) {
			JSONObject msg = json.getJSONObject("message");

			StringBuilder sb = new StringBuilder("TREE:").append(
					filter != null ? " filter=" + filter : "").append("\n");
			_tree(msg, filter, sb, "  ");
			Print.info(sb);
		} else {
			Print.err("ERROR: bad tree - " + json);
		}
	}

	private void _tree(JSONObject node, String filter, StringBuilder sb, String indent)
			throws JSONException {
		if (node.optBoolean("visible", false)) {
			String componentType = node.optString("ComponentType", "View");

			if (filter == null
					|| (filter != null && Verify
							.verifyWildcard(filter, componentType.toLowerCase()))) {
				sb.append(filter != null ? "  " : indent).append(componentType);
				sb.append("(").append(node.optString("monkeyId", "*")).append(")\n");
			}

			if (node.has("children")) {
				JSONArray children = node.getJSONArray("children");
				for (int i = 0; i < children.length(); i++) {
					JSONObject child = children.getJSONObject(i);
					_tree(child, filter, sb, indent + "  ");
				}
			}
		}
	}
}
