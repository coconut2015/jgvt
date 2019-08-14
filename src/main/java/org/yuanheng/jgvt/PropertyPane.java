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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.URL;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.eclipse.jgit.diff.DiffEntry;

/**
 * @author	Heng Yuan
 */
class PropertyPane extends JPanel
{
	private static final long serialVersionUID = -3411516962635048642L;

	private final Controller m_controller;
	private ChangeTree m_changeTree;
	private JEditorPane m_msgPane;
	private RelationNode m_node;

	private final HyperlinkListener m_urlHandler = new HyperlinkListener ()
	{
		@Override
		public void hyperlinkUpdate (HyperlinkEvent e)
		{
			try
			{
				if (e.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
				{
					handleURL (e.getURL ());
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}
	};

	private final ComponentListener m_resizeListener = new ComponentAdapter ()
	{
	    public void componentResized(ComponentEvent e)
	    {
    		((JSplitPane)e.getSource ()).setDividerLocation (0.5);
    		((JSplitPane)e.getSource ()).removeComponentListener (this);
	    }
	};

	public PropertyPane (Controller controller)
	{
		m_controller = controller;
		setLayout (new BorderLayout ());
		createChangeTree ();
		createHtmlPane ();
		JScrollPane scrollPane1 = new JScrollPane (m_changeTree);
		JScrollPane scrollPane2 = new JScrollPane (m_msgPane);
		JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, scrollPane1, scrollPane2);
		splitPane.addComponentListener (m_resizeListener);
		splitPane.setDividerLocation (0.5);
		splitPane.setResizeWeight (0.5);
		add (splitPane, BorderLayout.CENTER);
	}

	private void createChangeTree ()
	{
		m_changeTree = new ChangeTree ();
	}

	private void createHtmlPane ()
	{
		m_msgPane = new JEditorPane ();
		m_msgPane.setContentType ("text/html");
		m_msgPane.setEditable (false);
		m_msgPane.addHyperlinkListener (m_urlHandler);
	}

	public void handleURL (URL url)
	{
		String protocol = url.getProtocol ();
		if ("http".equals (protocol) &&
			"commit".equals (url.getHost ()))
		{
			String commitId = url.getPath ().substring (1);
			m_controller.select (commitId, true);
		}
	}

	public void select (RelationNode node)
	{
		if (m_node == node)
			return;
		m_node = node;

		m_msgPane.setText (CommitUtils.getComment (node));
		m_msgPane.setCaretPosition (0);

		List<DiffEntry> changes = m_controller.getGitRepo ().getChanges (node.getCommit ());
		m_changeTree.setList (node, changes);
	}
}
