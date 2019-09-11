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
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.*;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.Defaults;
import org.yuanheng.jgvt.GitRepo;
import org.yuanheng.jgvt.Main;
import org.yuanheng.jgvt.relation.RelationEditList;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.factories.Paddings;
import com.jgoodies.forms.factories.Paddings.Padding;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.util.LayoutStyle;

/**
 * @author	Heng Yuan
 */
class EditListDialog extends JDialog
{
	private static final long serialVersionUID = 5086101692778507560L;

	public static String[] SAVE_STRINGS = { "GIT_DIR", "Repo" };

	private final Controller m_controller;
	private final EditListPane m_editListPane;
	private final JRadioButton[] m_saveChoice;

	private Action m_deleteAction = new AbstractAction ("Delete")
	{
		private static final long serialVersionUID = -2751818157358488811L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (m_editListPane.deleteSelected ())
			{
				refresh ();
			}
		}
	};

	private Action m_clearAllAction = new AbstractAction ("Clear All")
	{
		private static final long serialVersionUID = 3450215904527457799L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			if (m_editListPane.deleteAll ())
			{
				refresh ();
			}
		}
	};

	private Action m_saveAction = new AbstractAction ("Save")
	{
		private static final long serialVersionUID = -6754672326850368268L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			for (int i = 0; i < m_saveChoice.length; ++i)
			{
				if (m_saveChoice[i].isSelected ())
				{
					save (i);
					break;
				}
			}
		}
	};

	private Action m_closeAction = new AbstractAction ("Close")
	{
		private static final long serialVersionUID = -7339160010955668677L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			dispose ();
		}
	};

	public EditListDialog (JFrame parent, Controller controller)
	{
		super (parent);
		m_controller = controller;
		setTitle ("Edit List");
		setDefaultCloseOperation (JDialog.DISPOSE_ON_CLOSE);

		JPanel contentPane = new JPanel (new BorderLayout ());
		contentPane.setBorder (Paddings.DIALOG);
		setContentPane (contentPane);

		m_editListPane = new EditListPane (controller, Main.editList);
		contentPane.add (m_editListPane, BorderLayout.CENTER);

		ButtonGroup bg = new ButtonGroup ();
		m_saveChoice = new JRadioButton[SAVE_STRINGS.length];
		for (int i = 0; i < SAVE_STRINGS.length; ++i)
		{
			JRadioButton button = new JRadioButton (SAVE_STRINGS[i]);
			m_saveChoice[i] = button;

			button.setSelected (i == 0);
			bg.add (button);
		}

		ButtonStackBuilder builder = ButtonStackBuilder.create ()
													   .addButton (m_deleteAction)
													   .addRelatedGap ()
													   .addButton (m_clearAllAction)
													   .addUnrelatedGap ()
													   .addFixed (new JSeparator ())
													   .addRelatedGap ();
		for (int i = 0; i < SAVE_STRINGS.length; ++i)
		{
			 builder.addFixed (m_saveChoice[i])
			 		.addRelatedGap ();
		}
		builder.addButton (m_saveAction)
			   .addRelatedGap ()
			   .addFixed (new JSeparator ())
			   .addUnrelatedGap ()
			   .addButton (m_closeAction)
			   .addGlue ();
		JPanel buttonPanel = builder.build ();
		Padding padding = Paddings.createPadding (Sizes.dluX(0), LayoutStyle.getCurrent().getButtonBarPad(), Sizes.dluX(0), Sizes.dluX(0));
		buttonPanel.setBorder (padding);
		contentPane.add (buttonPanel, BorderLayout.EAST);

		setMinimumSize (new Dimension (300, 300));
		setSize (480, 400);
	}

	private void refresh ()
	{
		m_controller.refresh ();
	}

	private void save (int choice)
	{
		GitRepo gitRepo = m_controller.getGitRepo ();
		if (gitRepo != null)
		{
			File dir;
			if (choice == 0)
			{
				dir = new File (gitRepo.getGitDir (), Defaults.GIT_DIR_JGVT_DIR);
				if (!dir.isDirectory ())
				{
					dir.mkdir ();
				}
			}
			else
			{
				dir = gitRepo.getRoot ();
			}
			if (!RelationEditList.write (new File (dir, Defaults.JGVT_EDITLIST), Main.editList))
			{
				JOptionPane.showMessageDialog (getParent (), "Unable to save edit list.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
