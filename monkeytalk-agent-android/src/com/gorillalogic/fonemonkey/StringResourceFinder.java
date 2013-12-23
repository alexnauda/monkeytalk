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
package com.gorillalogic.fonemonkey;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class StringResourceFinder
{
	private static Map<CharSequence, String> textMap;

	// Not necessary to synchronize as always will be called from UI thread
	private static void init(Context context)
	{
		if (textMap != null) return;

		textMap = new HashMap<CharSequence, String>();

		try
		{
			Class klass = Class.forName(context.getPackageName() + ".R$string");

			Field[] fields = klass.getDeclaredFields();

			for (int i=0; i < fields.length; ++i)
			{
				if (fields[i].isSynthetic()) continue;

				Integer id = (Integer) fields[i].get(null);

				String idName = context.getResources().getResourceName(id);

				textMap.put(context.getText(id), idName);
			}
		}
		// Should never happen
		catch (Exception e) { throw new RuntimeException(e); }
	}

	public static String getIdName(Context context, CharSequence text)
	{
		init(context);

		return textMap.get(text);
	}
}
