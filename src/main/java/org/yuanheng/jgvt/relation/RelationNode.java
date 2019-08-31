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

import java.util.Comparator;
import java.util.HashMap;

import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.Utils;

/**
 * @author	Heng Yuan
 */
public class RelationNode implements Comparable<RelationNode>
{
	private final static RelationNode[] s_emptyArray = new RelationNode[0];
	private final static Ref[] s_emptyRefArray = new Ref[0];

	public static int HASH_LENGTH = 6;

	private final RevCommit m_commit;
	private final String m_hash;
	private Ref[] m_tags = s_emptyRefArray;
	private Ref[] m_branches = s_emptyRefArray;
	private String m_annotation;
	private String m_toolTip;

	private RelationNode[] m_parents = s_emptyArray;
	private RelationNode[] m_children = s_emptyArray;

	private final LayoutInfo m_layoutInfo = new LayoutInfo ();
	private RelationBranch m_relationBranch;
	private final HashMap<RelationNode, RelationType> m_relationMap;

	RelationNode (RevCommit commit)
	{
		m_commit = commit;

		m_hash = commit.abbreviate (HASH_LENGTH).name ();
		m_relationMap = new HashMap<RelationNode, RelationType> ();
	}

	public RevCommit getCommit ()
	{
		return m_commit;
	}

	@Override
	public String toString ()
	{
		return m_hash;
	}

	public void addTag (Ref tag)
	{
		if (tag == null)
			return;
		m_tags = Utils.arrayAdd (Ref.class, m_tags, tag);
	}

	public void addBranch (Ref branch)
	{
		if (branch == null)
			return;
		m_branches = Utils.arrayAdd (Ref.class, m_branches, branch);
	}

	public String getAnnotation ()
	{
		if (m_annotation == null)
		{
			if (m_tags.length == 0 && m_branches.length == 0)
				return null;
			m_annotation = CommitUtils.getAnnotation (m_tags, m_branches);
		}
		return m_annotation;
	}

	public String getTooltip (int flag)
	{
		if (m_toolTip == null)
		{
			m_toolTip = CommitUtils.getToolTipString (m_commit, flag);
		}
		return m_toolTip;
	}

	public void addParent (RelationNode parent)
	{
		m_parents = Utils.arrayAdd (RelationNode.class, m_parents, parent);
		parent.addChild (this);
	}

	private void addChild (RelationNode child)
	{
		m_children = Utils.arrayAdd (RelationNode.class, m_children, child);
	}

	public RelationNode[] getParents ()
	{
		return m_parents;
	}

	public RelationNode[] getChildren ()
	{
		return m_children;
	}

	public void setNthChild (RelationNode child, int n)
	{
		if (m_children[n] == child)
			return;
		int i;
		for (i = 0; i < m_children.length; ++i)
		{
			if (m_children[i] == child)
				break;
		}

		if (n < i)
		{
			for (int j = i; j > n; --j)
			{
				m_children[j] = m_children[j - 1];
			}
			m_children[n] = child;
		}
		else if (n > i)
		{
			for (int j = i; j < n; ++j)
			{
				m_children[j] = m_children[j + 1];
			}
			m_children[n] = child;
		}
	}

	public void setRelation (RelationNode parent, RelationType type)
	{
		m_relationMap.put (parent, type);
	}

	public RelationType getRelation (RelationNode parent)
	{
		RelationType type = m_relationMap.get (parent);
		if (type != null)
		{
			return type;
		}
		if (m_parents[0] == parent)
		{
			return RelationType.CHILD;
		}
		return RelationType.MERGE;
	}

	public LayoutInfo getLayoutInfo ()
	{
		return m_layoutInfo;
	}

	public RelationBranch getRelationBranch ()
	{
		return m_relationBranch;
	}

	public void setRelationBranch (RelationBranch relationBranch)
	{
		m_relationBranch = relationBranch;
	}

	public Ref[] getBranches ()
	{
		return m_branches;
	}

	public Ref[] getTags ()
	{
		return m_tags;
	}

	public void swapParentOrder ()
	{
		if (m_parents.length == 2)
		{
			RelationNode tmp = m_parents[0];
			m_parents[0] = m_parents[1];
			m_parents[1] = tmp;
		}
	}

	public void resetLayout ()
	{
		m_layoutInfo.reset ();
		m_relationMap.clear ();
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

	public RelationNode[] canJoinParentBranch ()
	{
		if (getRelationBranch () == null ||
			getParents ().length == 0 ||
			this != getRelationBranch ().getFirst ())
		{
			return null;
		}

		RelationNode n1 = null;
		RelationNode n2 = null;
		int count = 0;
		for (RelationNode parent : getParents ())
		{
			if (parent == parent.getRelationBranch ().getLast ())
			{
				if (count == 0)
					n1 = parent;
				else
					n2 = parent;
				++count;
			}
		}
		switch (count)
		{
			case 1:
				return new RelationNode[] { n1 };
			case 2:
				return new RelationNode[] { n1, n2 };
			default:
				return null;
		}
	}

	@Override
	public int compareTo (RelationNode o)
	{
		return m_commit.compareTo (o.m_commit);
	}

	@Override
	public int hashCode ()
	{
		return m_commit.hashCode ();
	}

	public static Comparator<RelationNode> sortByWeightComparator = new Comparator<RelationNode> ()
	{
		@Override
		public int compare (RelationNode n1, RelationNode n2)
		{
			if (n1 == n2)
			{
				return 0;
			}
			if (n1.getWeight () != n2.getWeight ())
			{
				return n1.getWeight () - n2.getWeight ();
			}
			if (n1.getCommit ().getCommitTime () != n2.getCommit ().getCommitTime ())
			{
				return - (n1.getCommit ().getCommitTime () - n2.getCommit ().getCommitTime ());
			}
			return n1.getCommit ().compareTo (n2.getCommit ());
		}
	};

	public static Comparator<RelationNode> sortByDateComparator = new Comparator<RelationNode> ()
	{
		@Override
		public int compare (RelationNode o1, RelationNode o2)
		{
			return o1.getCommit ().getCommitTime () - o2.getCommit ().getCommitTime ();
		}
	};
}
