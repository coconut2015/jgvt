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
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.yuanheng.jgvt.Controller;

import com.jgoodies.forms.factories.Paddings;

/**
 * @author	Heng Yuan
 */
class BranchLogDialog extends JDialog
{
	private static final long serialVersionUID = 3605493344485080989L;

	public BranchLogDialog (JFrame parent, Controller controller)
	{
		super (parent);
		setTitle ("Branch Log");
		setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);

		JPanel contentPane = new JPanel (new BorderLayout ());
		contentPane.setBorder (Paddings.DIALOG);
		setContentPane (contentPane);

		contentPane.add (new BranchLogPane (controller, controller.getBranchLog ()), BorderLayout.CENTER);

		setMinimumSize (new Dimension (300, 300));
		setSize (480, 400);
	}
}
