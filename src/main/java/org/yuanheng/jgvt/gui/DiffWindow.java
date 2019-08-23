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

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.relation.RelationNode;

import com.jgoodies.forms.factories.Paddings;

/**
 * @author	Heng Yuan
 */
class DiffWindow extends JFrame
{
	private static final long serialVersionUID = 8827705597473629590L;

	public DiffWindow (Controller controller, RelationNode n1, RelationNode n2)
	{
		setTitle ("jgvt diff: " + CommitUtils.getName (n1) + ".." + CommitUtils.getName (n2));
		setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);

		JPanel contentPane = new JPanel (new BorderLayout ());
		contentPane.setBorder (Paddings.DIALOG);
		setContentPane (contentPane);

		DiffPane diffPane = new DiffPane (controller, n1, n2);
		contentPane.add (diffPane, BorderLayout.CENTER);
		pack ();
	}
}
