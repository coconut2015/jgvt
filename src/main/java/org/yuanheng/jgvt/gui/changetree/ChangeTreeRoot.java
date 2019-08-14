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
class ChangeTreeRoot extends ChangeTreeNode
{
	private RelationNode m_node;

	public ChangeTreeRoot ()
	{
		super (null);
	}

	public String toString ()
	{
		if (m_node == null)
			return "";
		return getNode().getCommit ().getId ().getName ();
	}

	public RelationNode getNode ()
	{
		return m_node;
	}

	public void setNode (RelationNode node)
	{
		m_node = node;
		clearHtml ();
	}

	@Override
	String computeHtml ()
	{
		RelationNode node = m_node;
		if (node != null)
		{
			return CommitUtils.getComment (node);
		}
		return "";
	}
}
