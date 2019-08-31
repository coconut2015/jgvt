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
package org.yuanheng.jgvt.relation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author	Heng Yuan
 */
public class RelationBranch implements Comparable<RelationBranch>
{
	private static int s_id = 0;

	private final int m_id;
	private final Set<RelationNode> m_nodes = new HashSet<RelationNode> ();
	private final LayoutInfo m_layoutInfo = new LayoutInfo ();
	private ArrayList<RelationNode> m_orderedList;

	public RelationBranch (RelationNode node)
	{
		m_id = s_id++;
		add (node);
	}

	public int size ()
	{
		return m_nodes.size ();
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

		if (node.getWeight () < getWeight ())
		{
			setWeight (node.getWeight ());
		}
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
				int index = 0;
				boolean found = false;
				for (RelationNode p : n.getParents ())
				{
					if (has (p))
					{
						found = true;
						node = n;
						// reorder the parents.
						if (index > 0)
						{
							RelationNode tmp = n.getParents ()[0];
							n.getParents ()[0] = p;
							n.getParents ()[index] = tmp;
						}
						break;
					}
					++index;
				}
				if (!found)
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
				boolean found = false;
				int index = 0;
				for (RelationNode c : node.getChildren ())
				{
					if (has (c))
					{
						found = true;
						if (index != 0)
						{
							node.setNthChild (c, 0);
						}
						node = c;
						break;
					}
					++index;
				}
				if (!found)
					break;
			}
			if (m_orderedList.size () != m_nodes.size ())
				throw new RuntimeException ("Build orderd list failure: size = " + size () + ", order = " + m_orderedList.size ());
		}
		return m_orderedList;
	}

	public RelationNode getFirst ()
	{
		return getOrderedList ().get (0);
	}

	public RelationNode getLast ()
	{
		List<RelationNode> list = getOrderedList ();
		return list.get (list.size () - 1);
	}

	public void reset ()
	{
		m_layoutInfo.reset ();
	}

	public boolean isVisited ()
	{
		return m_layoutInfo.isVisited ();
	}

	public void visit ()
	{
		m_layoutInfo.visit ();
	}

	public void resetVisit ()
	{
		m_layoutInfo.resetVisit ();
	}

	public int getWeight ()
	{
		return m_layoutInfo.getWeight ();
	}

	public void setWeight (int weight)
	{
		m_layoutInfo.setWeight (weight);
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

	public int getId ()
	{
		return m_id;
	}

	@Override
	public String toString ()
	{
		return "[" + m_id + "]";
	}
}
