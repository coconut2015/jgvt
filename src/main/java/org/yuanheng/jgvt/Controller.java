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

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.yuanheng.jgvt.gui.GUI;
import org.yuanheng.jgvt.gui.ListInfo;
import org.yuanheng.jgvt.gui.graph.GVTGraph;
import org.yuanheng.jgvt.gui.graph.GVTGraphFactory;
import org.yuanheng.jgvt.relation.RelationNode;
import org.yuanheng.jgvt.relation.RelationTree;
import org.yuanheng.jgvt.relation.RelationTreeFactory;

/**
 * This class handle all commands.
 *
 * @author	Heng Yuan
 */
public class Controller
{
	private final static HyperlinkListener s_browserUrlHandler = new HyperlinkListener ()
	{
		@Override
		public void hyperlinkUpdate (HyperlinkEvent e)
		{
			try
			{
				if (e.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
				{
					Utils.browse (e.getURL ().toURI ());
				}
			}
			catch (Exception ex)
			{
			}
		}
	};

	public static HyperlinkListener getBrowserUrlHandler ()
	{
		return s_browserUrlHandler;
	}

	private GUI m_gui;
	private GitRepo m_gitRepo;
	private File m_dir;
	private File m_file;
	private RelationTree m_tree;
	private final Pref m_prefs;
	private RelationNode m_selectedNode;
	private RelationNode m_rememberedNode;

	private final HyperlinkListener m_commitUrlHandler = new HyperlinkListener ()
	{
		@Override
		public void hyperlinkUpdate (HyperlinkEvent e)
		{
			try
			{
				if (e.getEventType () == HyperlinkEvent.EventType.ACTIVATED)
				{
					handleCommitURL (e.getURL ());
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace ();
			}
		}
	};

	public Controller (GitRepo gitRepo, File dir, File file)
	{
		m_gitRepo = gitRepo;
		m_dir = dir;
		m_file = file;
		m_prefs = new Pref ();
	}

	public void setGUI (GUI gui)
	{
		m_gui = gui;
	}

	public GUI getGUI ()
	{
		return m_gui;
	}

	public GVTGraph getGraph ()
	{
		return m_gui.getGraph ();
	}

	public RelationTree getRelationTree ()
	{
		return m_tree;
	}

	public void generateTree (String startCommit) throws Exception
	{
		m_gui.setRoot (m_gitRepo.getRoot ().getAbsolutePath ());
		m_gui.setBranch (m_gitRepo.getBranch ());
		if (m_file == null)
			m_gui.setFile ("");
		else
			m_gui.setFile (Utils.getRelativePath (m_file, m_gitRepo.getRoot ()).toString ());

		GVTGraph graph = m_gui.getGraph ();
		RelationTreeFactory nodeFactory = new RelationTreeFactory (m_gitRepo, RelationTreeFactory.getDefaultImportantBranchNames ());
		m_tree = nodeFactory.createTree (startCommit, m_gitRepo.getCommitLogs (m_file));

		GVTGraphFactory factory = new GVTGraphFactory (graph);
		factory.updateGraphModel (m_tree, graph.getToolTipFlag ());
 	}

	public void centerTree () throws Exception
	{
		ObjectId head = m_gitRepo.getHead ();
		if (head != null)
		{
			final RelationNode node = m_tree.getNode (head);
			SwingUtilities.invokeLater (new Runnable ()
			{
				@Override
				public void run ()
				{
					select (node, true);
				}
			});
		}
 	}

	public void select (RelationNode node, boolean center)
	{
		m_selectedNode = node;
		m_gui.select (node, center);
	}

	public void select (String commit, boolean center)
	{
		ObjectId id = ObjectId.fromString (commit);
		RelationNode node = m_tree.getNode (id);
		if (node != null)
			select (node, center);
	}

	public File getCurrentDirectory ()
	{
		return m_dir;
	}

	public void setCurrentDirectory (File dir)
	{
		m_dir = dir;
	}

	public Pref getPrefs ()
	{
		return m_prefs;
	}

	public GitRepo getGitRepo ()
	{
		return m_gitRepo;
	}

	public boolean remember ()
	{
		RelationNode node = m_selectedNode;
		if (node == null)
		{
			return false;
		}
		if (m_rememberedNode == node)
		{
			return true;
		}

		m_rememberedNode = node;
		m_gui.setRemembered (node);
		return true;
	}

	public void clearRemember ()
	{
		m_rememberedNode = null;
		m_gui.setRemembered (null);
	}

	public boolean locateRemember ()
	{
		if (m_rememberedNode == null)
			return false;
		m_gui.select (m_rememberedNode, true);
		return true;
	}

	public boolean compareToRemember ()
	{
		if (m_selectedNode == null ||
			m_rememberedNode == null ||
			m_selectedNode == m_rememberedNode)
			return false;
		m_gui.showDiffWindow (m_rememberedNode, m_selectedNode);
		return true;
	}

	public RelationNode getRememberedNode ()
	{
		return m_rememberedNode;
	}

	public boolean hasRememberNode ()
	{
		return m_rememberedNode != null;
	}

	public boolean hasSelectedNode ()
	{
		return m_selectedNode != null;
	}

	public HyperlinkListener getCommitUrlHandler ()
	{
		return m_commitUrlHandler;
	}

	public void handleCommitURL (URL url)
	{
		try
		{
			String protocol = url.getProtocol ();
			if ("http".equals (protocol))
			{
				String host = url.getHost ();
				if (CommitUtils.HOST_COMMIT.equals (host))
				{
					String commitId = url.getPath ().substring (1);
					select (commitId, true);
				}
				else if (CommitUtils.HOST_DIFFTOOL.equals (url.getHost ()))
				{
					String encodedCommits = url.getPath ().substring (1);
					String commits = URLDecoder.decode (encodedCommits, "UTF-8");
					String cmd = "git difftool " + commits;
					System.out.println (cmd);
					Runtime.getRuntime ().exec (cmd, null, m_gitRepo.getRoot ());
				}
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace ();
		}
	}

	private List<ListInfo> getListInfo (List<Ref> refs)
	{
		ArrayList<ListInfo> listInfos = new ArrayList<ListInfo> ();
		for (Ref ref : refs)
		{
			listInfos.add (new ListInfo (ref, m_tree.getNode (ref.getObjectId ())));
		}
		return listInfos;
	}

	public List<ListInfo> getBranchList () throws GitAPIException
	{
		return getListInfo (m_gitRepo.getAllBranches ());
	}

	public List<ListInfo> getTagList () throws GitAPIException
	{
		return getListInfo (m_gitRepo.getTags ());
	}
}
