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

import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import com.jgoodies.forms.builder.FormBuilder;

/**
 * @author	Heng Yuan
 */
class DotFileChooser
{
	private JPanel m_panel;
	private JSpinner m_abbrevInput;
	private DotFileOptions m_options;
	private JCheckBox m_clusterBox;
	private FileFilter m_filter;
	public DotFileChooser (DotFileOptions options)
	{
		m_options = options;
		m_filter = new FileFilter ()
		{
			@Override
			public boolean accept (File name)
			{
				return name.isDirectory () || name.getName ().endsWith (".dot");
			}

			@Override
			public String getDescription ()
			{
				return "Dot Graph Format (*.dot)";
			}
		};
	}

	public void updateJFileChooser (JFileChooser chooser)
	{
		if (m_panel == null)
		{
			SpinnerNumberModel spinnerModel = new SpinnerNumberModel ();
			spinnerModel.setMinimum (4);
			spinnerModel.setMaximum (48);
			spinnerModel.setValue (m_options.abbrevLength);
			m_abbrevInput = new JSpinner (spinnerModel);
			m_clusterBox = new JCheckBox ();
			m_clusterBox.setSelected (m_options.groupNodes);
			FormBuilder builder = FormBuilder.create ()
					.columns("left:pref, $lcgap, default")
					.rows("p, $lg, p")
					.padding ("2dlu, 2dlu, 2dlu, 2dlu");
			builder.addLabel ("Abbrevation length:").xy (1, 1);
			builder.add (m_abbrevInput).xy (3, 1);
			builder.addLabel ("Cluster branch:").xy (1, 3);
			builder.add (m_clusterBox).xy (3, 3);
			m_panel = builder.build ();
		}
		chooser.setSelectedFiles (new File[] { new File ("") });	// clear the previous selected files, if any.
		chooser.setAcceptAllFileFilterUsed (false);
		chooser.setFileFilter (m_filter);
		chooser.setAccessory (m_panel);
		chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
	}

	public void updateOptions ()
	{
		m_options.abbrevLength = (Integer)m_abbrevInput.getValue ();
		m_options.groupNodes = m_clusterBox.isSelected ();
	}
}
