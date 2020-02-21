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
package org.yuanheng.jgvt.relation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

import org.eclipse.jgit.lib.ObjectId;
import org.yuanheng.jgvt.Defaults;
import org.yuanheng.jgvt.GitRepo;

/**
 * @author	Heng Yuan
 */
public class RelationEditList implements Cloneable
{
	public static RelationEditList read (GitRepo gitRepo)
	{
		File gitRoot = gitRepo.getRoot ();
		File gitDir = gitRepo.getGitDir ();
		File file = null;

		if (gitRoot != null && gitRoot.isDirectory ())
		{
			File gitRootFile = new File (gitRoot, Defaults.JGVT_EDITLIST);
			if (gitRootFile.isFile ())
			{
				file = gitRootFile;
			}
		}

		if (gitDir != null && gitDir.isDirectory ())
		{
			File jgvtDir = new File (gitDir, Defaults.GIT_DIR_JGVT_DIR);
			File gitDirFile = new File (jgvtDir, Defaults.JGVT_EDITLIST);
			if (gitDirFile.isFile ())
			{
				file = gitDirFile;
			}
		}

		if (file == null)
		{
			return new RelationEditList ();
		}
		else
		{
			return read (file);
		}
	}

	public static RelationEditList read (File file)
	{
		Properties properties = new Properties ();
		try (FileReader reader = new FileReader (file))
		{
			properties.load (reader);
		}
		catch (Exception ex)
		{
		}
		RelationEditList list = new RelationEditList ();
		for (Map.Entry<Object,Object> entry: properties.entrySet ())
		{
			ObjectId id = ObjectId.fromString (entry.getKey ().toString ());
			int value = Integer.parseInt (entry.getValue ().toString ());
			list.add (id, value);
		}
		return list;
	}

	public static boolean write (File file, RelationEditList list)
	{
		Properties properties = new Properties ();
		for (RelationEditEntry entry : list.getList ())
		{
			properties.setProperty (entry.id.getName (), Integer.toString (entry.mergeParent));
		}
		try
		{
			FileWriter writer = new FileWriter (file);
			properties.store (writer, "jgvt");
			writer.close ();
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	private final HashMap<ObjectId, Integer> m_search;
	private final ArrayList<RelationEditEntry> m_list;
	private RelationEditListener m_listener;

	public RelationEditList ()
	{
		m_search = new HashMap<ObjectId, Integer> ();
		m_list = new ArrayList<RelationEditEntry> ();
	}

	@Override
	public Object clone ()
	{
		RelationEditList newList = new RelationEditList ();
		newList.m_search.putAll (m_search);
		newList.m_list.addAll (m_list);
		return newList;
	}

	public void addListener (RelationEditListener listener)
	{
		m_listener = listener;
	}

	public int size ()
	{
		return m_list.size ();
	}

	public int getJoinParent (ObjectId id)
	{
		Integer entryId = m_search.get (id);
		if (entryId == null)
		{
			return -1;
		}
		return m_list.get (entryId).mergeParent;
	}

	public void add (ObjectId id, int mergeParent)
	{
		Integer entryId = m_search.get (id);
		if (entryId == null)
		{
			RelationEditEntry entry = new RelationEditEntry ();
			// we use the copy to avoid dealing with RevCommit
			entry.id = id.copy ();
			entry.mergeParent = mergeParent;

			m_list.add (entry);
			int index = m_list.size () - 1;
			m_search.put (id, index);

			fireDataChangedEvent (RelationEditEvent.Type.Added, this, index, index);
		}
		else
		{
			RelationEditEntry entry = m_list.get (entryId);
			entry.mergeParent = mergeParent;

			fireDataChangedEvent (RelationEditEvent.Type.Changed, this, entryId, entryId);
		}
	}

	public void remove (ObjectId id)
	{
		Integer entryId = m_search.get (id);
		if (entryId != null)
		{
			remove (entryId);
		}
	}

	public void remove (int index)
	{
		RelationEditEntry entry = m_list.get (index);
		m_list.remove (index);
		m_search.remove (entry.id);

		fireDataChangedEvent (RelationEditEvent.Type.Removed, this, index, index);
	}

	public void remove (int start, int end)
	{
		for (int index = start; index <= end; ++index)
		{
			RelationEditEntry entry = m_list.get (index);
			m_search.remove (entry.id);
		}
		m_list.subList (start, end + 1).clear ();

		fireDataChangedEvent (RelationEditEvent.Type.Removed, this, start, end);
	}

	public void removeAll ()
	{
		int size = m_list.size ();
		if (size > 0)
		{
			remove (0, size - 1);
		}
	}

	public List<RelationEditEntry> getList ()
	{
		return m_list;
	}

	public RelationEditEntry get (int index)
	{
		return m_list.get (index);
	}

	public void fireDataChangedEvent (RelationEditEvent.Type type, Object src, int start, int end)
	{
		if (m_listener != null)
		{
			m_listener.dataChanged (new RelationEditEvent (type, src, start, end));
		}
	}
}
