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
 * For this case, branch A and branch B shares a common parent C
 * and common child D.  Thus forming a diamond shape.  In this
 * case, we would let child D merge with parent 0 branch (left)
 *
 * In this case, we merge A and B.
 *
 * Case: React-f0a4b2
 *
 * @author	Heng Yuan
 */
class DiamondMergeLeftParentAlgorithm implements DiscoveryAlgorithm
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

					log.log (firstNode, "DMLP", iteration);
					checkBranches.add (branch);
				}
			}
		}
	}
}
