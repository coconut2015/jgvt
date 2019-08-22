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

import org.eclipse.jgit.api.errors.GitAPIException;
import org.yuanheng.jgvt.CommitUtils;

/**
 * @author	Heng Yuan
 */
class BranchDiscoveryAlgorithm
{
	static void debug (String msg)
	{
//		System.out.println (msg);
	}

	/**
	 * Infer branches from the tree nodes, and a list of important branches.
	 *
	 * @param	tree
	 * 			relation tree
	 * @param	startNode
	 * 			the main branch start node.
	 * @throws	GitAPIException
	 * 			in case of git error
	 */
	public static void inferBranches (RelationTree tree, RelationNode startNode) throws GitAPIException
	{
		// See if we can trace from the main branch and collect branches.
		discoverInitialBranches (startNode, true);
	
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
		Collections.sort (multiParentNodes, RelationTree.s_sortByDate);
		for (RelationNode node : multiParentNodes)
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			discoverInitialBranches (node, false);
		}
	
		// find any remaining branches starting from leaves.
		List <RelationNode> leaves = tree.getLeaves ();
		Collections.sort (leaves, RelationTree.s_sortByDate);
		for (RelationNode node : leaves)
		{
			if (node.getLayoutInfo ().isVisited ())
				continue;
			discoverInitialBranches (node, false);
		}
	
		@SuppressWarnings ("unchecked")
		HashSet<RelationBranch>[] branchSets = (HashSet<RelationBranch>[]) new HashSet<?>[2]; 
		branchSets[0] = tree.getBranchSet ();
		branchSets[1] = new HashSet<RelationBranch> ();
	
		int index = 0;
		debug ("index: " + index + ": " + branchSets[index].size ());
		while (branchSets[index].size () > 0)
		{
			int nextIndex = 1 - index;
			branchSets[nextIndex].clear ();
			debug ("index: " + index + ": " + branchSets[index].size ());
	
			// perform simple branch merging
			branchMergeCaseSingleChild (branchSets[index], branchSets[nextIndex]);
	
			// perform slightly more complicated merging
			branchMergeCaseTwoChildren (branchSets[index], branchSets[nextIndex]);
			branchMergeCaseMultipleChildren (branchSets[index], branchSets[nextIndex]);
	
			branchMergeCaseRepeatMerge (branchSets[index], branchSets[nextIndex]);
	
			branchMergeCaseSwapMergeParents (branchSets[index], branchSets[nextIndex]);
	
			branchMergeCaseMergeOutMergeIn (branchSets[index], branchSets[nextIndex]);
	
			index = nextIndex;
			expandSearch(branchSets[index]);
		}
	
		tree.resetVisit ();
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
			if (firstNode.getParents ().length > 0)
			{
				RelationNode parent = firstNode.getParents ()[0];
				if (parent.getRelationBranch () != branch &&
					parent.getChildren ().length == 1)
				{
					// this node is the only child of the parent node,
					// and the parent is in a different branch.
					// merge the two branches.
					branch.merge (parent.getRelationBranch ());
	
					debug (CommitUtils.getName (firstNode.getCommit ()) + " 1CM");
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
				RelationBranch leftBranch = lastNode.getChildren ()[0].getRelationBranch ();
				RelationBranch rightBranch = lastNode.getChildren ()[1].getRelationBranch ();
	
				if (branch == leftBranch ||
					branch == rightBranch ||
					leftBranch == rightBranch)
					continue;
	
				RelationNode leftNode = lastNode.getChildren ()[0];
				// make sure the left child is the first node of the child branch
				if (leftNode != leftBranch.getFirst ())
					continue;
	
				RelationNode rightNode = lastNode.getChildren ()[1];
				// make sure the right child is the first node of the child branch
				if (rightNode != rightBranch.getFirst ())
					continue;
	
				// now check if leftBranch merges to rightBranch or
				// vice versa
				RelationNode leftLast = leftBranch.getLast ();
				if (leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightBranch)
				{
					branch.merge (rightBranch);
	
					debug (CommitUtils.getName (lastNode.getCommit ()) + " 2CM right");
					checkBranches.add (branch);
					continue;
				}
	
				RelationNode rightLast = rightBranch.getLast ();
				if (rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftBranch)
				{
					branch.merge (leftBranch);
	
					debug (CommitUtils.getName (lastNode.getCommit ()) + " 2CM left");
					checkBranches.add (branch);
					continue;
				}
			}
		}
	}

	/**
	 * This is similar to branchMergeCaseTwoChildren, but with more than 2 children.
	 * Separate them to optimize the cases a little.
	 */
	private static void branchMergeCaseMultipleChildren (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
	{
		HashSet<RelationBranch> childBranchSet = new HashSet<RelationBranch> ();
		for (RelationBranch branch : branches)
		{
			if (branch.size () == 0)
				continue;

			RelationNode lastNode = branch.getLast ();

			if (lastNode.getChildren ().length > 2)
			{
				childBranchSet.clear ();
				for (RelationNode child : lastNode.getChildren ())
				{
					RelationBranch childBranch = child.getRelationBranch ();
					if (child != childBranch.getFirst ())
						break;
					childBranchSet.add (child.getRelationBranch ());
				}
				if (childBranchSet.size () != lastNode.getChildren ().length)
				{
					childBranchSet.clear ();
					continue;
				}
				boolean mergeToEachOther = true;
				RelationBranch toMerge = null;
				for (RelationBranch childBranch : childBranchSet)
				{
					RelationNode childLastNode = childBranch.getLast ();
					if (childLastNode.getChildren ().length == 0)
					{
						continue;
					}
					if (childLastNode.getChildren ().length > 1)
					{
						if (toMerge == null)
						{
							toMerge = childBranch;
						}
						else if (toMerge != childBranch)
						{
							mergeToEachOther = false;
						}
						continue;
					}
					RelationBranch mergeToBranch = childLastNode.getChildren ()[0].getRelationBranch ();
					if (!childBranchSet.contains (mergeToBranch))
					{
						if (toMerge == null)
						{
							toMerge = childBranch;
						}
						else if (toMerge != childBranch)
						{
							mergeToEachOther = false;
							break;
						}
						continue;
					}
					if (toMerge == null)
					{
						toMerge = mergeToBranch;
					}
					else if (toMerge != mergeToBranch)
					{
						mergeToEachOther = false;
						break;
					}
				}
				if (!mergeToEachOther || toMerge == null)
				{
					childBranchSet.clear ();
					continue;
				}
				branch.merge (toMerge);
				debug (CommitUtils.getName (lastNode.getCommit ()) + " MC");
				checkBranches.add (branch);
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
				RelationBranch leftBranch = lastNode.getChildren ()[0].getRelationBranch ();
				RelationBranch rightBranch = lastNode.getChildren ()[1].getRelationBranch ();
	
				if (branch == leftBranch ||
					branch == rightBranch ||
					leftBranch == rightBranch)
					continue;
	
				RelationNode leftNode = lastNode.getChildren ()[0];
				RelationNode rightNode = lastNode.getChildren ()[1];
	
				// now check if leftBranch merges to rightBranch or
				// vice versa
				RelationNode leftLast = leftBranch.getLast ();
				if (leftNode == leftBranch.getFirst () &&
					leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightBranch &&
					rightNode != rightBranch.getFirst ())
				{
					branch.merge (leftBranch);
	
					debug (CommitUtils.getName (lastNode.getCommit ()) + " RM left");
					checkBranches.add (branch);
					continue;
				}
	
				RelationNode rightLast = rightBranch.getLast ();
				if (rightNode == rightBranch.getFirst () &&
					rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftBranch &&
					leftNode != leftBranch.getFirst ())
				{
					branch.merge (rightBranch);
	
					debug (CommitUtils.getName (lastNode.getCommit ()) + " RM right");
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
	private static void branchMergeCaseSwapMergeParents (Set<RelationBranch> branches, Set<RelationBranch> checkBranches)
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
	
				if (branch == leftParentBranch ||
					branch == rightParentBranch ||
					leftParentBranch == rightParentBranch)
					continue;
	
				if (rightParent != rightParentBranch.getLast ())
					continue;
	
				RelationNode rightParentBranchFirstNode = rightParentBranch.getFirst ();
				if (rightParentBranchFirstNode.getParents ().length == 1 &&
					rightParentBranchFirstNode.getParents ()[0].getRelationBranch () == leftParentBranch &&
					lastNode.getChildren ()[0].getRelationBranch () == leftParentBranch)
				{
					firstNode.getParents ()[0] = rightParent;
					firstNode.getParents ()[1] = leftParent;
					branch.merge (rightParentBranch);
	
					debug (CommitUtils.getName (firstNode.getCommit ()) + " SMP");
					checkBranches.add (branch);
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
	
			if (leftParent != leftParentBranch.getLast ())
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
				branch.merge (leftParentBranch);
	
				debug (CommitUtils.getName (firstNode.getCommit ()) + " MOMI");
				checkBranches.add (branch);
			}
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
