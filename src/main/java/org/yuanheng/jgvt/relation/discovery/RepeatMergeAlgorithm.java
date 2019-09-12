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

import java.util.Set;

import org.yuanheng.jgvt.relation.BranchLog;
import org.yuanheng.jgvt.relation.RelationBranch;
import org.yuanheng.jgvt.relation.RelationNode;

/**
 * For this case, we have a branch A which has one child merge to
 * branch B, and its other child in branch C also merge to branch B.
 * In this case, consider merging A and C.
 *
 * This case is different from branchMergeCaseTwoChildren in that
 * branch B could be the parent of A.
 *
 * @author	Heng Yuan
 */
class RepeatMergeAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
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
				if (DiscoveryUtils.isFirstInBranch (leftNode) &&
					leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightChildBranch &&
					!DiscoveryUtils.isFirstInBranch (rightNode))
				{
					branch.mergeChild (leftChildBranch);

					log.log (lastNode, "RM left", iteration);
					checkBranches.add (branch);
					continue;
				}

				RelationNode rightLast = rightChildBranch.getLast ();
				if (DiscoveryUtils.isFirstInBranch (rightNode) &&
					rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftChildBranch &&
					!DiscoveryUtils.isFirstInBranch (leftNode))
				{
					branch.mergeChild (rightChildBranch);

					log.log (lastNode, "RM right", iteration);
					checkBranches.add (branch);
					continue;
				}
			}
		}
	}
}
