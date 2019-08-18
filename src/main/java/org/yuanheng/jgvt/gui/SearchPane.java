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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.relation.RelationNode;

import com.jgoodies.forms.builder.FormBuilder;

/**
 * @author	Heng Yuan
 */
class SearchPane extends JPanel
{
	private static final long serialVersionUID = -9066552931222800698L;

	private final Controller m_controller;
	private JTextField m_input;
	private JTable m_table;
	private TableRowSorter<TableModel> m_sorter;
	private RegexRowFilter m_filter = new RegexRowFilter (Pattern.CASE_INSENSITIVE, null);

	private DocumentListener m_inputListener = new DocumentListener ()
	{
		@Override
		public void insertUpdate (DocumentEvent e)
		{
			updateFilter ();
		}

		@Override
		public void removeUpdate (DocumentEvent e)
		{
			updateFilter ();
		}

		@Override
		public void changedUpdate (DocumentEvent e)
		{
			updateFilter ();
		}
	};

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
					SearchPaneModel model = (SearchPaneModel) m_table.getModel ();
					RelationNode node = model.getRow (row);
					m_controller.select (node, true);
				}
			}
		}
	};

	public SearchPane (Controller controller)
	{
		m_controller = controller;
		ArrayList<RelationNode> nodes = new ArrayList<RelationNode> ();
		nodes.addAll (controller.getRelationTree ().getNodes ());
		FormBuilder builder = FormBuilder.create ()
				.columns("default:grow")
				.rows("pref:grow, $lg, p:grow");
		builder.panel (this);
		m_input = new JTextField ();
		m_input.getDocument ().addDocumentListener (m_inputListener);
		builder.add (m_input).xy (1, 1);
		m_table = new JTable (new SearchPaneModel (nodes));
		m_table.setDefaultRenderer (Date.class, new DateTimeRenderer (DateFormat.getDateTimeInstance ()));
		m_sorter = new TableRowSorter<TableModel>(m_table.getModel());
		m_sorter.setRowFilter (m_filter);
		m_table.setRowSorter (m_sorter);
		builder.add (new JScrollPane (m_table)).xy (1, 3);
		m_table.addMouseListener (m_doubleClickListener);
	}

	private void updateFilter ()
	{
		String text = m_input.getText ().trim ();
		if (text.length () > 0)
			m_filter.setRegEx (text);
		else
			m_filter.setRegEx (null);
		m_sorter.setRowFilter (m_filter);
	}
}
