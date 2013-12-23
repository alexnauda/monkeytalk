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

import org.json.JSONException;
import org.json.JSONObject;

import com.gorillalogic.monkeytalk.processor.ScriptProcessor;
import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.shell.Print;

public class Ping extends BaseCommand {

	public Ping(String line, ScriptProcessor processor) {
		super(line, processor);
	}

	@Override
	public void run() throws JSONException {
		Response resp = processor.getAgent().getCommandSender().ping(false);
		JSONObject json = resp.getBodyAsJSON();

		if (json.has("message")) {
			JSONObject msg = json.getJSONObject("message");
			String os = msg.optString("os", "<unknown>");
			String ver = msg.optString("mtversion", "?");
			String rec = msg.optString("record", "?");

			String[] parts = ver.split(" - ");

			StringBuilder sb = new StringBuilder("PING:\n");
			sb.append("  ").append(os).append(" Agent\n");
			if (parts.length > 0) {
				sb.append("  v").append(parts[0]).append("\n");
			}
			if (parts.length > 1) {
				sb.append("  ").append(parts[1]).append("\n");
			}
			sb.append("  recording ").append(rec);
			Print.info(sb);
		} else {
			Print.err("ERROR: bad ping - " + json);
		}
	}
}
