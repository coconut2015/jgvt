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
package org.yuanheng.jgvt.relation.discovery;

import org.yuanheng.jgvt.relation.RelationBranch;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * @author	Heng Yuan
 */
public class DiscoveryUtils
{
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
	public static boolean isMergePullRequest(RelationNode node)
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
	public static boolean isSideBranch (RelationBranch parent, RelationBranch child)
	{
		RelationNode firstNode = child.getFirst ();
		return (firstNode.getParents ().length == 1) &&
			   (firstNode.getParents ()[0].getRelationBranch () == parent);
	}

	public static boolean isFirstInBranch (RelationNode node)
	{
		return node == node.getRelationBranch ().getFirst ();
	}

	public static boolean isLastInBranch (RelationNode node)
	{
		return node == node.getRelationBranch ().getLast ();
	}

	public static boolean isMiddleInBranch (RelationNode node)
	{
		RelationBranch branch = node.getRelationBranch ();
		return node != branch.getFirst () && node != branch.getLast ();
	}

	public static boolean isMergeTo (RelationBranch branchFrom, RelationBranch branchTo)
	{
		RelationNode lastNode = branchFrom.getLast ();
		return lastNode.getChildren ().length == 1 &&
			   lastNode.getChildren ()[0].getRelationBranch () == branchTo;
	}

	/**
	 * Check if a node has 1 and only 1 joinable child.
	 *
	 * @param	node
	 * 			node to check
	 * @return	child node if it is only joinable child.
	 * 			null otherwise.
	 */
	public static RelationNode hasSingleJoinChild (RelationNode node)
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
}
