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
 * For this case, the first node of branch A has two parents B and C.
 * Branch A also merges to branch B.  C only has 1 child that is A.
 *
 * Then we merge A and C.
 *
 * @author	Heng Yuan
 */
class SideMergeSingleChildAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
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
					DiscoveryUtils.isLastInBranch (leftParent) &&
					!DiscoveryUtils.isLastInBranch (rightParent))
				{
					branch.mergeParent (leftParent.getRelationBranch ());

					log.log (firstNode, "SM1C right", iteration);
					checkBranches.add (branch);
				}
				else if (rightParent.getChildren ().length == 1 &&
						DiscoveryUtils.isLastInBranch (rightParent) &&
						 !DiscoveryUtils.isLastInBranch (leftParent))
				{
					branch.mergeParent (rightParent.getRelationBranch ());

					log.log (firstNode, "SM1C left", iteration);
					checkBranches.add (branch);
				}
			}
		}
	}
}
