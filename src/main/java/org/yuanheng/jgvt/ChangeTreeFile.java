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

import javax.swing.tree.TreeNode;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;

/**
 * @author	Heng Yuan
 */
class ChangeTreeFile extends ChangeTreeNode
{
	private final DiffEntry m_entry;
	private String m_dir;
	private String m_name;

	public ChangeTreeFile (TreeNode parent, DiffEntry entry)
	{
		super (parent);
		m_entry = entry;

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
		switch (m_entry.getChangeType ())
		{
			case DELETE:
				return m_entry.getOldPath ();
			default:
				return m_entry.getNewPath ();
		}
	}

	public ChangeType getChangeType ()
	{
		return m_entry.getChangeType ();
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
}
