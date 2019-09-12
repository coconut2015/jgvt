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
 * For this case, the last node of branch A has two children B and C.
 * B already has a direct parent in its branch.  C only has one parent.
 *
 * Then we merge A and C.
 *
 * Case: React-4294a7
 *
 * @author	Heng Yuan
 */
class ParallelParentsAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
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

			if (DiscoveryUtils.isFirstInBranch (leftChild) &&
				DiscoveryUtils.isFirstInBranch (rightChild))
			{
				if (rightChild.getParents ().length == 1 &&
					leftChild.getParents ().length == 2 &&
					leftChild.getParents ()[1] == lastNode &&
					DiscoveryUtils.isLastInBranch (leftChild.getParents ()[0]))
				{
					branch.mergeChild (rightChildBranch);

					log.log (lastNode, "PP right", iteration);
					checkBranches.add (branch);
				}
				else if (leftChild.getParents ().length == 1 &&
						 rightChild.getParents ().length == 2 &&
						 rightChild.getParents ()[1] == lastNode &&
						 DiscoveryUtils.isLastInBranch (rightChild.getParents ()[0]))
				{
					branch.mergeChild (leftChildBranch);

					log.log (lastNode, "PP left", iteration);
					checkBranches.add (branch);
				}
			}
		}
	}
}
