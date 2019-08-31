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
 * This class holds preferences that will need to be remembered.
 *
 * @author	Heng Yuan
 */
public class Pref
{
	private final static String KEY_EXPORT_DIRECTORY = "exportDirectory";
	private final static String KEY_ABBREV_LEN = "abbrevLength";
	private final static String KEY_BRANCH_SPACING = "branchSpacing";
	private final static String KEY_CHILD_SPACING = "childSpacing";
	private final static String KEY_START_X = "startX";
	private final static String KEY_START_Y = "startY";

	private final Preferences m_pref;

	private String m_exportDirectory = ".";
	private int m_abbrevLength = Defaults.DEFAULT_ABBREV_LEN;
	private double m_branchSpacing = Defaults.BRANCH_SPACING;
	private double m_childSpacing = Defaults.CHILD_SPACING;
	private double m_startX = Defaults.START_X;
	private double m_startY = Defaults.START_Y;

	public Pref ()
	{
		m_pref = Preferences.userNodeForPackage (Main.class);
		loadPref ();
	}

	private void loadPref ()
	{
		m_exportDirectory = m_pref.get (KEY_EXPORT_DIRECTORY, Utils.getUserDirectory ());
		m_abbrevLength = m_pref.getInt (KEY_ABBREV_LEN, Defaults.DEFAULT_ABBREV_LEN);
		m_branchSpacing = m_pref.getDouble (KEY_BRANCH_SPACING, Defaults.BRANCH_SPACING);
		m_childSpacing = m_pref.getDouble (KEY_CHILD_SPACING, Defaults.CHILD_SPACING);
		m_startX = m_pref.getDouble (KEY_START_X, Defaults.START_X);
		m_startY = m_pref.getDouble (KEY_START_Y, Defaults.START_Y);
	}

	public String getExportDirectory ()
	{
		return m_exportDirectory;
	}

	public void setExportDirectory (String dir)
	{
		m_exportDirectory = dir;
		m_pref.put (KEY_EXPORT_DIRECTORY, dir);
	}

	public int getAbbrevLength ()
	{
		return m_abbrevLength;
	}

	public void setAbbrevLength (int abbrevLength)
	{
		m_abbrevLength = abbrevLength;
		m_pref.putInt (KEY_ABBREV_LEN, abbrevLength);
	}

	public double getBranchSpacing ()
	{
		return m_branchSpacing;
	}

	public void setBranchSpacing (double branchSpacing)
	{
		m_branchSpacing = branchSpacing;
	}

	public double getChildSpacing ()
	{
		return m_childSpacing;
	}

	public void setChildSpacing (double childSpacing)
	{
		m_childSpacing = childSpacing;
	}

	public double getStartX ()
	{
		return m_startX;
	}

	public void setStartX (double startX)
	{
		m_startX = startX;
	}

	public double getStartY ()
	{
		return m_startY;
	}

	public void setStartY (double startY)
	{
		m_startY = startY;
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
