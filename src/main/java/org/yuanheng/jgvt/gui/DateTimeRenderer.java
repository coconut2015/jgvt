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
package org.yuanheng.jgvt.gui;

import java.text.DateFormat;

import javax.swing.table.DefaultTableCellRenderer;

/**
 * @author	Heng Yuan
 */
public class DateTimeRenderer extends DefaultTableCellRenderer.UIResource
{
	private static final long serialVersionUID = 2215534356965780186L;

	private final DateFormat m_format;;

	public DateTimeRenderer (DateFormat format)
	{
		m_format = format;
	}

	public void setValue (Object value)
	{
		setText ((value == null) ? "" : m_format.format (value));
	}
}
