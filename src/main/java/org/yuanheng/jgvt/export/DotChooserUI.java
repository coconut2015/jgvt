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

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.FormBuilder;

/**
 * @author	Heng Yuan
 */
public class DotChooserUI extends JPanel implements ExportFileFilterUI
{
	private static final long serialVersionUID = 874454987300870831L;

	private JSpinner m_abbrevInput;
	private JCheckBox m_clusterBox;

	public DotChooserUI ()
	{
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel ();
		spinnerModel.setMinimum (4);
		spinnerModel.setMaximum (48);
		spinnerModel.setValue (6);
		m_abbrevInput = new JSpinner (spinnerModel);
		m_clusterBox = new JCheckBox ();
		m_clusterBox.setSelected (true);
		FormBuilder builder = FormBuilder.create ()
				.columns("left:pref, $lcgap, default")
				.rows("p, $lg, p")
				.padding ("2dlu, 2dlu, 2dlu, 2dlu");
		builder.panel (this);
		builder.addLabel ("Abbrevation length:").xy (1, 1);
		builder.add (m_abbrevInput).xy (3, 1);
		builder.addLabel ("Cluster branch:").xy (1, 3);
		builder.add (m_clusterBox).xy (3, 3);
	}

	public void loadOptions (ExportFileFilter filter)
	{
		DotFileFilter f = (DotFileFilter) filter;
		DotOptions options = f.getOptions ();
		m_abbrevInput.setValue (options.abbrevLength);;
		m_clusterBox.setSelected (options.groupNodes);
	}

	public void saveOptions (ExportFileFilter filter)
	{
		DotFileFilter f = (DotFileFilter) filter;
		DotOptions options = f.getOptions ();
		options.abbrevLength = (Integer)m_abbrevInput.getValue ();
		options.groupNodes = m_clusterBox.isSelected ();
	}
}
