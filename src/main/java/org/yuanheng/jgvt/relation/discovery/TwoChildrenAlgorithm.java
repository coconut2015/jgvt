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
 * For this case, we have two child branches B and C for a branch A.
 * However, child branch B eventually merges to child branch C.  In
 * this case, we merge A and C.
 *
 * @author	Heng Yuan
 */
class TwoChildrenAlgorithm implements DiscoveryAlgorithm
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

				RelationNode leftNode = lastNode.getChildren ()[0];
				// make sure the left child is the first node of the child branch
				if (!DiscoveryUtils.isFirstInBranch (leftNode))
					continue;

				RelationNode rightNode = lastNode.getChildren ()[1];
				// make sure the right child is the first node of the child branch
				if (!DiscoveryUtils.isFirstInBranch(rightNode))
					continue;

				// now check if leftBranch merges to rightBranch or
				// vice versa
				RelationNode leftLast = leftChildBranch.getLast ();
				if (leftLast.getChildren ().length == 1 &&
					leftLast.getChildren ()[0].getRelationBranch () == rightChildBranch)
				{
					branch.mergeChild (rightChildBranch);

					log.log (lastNode, "2CM right", iteration);
					checkBranches.add (branch);
					continue;
				}

				RelationNode rightLast = rightChildBranch.getLast ();
				if (rightLast.getChildren ().length == 1 &&
					rightLast.getChildren ()[0].getRelationBranch () == leftChildBranch)
				{
					branch.mergeChild (leftChildBranch);

					log.log (lastNode, "2CM left", iteration);
					checkBranches.add (branch);
					continue;
				}
			}
		}
	}
}
