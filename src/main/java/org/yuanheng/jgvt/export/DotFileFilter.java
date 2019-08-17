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

import org.yuanheng.jgvt.Controller;

/**
 * @author	Heng Yuan
 */
public class DotFileFilter extends ExportFileFilter
{
	public final static String EXT = ".dot";

	private final DotOptions m_options = new DotOptions ();
	private DotChooserUI m_ui;

	public DotFileFilter ()
	{
		super (EXT, "Graphiz DOT Format");
	}

	public DotOptions getOptions ()
	{
		return m_options;
	}

	@Override
	public void save (Controller controller, File file) throws Exception
	{
		new DotExporter (getOptions ()).save (file, "jgvt", controller.getRelationTree ());
	}

	@Override
	public ExportFileFilterUI getUI ()
	{
		if (m_ui == null)
		{
			m_ui = new DotChooserUI ();
		}
		return m_ui;
	}
}
