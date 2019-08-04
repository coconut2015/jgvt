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

import java.io.Serializable;

import org.eclipse.jgit.lib.ObjectId;

/**
 * @author	Heng Yuan
 */
class GVTVertex implements Serializable
{
	private static final long serialVersionUID = 7784184285397252689L;

	private final ObjectId m_id;
	private String m_name;
	private String m_toolTip;

	public GVTVertex (ObjectId id)
	{
		m_id = id;
	}

	public ObjectId getId ()
	{
		return m_id;
	}

	public void setName (String name)
	{
		m_name = name;
	}

	public void setToolTip (String toolTip)
	{
		m_toolTip = toolTip;
	}

	@Override
	public String toString ()
	{
		return m_name;
	}

	public String getToolTip ()
	{
		return m_toolTip;
	}
}
