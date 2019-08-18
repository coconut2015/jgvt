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

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

/**
 * @author Heng Yuan
 */
public abstract class TableRowFilter extends RowFilter<TableModel, Object>
{
	private final int[] m_columns;

	public TableRowFilter (int[] columns)
	{
		m_columns = columns;
	}

	@Override
	public boolean include (Entry<? extends TableModel, ? extends Object> value)
	{
		int count = value.getValueCount ();
		if (m_columns != null && m_columns.length > 0)
		{
			// for each column specified, check
			for (int i = 0; i < m_columns.length; ++i)
			{
				if (include (value, m_columns[i]))
					return true;
			}
		}
		else
		{
			// okay, all columns need to be filtered.
			for (int i = 0; i < count; ++i)
			{
				if (include (value, i))
					return true;
			}
		}
		return false;
	}

	public abstract boolean include (Entry<? extends TableModel, ? extends Object> value, int index);
}
