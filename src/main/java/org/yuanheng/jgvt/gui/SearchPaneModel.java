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
	public final static int COL_DESCRIPTION = 2;

	public static String[] COLUMN_NAMES = { "Commit", "Time", "Description" };

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
		return 3;
	}

	@Override
	public String getColumnName (int column)
	{
		return COLUMN_NAMES[column];
	}

	@Override
	public Class<?> getColumnClass (int column)
	{
		if (column == COL_TIME)
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
			case COL_DESCRIPTION:
				return node.getCommit ().getShortMessage ();
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
