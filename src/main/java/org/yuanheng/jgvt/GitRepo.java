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
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.util.io.NullOutputStream;

/**
 * A wrapper around Git and GitRepo.
 *
 * @author	Heng Yuan
 */
public class GitRepo implements AutoCloseable
{
	private final Repository m_repo;
	private final Git m_git;
	private final File m_root;
	private boolean m_fetched;
	private WeakReference<Map<ObjectId, Ref>> m_tagMap = new WeakReference<Map<ObjectId, Ref>> (null);
	private WeakReference<Map<ObjectId, Ref>> m_branchMap = new WeakReference<Map<ObjectId, Ref>> (null);

	public GitRepo () throws IOException
	{
		this (new File ("."));
	}

	public GitRepo (File dir) throws IOException
	{
		FileRepositoryBuilder builder = new FileRepositoryBuilder ();
		m_repo = builder.findGitDir(dir)
						.readEnvironment()
						.findGitDir()
						.setMustExist(true)
						.build();
		m_git = Git.wrap (m_repo);
		m_root = builder.getGitDir ().getParentFile ().getCanonicalFile ();
	}

	public ObjectId getHead ()
	{
		try
		{
			return m_repo.resolve (Constants.HEAD);
		}
		catch (IOException ex)
		{
		}
		return null;
	}

	public File getRoot ()
	{
		return m_root;
	}

	public Repository getRepo ()
	{
		return m_repo;
	}

	public Git getGit ()
	{
		return m_git;
	}

	public void fetch ()
	{
		if (!m_fetched)
		{
			try
			{
				m_git.fetch ().call ();
				m_fetched = true;
			}
			catch (GitAPIException ex)
			{
			}
		}
	}

	public String getBranch ()
	{
		try
		{
			return m_repo.getBranch ();
		}
		catch (IOException ex)
		{
			return "";
		}
	}

	/**
	 * Caller should close the RevWalk object.
	 *
	 * @return	RevWalk object.
	 */
	public RevWalk createRevWalk ()
	{
		return new RevWalk (m_repo);
	}

	public Iterable<RevCommit> getCommitLogs (File file) throws GitAPIException, IOException
	{
		fetch ();
		LogCommand log = m_git.log ().all ().setMaxCount (Integer.MAX_VALUE);
		if (file != null)
		{
			log.addPath (Utils.getRelativePath (file, m_root).toString ());
		}

		return log.call ();
	}

	public Map<ObjectId, Ref> getTagMap () throws GitAPIException
	{
		Map<ObjectId, Ref> map = m_tagMap.get ();
		if (map == null)
		{
			List<Ref> refs = m_git.tagList ().call ();
			map = new HashMap<ObjectId, Ref> ();
			for (Ref ref : refs)
				map.put (ref.getObjectId (), ref);

			m_tagMap = new WeakReference<Map<ObjectId, Ref>> (map);
		}
		return map;
	}

	public Map<ObjectId, Ref> getBranchMap () throws GitAPIException
	{
		Map<ObjectId, Ref> map = m_branchMap.get ();
		if (map == null)
		{
			List<Ref> refs = m_git.branchList ().setListMode (ListMode.ALL).call ();
			map = new HashMap<ObjectId, Ref> ();
			for (Ref ref : refs)
				map.put (ref.getObjectId (), ref);

			m_branchMap = new WeakReference<Map<ObjectId, Ref>> (map);
		}
		return map;
	}

	public Map<String, ObjectId> getReverseBranchMap () throws GitAPIException
	{
		Map<ObjectId, Ref> branchMap = getBranchMap ();
		Map<String, ObjectId> reverseMap = new HashMap<String, ObjectId> ();

		for (Ref branch : branchMap.values ())
		{
			reverseMap.put (branch.getName (), branch.getObjectId ());
		}
		return reverseMap;
	}

	public List<ChangeInfo> getChanges (RevCommit commit)
	{
		ArrayList<ChangeInfo> changes = new ArrayList<ChangeInfo> ();
	    DiffFormatter diffFmt = new DiffFormatter(NullOutputStream.INSTANCE);
	    diffFmt.setRepository(m_repo);
        RevTree before = commit.getParentCount() > 0 ? commit.getParent(0).getTree() : null;
        RevTree current = commit.getTree();

        try
        {
	        for (DiffEntry diff: diffFmt.scan (before, current))
	        {
	        	ChangeInfo info = new ChangeInfo (diff);
	        	int added = 0;
	        	int deleted = 0;
	        	for (Edit edit : diffFmt.toFileHeader(diff).toEditList())
	        	{
	        		deleted += edit.getEndA() - edit.getBeginA();
	        		added += edit.getEndB() - edit.getBeginB();
	            }
	        	info.setAdded (added);
	        	info.setDeleted (deleted);
	            changes.add (info);
	        }
        }
        catch (Exception ex)
        {
        }
        diffFmt.close ();
		return changes;
	}

	@Override
	public void close () throws Exception
	{
		m_repo.close ();
	}
}
