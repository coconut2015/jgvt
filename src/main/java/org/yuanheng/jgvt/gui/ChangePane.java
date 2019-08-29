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
package org.yuanheng.jgvt.gui;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.List;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.yuanheng.jgvt.ChangeInfo;
import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.gui.changetree.ChangeTree;
import org.yuanheng.jgvt.gui.changetree.ChangeTreeNode;

/**
 * @author	Heng Yuan
 */
class ChangePane extends JPanel
{
	private static final long serialVersionUID = 1817444711843343803L;

	private final Controller m_controller;
	private ChangeTree m_changeTree;
	private JEditorPane m_msgPane;

	private final ListSelectionListener m_selectListener = new ListSelectionListener ()
	{
		@Override
		public void valueChanged (ListSelectionEvent e)
		{
			if (e.getValueIsAdjusting ())
				return;
			String html = m_changeTree.getSelectedNodeHtml (m_controller);
			if (html != null)
			{
				m_msgPane.setText (html);
				m_msgPane.setCaretPosition (0);
			}
		}
	};

	private final ComponentListener m_resizeListener = new ComponentAdapter ()
	{
	    @Override
		public void componentResized(ComponentEvent e)
	    {
    		((JSplitPane)e.getSource ()).setDividerLocation (0.5);
    		((JSplitPane)e.getSource ()).removeComponentListener (this);
	    }
	};

	public ChangePane (Controller controller, ChangeTreeNode root)
	{
		m_controller = controller;
		setLayout (new BorderLayout ());
		m_changeTree = new ChangeTree (root);
		createHtmlPane ();
		JScrollPane scrollPane1 = new JScrollPane (m_changeTree);
		JScrollPane scrollPane2 = new JScrollPane (m_msgPane);
		JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, scrollPane1, scrollPane2);
		splitPane.addComponentListener (m_resizeListener);
		splitPane.setDividerLocation (0.5);
		splitPane.setResizeWeight (0.5);
		add (splitPane, BorderLayout.CENTER);
		m_changeTree.getSelectionModel ().addListSelectionListener (m_selectListener);
	}

	private void createHtmlPane ()
	{
		m_msgPane = new JEditorPane ();
		m_msgPane.setContentType ("text/html");
		m_msgPane.setEditable (false);
		m_msgPane.addHyperlinkListener (m_controller.getCommitUrlHandler ());
	}

	public Controller getController ()
	{
		return m_controller;
	}

	public ChangeTree getChangeTree ()
	{
		return m_changeTree;
	}

	public ChangeTreeNode getRoot ()
	{
		return (ChangeTreeNode)m_changeTree.getTreeTableModel ().getRoot ();
	}

	public void setChanges (List<ChangeInfo> changes)
	{
		m_changeTree.setChanges (changes);
		m_changeTree.getSelectionModel ().setSelectionInterval (0, 0);
	}
}
