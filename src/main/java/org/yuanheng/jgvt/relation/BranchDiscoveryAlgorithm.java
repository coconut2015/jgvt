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
	static void debug (String msg)
	{
		if (Main.configs.debug)
		{
			System.out.println (msg);
		}
	}

	/**
	 * Practically any commits with "Merge pull requests" should not be
	 * trusted with the ordering of the parent commits, since they are
	 * usually ordered by time, which is completely useless and redundant.
	 *
	 * On the other hand, if the first parent's timestamp is older than
	 * the second parent's, it is trust worthy.
	 *
	 * @param	node
	 * 			a node to check
	 * @return	true if the node is a merge pull request node.
	 * 			false otherwise.
	 */
	private static boolean isMergePullRequest(RelationNode node)
	{
		return node.getParents ().length == 2 && node.getCommit ().getFullMessage ().startsWith ("Merge pull request");
	}

	/**
	 * Check if a child branch is a side branch of parent.
	 *
	 * Note that in some cases, it may be further necessary to call
	 * {@link #isMiddleInBranch(RelationNode)} to further check if
	 * the childbranch's parent node is in the middle of the parent
	 * branch.
	 *
	 * @param	parent
	 * 			parent branch
	 * @param	child
	 * 			child branch
	 * @return	true if the child is a side branch of the parent.
	 */
	private static boolean isSideBranch (RelationBranch parent, RelationBranch child)
	{
		RelationNode firstNode = child.getFirst ();
		return (firstNode.getParents ().length == 1) &&
			   (firstNode.getParents ()[0].getRelationBranch () == parent);
	}

	private static boolean isFirstInBranch (RelationNode node)
	{
		return node == node.getRelationBranch ().getFirst ();
	}

	private static boolean isLastInBranch (RelationNode node)
	{
		return node == node.getRelationBranch ().getLast ();
	}

	private static boolean isMiddleInBranch (RelationNode node)
	{
		RelationBranch branch = node.getRelationBranch ();
		return node != branch.getFirst () && node != branch.getLast ();
	}

	/**
	 * Check if a node has 1 and only 1 joinable child.
	 *
	 * @param	node
	 * 			node to check
	 * @return	child node if it is only joinable child.
	 * 			null otherwise.
	 */
	private static RelationNode hasSingleJoinChild (RelationNode node)
	{
		if (!isLastInBranch (node))
			return null;

		RelationNode toJoin = null;
		for (RelationNode child : node.getChildren ())
		{
			if (isFirstInBranch (child))
			{
				if (toJoin == null)
					toJoin = child;
				else
					return null;
			}
		}
		return toJoin;
	}

	private static void safeSearches (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		// perform simple branch merging
		branchMergeCaseSingleChild (branches, checkBranches);
		branchMergeCaseSideMergeSingleChild (branches, checkBranches);
		branchMergeCaseSideBranchMergeChild (branches, checkBranches);
		branchMergeCaseParentSideBranchParent (branches, checkBranches);

		// perform slightly more complicated merging
		branchMergeCaseTwoChildren (branches, checkBranches);
		branchMergeCaseMultipleChildren (branches, checkBranches);

		branchMergeCaseRepeatMerge (branches, checkBranches);

		branchMergeCaseMergeParent (branches, checkBranches);
		branchMergeCaseMergePullRequest (branches, checkBranches);

		branchMergeCaseMergeOutMergeIn (branches, checkBranches);
		branchMergeCaseGrandParentSideMerge (branches, checkBranches);
		branchMergeCaseTwoChildrenOneMergeParent (branches, checkBranches);
		branchMergeCaseParallelParents (branches, checkBranches);
	}

	private static void unsafeSearches (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		branchMergeCaseDiamondMergeLeftParent (branches, checkBranches);
		branchMergeCaseSwapParentsLongerChain (branches, checkBranches);
		branchMergeCaseDiamondSwapParentsTwoLongChain (branches, checkBranches);
	}

	/**
	 * Infer branches from the tree nodes, and a list of important branches.
	 *
	 * @param	tree
	 * 			relation tree
	 * @param	startNode
	 * 			the main branch start node.
	 */
	public static void inferBranches (RelationTree tree)
	{
		if (tree.size () == 0)
			return;

		// See if we can trace from the main branch and collect branches.
		discoverInitialBranches (tree.getStartNode (), true);

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
			discoverInitialBranches (node, false);
		}

		// find any remaining branches starting from leaves.
		List <RelationNode> leaves = tree.getLeaves ();
		Collections.sort (leaves, RelationNode.sortByDateComparator);
		for (RelationNode node : leaves)
		{
			if (node.isVisited ())
				continue;
			discoverInitialBranches (node, false);
		}

		mergeBranches (tree);
	}

	/**
	 * Discover the initial branches by having the the start (i.e. last node)
	 * of the main branch.  Once the main branch is set, we can discover
	 * side branches.
	 *
	 * @param	startNode
	 * 			the last node of the main branch
	 */
	private static void discoverInitialBranches (RelationNode startNode, boolean reachRoot)
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
				if (!reachRoot &&
					(parents.length > 1 ||
					 parents[0].getChildren ().length > 1))
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
			RelationBranch branch = node.getRelationBranch ();
			if (node.isVisited ())
				continue;

			if (branch == null)
			{
				branch = new RelationBranch (node);
			}
			node.visit ();

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

	public static void mergeBranches (RelationTree tree)
	{
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
		debug ("safe searches only");
		debug ("index: " + index + ": " + branchSets[index].size ());
		while (branchSets[index].size () > 0)
		{
			int nextIndex = 1 - index;
			branchSets[nextIndex].clear ();
			debug ("index: " + index + ": " + branchSets[index].size ());

			branchSets[index].addAll (singleNodeBranchSets[index]);

			safeSearches (branchSets[index], branchSets[nextIndex]);

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
		debug ("safe + unsafe searches");
		debug ("index: " + index + ": " + branchSets[index].size ());
		while (branchSets[index].size () > 0)
		{
			int nextIndex = 1 - index;
			branchSets[nextIndex].clear ();
			debug ("index: " + index + ": " + branchSets[index].size ());

			branchSets[index].addAll (singleNodeBranchSets[index]);

			safeSearches (branchSets[index], branchSets[nextIndex]);
			unsafeSearches (branchSets[index], branchSets[nextIndex]);

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
	 * A simple merge of two branches if the parent branch only has 1 child,
	 * which happens to be the child branch.
	 */
	private static void branchMergeCaseSingleChild (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length == 1)
			{
				RelationNode parent = firstNode.getParents ()[0];
				if (parent.getRelationBranch () != branch &&
					parent.getChildren ().length == 1)
				{
					// this node is the only child of the parent node,
					// and the parent is in a different branch.
					// merge the two branches.
					branch.mergeParent (parent.getRelationBranch ());

					debug (CommitUtils.getName (firstNode) + " 1CM");
					checkBranches.add (branch);
				}
			}
		}
	}

	/**
	 * For this case, the first node of branch A has two parents B and C.
	 * Branch A also merges to branch B.  C only has 1 child that is A.
	 *
	 * Then we merge A and C.
	 */
	private static void branchMergeCaseSideMergeSingleChild (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length == 2)
			{
				RelationNode leftParent = firstNode.getParents ()[0];
				RelationNode rightParent = firstNode.getParents ()[1];
				if (leftParent.getChildren ().length == 1 &&
					isLastInBranch (leftParent) &&
					!isLastInBranch (rightParent))
				{
					branch.mergeParent (leftParent.getRelationBranch ());

					debug (CommitUtils.getName (firstNode) + " SM1C right");
					checkBranches.add (branch);
				}
				else if (rightParent.getChildren ().length == 1 &&
						 isLastInBranch (rightParent) &&
						 !isLastInBranch (leftParent))
				{
					branch.mergeParent (rightParent.getRelationBranch ());

					debug (CommitUtils.getName (firstNode) + " SM1C left");
					checkBranches.add (branch);
				}
			}
		}
	}

	/**
	 * For this case, the first node of branch A has two parents B and C.
	 * Branch C is a side branch of B, and B has only 1 child that is A.
	 *
	 * Then we merge A and B.
	 *
	 * Case: React-6a0976 SBMC left
	 * Case: React-8d3465 SBMC right
	 */
	private static void branchMergeCaseSideBranchMergeChild (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length == 2)
			{
				RelationNode leftParent = firstNode.getParents ()[0];
				RelationBranch leftParentBranch = leftParent.getRelationBranch ();
				RelationNode rightParent = firstNode.getParents ()[1];
				RelationBranch rightParentBranch = rightParent.getRelationBranch ();

				if (leftParent.getChildren ().length == 1 &&
					isLastInBranch (leftParent) &&
					rightParent.getChildren ().length == 1 &&
					isLastInBranch (rightParent))
				{
					if (isSideBranch (leftParentBranch, rightParentBranch))
					{
						if (isMiddleInBranch (rightParentBranch.getFirst ().getParents ()[0]))
						{
							branch.mergeParent (leftParentBranch);

							debug (CommitUtils.getName (firstNode) + " SBMC left");
							checkBranches.add (branch);
						}
					}
					else if (isSideBranch (rightParentBranch, leftParentBranch))
					{
						if (isMiddleInBranch (leftParentBranch.getFirst ().getParents ()[0]))
						{
							branch.mergeParent (rightParentBranch);

							debug (CommitUtils.getName (firstNode) + " SBMC right");
							checkBranches.add (branch);
						}
					}
				}
			}
		}
	}

	/**
	 * For this case, the last node of branch A has two children B and C.
	 * B already has a direct parent in its branch.
	 *
	 * Then we merge A and C.
	 *
	 * Case: React-af47c3
	 */
	private static void branchMergeCaseTwoChildrenOneMergeParent (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();
			if (lastNode.getChildren ().length != 2)
				continue;

			RelationNode leftChild = lastNode.getChildren ()[0];
			RelationNode rightChild = lastNode.getChildren ()[1];
			RelationBranch leftChildBranch = leftChild.getRelationBranch ();
			RelationBranch rightChildBranch = rightChild.getRelationBranch ();

			if (leftChild.getParents ().length == 1 &&
				isFirstInBranch (leftChild) &&
				!isFirstInBranch (rightChild))
			{
				branch.mergeChild (leftChildBranch);

				debug (CommitUtils.getName (lastNode) + " TCOMP left");
				checkBranches.add (branch);
			}
			else if (rightChild.getParents ().length == 1 &&
					 isFirstInBranch (rightChild) &&
					 !isFirstInBranch (leftChild))
			{
				branch.mergeChild (rightChildBranch);

				debug (CommitUtils.getName (lastNode) + " TCOMP right");
				checkBranches.add (branch);
			}
		}
	}

	/**
	 * For this case, the first node of branch A has two parent nodes B and C.
	 * B already has a child in its branch, but C only has a single child in A.
	 *
	 * Case 1: React-33d439 PSBP left (left parent has only 1 child)
	 * Case 2: React-0c8934 PSBP left (left parent has only 1 joinable child)
	 */
	private static void branchMergeCaseParentSideBranchParent (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 2)
				continue;

			RelationNode leftParent = firstNode.getParents ()[0];
			RelationNode rightParent = firstNode.getParents ()[1];
			RelationBranch leftParentBranch = leftParent.getRelationBranch ();
			RelationBranch rightParentBranch = rightParent.getRelationBranch ();

			if (!isLastInBranch (rightParent))
			{
				if (leftParent.getChildren ().length == 1 ||
					hasSingleJoinChild (leftParent) == firstNode)
				{
					branch.mergeParent (leftParentBranch);

					debug (CommitUtils.getName (firstNode) + " PSBP left");
					checkBranches.add (branch);
				}
			}
			else if (!isLastInBranch (leftParent))
			{
				if (rightParent.getChildren ().length == 1 ||
					hasSingleJoinChild (rightParent) == firstNode)
				{
					branch.mergeParent (rightParentBranch);

					debug (CommitUtils.getName (firstNode) + " PSBP right");
					checkBranches.add (branch);
				}
			}
		}
	}

	/**
	 * For this case, the last node of branch A has two children B and C.
	 * B already has a direct parent in its branch.  C only has one parent.
	 *
	 * Then we merge A and C.
	 *
	 * Case: React-4294a7
	 */
	private static void branchMergeCaseParallelParents (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();
			if (lastNode.getChildren ().length != 2)
				continue;

			RelationNode leftChild = lastNode.getChildren ()[0];
			RelationNode rightChild = lastNode.getChildren ()[1];

			RelationBranch leftChildBranch = leftChild.getRelationBranch ();
			RelationBranch rightChildBranch = rightChild.getRelationBranch ();

			if (isFirstInBranch (leftChild) &&
				isFirstInBranch (rightChild))
			{
				if (rightChild.getParents ().length == 1 &&
					leftChild.getParents ().length == 2 &&
					leftChild.getParents ()[1] == lastNode &&
					isLastInBranch (leftChild.getParents ()[0]))
				{
					branch.mergeChild (rightChildBranch);

					debug (CommitUtils.getName (lastNode) + " PP right");
					checkBranches.add (branch);
				}
				else if (leftChild.getParents ().length == 1 &&
						 rightChild.getParents ().length == 2 &&
						 rightChild.getParents ()[1] == lastNode &&
						 isLastInBranch (rightChild.getParents ()[0]))
				{
					branch.mergeChild (leftChildBranch);

					debug (CommitUtils.getName (lastNode) + " PP left");
					checkBranches.add (branch);
				}
			}
		}
	}

	/**
	 * For this case, we have two child branches B and C for a branch A.
	 * However, child branch B eventually merges to child branch C.  In
	 * this case, we merge A and C.
	 */
	private static void branchMergeCaseTwoChildren (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();

			if (lastNode.getChildren ().length == 2)
			{
				RelationBranch leftChildBranch = lastNode.getChildren ()[0].getRelationBranch ();
				RelationBranch rightChildBranch = lastNode.getChildren ()[1].getRelationBranch ();

				if (branch == leftChildBranch ||
					branch == rightChildBranch ||
					leftChildBranch == rightChildBranch)
					continue;

				RelationNode leftNode = lastNode.getChildren ()[0];
				// make sure the left child is the first node of the child branch
				if (!isFirstInBranch (leftNode))
					continue;

				RelationNode rightNode = lastNode.getChildren ()[1];
				// make sure the right child is the first node of the child branch
				if (!isFirstInBranch(rightNode))
					continue;

				// now check if leftBranch merges to rightBranch or
				// vice versa
				RelationNode leftLast = leftChildBranch.getLast ();
				if (leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightChildBranch)
				{
					branch.mergeChild (rightChildBranch);

					debug (CommitUtils.getName (lastNode) + " 2CM right");
					checkBranches.add (branch);
					continue;
				}

				RelationNode rightLast = rightChildBranch.getLast ();
				if (rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftChildBranch)
				{
					branch.mergeChild (leftChildBranch);

					debug (CommitUtils.getName (lastNode) + " 2CM left");
					checkBranches.add (branch);
					continue;
				}
			}
		}
	}

	/**
	 * When we have multiple children (greater than 2).  We check if 50%
	 * (not counting the child branch to be merged) of child branches
	 * merge to a particular branch.  That popular child branch is merged
	 * with parent.
	 */
	private static void branchMergeCaseMultipleChildren (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		HashMap<RelationBranch, Integer> childBranchMap = new HashMap<RelationBranch, Integer> ();
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();

			if (lastNode.getChildren ().length > 2)
			{
				childBranchMap.clear ();
				for (RelationNode child : lastNode.getChildren ())
				{
					// Only consider all first in branch cases.
					// Otherwise, sometimes things can get screwed up.
					if (!isFirstInBranch (child))
						break;
					childBranchMap.put (child.getRelationBranch (), 0);
				}
				if (childBranchMap.size () != lastNode.getChildren ().length)
				{
					childBranchMap.clear ();
					continue;
				}
				int halfCount = lastNode.getChildren ().length / 2;
DoneSearch:
				for (RelationBranch childBranch : childBranchMap.keySet ())
				{
					RelationNode childLastNode = childBranch.getLast ();
					if (childLastNode.getChildren ().length == 0)
					{
						continue;
					}
					if (childLastNode.getChildren ().length == 1)
					{
						RelationBranch toMergeChildBranch = childLastNode.getChildren ()[0].getRelationBranch ();
						Integer value = childBranchMap.get (toMergeChildBranch);
						if (value == null)
						{
							continue;
						}
						int count = value + 1;
						if (count >= halfCount)
						{
							// found it.
							branch.mergeChild (toMergeChildBranch);
							debug (CommitUtils.getName (lastNode) + " MC");
							checkBranches.add (branch);
							break DoneSearch;
						}
						childBranchMap.put (toMergeChildBranch, count);
					}
				}
				childBranchMap.clear ();
			}
		}
	}

	/**
	 * For this case, we have a branch A which has one child merge to
	 * branch B, and its other child in branch C also merge to branch B.
	 * In this case, consider merging A and C.
	 *
	 * This case is different from branchMergeCaseTwoChildren in that
	 * branch B could be the parent of A.
	 */
	private static void branchMergeCaseRepeatMerge (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();

			if (lastNode.getChildren ().length == 2)
			{
				RelationBranch leftChildBranch = lastNode.getChildren ()[0].getRelationBranch ();
				RelationBranch rightChildBranch = lastNode.getChildren ()[1].getRelationBranch ();

				if (branch == leftChildBranch ||
					branch == rightChildBranch ||
					leftChildBranch == rightChildBranch)
					continue;

				RelationNode leftNode = lastNode.getChildren ()[0];
				RelationNode rightNode = lastNode.getChildren ()[1];

				// now check if leftBranch merges to rightBranch or
				// vice versa
				RelationNode leftLast = leftChildBranch.getLast ();
				if (isFirstInBranch (leftNode) &&
					leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightChildBranch &&
					!isFirstInBranch (rightNode))
				{
					branch.mergeChild (leftChildBranch);

					debug (CommitUtils.getName (lastNode) + " RM left");
					checkBranches.add (branch);
					continue;
				}

				RelationNode rightLast = rightChildBranch.getLast ();
				if (isFirstInBranch (rightNode) &&
					rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftChildBranch &&
					!isFirstInBranch (leftNode))
				{
					branch.mergeChild (rightChildBranch);

					debug (CommitUtils.getName (lastNode) + " RM right");
					checkBranches.add (branch);
					continue;
				}
			}
		}
	}

	/**
	 * For this case, main branch A has a side branch B.  Then A and B
	 * are merged to create branch C.  Eventually, C merges back to A.
	 * This is an issue usually because C's parents A and B are swapped
	 * (i.e. index 0 is A, and index 1 is B).
	 *
	 * In this case, consider merging B and C.
	 */
	private static void branchMergeCaseMergeParent (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			RelationNode lastNode = branch.getLast ();

			if (firstNode.getParents ().length == 2 &&
				lastNode.getChildren ().length == 1)
			{
				RelationNode leftParent = firstNode.getParents ()[0];
				RelationBranch leftParentBranch = leftParent.getRelationBranch ();
				RelationNode rightParent = firstNode.getParents ()[1];
				RelationBranch rightParentBranch = rightParent.getRelationBranch ();

				if (isLastInBranch (rightParent))
				{
					if (isSideBranch (leftParentBranch, rightParentBranch) &&
						isMergeTo (branch, leftParentBranch))
					{
						branch.mergeParent (rightParentBranch);

						debug (CommitUtils.getName (firstNode) + " MP right");
						checkBranches.add (branch);
					}
				}
				else if (isLastInBranch (leftParent))
				{
					if (isSideBranch(rightParentBranch, leftParentBranch) &&
						isMergeTo (branch, rightParentBranch))
					{
						branch.mergeParent (leftParentBranch);

						debug (CommitUtils.getName (firstNode) + " MP left");
						checkBranches.add (branch);
					}
				}
			}
		}
	}

	/**
	 * For this case, the first node of branch A has two parent branches B
	 * and C.  Both B and C ends right there.  C is a side branch of B.
	 *
	 * Then we merge A and B.
	 */
	private static void branchMergeCaseMergePullRequest (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length == 2)
			{
				RelationNode leftParent = firstNode.getParents ()[0];
				RelationNode rightParent = firstNode.getParents ()[1];

				if (isLastInBranch (leftParent) &&
					isLastInBranch (rightParent))
				{
					RelationBranch leftParentBranch = leftParent.getRelationBranch ();
					RelationBranch rightParentBranch = rightParent.getRelationBranch ();

					if (isSideBranch (rightParentBranch, leftParentBranch))
					{
						branch.mergeParent (rightParentBranch);

						debug (CommitUtils.getName (firstNode) + " MPR right");
						checkBranches.add (branch);
					}
					else if (isSideBranch(leftParentBranch, rightParentBranch))
					{
						branch.mergeParent (leftParentBranch);

						debug (CommitUtils.getName (firstNode) + " MPR left");
						checkBranches.add (branch);
					}
				}
			}
		}
	}

	/**
	 * For this case, if branch A firstNode's parent 0 is the last
	 * node of branch B.  Then parent 0 has a merge arrow to branch C, and
	 * C merges to branch A.
	 *
	 * In this case, we merge A and B.
	 */
	private static void branchMergeCaseMergeOutMergeIn (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 2)
				continue;

			RelationNode leftParent = firstNode.getParents ()[0];
			RelationNode rightParent = firstNode.getParents ()[1];
			RelationBranch leftParentBranch = leftParent.getRelationBranch ();
			RelationBranch rightParentBranch = rightParent.getRelationBranch ();

			if (branch == leftParentBranch ||
				branch == rightParentBranch ||
				leftParentBranch == rightParentBranch)
				continue;

			if (!isLastInBranch (leftParent))
				continue;

			boolean hasMergeToRight = false;
			for (RelationNode child : leftParent.getChildren ())
			{
				if (child.getRelationBranch () == rightParentBranch)
				{
					hasMergeToRight = true;
					break;
				}
			}
			if (hasMergeToRight)
			{
				branch.mergeParent (leftParentBranch);

				debug (CommitUtils.getName (firstNode) + " MOMI");
				checkBranches.add (branch);
			}
		}
	}

	/**
	 * For this case, the first node of a branch A has two parents B (left)
	 * and C (right).  B's first node's left parent is branch D.  D has a
	 * child that merges to the first node of branch A.
	 *
	 * In this case, we merge A, B, and D.
	 *
	 * Case: React-f1fc4b
	 */
	private static void branchMergeCaseGrandParentSideMerge (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 2)
				continue;

			RelationNode rightParent = firstNode.getParents ()[1];
			if (rightParent.getChildren ().length != 1)
				continue;
			RelationBranch rightParentBranch = rightParent.getRelationBranch ();
			RelationNode rightParentFirst = rightParentBranch.getFirst ();
			if (rightParentFirst.getParents ().length != 1)
				continue;

			RelationNode leftParent = firstNode.getParents ()[0];
			RelationBranch leftParentBranch = leftParent.getRelationBranch ();
			RelationNode leftParentFirst = leftParentBranch.getFirst ();
			if (leftParentFirst.getParents ().length > 1 &&
				leftParentFirst.getParents ()[0] == rightParentFirst.getParents ()[0])
			{
				branch.mergeParent (leftParentBranch);

				debug (CommitUtils.getName (firstNode) + " GPSM");
				checkBranches.add (branch);
			}
		}
	}

	private static boolean isMergeTo (RelationBranch branchFrom, RelationBranch branchTo)
	{
		RelationNode lastNode = branchFrom.getLast ();
		return lastNode.getChildren ().length == 1 &&
			   lastNode.getChildren ()[0].getRelationBranch () == branchTo;
	}

	////////////////////////////////////////////////////////////////////////////////
	//
	// Unsafe algorithms
	//
	////////////////////////////////////////////////////////////////////////////////

	/**
	 * For this case, branch A and branch B shares a common parent C
	 * and common child D.  Thus forming a diamond shape.  In this
	 * case, we would let child D merge with parent 0 branch (left)
	 *
	 * In this case, we merge A and B.
	 *
	 * Case: React-f0a4b2
	 */
	private static void branchMergeCaseDiamondMergeLeftParent (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 2)
				continue;
			RelationNode leftParent = firstNode.getParents ()[0];
			RelationNode rightParent = firstNode.getParents ()[1];
			if (leftParent.getChildren ().length == 1 &&
				rightParent.getChildren ().length == 1)
			{
				RelationNode leftFirst = leftParent.getRelationBranch ().getFirst ();
				if (leftFirst.getParents ().length != 1)
					continue;
				RelationNode rightFirst = leftParent.getRelationBranch ().getFirst ();
				if (rightFirst.getParents ().length != 1)
					continue;
				if (leftFirst.getParents ()[0] == rightFirst.getParents ()[0])
				{
					branch.mergeParent (leftParent.getRelationBranch ());

					debug (CommitUtils.getName (firstNode) + " DMLP");
					checkBranches.add (branch);
				}
			}
		}
	}

	/**
	 * For this case, branch A's first node is the 2nd to the last of branch B.
	 * branch A's second node's second parent is the last node of the branch B.
	 * In this case, branch A's second node's parents should be swapped such
	 * that we get a much longer branch B.
	 *
	 * Case: React-0db777
	 */
	private static void branchMergeCaseSwapParentsLongerChain (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			// if the chain is smaller than 3, not worth doing it.
			if (branch.size () < 3)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 1)
				continue;
			RelationNode secondNode = branch.getOrderedList ().get (1);
			if (secondNode.getParents ().length != 2)
				continue;

			RelationBranch parentBranch = firstNode.getParents ()[0].getRelationBranch ();
			if (secondNode.getParents ()[1].getRelationBranch () != parentBranch)
				continue;
			if (parentBranch.getLast () != secondNode.getParents ()[1])
				continue;

			// the first node is in a branch by itself
			new RelationBranch (firstNode);

			branch.mergeParent (parentBranch);
			debug (CommitUtils.getName (firstNode) + " SPLC");

			checkBranches.add (branch);
		}
	}

	/**
	 * For this case, the first node of branch A has two parent branches
	 * B and C.  C has only one child in A, but B has two children.  B
	 * and C share a common parent.
	 *
	 * Case: React-ec036e
	 */
	private static void branchMergeCaseDiamondSwapParentsTwoLongChain (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		for (RelationBranch branch : branches)
		{
			// if the chain is smaller than 3, not worth doing it.
			if (branch.size () == 0)
				continue;

			RelationNode firstNode = branch.getFirst ();
			if (firstNode.getParents ().length != 2)
				continue;

			RelationNode leftParent = firstNode.getParents ()[0];
			RelationBranch leftParentBranch = leftParent.getRelationBranch ();
			RelationNode rightParent = firstNode.getParents ()[1];
			RelationBranch rightParentBranch = rightParent.getRelationBranch ();

			if (leftParent != leftParentBranch.getLast () ||
				rightParent != rightParentBranch.getLast () ||
				leftParent.getChildren ().length != 2 ||
				rightParent.getChildren ().length != 1)
				continue;

			RelationNode leftParentFirst = leftParentBranch.getFirst ();
			RelationNode rightParentFirst = rightParentBranch.getFirst ();

			if (leftParentFirst.getParents ().length != 1 ||
				rightParentFirst.getParents ().length != 1 ||
				leftParentFirst.getParents ()[0] != rightParentFirst.getParents ()[0])
				continue;

			branch.mergeParent (rightParentBranch);

			debug (CommitUtils.getName (firstNode) + " DSPTLC");

			checkBranches.add (branch);
		}
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
