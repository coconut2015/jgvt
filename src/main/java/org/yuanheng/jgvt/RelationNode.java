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

import java.io.Serializable;
import java.util.HashMap;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class RelationNode implements Serializable, Comparable<RelationNode>
{
	private final static RelationNode[] s_emptyArray = new RelationNode[0];
	private final static Ref[] s_emptyRefArray = new Ref[0];

	private static final long serialVersionUID = 3470339333207629584L;

	public final static int TOOLTIP_AUTHOR = 1;
	public final static int TOOLTIP_AUTHOR_EMAIL = 2;
	public final static int TOOLTIP_AUTHOR_TS = 4;
	public final static int TOOLTIP_COMMITTER = 8;
	public final static int TOOLTIP_COMMITTER_EMAIL = 16;
	public final static int TOOLTIP_COMMITTER_TS = 32;

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
			StringBuilder builder = new StringBuilder ();
			boolean first;
			if (m_tags.length > 0)
			{
				builder.append ('(');
				first = true;
				for (Ref tag : m_tags)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						builder.append (',');
					}
					builder.append (tag.getName ().substring (Constants.R_TAGS.length ()));
				}
				builder.append (')');
			}
			if (m_branches.length > 0)
			{
				builder.append ('[');
				first = true;
				for (Ref ref : m_branches)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						builder.append (',');
					}
					String name = ref.getName ();
					if (name.startsWith (Constants.R_HEADS))
					{
						name = name.substring (Constants.R_HEADS.length ());
					}
					else if (name.startsWith (Constants.R_REMOTES))
					{
						name = name.substring (name.lastIndexOf ('/') + 1) + "*";
					}
					builder.append (name);
				}
				builder.append (']');
			}
			m_annotation = builder.toString ();
		}
		return m_annotation;
	}

	public String getTooltip (int flag)
	{
		if (m_toolTip == null)
		{
			boolean first = true;
			StringBuilder builder = new StringBuilder ().append ("<html>");

			PersonIdent authorIdent = m_commit.getAuthorIdent();

			if ((flag & TOOLTIP_AUTHOR) != 0)
			{
				builder.append ("Author: " + authorIdent.getName ());
				first = false;
			}
			if ((flag & TOOLTIP_AUTHOR_EMAIL) != 0)
			{
				if (!first)
					builder.append ("<br/>");
				builder.append ("Email: " + authorIdent.getEmailAddress ());
				first = false;
			}
			if ((flag & TOOLTIP_AUTHOR_TS) != 0)
			{
				if (!first)
					builder.append ("<br/>");
				builder.append ("Time: " + authorIdent.getWhen());
				first = false;
			}

			PersonIdent committerIdent = m_commit.getCommitterIdent ();

			if ((flag & TOOLTIP_AUTHOR) != 0)
			{
				if (!first)
					builder.append ("<br/>");
				builder.append ("Author: " + committerIdent.getName ());
				first = false;
			}
			if ((flag & TOOLTIP_AUTHOR_EMAIL) != 0)
			{
				if (!first)
					builder.append ("<br/>");
				builder.append ("Email: " + committerIdent.getEmailAddress ());
				first = false;
			}
			if ((flag & TOOLTIP_AUTHOR_TS) != 0)
			{
				if (!first)
					builder.append ("<br/>");
				builder.append ("Time: " + committerIdent.getWhen());
				first = false;
			}

			m_toolTip = builder.toString ();
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
		/*
		if (parent.m_branchHead == null)
		{
			return RelationType.CHILD;
		}
		if (m_parents.length > 1)
		{
			return RelationType.MERGE;
		}
		return RelationType.CHILD;
		*/
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

	@Override
	public int compareTo (RelationNode o)
	{
		return m_commit.compareTo (o.getCommit ());
	}

	@Override
	public int hashCode ()
	{
		return m_commit.hashCode ();
	}
}
