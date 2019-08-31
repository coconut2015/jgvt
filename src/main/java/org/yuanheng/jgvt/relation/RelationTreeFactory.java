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
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.yuanheng.jgvt.GitRepo;

/**
 * @author	Heng Yuan
 */
public class RelationTreeFactory
{
	public static List<String> getDefaultImportantBranchNames ()
	{
		ArrayList<String> branches = new ArrayList<String> ();
		branches.add ("(.*/)?master");
		branches.add ("(.*/)?gh-pages");
		return branches;
	}

	private final GitRepo m_gitRepo;
	private final List<String> m_importantBranchNames;

	public RelationTreeFactory (GitRepo gitRepo, List<String> importantBranchNames)
	{
		m_gitRepo = gitRepo;
		m_importantBranchNames = importantBranchNames;
	}

	private static List<RelationNode> getImportantNodes (RelationTree tree, List<String> importantBranches, GitRepo gitRepo) throws GitAPIException
	{
		ArrayList<RelationNode> nodes = new ArrayList<RelationNode> ();

		if (importantBranches.size () == 0)
			return nodes;

		List<Ref> branches = gitRepo.getAllBranches ();
		for (int i = 0; i < importantBranches.size (); ++i)
		{
			Pattern p = Pattern.compile (importantBranches.get (i));
			for (Ref ref : branches)
			{
				String name = ref.getName ();
				{
					if (p.matcher (name).matches ())
					{
						RelationNode node = tree.getNode (ref.getObjectId ());
						if (node.getWeight () > i)
						{
							node.setWeight (i);
							if (node != null)
							{
								nodes.add (node);
							}
						}
					}
				}
			}
		}
		Collections.sort (nodes, RelationNode.sortByWeightComparator);

		// now based on the order, renumber the weight
		int weight = 0;
		for (RelationNode node : nodes)
		{
			node.setWeight (weight);
			++weight;
		}

		return nodes;
	}

	public RelationTree createTree (Iterable<RevCommit> commitLogs) throws GitAPIException, IOException
	{
		RelationTree tree = new RelationTree ();

		// First pass to construct the node graph to construct basic node
		// relationship.
		tree.addNodes (commitLogs, m_gitRepo);

		if (tree.getNodes ().size () == 0)
		{
			return tree;
		}

		List<RelationNode> importantNodes = getImportantNodes (tree, m_importantBranchNames, m_gitRepo);
		RelationNode startNode;
		if (importantNodes.size () > 0)
		{
			startNode = importantNodes.get (0);
		}
		else
		{
			// use the starting node as the dummy start node.
			startNode = Collections.min (tree.getNodes ());
		}
		BranchDiscoveryAlgorithm.inferBranches (tree, startNode);

		// Third pass to layout the branches
		BranchLayoutAlgorithm.layoutBranches (tree, startNode);

		return tree;
	}
}
