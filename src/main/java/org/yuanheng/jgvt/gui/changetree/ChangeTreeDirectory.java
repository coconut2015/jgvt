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
package org.yuanheng.jgvt.gui.changetree;

import org.yuanheng.jgvt.Controller;

/**
 * @author	Heng Yuan
 */
class ChangeTreeDirectory extends ChangeTreeNode
{
	private String m_path;

	ChangeTreeDirectory (ChangeTreeNode parent, String path)
	{
		super (parent);
		m_path = path;
	}

	public String getPath ()
	{
		return m_path;
	}

	@Override
	public String toString ()
	{
		return m_path;
	}

	@Override
	String computeHtml (Controller controller)
	{
		return null;
	}
}
