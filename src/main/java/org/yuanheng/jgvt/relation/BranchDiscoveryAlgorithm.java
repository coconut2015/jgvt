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

import java.util.*;

import org.yuanheng.jgvt.CommitUtils;
import org.yuanheng.jgvt.Main;
import org.yuanheng.jgvt.relation.discovery.DiscoveryAlgorithm;
import org.yuanheng.jgvt.relation.discovery.DiscoveryAlgorithmManager;
import org.yuanheng.jgvt.relation.discovery.DiscoveryUtils;

/**
 * I am primarily using the following GitHub repos to learn the branch patterns:
 * <p>
 * <ul>
 * <li>SQLite JDBC Driver: https://github.com/xerial/sqlite-jdbc
 * <li>React: https://github.com/facebook/react
 * </ul>
 *
 * @author	Heng Yuan
 */
public class BranchDiscoveryAlgorithm
{
	/**
	 * Infer branches from the tree nodes, and a list of important branches.
	 *
	 * @param	tree
	 * 			relation tree
	 * @param	startNode
	 * 			the main branch start node.
	 * @param	editList
	 * 			a list of nodes which should be part of the parent branch.
	 */
	public static void inferBranches (RelationTree tree, RelationEditList editList, BranchLog log)
	{
		if (tree.size () == 0)
			return;

		// See if we can trace from the main branch and collect branches.
		discoverInitialBranches (tree.getStartNode (), true, editList);

		// Now find remaining nodes with multiple parents.
		ArrayList<RelationNode> multiParentNodes = new ArrayList<RelationNode> ();
		for (RelationNode node : tree.getNodes ())
		{
			if (node.isVisited ())
				continue;
			if (node.getParents ().length > 1)
			{
				multiParentNodes.add (node);
			}
		}
		Collections.sort (multiParentNodes, RelationNode.sortByDateComparator);
		for (RelationNode node : multiParentNodes)
		{
			if (node.isVisited ())
				continue;
			discoverInitialBranches (node, false, editList);
		}

		// find any remaining branches starting from leaves.
		List <RelationNode> leaves = tree.getLeaves ();
		Collections.sort (leaves, RelationNode.sortByDateComparator);
		for (RelationNode node : leaves)
		{
			if (node.isVisited ())
				continue;
			discoverInitialBranches (node, false, editList);
		}

		mergeBranches (tree, log);
	}

	/**
	 * Discover the initial branches by having the the start (i.e. last node)
	 * of the main branch.  Once the main branch is set, we can discover
	 * side branches.
	 *
	 * @param	startNode
	 * 			the last node of the main branch
	 * @param	reachRoot
	 * 			should reaching root via parent 0 be the goal of the initial search
	 * @param	editList
	 * 			a list of nodes which should be part of the parent branch.
	 */
	private static void discoverInitialBranches (RelationNode startNode, boolean reachRoot, RelationEditList editList)
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
				int joinParent = editList.getJoinParent (node.getCommit ());
				if (joinParent >= 0)
				{
					if (joinParent == 1)
					{
						node.swapParentOrder ();
					}

					RelationNode parentNode = parents[joinParent];
					parentNode.setNthChild (node, 0);
					if (parentNode.getRelationBranch () != null)
					{
						mainBranch.mergeParent (parentNode.getRelationBranch ());
						break;
					}
					else
					{
						node = parentNode;
						mainBranch.add (parentNode);
						continue;
					}
				}
				else
				{
					RelationNode parentNode = parents[0];
					if (parentNode.getRelationBranch () != null)
					{
						break;
					}
					if (!reachRoot &&
						(parents.length > 1 ||
						 parentNode.getChildren ().length > 1))
					{
						break;
					}

					// set this child as the parent's first
					parentNode.setNthChild (node, 0);
					node = parentNode;
					mainBranch.add (node);
				}
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
			RelationBranch branch = node.getRelationBranch ();
			if (node.isVisited ())
				continue;

			if (branch == null)
			{
				branch = new RelationBranch (node);
			}
			node.visit ();

			int joinParent = editList.getJoinParent (node.getCommit ());
			if (joinParent >= 0 &&
				node.getParents ().length > joinParent)
			{
				if (joinParent == 1)
				{
					node.swapParentOrder ();
				}
				RelationNode parentNode = node.getParents ()[joinParent];
				parentNode.setNthChild (node, 0);

				if (parentNode.getRelationBranch () != null)
				{
					branch.mergeParent (parentNode.getRelationBranch ());
				}
				else
				{
					branch.add (parentNode);
					stack.add (parentNode);
				}
			}
			else
			{
				// scan parents
				if (node.getParents ().length == 1 &&
					node.getParents ()[0].getChildren ().length == 1 &&
					node.getParents ()[0].getRelationBranch () == null)
				{
					branch.add (node.getParents ()[0]);
					stack.add (node.getParents ()[0]);
				}
				else
				{
					for (RelationNode parent : node.getParents ())
					{
						RelationBranch parentBranch = parent.getRelationBranch ();
						if (parentBranch != null)
						{
							continue;
						}
						new RelationBranch (parent);
						stack.add (parent);
					}
				}
			}
		}
	}

	public static void mergeBranches (RelationTree tree, BranchLog log)
	{
		int iteration = 0;

		@SuppressWarnings ("unchecked")
		HashSet<RelationBranch>[] branchSets = (HashSet<RelationBranch>[]) new HashSet<?>[2];
		branchSets[0] = tree.getBranchSet ();
		branchSets[1] = new HashSet<RelationBranch> ();

		@SuppressWarnings ("unchecked")
		HashSet<RelationBranch>[] singleNodeBranchSets = (HashSet<RelationBranch>[]) new HashSet<?>[2];
		singleNodeBranchSets[0] = new HashSet<RelationBranch> ();
		singleNodeBranchSets[1] = new HashSet<RelationBranch> ();
		for (RelationNode node : tree.getNodes ())
		{
			if (node.getRelationBranch ().size () == 1)
				singleNodeBranchSets[0].add (node.getRelationBranch ());
		}

		// first do only safe searches
		int index = 0;
		DiscoveryUtils.debug ("safe searches only");
		DiscoveryUtils.debug ("index: " + index + ": " + branchSets[index].size ());
		while (branchSets[index].size () > 0)
		{
			int nextIndex = 1 - index;
			branchSets[nextIndex].clear ();
			DiscoveryUtils.debug ("index: " + index + ": " + branchSets[index].size ());

			branchSets[index].addAll (singleNodeBranchSets[index]);

			for (DiscoveryAlgorithm algorithm : DiscoveryAlgorithmManager.getInstance ().getList (0))
			{
				algorithm.discover (branchSets[index], branchSets[nextIndex], iteration, log);
			}

			/////////////////////////////////////////////////////////////
			// end of algorithms
			/////////////////////////////////////////////////////////////
			singleNodeBranchSets[nextIndex].clear ();
			for (RelationBranch branch : singleNodeBranchSets[index])
			{
				if (branch.size () == 1)
					singleNodeBranchSets[nextIndex].add (branch);
			}
			index = nextIndex;
			expandSearch(branchSets[index]);

			++iteration;
		}

		// then do both safe and unsafe searches
		index = 0;
		branchSets[0] = tree.getBranchSet ();
		branchSets[1].clear ();
		singleNodeBranchSets[0].clear ();
		singleNodeBranchSets[1].clear ();
		for (RelationNode node : tree.getNodes ())
		{
			if (node.getRelationBranch ().size () == 1)
				singleNodeBranchSets[0].add (node.getRelationBranch ());
		}
		DiscoveryUtils.debug ("safe + unsafe searches");
		DiscoveryUtils.debug ("index: " + index + ": " + branchSets[index].size ());
		while (branchSets[index].size () > 0)
		{
			int nextIndex = 1 - index;
			branchSets[nextIndex].clear ();
			DiscoveryUtils.debug ("index: " + index + ": " + branchSets[index].size ());

			branchSets[index].addAll (singleNodeBranchSets[index]);

			for (int level = 0; level < 2; ++level)
			{
				for (DiscoveryAlgorithm algorithm : DiscoveryAlgorithmManager.getInstance ().getList (level))
				{
					algorithm.discover (branchSets[index], branchSets[nextIndex], iteration, log);
				}
			}


			/////////////////////////////////////////////////////////////
			// end of algorithms
			/////////////////////////////////////////////////////////////
			singleNodeBranchSets[nextIndex].clear ();
			for (RelationBranch branch : singleNodeBranchSets[index])
			{
				if (branch.size () == 1)
					singleNodeBranchSets[nextIndex].add (branch);
			}
			index = nextIndex;
			expandSearch(branchSets[index]);

			++iteration;
		}

		if (Main.configs.debug)
		{
			for (RelationNode node : tree.getNodes ())
			{
				RelationBranch branch = node.getRelationBranch ();
				if (node == branch.getFirst () ||
					node == branch.getLast ())
				{
					if (node.getCommit ().getFullMessage ().startsWith ("Merge"))
					{
						System.out.println ("Investigate: " + CommitUtils.getName (node));
					}
				}
			}
		}
		tree.resetVisit ();
	}

	/**
	 * We need to expand the search since the branchSet only contains the ones
	 * just got modified.  We need to include parents and children the modified
	 * branches.
	 *
	 * @param	branchSet
	 *			the modified branches
	 */
	private static void expandSearch (Set<RelationBranch> branchSet)
	{
		HashSet<RelationBranch> expandSet = new HashSet<RelationBranch> ();

		for (RelationBranch branch: branchSet)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			for (RelationNode parent : firstNode.getParents ())
			{
				expandSet.add (parent.getRelationBranch ());
			}
			RelationNode lastNode = branch.getLast ();
			for (RelationNode child : lastNode.getChildren ())
			{
				expandSet.add (child.getRelationBranch ());
			}
		}

		branchSet.addAll (expandSet);
	}
}
