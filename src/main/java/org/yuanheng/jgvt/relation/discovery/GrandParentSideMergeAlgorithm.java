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
 * For this case, the first node of a branch A has two parents B (left)
 * and C (right).  B's first node's left parent is branch D.  D has a
 * child that merges to the first node of branch A.
 *
 * In this case, we merge A, B, and D.
 *
 * Case: React-f1fc4b
 *
 * @author	Heng Yuan
 */
class GrandParentSideMergeAlgorithm implements DiscoveryAlgorithm
{
	@Override
	public void discover (Set<RelationBranch> branches, Set<RelationBranch> checkBranches, int iteration, BranchLog log)
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
			if (leftParent != leftParentBranch.getLast ())
				continue;
			RelationNode leftParentFirst = leftParentBranch.getFirst ();
			if (leftParentFirst.getParents ().length > 1 &&
				leftParentFirst.getParents ()[0] == rightParentFirst.getParents ()[0])
			{
				branch.mergeParent (leftParentBranch);

				log.log (firstNode, "GPSM", iteration);
				checkBranches.add (branch);
			}
		}
	}
}
