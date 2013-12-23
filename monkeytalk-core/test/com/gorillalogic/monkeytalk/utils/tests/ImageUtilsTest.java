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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.Test;

import com.gorillalogic.monkeytalk.utils.FileUtils;
import com.gorillalogic.monkeytalk.utils.ImageUtils;

public class ImageUtilsTest {
	private static final File TEST_DIR = new File(new File("resources"), "test");
	private static final File GORILLA = new File(TEST_DIR, "gorilla.png");
	private static final String GORILLA_PATH = GORILLA.getAbsolutePath();

	@Test
	public void testColorCompare() throws Exception {
		Method m = ImageUtils.class.getDeclaredMethod("equalsWithTolerance", Integer.TYPE,
				Integer.TYPE, Integer.TYPE);
		m.setAccessible(true);

		Boolean b = (Boolean) m.invoke(null, 0, 0, 0);
		assertThat(b, is(true));

		b = (Boolean) m.invoke(null, 0, 0, 1);
		assertThat(b, is(true));

		b = (Boolean) m.invoke(null, 0, 1, 0);
		assertThat(b, is(false));

		b = (Boolean) m.invoke(null, 0, 1, 1);
		assertThat(b, is(true));

		// special alpha test
		Color c1 = new Color(0, 0, 0, 200);
		Color c2 = new Color(0, 0, 0, 205);

		b = (Boolean) m.invoke(null, c1.getRGB(), c2.getRGB(), 4);
		assertThat(b, is(false));

		b = (Boolean) m.invoke(null, c1.getRGB(), c2.getRGB(), 5);
		assertThat(b, is(true));
	}

	@Test
	public void testImageIO() throws IOException {
		BufferedImage png = ImageUtils.getImageFromFile(GORILLA_PATH);
		assertThat(png, notNullValue());
		assertThat(png.getWidth(), is(153));
		assertThat(png.getHeight(), is(69));

		BufferedImage png2 = ImageUtils.getImageFromFile(GORILLA);
		assertThat(png2, notNullValue());
		assertThat(png2.getWidth(), is(153));
		assertThat(png2.getHeight(), is(69));

		File dir = FileUtils.tempDir();
		File f = new File(dir, "gorilla.png");

		boolean saved = ImageUtils.savePNG(f.getAbsolutePath(), png);
		assertThat(saved, is(true));
		assertThat(f.exists(), is(true));
		assertThat(f.isFile(), is(true));
		f.delete();
	}

	@Test
	public void testGetDimensions() throws IOException {
		BufferedImage png = ImageUtils.getImageFromFile(GORILLA);
		assertThat(ImageUtils.getDimensions(null), nullValue());

		Dimension d = ImageUtils.getDimensions(png);
		assertThat(d, notNullValue());
		assertThat(d.width, is(153));
		assertThat(d.height, is(69));
	}

	@Test
	public void testCompare() throws IOException {
		// all pixels are: r=0,g=0,b=0,a=255
		BufferedImage black = ImageUtils.getImageFromFile(new File(TEST_DIR, "black.png"));

		// one pixel is 200,0,0,255
		BufferedImage red = ImageUtils.getImageFromFile(new File(TEST_DIR, "black-red.png"));

		// one pixel is 0,200,0,255
		BufferedImage green = ImageUtils.getImageFromFile(new File(TEST_DIR, "black-green.png"));

		// one pixel is 0,0,200,255
		BufferedImage blue = ImageUtils.getImageFromFile(new File(TEST_DIR, "black-blue.png"));

		// one pixel is 0,0,0,55
		BufferedImage alpha = ImageUtils.getImageFromFile(new File(TEST_DIR, "black-alpha.png"));

		// one of each above pixels: black, red, green, blue, alpha
		BufferedImage rainbow = ImageUtils.getImageFromFile(new File(TEST_DIR, "rainbow.png"));

		assertThat(black, notNullValue());
		assertThat(black.getWidth(), is(5));
		assertThat(black.getHeight(), is(1));
		assertThat(ImageUtils.compare(black, black), is(true));
		assertThat(ImageUtils.compare(black, red), is(false));

		assertThat(ImageUtils.compare(black, red, 199), is(false));
		assertThat(ImageUtils.compare(black, red, 200), is(true));
		assertThat(ImageUtils.compare(red, black, 199), is(false));
		assertThat(ImageUtils.compare(red, black, 200), is(true));

		assertThat(ImageUtils.compare(black, green, 199), is(false));
		assertThat(ImageUtils.compare(black, green, 200), is(true));
		assertThat(ImageUtils.compare(green, black, 199), is(false));
		assertThat(ImageUtils.compare(green, black, 200), is(true));

		assertThat(ImageUtils.compare(black, blue, 199), is(false));
		assertThat(ImageUtils.compare(black, blue, 200), is(true));
		assertThat(ImageUtils.compare(blue, black, 199), is(false));
		assertThat(ImageUtils.compare(blue, black, 200), is(true));

		assertThat(ImageUtils.compare(black, alpha, 199), is(false));
		assertThat(ImageUtils.compare(black, alpha, 200), is(true));
		assertThat(ImageUtils.compare(alpha, black, 199), is(false));
		assertThat(ImageUtils.compare(alpha, black, 200), is(true));

		assertThat(ImageUtils.compare(black, rainbow, 199), is(false));
		assertThat(ImageUtils.compare(black, rainbow, 200), is(true));
		assertThat(ImageUtils.compare(rainbow, black, 199), is(false));
		assertThat(ImageUtils.compare(rainbow, black, 200), is(true));
	}
}