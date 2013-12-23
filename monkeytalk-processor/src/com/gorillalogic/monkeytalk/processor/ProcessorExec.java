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
package com.gorillalogic.monkeytalk.processor;

import java.io.IOException;
import java.util.List;

import com.gorillalogic.monkeytalk.sender.Response;
import com.gorillalogic.monkeytalk.sender.Response.ResponseStatus;
import com.gorillalogic.monkeytalk.utils.exec.*;

/**
 * Helper class for System.Exec to execute commands on the system.
 */
public class ProcessorExec {

	/**
	 * Execute the given command set, block until it returns, read the result, and return it. Throws
	 * an {@code IOException} for any non-zero exit code, but includes the error output as the
	 * message.
	 * 
	 * @param cmds
	 *            the command set to execute
	 * @return the result of executing the given command
	 * @throws IOException
	 */
	private static String run(String[] cmds) throws IOException {
		ExecResult result = Exec.run(cmds);
		
		// retrieve the output
		String output = result.getStdout();
		String err = result.getStderr();

		// combine stdout and stderr so we don't miss anything
		String out = output + (output.length() > 0 && err.length() > 0 ? "\n" : "") + err;

		if (result.getExitValue() == 0) {
			// all good, return the output
			return out;
		} else {
			// uh oh, throw exception for non-zero exit code with output as msg
			throw new IOException(out + (out.length() > 0 ? "\n" : "") + "err " + result.getExitValue());
		}
	}

	/**
	 * Execute the given command set, and return the result wrapped in a {@link Response} object. If
	 * the underlying command throws an {@code IOException} catch it and return an error
	 * {@code Response} object instead.
	 * 
	 * @param cmds
	 *            the command set to execute
	 * @return the result wrapped in a Response
	 */
	public static Response run(List<String> cmds) {
		Response resp = new Response();
		try {
			String output = run(cmds.toArray(new String[] {}));
			resp = new Response(ResponseStatus.OK, output, null, null);
		} catch (IOException ex) {
			resp = new Response(ResponseStatus.ERROR, ex.getMessage(), null, null);
		}
		return resp;
	}

}
