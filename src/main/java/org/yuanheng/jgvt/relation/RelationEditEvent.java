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

/**
 * @author	Heng Yuan
 */
public class RelationEditEvent
{
	public enum Type
	{
		Added,
		Removed,
		Changed
	}

	private final Type m_type;
	private	final Object m_src;
	private final int m_start;
	private final int m_end;

	public RelationEditEvent (Type type, Object src, int start, int end)
	{
		m_type = type;
		m_src = src;
		m_start = start;
		m_end = end;
	}

	public Type getType ()
	{
		return m_type;
	}

	public Object getSrc ()
	{
		return m_src;
	}

	public int getStart ()
	{
		return m_start;
	}

	public int getEnd ()
	{
		return m_end;
	}
}
