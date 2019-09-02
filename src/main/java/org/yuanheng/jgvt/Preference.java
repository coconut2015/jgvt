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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 * This class holds preferences that will need to be remembered.
 *
 * @author	Heng Yuan
 */
public class Preference
{
	private final static String FILE_PREFERENCE = ".jgvtconfig";

	private final static String KEY_EXPORT_DIRECTORY = "exportDirectory";
	private final static String KEY_ABBREV_LEN = "abbrevLength";
	private final static String KEY_BRANCH_SPACING = "branchSpacing";
	private final static String KEY_CHILD_SPACING = "childSpacing";
	private final static String KEY_START_X = "startX";
	private final static String KEY_START_Y = "startY";

	private static int getInt (Properties properties, String key, int min, int max, int defaultValue)
	{
		int value = Integer.MAX_VALUE;
		try
		{
			value = Integer.parseInt (properties.getProperty (key));
		}
		catch (Exception ex)
		{
		}
		if (value < min || value > max)
		{
			value = defaultValue;
		}
		return value;
	}

	private static double getDouble (Properties properties, String key, double min, double max, double defaultValue)
	{
		double value = Double.MAX_VALUE;
		try
		{
			value = Double.parseDouble (properties.getProperty (key));
		}
		catch (Exception ex)
		{
		}
		if (value < min || value > max)
		{
			value = defaultValue;
		}
		return value;
	}

	public static Preference getPreference (File gitRoot)
	{
		return new Preference (gitRoot);
	}

	private final File m_gitRoot;
	private Properties m_settings;
	private String m_exportDirectory;
	private int m_abbrevLength;
	private double m_branchSpacing = Defaults.BRANCH_SPACING;
	private double m_childSpacing = Defaults.CHILD_SPACING;
	private double m_startX = Defaults.START_X;
	private double m_startY = Defaults.START_Y;

	private Preference (File gitRoot)
	{
		m_gitRoot = gitRoot;

		Properties userProperties = new Properties ();
		Properties inGitProperties = userProperties;
		Properties gitDirProperties = inGitProperties;

		File userConfig = new File (new File (Utils.getUserDirectory ()), FILE_PREFERENCE);
		if (userConfig.isFile ())
		{
			try (FileReader reader = new FileReader (userConfig))
			{
				userProperties.load (reader);
			}
			catch (Exception ex)
			{
			}
		}

		if (gitRoot != null && gitRoot.isDirectory ())
		{
			File inGitConfig = new File (gitRoot, FILE_PREFERENCE);
			if (inGitConfig.isFile ())
			{
				try (FileReader reader = new FileReader (inGitConfig))
				{
					inGitProperties = new Properties (userProperties);
					inGitProperties.load (reader);
				}
				catch (Exception ex)
				{
				}
			}
			File jgvtDir = new File (gitRoot, Defaults.GIT_DIR_JGVT_DIR);
			File gitDirJgvtDirConfig = new File (jgvtDir, FILE_PREFERENCE);
			if (gitDirJgvtDirConfig.isFile ())
			{
				try (FileReader reader = new FileReader (gitDirJgvtDirConfig))
				{
					gitDirProperties = new Properties (inGitProperties);
					inGitProperties.load (reader);
				}
				catch (Exception ex)
				{
				}
			}
			gitDirProperties = inGitProperties;
		}
		m_settings = gitDirProperties;

		loadProperties ();
	}

	private void loadProperties ()
	{
		m_exportDirectory = m_settings.getProperty (KEY_EXPORT_DIRECTORY);
		if (m_exportDirectory == null || !(new File (m_exportDirectory)).isDirectory ())
		{
			m_exportDirectory = Utils.getUserDirectory ();
		}

		m_abbrevLength = getInt (m_settings, KEY_ABBREV_LEN, 1, 48, Defaults.DEFAULT_ABBREV_LEN);
		m_branchSpacing = getDouble (m_settings, KEY_BRANCH_SPACING, 10, 1000, Defaults.BRANCH_SPACING);
		m_childSpacing = getDouble (m_settings, KEY_CHILD_SPACING, 10, 1000, Defaults.CHILD_SPACING);
		m_startX = getDouble (m_settings, KEY_START_X, 10, 1000, Defaults.START_X);
		m_startY = getDouble (m_settings, KEY_START_Y, 10, 1000, Defaults.START_Y);
	}

	public boolean save ()
	{
		try
		{
			if (m_gitRoot == null ||
				!m_gitRoot.isDirectory ())
				return false;
			File jgvtConfigDir = new File (m_gitRoot, Defaults.GIT_DIR_JGVT_DIR);
			if (!jgvtConfigDir.isDirectory ())
			{
				if (!jgvtConfigDir.mkdir ())
					return false;
			}
			return save (new File (jgvtConfigDir, FILE_PREFERENCE));
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public boolean save (File file)
	{
		storeProperties ();
		try
		{
			m_settings.store (new FileWriter (file), "jgvt configuration");
			return true;
		}
		catch (IOException ex)
		{
			return false;
		}
	}

	private void storeProperties ()
	{
		m_settings.setProperty (KEY_EXPORT_DIRECTORY, m_exportDirectory);
		m_settings.setProperty (KEY_ABBREV_LEN, "" + m_abbrevLength);
		m_settings.setProperty (KEY_BRANCH_SPACING, "" + m_branchSpacing);
		m_settings.setProperty (KEY_CHILD_SPACING, "" + m_childSpacing);
		m_settings.setProperty (KEY_START_X, "" + m_startX);
		m_settings.setProperty (KEY_START_Y, "" + m_startY);
	}

	public String getExportDirectory ()
	{
		return m_exportDirectory;
	}

	public void setExportDirectory (String dir)
	{
		m_exportDirectory = dir;
	}

	public int getAbbrevLength ()
	{
		return m_abbrevLength;
	}

	public void setAbbrevLength (int abbrevLength)
	{
		m_abbrevLength = abbrevLength;
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
}
