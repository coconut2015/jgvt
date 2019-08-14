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
package org.yuanheng.jgvt.gui.changetree;

import org.yuanheng.jgvt.gui.treetable.AbstractTreeTableModel;
import org.yuanheng.jgvt.gui.treetable.TreeTableModel;

/**
 * @author	Heng Yuan
 */
class ChangeTreeTableModel extends AbstractTreeTableModel
{
	public static String[] COLUMN_NAMES = { "File", "Add", "Del" };

	public ChangeTreeTableModel ()
	{
		super (new ChangeTreeRoot ());
	}

	public ChangeTreeRoot getRoot ()
	{
		return (ChangeTreeRoot) root;
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

	public Class<?> getColumnClass (int column)
	{
		if (column == 0)
		{
			return TreeTableModel.class;
		}
		else
		{
			return Integer.class;
		}
	}

	@Override
	public boolean isCellEditable (Object node, int column)
	{
		return column == 0;
	}

	@Override
	public Object getValueAt (Object node, int column)
	{
		switch (column)
		{
			case 1:
				return ((ChangeTreeNode)node).getAdded ();
			case 2:
				return ((ChangeTreeNode)node).getDeleted ();
			default:
				return node;
		}
	}

	@Override
	public Object getChild (Object parent, int index)
	{
		return ((ChangeTreeNode)parent).getChildAt (index);
	}

	@Override
	public int getChildCount (Object parent)
	{
		return ((ChangeTreeNode)parent).getChildCount ();
	}

	public void fireTreeStructureChanged (Object source)
	{
		fireTreeStructureChanged (source, new Object[] { source }, null, null);
	}
}
