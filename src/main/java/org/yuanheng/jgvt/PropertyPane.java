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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.text.DateFormat;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class PropertyPane extends JPanel
{
	private static final long serialVersionUID = -3411516962635048642L;

	public static int TABLE_COLUMN_PADDING = 10;
	public static String[] TABLE_HEADERS = { "Name", "Value" };
	public static String[] NAME_ENTRIES = { "Author Name", "Author Email", "Author Time", "Commiter Name", "Commiter Email", "Commit Time" };

	private JTable m_propTable;
	private JTextPane m_msgPane;
	private Object[][] m_propValues;
	private RevCommit m_commit;
	private boolean m_setTableWidth = true;

	public PropertyPane ()
	{
		setLayout (new BorderLayout ());
		createCommitPane ();
		createHtmlPane ();
		JPanel northPane = new JPanel ();
		northPane.setLayout (new BorderLayout ());
		northPane.add (m_propTable, BorderLayout.CENTER);
		northPane.add (m_propTable.getTableHeader (), BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane (m_msgPane);
		JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, northPane, scrollPane);
		add (splitPane, BorderLayout.CENTER);
	}

	/**
	 * Makes sure the first column is not resizeable.
	 * @param	g
	 * 			the graphics component
	 */
	private void setTableColumnWidth (Graphics g)
	{
		int col1Width = SwingUtils.getWidth (g, m_propTable.getFont (), NAME_ENTRIES);
		col1Width += TABLE_COLUMN_PADDING;
		m_propTable.getColumnModel ().getColumn (0).setMinWidth (col1Width);
		m_propTable.getColumnModel ().getColumn (0).setMaxWidth (col1Width);

		int col2Width = SwingUtils.getWidth (g, m_propTable.getFont (), 20);
		m_propTable.getColumnModel ().getColumn (1).setPreferredWidth (col2Width);
	}

	@Override
	public Dimension getPreferredSize ()
	{
		/*
		 * Hopefully, getPreferredSize() is a good place to calculate
		 * the table column sizes.
		 */
		if (m_setTableWidth)
		{
			Graphics g = this.getGraphics ();
			if (g != null)
			{
				m_setTableWidth = false;
				setTableColumnWidth (g);
			}
		}
		return super.getPreferredSize ();
	}

	private void createCommitPane ()
	{
		m_propValues = new Object[NAME_ENTRIES.length][2];
		for (int row = 0; row < m_propValues.length; ++row)
		{
			m_propValues[row][0] = NAME_ENTRIES[row];
			m_propValues[row][1] = null;
		}
		AbstractTableModel tableModel = new AbstractTableModel ()
		{
			private static final long serialVersionUID = 3439506815456260747L;

			@Override
		    public boolean isCellEditable(int row, int column)
		    {
		        return false;
		    }

			@Override
			public int getRowCount ()
			{
				return NAME_ENTRIES.length;
			}

			@Override
			public int getColumnCount ()
			{
				return 2;
			}

			@Override
			public Object getValueAt (int rowIndex, int columnIndex)
			{
				return m_propValues[rowIndex][columnIndex];
			}
		};
		m_propTable = new JTable (tableModel);
		m_propTable.getTableHeader ().setReorderingAllowed (false);
	}

	private void createHtmlPane ()
	{
		m_msgPane = new JTextPane ();
		m_msgPane.setEditable (false);
	}

	public void readCommit (RevCommit commit)
	{
		if (m_commit == commit)
			return;
		m_commit = commit;

		DateFormat format = DateFormat.getDateTimeInstance ();
		PersonIdent ident;
		ident = commit.getAuthorIdent ();
		m_propValues[0][1] = ident.getName ();
		m_propValues[1][1] = ident.getEmailAddress ();
		m_propValues[2][1] = format.format (ident.getWhen ());
		ident = commit.getCommitterIdent ();
		m_propValues[3][1] = ident.getName ();
		m_propValues[4][1] = ident.getEmailAddress ();
		m_propValues[5][1] = format.format (ident.getWhen ());

		TableModelEvent e = new TableModelEvent (m_propTable.getModel (), 0, NAME_ENTRIES.length, 1);
		m_propTable.tableChanged (e);

		m_msgPane.setText (commit.getFullMessage ());
		m_msgPane.setCaretPosition (0);
	}
}
