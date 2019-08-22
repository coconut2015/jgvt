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
package org.yuanheng.jgvt.relation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.yuanheng.jgvt.GitRepo;

/**
 * @author	Heng Yuan
 */
public class RelationTreeFactory
{
	public static List<Pattern> getDefaultImportantBranchNames ()
	{
		ArrayList<Pattern> branches = new ArrayList<Pattern> ();
		branches.add (Pattern.compile ("(.*/)?master"));
		branches.add (Pattern.compile ("(.*/)?gh-pages"));
		return branches;
	}

	private final GitRepo m_gitRepo;
	private final List<Pattern> m_importantBranchNames;

	public RelationTreeFactory (GitRepo gitRepo, List<Pattern> importantBranchNames)
	{
		m_gitRepo = gitRepo;
		m_importantBranchNames = importantBranchNames;
	}

	private static RelationNode getImportantNode (RelationTree tree, List<Pattern> importantBranches, GitRepo gitRepo) throws GitAPIException
	{
		if (importantBranches.size () == 0)
			return null;

		RevCommit matchCommit = null;

		List<Ref> branches = gitRepo.getAllBranches ();
		for (int i = 0; i < importantBranches.size () && matchCommit == null; ++i)
		{
			Pattern p = importantBranches.get (i);
			for (Ref ref : branches)
			{
				String name = ref.getName ();
				{
					if (p.matcher (name).matches ())
					{
						RelationNode newNode = tree.getNode (ref.getObjectId ());
						if (newNode != null)
						{
							RevCommit newCommit = newNode.getCommit ();
							if (matchCommit == null ||
								matchCommit.getCommitTime () < newCommit.getCommitTime ())
							{
								matchCommit = newCommit;
							}
						}
					}
				}
			}
		}
		if (matchCommit == null)
			return null;
		return tree.getNode (matchCommit);
	}

	public RelationTree createTree (String startCommit, Iterable<RevCommit> commitLogs) throws GitAPIException, IOException
	{
		RelationTree tree = new RelationTree ();

		// First pass to construct the node graph to construct basic node
		// relationship.
		tree.addNodes (commitLogs, m_gitRepo);

		RelationNode startNode = null;
		if (startCommit == null)
		{
			startNode = getImportantNode (tree, m_importantBranchNames, m_gitRepo);
			if (startNode == null)
			{
				System.out.println ("Unable to determine the main branch.  Please specify the last commit of the main branch.");
				System.exit (1);
			}
		}
		else
		{
			m_gitRepo.fetch ();
			try
			{
				ObjectId id = m_gitRepo.getRepo ().resolve (startCommit);
				startNode = tree.getNode (id);
			}
			catch (Exception ex)
			{
			}
			if (startNode == null)
			{
				System.out.println ("Invalid commit name.");
				System.exit (1);
			}
		}
		BranchDiscoveryAlgorithm.inferBranches (tree, startNode);

		// Third pass to layout the branches
		BranchLayoutAlgorithm.layoutBranches (tree, startNode);

		return tree;
	}
}
