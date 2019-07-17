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

import java.io.File;

import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Test;

/**
 * Dummy test class to make lauching the gui easier.
 *
 * @author Heng Yuan
 */
public class TestGitRepo
{
	@Test
	public void testGetRoot () throws Exception
	{
		// No exceptions should be thrown
		GitRepo gitRepo = new GitRepo ();
		Assert.assertEquals ("jgvt", gitRepo.getRoot ().getName ());
		gitRepo.close ();
	}

	@Test
	public void testGetRepo () throws Exception
	{
		// No exceptions should be thrown
		GitRepo gitRepo = new GitRepo ();
		Repository repo = gitRepo.getRepo ();
		Assert.assertEquals ("master", repo.getBranch ());
		gitRepo.close ();
	}

	private void printRevCommit (RevCommit c)
	{
//		PersonIdent authorIdent = c.getAuthorIdent();
		java.util.Date d = new java.util.Date (c.getCommitTime () * 1000L);

		System.out.print ("name: " + c.getName ());
    	System.out.println (", time: " + d);
	}

	@Test
	public void testLog () throws Exception
	{
		GitRepo gitRepo = new GitRepo (new File ("../jaqy"));
		LogCommand log = gitRepo.getGit ().log ();

	    for (RevCommit c : log.call ())
		{
	    	printRevCommit (c);
	    	System.out.println ("name: " + c.getName ());
	    	int parentCount = c.getParentCount ();
	    	for (int i = 0; i < parentCount; ++i)
	    	{
	    		RevCommit p = c.getParent (i);
		    	System.out.print ("parent: ");
		    	printRevCommit (p);
	    	}
		}
		gitRepo.close ();
	}
}
