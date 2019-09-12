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

import javax.swing.table.AbstractTableModel;

import org.yuanheng.jgvt.relation.BranchLog;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
public class BranchLogModel extends AbstractTableModel
{
	private static final long serialVersionUID = 685954075194500855L;

	public final static int COL_INDEX = 0;
	public final static int COL_COMMIT = 1;
	public final static int COL_ALGORITHM = 2;
	public final static int COL_ITERATION = 3;

	public static String[] COLUMN_NAMES = { "Index", "Commit", "Algorithm", "Iteration" };

	private final BranchLog m_log;

	public BranchLogModel (BranchLog log)
	{
		m_log = log;
	}

	@Override
	public int getRowCount ()
	{
		return m_log.size ();
	}

	@Override
	public int getColumnCount ()
	{
		return COLUMN_NAMES.length;
	}

	@Override
	public String getColumnName (int column)
	{
		return COLUMN_NAMES[column];
	}

	@Override
	public Class<?> getColumnClass (int column)
	{
		switch (column)
		{
			case COL_INDEX:
			case COL_ITERATION:
				return Integer.class;
			case COL_COMMIT:
			case COL_ALGORITHM:
				return String.class;
			default:
				return Object.class;
		}
	}

	@Override
	public Object getValueAt (int row, int column)
	{
		switch (column)
		{
			case COL_INDEX:
				return row;
			case COL_COMMIT:
				return m_log.get (row).node.getCommit ().getName ();
			case COL_ALGORITHM:
				return m_log.get (row).algorithm;
			case COL_ITERATION:
				return m_log.get (row).iteration;
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
		return m_log.get (row).node;
	}
}
