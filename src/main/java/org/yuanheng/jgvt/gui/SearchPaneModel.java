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

import java.util.Date;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
public class SearchPaneModel extends AbstractTableModel
{
	private static final long serialVersionUID = 685954075194500855L;

	public final static int COL_COMMIT = 0;
	public final static int COL_TIME = 1;
	public final static int COL_MESSAGE = 2;
	public final static int COL_AUTHOR = 3;
	public final static int COL_AUTHOR_EMAIL = 4;
	public final static int COL_AUTHOR_TIME = 5;
	public final static int COL_COMMITTER = 6;
	public final static int COL_COMMITTER_EMAIL = 7;
	public final static int COL_COMMITTER_TIME = 8;

	public static String[] COLUMN_NAMES = { "Commit", "Time", "Message", "Author", "Author Email", "Author Time", "Committer", "Committer Email", "Committer Time" };

	private final List<RelationNode> m_nodes;

	public SearchPaneModel (List<RelationNode> nodes)
	{
		m_nodes = nodes;
	}

	@Override
	public int getRowCount ()
	{
		return m_nodes.size ();
	}

	@Override
	public int getColumnCount ()
	{
		return 9;
	}

	@Override
	public String getColumnName (int column)
	{
		return COLUMN_NAMES[column];
	}

	@Override
	public Class<?> getColumnClass (int column)
	{
		if (column == COL_TIME ||
			column == COL_AUTHOR_TIME ||
			column == COL_COMMITTER_TIME)
			return Date.class;
		return String.class;
	}

	@Override
	public Object getValueAt (int row, int column)
	{
		RelationNode node = m_nodes.get (row);
		switch (column)
		{
			case COL_COMMIT:
				return node.getCommit ().getName ();
			case COL_TIME:
				return node.getCommit ().getCommitterIdent ().getWhen ();
			case COL_MESSAGE:
				return node.getCommit ().getShortMessage ();
			case COL_AUTHOR:
				return node.getCommit ().getAuthorIdent ().getName ();
			case COL_AUTHOR_EMAIL:
				return node.getCommit ().getAuthorIdent ().getEmailAddress ();
			case COL_AUTHOR_TIME:
				return node.getCommit ().getAuthorIdent ().getWhen ();
			case COL_COMMITTER:
				return node.getCommit ().getCommitterIdent ().getName ();
			case COL_COMMITTER_EMAIL:
				return node.getCommit ().getCommitterIdent ().getEmailAddress ();
			case COL_COMMITTER_TIME:
				return node.getCommit ().getCommitterIdent ().getWhen ();
		}
		return null;
	}

	@Override
	public boolean isCellEditable (int rowIndex, int columnIndex)
	{
		return false;
	}

	public RelationNode getRow (int row)
	{
		return m_nodes.get (row);
	}
}
