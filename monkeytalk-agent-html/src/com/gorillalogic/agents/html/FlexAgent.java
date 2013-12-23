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
package com.gorillalogic.agents.html;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.gorillalogic.monkeytalk.sender.CommandSender;
import com.gorillalogic.monkeytalk.server.ServerConfig;

public class FlexAgent extends WebDriverAgent {
    String airlocation;
    Process flexsocketlistener;

	public FlexAgent() {
		super(ServerConfig.DEFAULT_PLAYBACK_PORT_FLEX);
	}
	@Override
	public String getName() {
		return "Flex";
	}

	@Override
	public void start() {
		super.start();

		putAirAppOnDisk();
		try {
		    // Execute a command without arguments
			String osName = System.getProperty("os.name");
			if(osName.contains("Mac")){
				String fileName = "/Applications/Adobe/Flash Player/AddIns/airappinstaller/airappinstaller";
				String[] command1 = {fileName, "-silent", airlocation};
				String command3 = "open -a /Applications/FlexSocketListener.app/Contents/MacOS/FlexsocketListener";
				Runtime.getRuntime().exec(command1);
				flexsocketlistener = Runtime.getRuntime().exec(command3);
		  
			} else if(osName.contains("Win")){ 
				String command3 = "C:/Program Files/FlexSocketListener/FlexSocketListener.exe";
				flexsocketlistener = Runtime.getRuntime().exec(command3);
			}
			else {
				String command3 = "cd /opt/FlexSocketListener/bin && ./FlexSocketListener";
				flexsocketlistener = Runtime.getRuntime().exec(command3);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		super.stop();

		try {
			Runtime.getRuntime().exec("killall FlexSocketListener");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void putAirAppOnDisk() {


		try {
			InputStream is = new BufferedInputStream(
					this.getClass().getClassLoader().getResourceAsStream("connectors/FlexSocketListener.air"));
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(new File(
							"FlexSocketListener.air"))));
			int c;
			while ((c = is.read()) != -1) {
				out.writeByte(c);
			}
			is.close();
			out.close();
			airlocation = new File(
				"FlexSocketListener.air").getAbsolutePath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	protected CommandSender createCommandSender(String host, int port) {
		return null;
		/*
        return new CommandSender(host, port) {

        	@Override
			public Response play(Command command) {
        		if(command.getComponentType().equalsIgnoreCase("Browser")){
				try {
					return scp.processACommand(command);
				} catch (IllegalArgumentException ex) {
					return new Response(HttpStatus.BAD_REQUEST.getCode(), ex.getMessage());
				} catch (Exception ex) {
					ex.printStackTrace();
					return new Response(HttpStatus.INTERNAL_ERROR.getCode(), ex.getClass().getName() + ": "  + ex.getMessage());
				}
        		} else {
        			return super.play(command);
        		}
			}
        	
			@Override
			public Response play(String componentType, String monkeyId,
					String action, List<String> args,
					Map<String, String> modifiers) {
        		return play(new Command(componentType, monkeyId, action, args, modifiers));
        	}
			
			@Override
			public Response ping(boolean recordOn, String recordHost, int recordPort) {
				try {
					FlexAgent.this.ping();
				} catch (Exception ex) {
					return new Response(HttpStatus.INTERNAL_ERROR.getCode(), "WebDriver not responding to ping: " + ex.getMessage());
				}
				return new Response();
			}
        };
        */
	}
	
}
