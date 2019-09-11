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

import org.yuanheng.jgvt.relation.RelationEditEntry;
import org.yuanheng.jgvt.relation.RelationEditEvent;
import org.yuanheng.jgvt.relation.RelationEditList;
import org.yuanheng.jgvt.relation.RelationEditListener;

/**
 * @author	Heng Yuan
 */
public class EditListPaneModel extends AbstractTableModel implements RelationEditListener
{
	private static final long serialVersionUID = 7144135321549535672L;

	public final static int COL_COMMIT = 0;
	public final static int COL_JOIN = 1;

	public static String[] COLUMN_NAMES = { "Commit", "Join Parent" };

	private final RelationEditList m_list;

	public EditListPaneModel (RelationEditList list)
	{
		m_list = list;
		m_list.addListener (this);
	}

	@Override
	public int getRowCount ()
	{
		return m_list.size ();
	}

	@Override
	public int getColumnCount ()
	{
		return 2;
	}

	@Override
	public String getColumnName (int column)
	{
		return COLUMN_NAMES[column];
	}

	@Override
	public Class<?> getColumnClass (int column)
	{

		if (column == COL_COMMIT)
			return Object.class;
		else if (column == COL_JOIN)
			return Integer.class;
		return String.class;
	}

	@Override
	public Object getValueAt (int row, int column)
	{
		RelationEditEntry entry = m_list.get (row);
		switch (column)
		{
			case COL_COMMIT:
			{
				return entry.id.getName ();
			}
			case COL_JOIN:
			{
				return entry.mergeParent;
			}
		}
		return null;
	}

	@Override
	public boolean isCellEditable (int rowIndex, int columnIndex)
	{
		return false;
	}

	public RelationEditEntry getRow (int row)
	{
		return m_list.get (row);
	}

	@Override
	public void dataChanged (RelationEditEvent e)
	{
		switch (e.getType ())
		{
			case Added:
				fireTableRowsDeleted (e.getStart (), e.getEnd ());
				break;
			case Removed:
				fireTableRowsInserted (e.getStart (), e.getEnd ());
				break;
			case Changed:
				fireTableRowsUpdated (e.getStart (), e.getEnd ());
		}
	}
}
