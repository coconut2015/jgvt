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
package org.yuanheng.jgvt.swing;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author	Heng Yuan
 */
class StatusBar extends JPanel
{
	private static final long serialVersionUID = -6081481510176159450L;

	private JTextField m_rootField;
	private JTextField m_branchField;
	private JTextField m_fileField;

	public StatusBar ()
	{
		m_rootField = new JTextField ();
		m_rootField.setEditable (false);

		m_branchField = new JTextField ();
		m_branchField.setEditable (false);

		m_fileField = new JTextField ();
		m_fileField.setEditable (false);

		add(new JLabel ("Repo:"));
		add(m_rootField);
		add(new JLabel ("Branch:"));
		add(m_branchField);
		add(new JLabel ("File:"));
		add(m_fileField);
	}

	public void setRoot (String gitRoot)
	{
		m_rootField.setText (gitRoot);
	}

	public void setBranch (String branch)
	{
		m_branchField.setText (branch);
	}

	public void setFile (String file)
	{
		if (file == null)
			file = "";
		m_fileField.setText (file);
	}
}
