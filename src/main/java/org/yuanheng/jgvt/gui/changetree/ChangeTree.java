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

import java.util.HashMap;
import java.util.List;

import javax.swing.ListSelectionModel;

import org.yuanheng.jgvt.ChangeInfo;
import org.yuanheng.jgvt.gui.treetable.JTreeTable;
import org.yuanheng.jgvt.gui.treetable.TreeTableModel;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
public class ChangeTree extends JTreeTable
{
	private static final long serialVersionUID = -4267180235096815912L;

	public ChangeTree ()
	{
		super (new ChangeTreeTableModel ());
		this.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
	}

	public TreeTableModel getTreeTableModel ()
	{
		return (TreeTableModel) getTree ().getModel ();
	}

	public void setList (RelationNode node, List<ChangeInfo> list)
	{
		ChangeTreeRoot root = (ChangeTreeRoot) getTreeTableModel ().getRoot ();
		root.setNode (node);
		root.clear ();

		HashMap<String, ChangeTreeDirectory> dirMap = new HashMap<String, ChangeTreeDirectory> ();

		for (ChangeInfo info : list)
		{
			ChangeTreeFile file = new ChangeTreeFile (root, info);
			String dir = file.getDirectory ();
			if (dir.length () > 0)
			{
				ChangeTreeDirectory dirNode = dirMap.get (dir);
				if (dirNode == null)
				{
					dirNode = new ChangeTreeDirectory (root, dir);
					root.add (dirNode);
					dirMap.put (dir, dirNode);
				}
				dirNode.add (file);
			}
			else
			{
				root.add (file);
			}
		}

		((ChangeTreeTableModel)getTreeTableModel ()).fireTreeStructureChanged (root);

		for (int i = 0; i < tree.getRowCount(); ++i)
		{
		    tree.expandRow(i);
		}
	}

	public String getSelectedNodeHtml ()
	{
		int row = getSelectedRow ();
		if (row < 0)
			return null;
		ChangeTreeNode cn = (ChangeTreeNode)getValueAt (row, 0);
		return cn.getHtml ();
	}
}
