package com.gorillalogic.agents.utils;

public enum OS {
	WINDOWS, 
	MAC, 
	LINUX;	
	
	public static OS getOSType(String osPath) {
		if(osPath.toLowerCase().indexOf("mac") != -1) {
			return MAC;
		}
		else if (osPath.toLowerCase().indexOf("nix") != -1) {
			return LINUX;
		}
		else if (osPath.toLowerCase().indexOf("win") != -1) {
			return WINDOWS;
		}
		return null;
	}
}
