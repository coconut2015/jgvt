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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author	Heng Yuan
 */
class RelationBranch implements Comparable<RelationBranch>
{
	private static int s_id = 0;

	private final int m_id;
	private final Set<RelationNode> m_nodes = new HashSet<RelationNode> ();
	private final LayoutInfo m_layoutInfo = new LayoutInfo ();
	private ArrayList<RelationNode> m_orderedList;

	public RelationBranch (RelationNode node)
	{
		m_id = s_id++;
		m_nodes.add (node);
		LayoutInfo lastLayoutInfo = node.getLayoutInfo ();
		getLayoutInfo().setWeight (lastLayoutInfo.getWeight ());
		getLayoutInfo().setX (lastLayoutInfo.getX ());
		getLayoutInfo().setY (lastLayoutInfo.getY ());
		node.setRelationBranch (this);
	}

	public boolean has (RelationNode node)
	{
		return m_nodes.contains (node);
	}

	public void add (RelationNode node)
	{
		RelationBranch otherBranch = node.getRelationBranch ();
		if (otherBranch != null)
		{
			if (otherBranch == this)
			{
				return;
			}

			if (otherBranch.has (node))
			{
				otherBranch.remove (node);
			}
		}
		node.setRelationBranch (this);
		m_nodes.add (node);
		m_orderedList = null;
	}

	public void remove (RelationNode node)
	{
		m_nodes.remove (node);
		node.setRelationBranch (null);
		m_orderedList = null;
	}

	public void merge (RelationBranch otherBranch)
	{
		ArrayList<RelationNode> nodes = new ArrayList<RelationNode> ();
		nodes.addAll (otherBranch.m_nodes);
		for (RelationNode node : nodes)
		{
			add (node);
		}
		m_orderedList = null;
	}

	public List<RelationNode> getOrderedList ()
	{
		if (m_orderedList == null)
		{
			RelationNode node = null;
			for (RelationNode n : m_nodes)
			{
				if (n.getParents ().length == 0)
				{
					node = n;
					break;
				}
				if (!has (n.getParents ()[0]))
				{
					node = n;
					break;
				}
			}
			m_orderedList = new ArrayList<RelationNode> ();
			for (;;)
			{
				m_orderedList.add (node);
				if (node.getChildren ().length == 0)
				{
					break;
				}
				node = node.getChildren ()[0];
				if (!has (node))
				{
					break;
				}
			}
		}
		return m_orderedList;
	}

	@Override
	public int hashCode ()
	{
		return m_id;
	}

	@Override
	public int compareTo (RelationBranch o)
	{
		return m_id - o.m_id;
	}

	public LayoutInfo getLayoutInfo ()
	{
		return m_layoutInfo;
	}
}
