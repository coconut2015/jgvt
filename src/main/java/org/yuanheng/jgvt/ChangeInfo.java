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

import org.eclipse.jgit.diff.DiffEntry;

/**
 * @author	Heng Yuan
 */
public class ChangeInfo
{
	private DiffEntry m_entry;
	private int m_add;
	private int m_delete;

	public ChangeInfo (DiffEntry entry)
	{
		m_entry = entry;
	}

	public DiffEntry getDiffEntry ()
	{
		return m_entry;
	}

	public int getAdded ()
	{
		return m_add;
	}

	public void setAdded (int add)
	{
		m_add = add;
	}

	public int getDeleted ()
	{
		return m_delete;
	}

	public void setDeleted (int delete)
	{
		m_delete = delete;
	}
}
