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
import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;

/**
 * This class handle all commands.
 *
 * @author	Heng Yuan
 */
class Controller
{
	private GUI m_gui;
	private GitRepo m_gitRepo;
	private File m_dir;
	private File m_file;
	private RelationTree m_tree;
	private final Pref m_prefs;

	private DotFileOptions m_dotFileOptions;

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

	public void updateGUI () throws Exception
	{
		m_gui.setRoot (m_gitRepo.getRoot ().getAbsolutePath ());
		m_gui.setBranch (m_gitRepo.getBranch ());
		if (m_file == null)
			m_gui.setFile ("");
		else
			m_gui.setFile (Utils.getRelativePath (m_file, m_gitRepo.getRoot ()).toString ());

		GVTGraph graph = m_gui.getGraph ();
		RelationTreeFactory nodeFactory = new RelationTreeFactory (m_gitRepo, RelationTreeFactory.getDefaultImportantBranchNames ());
		m_tree = nodeFactory.createTree (m_gitRepo.getCommitLogs (m_file));

		GVTGraphFactory factory = new GVTGraphFactory (graph);
		factory.updateGraphModel (m_tree, graph.getToolTipFlag ());
 	}

	public void select (RelationNode node, boolean center)
	{
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

	public void exportDot (File file) throws IOException
	{
		new DotConverter ().save (file, "jgvt", getDotFileOptions (), m_tree);
	}

	public Pref getPrefs ()
	{
		return m_prefs;
	}

	public DotFileOptions getDotFileOptions ()
	{
		if (m_dotFileOptions == null)
		{
			m_dotFileOptions = new DotFileOptions ();
		}
		return m_dotFileOptions;
	}

	public GitRepo getGitRepo ()
	{
		return m_gitRepo;
	}
}
