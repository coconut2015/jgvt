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
 * For this case, the first node of branch A has two parent branches
 * B and C.  C has only one child in A, but B has two children.  B
 * and C share a common parent.
 *
 * Case: React-ec036e
 *
 * @author	Heng Yuan
 */
class DiamondSwapParentsTwoLongChainAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
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

			log.log (firstNode, "DSPTLC", iteration);
			checkBranches.add (branch);
		}
	}
}
