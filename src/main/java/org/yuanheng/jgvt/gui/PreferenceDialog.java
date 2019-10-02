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
import java.awt.event.ActionEvent;

import javax.swing.*;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.Defaults;
import org.yuanheng.jgvt.Main;
import org.yuanheng.jgvt.Preference;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.factories.Paddings;

/**
 * @author	Heng Yuan
 */
class PreferenceDialog extends JDialog
{
	private static final long serialVersionUID = -8240860562153506462L;

	public static String[] SAVE_STRINGS = { "User home", "GIT_DIR", "Repo" };

	private final Controller m_controller;
	private JSpinner m_abbrevLenInput;
	private JSpinner m_branchSpacingInput;
	private JSpinner m_childSpacingInput;
	private JSpinner m_startXInput;
	private JSpinner m_startYInput;
	private JCheckBox m_leftOnlyInput;
	private JComboBox<String> m_saveToInput;

	private Action m_restoreDefaultsAction = new AbstractAction ("Restore Defaults")
	{
		private static final long serialVersionUID = 6953681480309633648L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_abbrevLenInput.setValue (Defaults.DEFAULT_ABBREV_LEN);
			m_branchSpacingInput.setValue (Defaults.BRANCH_SPACING);
			m_childSpacingInput.setValue (Defaults.CHILD_SPACING);
			m_startXInput.setValue (Defaults.START_X);
			m_startYInput.setValue (Defaults.START_Y);
		}
	};

	private Action m_saveAction = new AbstractAction ("Save and Close")
	{
		private static final long serialVersionUID = 650651225477308042L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			boolean changed = storePreference ();
			dispose ();
			if (changed)
			{
				SwingUtilities.invokeLater (() -> { m_controller.refresh (); });
			}
		}
	};

	private Action m_cancelAction = new AbstractAction ("Cancel")
	{
		private static final long serialVersionUID = -1975408902527005349L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			dispose ();
		}
	};

	public PreferenceDialog (Controller controller, JFrame parent)
	{
		super (parent);
		setTitle ("Preferences");
		setModal (true);

		m_controller = controller;

		JPanel contentPane = new JPanel (new BorderLayout ());
		contentPane.setBorder (Paddings.DIALOG);
		setContentPane (contentPane);

		FormBuilder builder = FormBuilder.create ()
				.columns("right:pref, 4dlu, default, 4dlu, right:pref, 4dlu, default")
				.rows("pref, $lg, pref, $lg, pref, $lg, pref, $lg, pref");

		{
			builder.add ("SHA1 Abbreviation Length:").xy (1, 1);
			SpinnerNumberModel model = new SpinnerNumberModel (1, 1, 48, 1);
			m_abbrevLenInput = new JSpinner (model);
			builder.add (m_abbrevLenInput).xy (3, 1);
		}

		{
			builder.add ("Branch spacing:").xy (1, 3);
			SpinnerNumberModel model = new SpinnerNumberModel (Defaults.MIN_BRANCH_SPACING, Defaults.MIN_BRANCH_SPACING, Defaults.MAX_BRANCH_SPACING, 10);
			m_branchSpacingInput = new JSpinner (model);
			builder.add (m_branchSpacingInput).xy (3, 3);
		}

		{
			builder.add ("Child spacing:").xy (5, 3);
			SpinnerNumberModel model = new SpinnerNumberModel (Defaults.MIN_CHILD_SPACING, Defaults.MIN_CHILD_SPACING, Defaults.MAX_CHILD_SPACING, 10);
			m_childSpacingInput = new JSpinner (model);
			builder.add (m_childSpacingInput).xy (7, 3);
		}

		{
			builder.add ("Start X:").xy (1, 5);
			SpinnerNumberModel model = new SpinnerNumberModel (Defaults.MIN_START_X, Defaults.MIN_START_X, Defaults.MAX_START_X, 10);
			m_startXInput = new JSpinner (model);
			builder.add (m_startXInput).xy (3, 5);
		}

		{
			builder.add ("Start Y:").xy (5, 5);
			SpinnerNumberModel model = new SpinnerNumberModel (Defaults.MIN_START_Y, Defaults.MIN_START_Y, Defaults.MAX_START_Y, 10);
			m_startYInput = new JSpinner (model);
			builder.add (m_startYInput).xy (7, 5);
		}

		{
			builder.add ("Trust Parent 0:").xy (1, 7);
			m_leftOnlyInput = new JCheckBox ((Icon)null, Defaults.LEFT_ONLY);
			builder.add (m_leftOnlyInput).xy (3, 7);
		}

		{
			builder.add ("Save to:").xy (1, 9);
			m_saveToInput = new JComboBox<String> (SAVE_STRINGS);
			builder.add (m_saveToInput).xy (3, 9);
		}

		contentPane.add (builder.getPanel (), BorderLayout.CENTER);

		{
			JPanel buttomPanel = ButtonBarBuilder.create ()
												 .addGlue ()
												 .addButton (m_restoreDefaultsAction)
												 .addRelatedGap ()
												 .addButton (m_saveAction)
												 .addRelatedGap ()
												 .addButton (m_cancelAction)
												 .build ();
			buttomPanel.setBorder (Paddings.BUTTON_BAR_PAD);
			contentPane.add (buttomPanel, BorderLayout.SOUTH);
		}

		loadPreference ();
		pack ();
		setResizable (false);
		setLocationRelativeTo (parent);
	}

	private void loadPreference ()
	{
		m_abbrevLenInput.setValue (Main.pref.getAbbrevLength ());
		m_branchSpacingInput.setValue (Main.pref.getBranchSpacing ());
		m_childSpacingInput.setValue (Main.pref.getChildSpacing ());
		m_startXInput.setValue (Main.pref.getStartX ());
		m_startYInput.setValue (Main.pref.getStartY ());
		m_leftOnlyInput.setSelected (Main.pref.getLeftOnly ());
	}

	private boolean save (Preference pref, int index)
	{
		Preference.SaveType saveType;
		if (index == 0)
		{
			saveType = Preference.SaveType.UserHome;
		}
		else if (index == 1)
		{
			saveType = Preference.SaveType.GitDir;
		}
		else if (index == 0)
		{
			saveType = Preference.SaveType.Repo;
		}
		else
		{
			return false;
		}
		return Main.pref.save (saveType);
	}

	public boolean storePreference ()
	{
		boolean changed = false;
		changed |= Main.pref.setAbbrevLength (((Number)m_abbrevLenInput.getValue ()).intValue ());
		changed |= Main.pref.setBranchSpacing (((Number)m_branchSpacingInput.getValue ()).doubleValue ());
		changed |= Main.pref.setChildSpacing (((Number)m_childSpacingInput.getValue ()).doubleValue ());
		changed |= Main.pref.setStartX (((Number)m_startXInput.getValue ()).doubleValue ());
		changed |= Main.pref.setStartY (((Number)m_startYInput.getValue ()).doubleValue ());
		changed |= Main.pref.setLeftOnly (m_leftOnlyInput.isSelected ());

		boolean saved = save (Main.pref, m_saveToInput.getSelectedIndex ());
		if (!saved)
		{
			JOptionPane.showMessageDialog (getParent (), "Unable to save preferences.", "Error", JOptionPane.ERROR_MESSAGE);
		}

		return changed;
	}
}
