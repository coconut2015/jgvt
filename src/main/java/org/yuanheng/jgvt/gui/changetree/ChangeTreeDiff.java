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

import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
public class ChangeTreeDiff extends ChangeTreeNode
{
	private RelationNode m_n1;
	private RelationNode m_n2;

	public ChangeTreeDiff (RelationNode n1, RelationNode n2)
	{
		super (null);
		m_n1 = n1;
		m_n2 = n2;
	}

	@Override
	public String toString ()
	{
		String n1 = CommitUtils.getName (m_n1);
		String n2 = CommitUtils.getName (m_n2);
		return n1 + ".." + n2;
	}

	public RelationNode getNode1 ()
	{
		return m_n1;
	}

	public RelationNode getNode2 ()
	{
		return m_n2;
	}

	@Override
	String computeHtml ()
	{
		return CommitUtils.getComment (m_n1, m_n2);
	}
}
