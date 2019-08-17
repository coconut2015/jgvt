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
package org.yuanheng.jgvt.export;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.yuanheng.jgvt.Controller;

/**
 * @author	Heng Yuan
 */
public abstract class ExportFileFilter extends FileFilter
{
	private final String m_ext;
	private final String m_description;

	public ExportFileFilter (String extension, String description)
	{
		m_ext = extension;
		m_description = description + " (*" + extension + ")";
	}

	@Override
	public boolean accept (File name)
	{
		return name.isDirectory () || name.getName ().endsWith (m_ext);
	}

	@Override
	public String getDescription ()
	{
		return m_description;
	}

	public String getExtension ()
	{
		return m_ext;
	}

	public abstract ExportFileFilterUI getUI ();
	public abstract void save (Controller controller, File file) throws Exception;
}
