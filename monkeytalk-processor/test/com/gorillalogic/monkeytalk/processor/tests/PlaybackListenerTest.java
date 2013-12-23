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
package com.gorillalogic.monkeytalk.processor.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.gorillalogic.monkeytalk.processor.PlaybackListener;
import com.gorillalogic.monkeytalk.processor.PlaybackResult;
import com.gorillalogic.monkeytalk.processor.Scope;
import com.gorillalogic.monkeytalk.sender.Response;

public class PlaybackListenerTest {

	private static int counter;

	@Test
	public void testAnonymousPlaybackListener() {
		counter = 0;

		PlaybackListener listener = new PlaybackListener() {

			@Override
			public void onStart(Scope scope) {
				counter += 1;
			}

			@Override
			public void onScriptStart(Scope scope) {
				counter += 1000;
			}

			@Override
			public void onScriptComplete(Scope scope, PlaybackResult r) {
				counter += 100;
			}

			@Override
			public void onComplete(Scope scope, Response response) {
				counter += 10;
			}

			@Override
			public void onPrint(String message) {
			}
		};

		assertThat(listener, notNullValue());

		listener.onScriptStart(null);
		listener.onStart(null);
		listener.onComplete(null, new Response());
		listener.onScriptComplete(null, new PlaybackResult());

		assertThat(counter, is(1111));
	}
}