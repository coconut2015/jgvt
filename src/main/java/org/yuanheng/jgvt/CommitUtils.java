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

import java.io.IOException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
public class CommitUtils
{
	public final static int TOOLTIP_AUTHOR = 0x01;
	public final static int TOOLTIP_AUTHOR_TS = 0x02;
	public final static int TOOLTIP_COMMITTER = 0x04;
	public final static int TOOLTIP_COMMITTER_TS = 0x08;
	public final static int TOOLTIP_MESSAGE = 0x10;
	public final static int TOOLTIP_ALL = 0xFFFFFFFF;
	private static String CSS;

	static
	{
		try
		{
			CSS = Utils.getString (CommitUtils.class.getResourceAsStream ("jgvt.css"));
		}
		catch (IOException ex)
		{
			CSS = "";
		}
	}

	public static String getToolTipString (RevCommit commit, int flag)
	{
		boolean first = true;
		StringBuilder builder = new StringBuilder ().append ("<html>");

		PersonIdent authorIdent = commit.getAuthorIdent();
		if ((flag & TOOLTIP_AUTHOR) != 0)
		{
			if (!first)
				builder.append ("<br/>");
			first = false;
			builder.append ("Author: " + authorIdent.getName () + " &lt;<a href=\"mailto:" + authorIdent.getEmailAddress () + "\">" + authorIdent.getEmailAddress () + "</a>&gt;");
		}
		if ((flag & TOOLTIP_AUTHOR_TS) != 0)
		{
			if (!first)
				builder.append ("<br/>");
			first = false;
			builder.append ("Time: " + authorIdent.getWhen());
		}

		PersonIdent committerIdent = commit.getCommitterIdent ();
		if ((flag & TOOLTIP_COMMITTER) != 0)
		{
			if (!first)
				builder.append ("<br/>");
			first = false;
			builder.append ("Committer: " + authorIdent.getName () + " &lt;<a href=\"mailto:" + committerIdent.getEmailAddress () + "\">" + committerIdent.getEmailAddress () + "</a>&gt;");
		}
		if ((flag & TOOLTIP_COMMITTER_TS) != 0)
		{
			if (!first)
				builder.append ("<br/>");
			first = false;
			builder.append ("Time: " + committerIdent.getWhen());
		}

		if ((flag & TOOLTIP_MESSAGE) != 0)
		{
			if (!first)
				builder.append ("<br/>");
			first = false;
			builder.append (commit.getFullMessage ());
		}
		builder.append ("</html>");
		return builder.toString ();
	}

	public static String getAnnotation (Ref[] tags, Ref[] branches)
	{
		StringBuilder builder = new StringBuilder ();
		boolean first;
		if (tags.length > 0)
		{
			builder.append ('(');
			first = true;
			for (Ref tag : tags)
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
		if (branches.length > 0)
		{
			builder.append ('[');
			first = true;
			for (Ref ref : branches)
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
		return builder.toString ();
	}

	private static String getHeader (String str)
	{
		return "<td class=\"subject\">" + str + "</td>";
	}

	private static String getValue (String str)
	{
		return "<td class=\"value\">" + str + "</td>";
	}

	private static String getCommitLink (RevCommit commit)
	{
		String name = commit.getId ().getName ();
		return "<a href=\"http://commit/" + name + "\">" + name + "</a>";
	}

	public static String getComment (RelationNode node)
	{
		boolean first;
		StringBuilder subBuilder;
		RevCommit commit = node.getCommit ();
		StringBuilder builder = new StringBuilder ().append ("<html>");
		builder.append ("<head>").append (CSS);
		builder.append ("<body><table>");

		builder.append ("<tr>");
		builder.append (getHeader ("SHA-1"));
		builder.append (getValue (commit.getId ().getName ()));
		builder.append ("</tr>");

		PersonIdent authorIdent = commit.getAuthorIdent();
		builder.append ("<tr>");
		builder.append (getHeader ("Author"));
		builder.append (getValue (authorIdent.getName () + " &lt;<a href=\"mailto:" + authorIdent.getEmailAddress () + "\">" + authorIdent.getEmailAddress () + "</a>&gt;"));
		builder.append ("</tr>");
		builder.append ("<tr>");
		builder.append (getHeader ("Time"));
		builder.append (getValue (authorIdent.getWhen().toString ()));
		builder.append ("</tr>");

		PersonIdent committerIdent = commit.getCommitterIdent ();
		builder.append ("<tr>");
		builder.append (getHeader ("Committer"));
		builder.append (getValue (committerIdent.getName () + " &lt;<a href=\"mailto:" + committerIdent.getEmailAddress () + "\">" + committerIdent.getEmailAddress () + "</a>&gt;"));
		builder.append ("</tr>");
		builder.append ("<tr>");
		builder.append (getHeader ("Time"));
		builder.append (getValue (committerIdent.getWhen().toString ()));
		builder.append ("</tr>");

		builder.append ("<tr>");
		builder.append (getHeader ("Parents"));
		first = true;
		subBuilder = new StringBuilder ();
		for (RelationNode n : node.getParents ())
		{
			if (first)
				first = false;
			else
				subBuilder.append ("<br/>");
			subBuilder.append (getCommitLink (n.getCommit ()));
		}
		builder.append (getValue (subBuilder.toString ()));
		builder.append ("</tr>");

		builder.append ("<tr>");
		builder.append (getHeader ("Children"));
		first = true;
		subBuilder = new StringBuilder ();
		for (RelationNode n : node.getChildren ())
		{
			if (first)
				first = false;
			else
				subBuilder.append ("<br/>");
			subBuilder.append (getCommitLink (n.getCommit ()));
		}
		builder.append (getValue (subBuilder.toString ()));
		builder.append ("</tr>");

		if (node.getTags ().length > 0)
		{
			builder.append ("<tr>");
			builder.append (getHeader ("Tags"));
			first = true;
			subBuilder = new StringBuilder ();
			for (Ref tag : node.getTags ())
			{
				if (first)
					first = false;
				else
					subBuilder.append ("<br/>");
				subBuilder.append (tag.getName ());
			}
			builder.append (getValue (subBuilder.toString ()));
			builder.append ("</tr>");
		}

		if (node.getBranches ().length > 0)
		{
			builder.append ("<tr>");
			builder.append (getHeader ("Branches"));
			first = true;
			subBuilder = new StringBuilder ();
			for (Ref branch : node.getBranches ())
			{
				if (first)
					first = false;
				else
					subBuilder.append ("<br/>");
				subBuilder.append (branch.getName ());
			}
			builder.append (getValue (subBuilder.toString ()));
			builder.append ("</tr>");
		}

		builder.append ("<tr>");
		builder.append (getHeader ("Message"));
		builder.append (getValue (commit.getFullMessage ()));
		builder.append ("</tr>");
		builder.append ("</table><body></html>");
		return builder.toString ();
	}
}
