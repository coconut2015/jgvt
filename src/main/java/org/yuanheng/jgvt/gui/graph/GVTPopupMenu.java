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
package org.yuanheng.jgvt.gui.graph;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.Controller;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
class GVTPopupMenu extends JPopupMenu
{
	private static final long serialVersionUID = -6307549652207117222L;

	private final Controller m_controller;
	private final GVTGraphComponent m_graphComp;

	private final JMenu m_parentMenu;
	private final JMenu m_childMenu;
	private final JMenu m_joinMenu;

	private RelationNode m_node;

	private Action m_rememberAction = new AbstractAction ("Remember")
	{
		private static final long serialVersionUID = -3444960236270282069L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_controller.remember (m_node);
		}
	};

	private Action m_compareRememberAction = new AbstractAction ("Compare to remembered")
	{
		private static final long serialVersionUID = 8662696193508753514L;

		@Override
		public void actionPerformed (ActionEvent e)
		{
			m_controller.compareToRemember (m_node);
		}
	};

	private ActionListener m_centerNodeListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			GVTPopupMenuItem item = (GVTPopupMenuItem) e.getSource ();
			m_controller.select (item.getNode (), true);
		}
	};

	private ActionListener m_joinBranchListener = new ActionListener ()
	{
		@Override
		public void actionPerformed (ActionEvent e)
		{
			final RelationNode node = m_node;
			GVTPopupMenuItem item = (GVTPopupMenuItem) e.getSource ();
			m_controller.joinBranch (node, item.getNode ());
			SwingUtilities.invokeLater (() -> { m_controller.select (node, true); });
		}
	};

	public GVTPopupMenu (Controller controller, GVTGraphComponent graphComp)
	{
		m_controller = controller;
		m_graphComp = graphComp;

		m_parentMenu = new JMenu ("Parents");
		m_childMenu = new JMenu ("Children");
		m_joinMenu = new JMenu ("Join branch");

		add (new JMenuItem (m_rememberAction));
		add (new JMenuItem (m_compareRememberAction));
		addSeparator ();
		add (m_parentMenu);
		add (m_childMenu);
		add (m_joinMenu);
	}

	public void show (RelationNode node, int x, int y)
	{
		m_node = node;
		m_compareRememberAction.setEnabled (m_controller.hasRememberNode () &&
											m_controller.getRememberedNode () != node);

		setLabel (CommitUtils.getName (node));

		m_parentMenu.removeAll ();
		for (RelationNode parent : node.getParents ())
		{
			GVTPopupMenuItem item = new GVTPopupMenuItem (parent);
			item.addActionListener (m_centerNodeListener);
			m_parentMenu.add (item);
		}
		m_childMenu.removeAll ();
		for (RelationNode child : node.getChildren ())
		{
			GVTPopupMenuItem item = new GVTPopupMenuItem (child);
			item.addActionListener (m_centerNodeListener);
			m_childMenu.add (item);
		}

		m_joinMenu.removeAll ();
		RelationNode[] joinNodes = node.canJoinParentBranch ();
		if (joinNodes != null)
		{
			m_joinMenu.setEnabled (true);
			for (RelationNode parent : joinNodes)
			{
				GVTPopupMenuItem item = new GVTPopupMenuItem (parent);
				item.addActionListener (m_joinBranchListener);
				m_joinMenu.add (item);
			}
		}
		else
		{
			m_joinMenu.setEnabled (false);
		}

		show (m_graphComp.getGraphControl (), x, y);
	}
}
