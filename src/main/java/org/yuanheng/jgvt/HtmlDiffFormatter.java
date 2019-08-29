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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.util.io.NullOutputStream;

/**
 * @author	Heng Yuan
 */
public class HtmlDiffFormatter extends DiffFormatter
{
	private OutputStream m_os;
	/**
	 * @param	os
	 * 			UTF-8 output stream
	 */
	public HtmlDiffFormatter (OutputStream os)
	{
		super (NullOutputStream.INSTANCE);
		m_os = os;
	}

	@Override
	protected void formatGitDiffFirstHeaderLine (ByteArrayOutputStream o, ChangeType type, String oldPath, String newPath) throws IOException
	{
	}

	@Override
	protected void formatIndexLine (OutputStream o, DiffEntry ent) throws IOException
	{
	}

	@Override
	protected void writeContextLine (RawText text, int line) throws IOException
	{
		m_os.write (("<tr><td class=\"linecontext\">" + line + "</td><td class=\"linecontext\"></td><td class=\"context\"></td><td class=\"context\"><pre>").getBytes (StandardCharsets.UTF_8));
		text.writeLine(m_os, line);
		m_os.write ("</pre></td></tr>".getBytes (StandardCharsets.UTF_8));
	}

	@Override
	protected void writeAddedLine (RawText text, int line) throws IOException
	{
		m_os.write (("<tr><td class=\"lineadded\"></td><td class=\"lineadded\">" + line + "</td><td class=\"added\">+</td><td class=\"added\"><pre>").getBytes (StandardCharsets.UTF_8));
		text.writeLine(m_os, line);
		m_os.write ("</pre></td></tr>".getBytes (StandardCharsets.UTF_8));
	}

	@Override
	protected void writeRemovedLine (RawText text, int line) throws IOException
	{
		m_os.write (("<tr><td class=\"lineremoved\">" + line + "</td><td class=\"lineremoved\"></td><td class=\"removed\">-</td><td class=\"removed\"><pre>").getBytes (StandardCharsets.UTF_8));
		text.writeLine(m_os, line);
		m_os.write ("</pre></td></tr>".getBytes (StandardCharsets.UTF_8));
	}

	@Override
	protected void writeHunkHeader (int aStartLine, int aEndLine, int bStartLine, int bEndLine) throws IOException
	{
		StringBuilder builder = new StringBuilder ();
		builder.append ("<tr><td class=\"linehunk\" colspan=\"2\"><td class=\"hunk\"></td><td class=\"hunk\" colspan=\"2\">");
		builder.append ("@@");
		builder.append (" -");
		appendRange (builder, aStartLine + 1, aEndLine - aStartLine);
		builder.append (" +");
		appendRange(builder, bStartLine + 1, bEndLine - bStartLine);
		builder.append (" @@</td></tr>");
		m_os.write (builder.toString ().getBytes (StandardCharsets.UTF_8));
	}

	private void appendRange(StringBuilder builder, int begin, int cnt)
	{
		switch (cnt)
		{
			case 0:
				builder.append (begin - 1).append (",0");
				break;
			case 1:
				builder.append (begin);
				break;
			default:
				builder.append (begin).append (",").append (cnt);
				break;
		}
	}
}
