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
package org.yuanheng.jgvt;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.revwalk.RevCommit;

/**
 * @author	Heng Yuan
 */
class RelationTreeFactory
{
	private final GitRepo m_gitRepo;

	RelationTreeFactory (GitRepo gitRepo)
	{
		m_gitRepo = gitRepo;
	}

	public RelationTree createTree (Iterable<RevCommit> commitLogs) throws GitAPIException, MissingObjectException, IncorrectObjectTypeException, IOException
	{
		RelationTree tree = new RelationTree ();

		// First pass to construct the node graph to construct basic node
		// relationship.
		for (RevCommit commit : commitLogs)
		{
			RelationNode node = tree.getCommitNode (commit, m_gitRepo);

			for (RevCommit parentCommit : commit.getParents ())
			{
				RelationNode parentNode = tree.getCommitNode (parentCommit, m_gitRepo);

				node.addParent (parentNode);
			}
		}
		return tree;
	}
}
