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
	public enum SaveType
	{
		UserHome,
		GitDir,
		Repo
	}

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

	public static Preference getPreference (GitRepo gitRepo)
	{
		return new Preference (gitRepo);
	}

	private final GitRepo m_gitRepo;
	private Properties m_settings;
	private String m_exportDirectory;
	private int m_abbrevLength;
	private double m_branchSpacing = Defaults.BRANCH_SPACING;
	private double m_childSpacing = Defaults.CHILD_SPACING;
	private double m_startX = Defaults.START_X;
	private double m_startY = Defaults.START_Y;

	private Preference (GitRepo gitRepo)
	{
		m_gitRepo = gitRepo;
		File gitRoot = null;
		File gitDir = null;

		if (gitRepo != null)
		{
			gitRoot = gitRepo.getRoot ();
			gitDir = gitRepo.getGitDir ();
		}

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
		}

		File jgvtDir = new File (gitDir, Defaults.GIT_DIR_JGVT_DIR);
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
		m_branchSpacing = getDouble (m_settings, KEY_BRANCH_SPACING,  Defaults.MIN_BRANCH_SPACING,  Defaults.MAX_BRANCH_SPACING, Defaults.BRANCH_SPACING);
		m_childSpacing = getDouble (m_settings, KEY_CHILD_SPACING, Defaults.MIN_CHILD_SPACING, Defaults.MAX_CHILD_SPACING, Defaults.CHILD_SPACING);
		m_startX = getDouble (m_settings, KEY_START_X, Defaults.MIN_START_X, Defaults.MAX_START_X, Defaults.START_X);
		m_startY = getDouble (m_settings, KEY_START_Y, Defaults.MIN_START_Y, Defaults.MAX_START_Y, Defaults.START_Y);
	}

	public boolean save (SaveType saveType)
	{
		File dir = null;
		switch (saveType)
		{
			case UserHome:
			{
				dir = new File (Utils.getUserDirectory ());
				if (!dir.isDirectory ())
				{
					return false;
				}
				break;
			}
			case GitDir:
			{
				File gitDir = m_gitRepo.getGitDir ();
				if (gitDir == null ||
					!gitDir.isDirectory ())
					return false;
				dir = new File (gitDir, Defaults.GIT_DIR_JGVT_DIR);
				if (!dir.isDirectory ())
				{
					try
					{
						if (!dir.mkdir ())
						{
							return false;
						}
					}
					catch (Exception ex)
					{
						return false;
					}
				}
				break;
			}
			case Repo:
			{
				File gitDir = m_gitRepo.getGitDir ();
				if (gitDir == null ||
					!gitDir.isDirectory ())
					return false;
				dir = gitDir;
			}
			default:
			{
				return false;
			}
		}
		return save (dir);
	}

	public boolean save (File dir)
	{
		storeProperties ();
		try
		{
			File file = new File (dir, FILE_PREFERENCE);
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

	public boolean setAbbrevLength (int abbrevLength)
	{
		boolean changed = (m_abbrevLength != abbrevLength);
		m_abbrevLength = abbrevLength;
		return changed;
	}

	public double getBranchSpacing ()
	{
		return m_branchSpacing;
	}

	public boolean setBranchSpacing (double branchSpacing)
	{
		boolean changed = (m_branchSpacing != branchSpacing);
		m_branchSpacing = branchSpacing;
		return changed;
	}

	public double getChildSpacing ()
	{
		return m_childSpacing;
	}

	public boolean setChildSpacing (double childSpacing)
	{
		boolean changed = (m_childSpacing != childSpacing);
		m_childSpacing = childSpacing;
		return changed;
	}

	public double getStartX ()
	{
		return m_startX;
	}

	public boolean setStartX (double startX)
	{
		boolean changed = (m_startX != startX);
		m_startX = startX;
		return changed;
	}

	public double getStartY ()
	{
		return m_startY;
	}

	public boolean setStartY (double startY)
	{
		boolean changed = (m_startY != startY);
		m_startY = startY;
		return changed;
	}
}
