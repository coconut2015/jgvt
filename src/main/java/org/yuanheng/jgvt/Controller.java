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

import org.yuanheng.jgvt.swing.SwingGUI;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.view.mxGraph;

/**
 * This class handle all commands.
 *
 * @author	Heng Yuan
 */
public class Controller
{
	private SwingGUI m_gui;
	private GitRepo m_gitRepo;
	private File m_dir;
	private File m_file;

	public Controller (GitRepo gitRepo, File dir, File file)
	{
		m_gitRepo = gitRepo;
		m_dir = dir;
		m_file = file;
	}

	public void setGUI (SwingGUI gui)
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

		mxGraph graph = m_gui.getGraph ();
		TreeFactory factory = new TreeFactory (graph, m_gitRepo);
		factory.updateGraphModel (m_gitRepo.getCommitLogs (m_file));
		new mxHierarchicalLayout(graph).execute(graph.getDefaultParent());
 	}

	public File getCurrentDirectory ()
	{
		return m_dir;
	}

	public void setCurrentDirectory (File dir)
	{
		m_dir = dir;
	}
}
