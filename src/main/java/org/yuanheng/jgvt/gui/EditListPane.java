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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.Main;
import org.yuanheng.jgvt.relation.RelationEditEntry;
import org.yuanheng.jgvt.relation.RelationEditList;

import com.jgoodies.forms.builder.FormBuilder;

/**
 * @author	Heng Yuan
 */
class EditListPane extends JPanel
{
	private static final long serialVersionUID = 2268539607214742499L;

	private final Controller m_controller;
	private JLabel m_rowCountLabel;
	private JTable m_table;

	private MouseListener m_doubleClickListener = new MouseAdapter ()
	{
		@Override
		public void mouseClicked (MouseEvent e)
		{
			if (e.getClickCount () >= 2)
			{
				int row = m_table.getSelectedRow ();
				if (row != -1)
				{
					row = m_table.convertRowIndexToModel (row);
					EditListPaneModel model = (EditListPaneModel) m_table.getModel ();
					RelationEditEntry entry = model.getRow (row);
					m_controller.select (entry.id.getName (), true);
				}
			}
		}
	};

	public EditListPane (Controller controller, RelationEditList list)
	{
		setLayout (new BorderLayout ());

		m_controller = controller;
		FormBuilder builder = FormBuilder.create ()
				.columns("right:pref, 4dlu, default:grow")
				.rows("fill:min:grow, $lg, pref");
		m_table = new JTable (new EditListPaneModel (list));
		builder.add (new JScrollPane (m_table)).xyw (1, 1, 3);
		m_table.addMouseListener (m_doubleClickListener);

		builder.add ("Rows:").xy (1, 3);
		m_rowCountLabel = new JLabel ("0");
		builder.add (m_rowCountLabel).xy (3, 3);

		RowCountListener rowCountListener = new RowCountListener (m_table, m_rowCountLabel);
		m_table.getModel ().addTableModelListener (rowCountListener);

		add (builder.getPanel (), BorderLayout.CENTER);
	}

	public boolean deleteSelected ()
	{
		ListSelectionModel selectModel = m_table.getSelectionModel ();
		if (selectModel.isSelectionEmpty ())
			return false;

		int start = selectModel.getMinSelectionIndex ();
		int end = selectModel.getMaxSelectionIndex ();

		Main.editList.remove (start, end);

		refreshSelect (start);
		return true;
	}

	public boolean deleteAll ()
	{
		if (Main.editList.size () == 0)
			return false;

		Main.editList.removeAll ();

		refreshSelect (0);
		return true;
	}

	private void refreshSelect (final int start)
	{
		final ListSelectionModel selectModel = m_table.getSelectionModel ();

		int size = Main.editList.size ();
		if (size == 0 || start >= size)
		{
			SwingUtilities.invokeLater (() -> { selectModel.clearSelection (); });
		}
		else
		{
			SwingUtilities.invokeLater (() -> { selectModel.setSelectionInterval (start, start); });
		}
	}
}
