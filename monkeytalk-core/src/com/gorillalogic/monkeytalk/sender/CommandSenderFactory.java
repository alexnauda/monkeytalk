package com.gorillalogic.monkeytalk.sender;

import java.util.Hashtable;
import java.util.Map;

public class CommandSenderFactory {
	private static Map<String,Class> classMap = new Hashtable<String,Class>();
	private static final String DEFAULT_KEY = "default";
	
	static {
		registerDefaultClass(CommandSender.class);
	}
	
	public static void registerClass(String key, Class klass) {
		classMap.put(key,klass);
	}
	public static void registerDefaultClass(Class klass) {
		CommandSenderFactory.registerClass(CommandSenderFactory.DEFAULT_KEY, klass);
	}
	
	public static CommandSender createCommandSender(String host, int port) {
		return createCommandSender(DEFAULT_KEY, host, port, null);
	}
	
	public static CommandSender createCommandSender(String host, int port, String path) {
		return createCommandSender(DEFAULT_KEY, host, port, path);
	}
	
	public static CommandSender createCommandSender(String key, String host, int port, String path) {
		Class klass = classMap.get(key);
		if (klass==null) {
			throw new IllegalArgumentException(
					"no CommandSender registered with key '" + key + "'");
		}
		CommandSender commandSender = null;
		try {
			commandSender = (CommandSender)klass.newInstance();
			commandSender.init(null, host, port, path);
		} catch (Exception e) {
			// somebody registered something bad here
			throw new RuntimeException(e.getMessage());
		}
		return commandSender;
	}

}
