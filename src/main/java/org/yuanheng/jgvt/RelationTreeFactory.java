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

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class RelationTreeFactory
{
	public static List<Pattern> getDefaultImportantBranchNames ()
	{
		ArrayList<Pattern> branches = new ArrayList<Pattern> ();
		branches.add (Pattern.compile ("(.*/)?master"));
		branches.add (Pattern.compile ("(.*/)?gh-pages"));
		return branches;
	}

	private final static Comparator<RelationNode> s_sortByDate = new Comparator<RelationNode> ()
	{
		@Override
		public int compare (RelationNode o1, RelationNode o2)
		{
			return o1.getCommit ().getCommitTime () - o2.getCommit ().getCommitTime ();
		}
	};

	private final GitRepo m_gitRepo;
	private final List<Pattern> m_importantBranchNames;

	RelationTreeFactory (GitRepo gitRepo, List<Pattern> importantBranchNames)
	{
		m_gitRepo = gitRepo;
		m_importantBranchNames = importantBranchNames;
	}

	private static List<RelationNode> getBranches (RelationTree tree, List<Pattern> importantBranches, Map<String, ObjectId> reverseBranchMap)
	{
		ArrayList<RelationNode> branches = new ArrayList<RelationNode> ();
		if (importantBranches.size () == 0)
			return branches;

		for (String branchName : reverseBranchMap.keySet ())
		{
			for (int i = 0; i < importantBranches.size (); ++i)
			{
				if (importantBranches.get (i).matcher (branchName).matches ())
				{
					branches.add (tree.getNode (reverseBranchMap.get (branchName)));
					break;
				}
			}
		}
		return branches;
	}

	private void walkTree (RelationNode startNode)
	{
		ArrayList<RelationNode> stack = new ArrayList<RelationNode> ();

		// initiate the tree with the main branch on the left.
		RelationNode node = startNode;
		RelationBranch mainBranch = new RelationBranch (startNode);
		for (;;)
		{
			stack.add (node);
			RelationNode[] parents = node.getParents ();
			if (parents.length > 0)
			{
				if (parents[0].getRelationBranch () != null)
				{
					break;
				}

				// set this child as the parent's first
				parents[0].setNthChild (node, 0);
				node = parents[0];
				mainBranch.add (node);
			}
			else
			{
				break;
			}
		}

		// now walk the tree
		while (stack.size () > 0)
		{
			node = stack.remove (stack.size () - 1);
			LayoutInfo layoutInfo = node.getLayoutInfo ();  
			RelationBranch branch = node.getRelationBranch ();
			if (layoutInfo.isVisited ())
				continue;

			if (branch == null)
			{
				branch = new RelationBranch (node);
			}
			layoutInfo.visit ();

			// scan parent first
			int index = 0;
			for (RelationNode parent : node.getParents ())
			{
				++index;
				RelationBranch parentBranch = parent.getRelationBranch ();
				if (parentBranch != null)
				{
					continue;
				}

				if (index == 0)
				{
					branch.add (parent);
				}
				else
				{
					// parent is the END of a branch which merges
					// to the node.
					new RelationBranch (parent);
				}
				stack.add (parent);
			}
		}
	}

	private void scanBranches (RelationTree tree, List<RelationNode> importantBranches) throws GitAPIException
	{
		// See if we can trace from the main branch and collect branches.
		if (importantBranches.size () > 0)
		{
			RelationNode startNode = importantBranches.get (0);
			walkTree (startNode);
		}

		// Now find remaining nodes with multiple parents.
		ArrayList<RelationNode> multiParentNodes = new ArrayList<RelationNode> ();
		for (RelationNode node : tree.getNodes ())
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			if (node.getParents ().length > 1)
			{
				multiParentNodes.add (node);
			}
		}
		Collections.sort (multiParentNodes, s_sortByDate);
		for (RelationNode node : multiParentNodes)
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			walkTree (node);
		}

		// find any remaining branches starting from leaves.
		List <RelationNode> leaves = tree.getLeaves ();
		Collections.sort (leaves, s_sortByDate);
		for (RelationNode node : leaves)
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			walkTree (node);
		}

		// perform branch merging
		for (RelationNode node : tree.getNodes ())
		{
			if (node.getParents ().length > 0)
			{
				RelationNode parent = node.getParents ()[0];
				RelationBranch branch = node.getRelationBranch ();
				if (parent.getRelationBranch () != branch &&
					parent.getChildren ().length == 1)
				{
					// this node is the only child of the parent node,
					// and the parent is in a different branch.
					// merge the two branches.
					branch.merge (parent.getRelationBranch ());
				}
			}
		}
	}

	private void layoutBranches (RelationTree tree, List<RelationNode> importantBranches) throws GitAPIException, IOException
	{
		if (importantBranches.size () > 0)
		{
		}
		else
		{
			throw new RuntimeException ("Not yet implemented.");
		}
	}

	public RelationTree createTree (Iterable<RevCommit> commitLogs) throws GitAPIException, MissingObjectException, IncorrectObjectTypeException, IOException
	{
		RelationTree tree = new RelationTree ();

		// First pass to construct the node graph to construct basic node
		// relationship.
		for (RevCommit commit : commitLogs)
		{
			RelationNode node = tree.getNode (commit, m_gitRepo);

			for (RevCommit parentCommit : commit.getParents ())
			{
				RelationNode parentNode = tree.getNode (parentCommit, m_gitRepo);

				node.addParent (parentNode);
			}
		}

		Map<String, ObjectId> reverseBranchMap = m_gitRepo.getReverseBranchMap ();
		List<RelationNode> importantBranches = getBranches (tree, m_importantBranchNames, reverseBranchMap);

		scanBranches (tree, importantBranches);
		layoutBranches (tree, importantBranches);

		return tree;
	}
}
