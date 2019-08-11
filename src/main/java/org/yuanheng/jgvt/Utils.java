/*
 * Copyright (c) 2019 Heng Yuan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.yuanheng.jgvt;

import java.awt.Desktop;
import java.io.*;
import java.lang.reflect.Array;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author	Heng Yuan
 */
class Utils
{
	public static String getString (InputStream is) throws IOException
	{
		Reader reader = new InputStreamReader (is);
		char[] buf = new char[4096];
		int len;
		StringBuilder builder = new StringBuilder ();
		while ((len = reader.read (buf)) >= 0)
		{
			builder.append (buf, 0, len);
		}
		return builder.toString ();
	}

	public static Path getRelativePath (File file, File root) throws IOException
	{
		file = file.getCanonicalFile ();
		root = root.getCanonicalFile ();
		Path filePath = Paths.get (file.toURI ());
		Path rootPath = Paths.get (root.toURI ());
		return rootPath.relativize (filePath);
	}

	public static <T> T[] arrayAdd (Class<T> c, T[] oldArray, T elem)
	{
		@SuppressWarnings("unchecked")
		T[] newArray = (T[])Array.newInstance (c, oldArray.length + 1);
		for (int i = 0; i < oldArray.length; ++i)
			newArray[i] = oldArray[i];
		newArray[oldArray.length] = elem;
		return newArray;
	}

	/**
	 * Open a URL in the desktop browser.
	 *
	 * @param	url
	 * 			the URL to be opened.
	 */
	public static void browse (URI uri)
	{
		if (Desktop.isDesktopSupported () &&
			Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
		{
			try
			{
				Desktop.getDesktop().browse(uri);
			}
			catch (Exception ex)
			{
			}
		}
	}
}
