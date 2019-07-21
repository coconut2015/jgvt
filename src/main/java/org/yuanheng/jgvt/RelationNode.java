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

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class RelationNode implements Serializable
{
	private final static RelationNode[] s_emptyArray = new RelationNode[0];

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
	private Ref m_tag;
	private String m_tagName;
	private Ref m_branchHead;
	private String m_branchName;
	private String m_toolTip;

	private RelationNode[] m_parents = s_emptyArray;

	public RelationNode (RevCommit commit)
	{
		m_commit = commit;

		m_hash = commit.abbreviate (HASH_LENGTH).name ();
	}

	public RevCommit getCommit ()
	{
		return m_commit;
	}

	@Override
	public String toString ()
	{
		if (m_branchHead != null)
		{
			if (m_branchName == null)
			{
				String name = m_branchHead.getName ();
				if (name.startsWith (Constants.R_HEADS))
					name = name.substring (Constants.R_HEADS.length ());
				else if (name.startsWith (Constants.R_REMOTES))
				{
					name = name.substring (Constants.R_REMOTES.length ());
				}
				m_branchName = name;
			}
			return m_branchName;
		}
		return m_hash;
	}

	public Ref getTag ()
	{
		return m_tag;
	}

	public String getTagName ()
	{
		if (m_tag == null)
			return null;
		if (m_tagName == null)
		{
			m_tagName = m_tag.getName ().substring (Constants.R_TAGS.length ());
		}
		return m_tagName;
	}

	public void setTag (Ref tag)
	{
		m_tag = tag;
	}

	boolean hasBranch ()
	{
		return m_branchHead != null;
	}

	public Ref getBranch ()
	{
		return m_branchHead;
	}

	public void setBranch (Ref branch)
	{
		m_branchHead = branch;
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
	}

	public RelationNode[] getParents ()
	{
		return m_parents;
	}

	public RelationType getRelation (RelationNode parent)
	{
		if (parent.m_branchHead == null)
		{
			return RelationType.CHILD;
		}
		if (m_parents.length > 1)
		{
			return RelationType.MERGE;
		}
		return RelationType.CHILD;
	}
}
