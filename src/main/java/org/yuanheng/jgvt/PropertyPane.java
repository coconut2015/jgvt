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

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class PropertyPane extends JPanel
{
	private static final long serialVersionUID = -3411516962635048642L;

	private final Controller m_controller;
	private ChangeTree m_changeTree;
	private JTextPane m_msgPane;
	private RevCommit m_commit;

	public PropertyPane (Controller controller)
	{
		m_controller = controller;
		setLayout (new BorderLayout ());
		createChangeTree ();
		createHtmlPane ();
		JScrollPane scrollPane1 = new JScrollPane (m_changeTree);
		JScrollPane scrollPane2 = new JScrollPane (m_msgPane);
		JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, scrollPane1, scrollPane2);
		add (splitPane, BorderLayout.CENTER);
	}

	private void createChangeTree ()
	{
		m_changeTree = new ChangeTree ();
	}

	private void createHtmlPane ()
	{
		m_msgPane = new JTextPane ();
		m_msgPane.setContentType ("text/html");
		m_msgPane.setEditable (false);
	}

	public void readCommit (RevCommit commit)
	{
		if (m_commit == commit)
			return;
		m_commit = commit;

		m_msgPane.setText (CommitUtils.getComment (commit));
		m_msgPane.setCaretPosition (0);

		List<DiffEntry> changes = m_controller.getGitRepo ().getChanges (commit);
		m_changeTree.setList (changes);
	}
}
