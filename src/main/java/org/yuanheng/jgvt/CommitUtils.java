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

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class CommitUtils
{
	public final static int TOOLTIP_AUTHOR = 0x01;
	public final static int TOOLTIP_AUTHOR_TS = 0x02;
	public final static int TOOLTIP_COMMITTER = 0x04;
	public final static int TOOLTIP_COMMITTER_TS = 0x08;
	public final static int TOOLTIP_MESSAGE = 0x10;
	public final static int TOOLTIP_ALL = 0xFFFFFFFF;

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

	public static String getComment (RevCommit commit)
	{
		return getToolTipString (commit, TOOLTIP_ALL);
	}
}
