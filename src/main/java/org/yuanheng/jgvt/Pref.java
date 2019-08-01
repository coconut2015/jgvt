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

import java.util.prefs.Preferences;

/**
 * @author	Heng Yuan
 */
class Pref
{
	private final static String DEFAULT_DIRECTORY = "defaultDirectory";

	private final Preferences m_pref;

	public Pref ()
	{
		m_pref = Preferences.userNodeForPackage (Main.class);
	}

	public String getDefaultDirectory ()
	{
		return m_pref.get (DEFAULT_DIRECTORY, ".");
	}

	public void setDefaultDirectory (String dir)
	{
		m_pref.put (DEFAULT_DIRECTORY, dir);
	}

	public void sync ()
	{
		try
		{
			m_pref.sync ();
		}
		catch (Exception ex)
		{
		}
	}
}
