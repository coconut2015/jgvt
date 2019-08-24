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

import javax.swing.tree.TreeNode;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.revwalk.RevCommit;
import org.yuanheng.jgvt.ChangeInfo;
import org.yuanheng.jgvt.CommitUtils;

/**
 * @author	Heng Yuan
 */
class ChangeTreeFile extends ChangeTreeNode
{
	private final ChangeInfo m_info;
	private String m_dir;
	private String m_name;

	public ChangeTreeFile (ChangeTreeNode parent, ChangeInfo info)
	{
		super (parent);
		m_info = info;

		String path = getPath ();
		int index = path.lastIndexOf ('/');
		if (index >= 0)
		{
			m_dir = path.substring (0, index);
			m_name = path.substring (index + 1);
		}
		else
		{
			m_dir = "";
			m_name = path;
		}
	}

	@Override
	public Integer getAdded ()
	{
		return m_info.getAdded ();
	}

	@Override
	public Integer getDeleted ()
	{
		return -m_info.getDeleted ();
	}

	public String getDirectory ()
	{
		return m_dir;
	}

	public String getName ()
	{
		return m_name;
	}

	public String getPath ()
	{
		switch (m_info.getDiffEntry ().getChangeType ())
		{
			case DELETE:
				return m_info.getDiffEntry ().getOldPath ();
			default:
				return m_info.getDiffEntry ().getNewPath ();
		}
	}

	public ChangeType getChangeType ()
	{
		return m_info.getDiffEntry ().getChangeType ();
	}

	@Override
	public boolean getAllowsChildren ()
	{
		return false;
	}

	@Override
	public String toString ()
	{
		return m_name;
	}

	@Override
	String computeHtml ()
	{
		TreeNode node = getParent ();
		while (node.getParent () != null)
			node = node.getParent ();
		if (node instanceof ChangeTreeCommit)
		{
			ChangeTreeCommit c = (ChangeTreeCommit) node;
			RevCommit c1 = c.getNode ().getCommit ();
			RevCommit c2 = null;
			if (c1.getParentCount () > 0)
				c2 = c1.getParent (0);
			return CommitUtils.getComment (c1, c2, m_info);
		}
		else if (node instanceof ChangeTreeDiff)
		{
			ChangeTreeDiff diff = (ChangeTreeDiff) node;
			return CommitUtils.getComment (diff.getNode1 ().getCommit (), diff.getNode2 ().getCommit (), m_info);
		}
		// should not reach here.
		return getChangeType () + " " + toString ();
	}
}
