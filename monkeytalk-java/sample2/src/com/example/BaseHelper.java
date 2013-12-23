package com.example;

import java.io.File;
import java.io.IOException;

import org.junit.Before;

import com.gorillalogic.monkeytalk.java.MonkeyTalkDriver;
import com.gorillalogic.monkeytalk.java.api.Application;

public class BaseHelper {
	private MonkeyTalkDriver mt;
	protected Application app;

	@Before
	public void before() throws IOException {
		File dir = new File(".");

		// Android driver
		// mt = new MonkeyTalkDriver(dir, "AndroidEmulator");
		// mt.setAdb(new File("/Users/justin/dev/android-sdk-macosx/platform-tools/adb"));

		// iOS driver
		mt = new MonkeyTalkDriver(dir, "iOS");

		mt.setThinktime(10);
		app = mt.app();
	}
}
