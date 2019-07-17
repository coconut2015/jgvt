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
import java.util.Date;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
public class CommitNode implements Serializable
{
	public static int HASH_LENGTH = 6;

	private static final long serialVersionUID = 3470339333207629584L;

	private final RevCommit m_commit;
	private String m_hash;
	private Ref m_tag;
	private Ref m_branch;
	private String m_toolTip;

	public CommitNode (RevCommit commit)
	{
		m_commit = commit;

		m_hash = commit.abbreviate (HASH_LENGTH).name ();
	}

	public RevCommit getCommit ()
	{
		return m_commit;
	}

	public void setName (String name)
	{
		m_hash = name;
	}

	@Override
	public String toString ()
	{
		if (m_branch != null)
			return m_branch.getName ();
		return m_hash;
	}

	public Ref getTag ()
	{
		return m_tag;
	}

	public void setTag (Ref tag)
	{
		m_tag = tag;
	}

	public Ref getBranch ()
	{
		return m_branch;
	}

	public void setBranch (Ref branch)
	{
		m_branch = branch;
	}

	public String getTooltip ()
	{
		if (m_toolTip == null)
		{
			StringBuilder builder = new StringBuilder ();

			PersonIdent authorIdent = m_commit.getAuthorIdent();
			Date authorDate = authorIdent.getWhen();

			builder.append ("<html>Author: " + authorIdent.getName ());
			builder.append ("<br/>Time: " + authorDate);

			PersonIdent committerIdent = m_commit.getCommitterIdent ();
			Date committerDate = committerIdent.getWhen();

			builder.append ("<br/>Committer: " + committerIdent.getName ());
			builder.append ("<br/>Time: " + committerDate);
			builder.append ("</html>");
			m_toolTip = builder.toString ();
		}
		return m_toolTip;
	}
}
