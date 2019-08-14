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

import java.awt.Dimension;
import java.util.HashMap;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.eclipse.jgit.diff.DiffEntry;

/**
 * @author	Heng Yuan
 */
public class ChangeTree extends JTree
{
	public static int PREFERED_WIDTH = 200;
	public static int PREFERED_HEIGHT = 200;

	private static final long serialVersionUID = -4267180235096815912L;

	public ChangeTree ()
	{
		ChangeTreeNode root = new ChangeTreeNode (null);
		DefaultTreeModel model = new DefaultTreeModel (root);
		setModel (model);
		setPreferredSize (new Dimension (PREFERED_WIDTH, PREFERED_HEIGHT));
	}

	public void setList (RelationNode node, List<DiffEntry> list)
	{
		DefaultTreeModel model = createModel (node, list);
		setModel (model);
		expand ((TreeNode)model.getRoot (), new Object[0]);
	}

	private DefaultTreeModel createModel (RelationNode node, List<DiffEntry> list)
	{
		ChangeTreeNode root = new ChangeTreeRoot (node);

		HashMap<String, ChangeTreeDirectory> dirMap = new HashMap<String, ChangeTreeDirectory> ();

		for (DiffEntry entry : list)
		{
			ChangeTreeFile file = new ChangeTreeFile (root, entry);
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

		return new DefaultTreeModel (root);
	}

	public void expand (TreeNode node, Object[] path)
	{
		int childCount = node.getChildCount ();
		if (childCount > 0)
		{
			Object[] newPath = new Object[path.length + 1];
			for (int i = 0; i < path.length; ++i)
				newPath[i] = path[i];
			newPath[path.length] = node;
			path = newPath;
			for (int i = 0; i < childCount; ++i)
			{
				TreeNode childNode = node.getChildAt (i);
				if (!childNode.isLeaf ())
				{
					expand (childNode, path);
				}
			}

			expandPath (new TreePath (path));
		}
	}
}
