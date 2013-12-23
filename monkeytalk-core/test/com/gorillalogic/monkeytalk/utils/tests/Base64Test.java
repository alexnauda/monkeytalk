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
package com.gorillalogic.monkeytalk.utils.tests;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import com.gorillalogic.monkeytalk.utils.Base64;

public class Base64Test {

	@Test
	public void testBase64() throws IOException {
		// hexdump -C foo.png
		String hex = "89 50 4e 47 0d 0a 1a 0a 00 00 00 0d 49 48 44 52"
				+ "00 00 00 02 00 00 00 02 08 02 00 00 00 fd d4 9a"
				+ "73 00 00 00 19 74 45 58 74 53 6f 66 74 77 61 72"
				+ "65 00 41 64 6f 62 65 20 49 6d 61 67 65 52 65 61"
				+ "64 79 71 c9 65 3c 00 00 00 17 49 44 41 54 78 da"
				+ "62 fa cf c0 c0 f0 9f 81 89 11 48 31 fe 07 08 30"
				+ "00 17 29 03 04 57 48 34 16 00 00 00 00 49 45 4e" + "44 ae 42 60 82";

		// base64 -i foo.png
		String expected = "iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAABdJREFUeNpi+s/AwPCfgYkRSDH+BwgwABcpAwRXSDQWAAAAAElFTkSuQmCC";

		String in = hex.replace(" ", "");
		byte[] bytesIn = DatatypeConverter.parseHexBinary(in);
		String base64 = Base64.encodeBytes(bytesIn);

		assertThat(base64, is(expected));
		
		byte[] bytesOut = Base64.decode(expected);
		String out = toHexString(bytesOut);
		assertThat(out,is(in));
	}

	@Test
	public void testBase64RoundTrip() throws IOException {
		for (int i = 0; i < 100; i++) {
			String in = UUID.randomUUID().toString();
			byte[] bytesIn = in.getBytes();
			String base64 = Base64.encodeBytes(bytesIn);
			byte[] bytesOut = Base64.decode(base64);
			String out = new String(bytesOut);

			assertThat(out, is(in));
		}
	}

	private String toHexString(byte[] bytes) {
		char[] a = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] c = new char[bytes.length * 2];
		int v;
		for (int j = 0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			c[j * 2] = a[v / 16];
			c[j * 2 + 1] = a[v % 16];
		}
		return new String(c);
	}
}