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

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * @author	Heng Yuan
 */
public class ListPaneModel extends AbstractTableModel
{
	private static final long serialVersionUID = 4337544331996692177L;

	public final static int COL_COMMIT = 0;
	public final static int COL_NAME = 1;
	public final static int COL_DESCRIPTION = 2;

	public static String[] COLUMN_NAMES = { "Commit", "Name", "Description" };

	private final List<ListInfo> m_listInfo;

	public ListPaneModel (List<ListInfo> listInfo)
	{
		m_listInfo = listInfo;
	}

	@Override
	public int getRowCount ()
	{
		return m_listInfo.size ();
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
		return String.class;
	}

	@Override
	public Object getValueAt (int row, int column)
	{
		ListInfo info = m_listInfo.get (row);
		switch (column)
		{
			case COL_COMMIT:
				return info.node.getCommit ().getName ();
			case COL_NAME:
				return info.ref.getName ();
			case COL_DESCRIPTION:
				return info.node.getCommit ().getShortMessage ();
		}
		return null;
	}

	@Override
	public boolean isCellEditable (int rowIndex, int columnIndex)
	{
		return false;
	}

	public ListInfo getRow (int row)
	{
		return m_listInfo.get (row);
	}
}
