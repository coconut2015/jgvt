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
package org.yuanheng.jgvt.gui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
class RememberButton extends JButton
{
	private static final long serialVersionUID = 3494734178370113182L;

	private final static String EMPTYSTRING = "Remembered";

	private final Controller m_controller;
	private RelationNode m_node;

	public RememberButton (Controller controller)
	{
		super (EMPTYSTRING);
		m_controller = controller;
		setOpaque (true);
		setBackground (new Color (0xa0c8f0));
		setEnabled (false);

		addActionListener(new ActionListener ()
		{
			@Override
			public void actionPerformed (ActionEvent e)
			{
				if (m_node != null)
				{
					m_controller.select (m_node, true);
				}
			}
		});
	}

	public void setNode (RelationNode node)
	{
		m_node = node;
		if (node == null)
		{
			setText (EMPTYSTRING);
			setEnabled (false);
		}
		else
		{
			setText (CommitUtils.getName (node.getCommit ()));
			setEnabled (true);
		}
	}
}
