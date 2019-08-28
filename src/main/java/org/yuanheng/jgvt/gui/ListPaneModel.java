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

/**
 * @author	Heng Yuan
 */
public class ListPaneModel extends AbstractTableModel
{
	private static final long serialVersionUID = 4337544331996692177L;

	public final static int COL_COMMIT = 0;
	public final static int COL_NAME = 1;
	public final static int COL_TIME = 2;
	public final static int COL_MESSAGE = 3;
	public final static int COL_AUTHOR = 4;
	public final static int COL_AUTHOR_EMAIL = 5;
	public final static int COL_AUTHOR_TIME = 6;
	public final static int COL_COMMITTER = 7;
	public final static int COL_COMMITTER_EMAIL = 8;
	public final static int COL_COMMITTER_TIME = 9;

	public static String[] COLUMN_NAMES = { "Commit", "Name", "Time", "Message", "Author", "Author Email", "Author Time", "Committer", "Committer Email", "Committer Time" };

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
		return 10;
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
		ListInfo info = m_listInfo.get (row);
		switch (column)
		{
			case COL_COMMIT:
			{
				return info.ref.getObjectId ().getName ();
			}
			case COL_NAME:
			{
				return info.ref.getName ();
			}
			case COL_TIME:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getCommitterIdent ().getWhen ();
			}
			case COL_MESSAGE:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getShortMessage ();
			}
			case COL_AUTHOR:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getAuthorIdent ().getName ();
			}
			case COL_AUTHOR_EMAIL:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getAuthorIdent ().getEmailAddress ();
			}
			case COL_AUTHOR_TIME:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getAuthorIdent ().getWhen ();
			}
			case COL_COMMITTER:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getCommitterIdent ().getName ();
			}
			case COL_COMMITTER_EMAIL:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getCommitterIdent ().getEmailAddress ();
			}
			case COL_COMMITTER_TIME:
			{
				if (info.node == null)
					return null;
				return info.node.getCommit ().getCommitterIdent ().getWhen ();
			}
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
