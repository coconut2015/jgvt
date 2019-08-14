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

import java.util.ArrayList;

/**
 * @author	Heng Yuan
 */
class LayoutMatrix
{
	private final ArrayList<int[]> m_matrix;

	public LayoutMatrix ()
	{
		m_matrix = new ArrayList<int[]> ();
	}

	public void take (int x, int y)
	{
		if (m_matrix.size () <= y)
		{
			int count = y - m_matrix.size () + 1;
			for (int i = 0; i < count; ++i)
			{
				m_matrix.add (null);
			}
		}
		int[] row = m_matrix.get (y);
		if (row == null || row.length <= (x / 8))
		{
			int[] newRow = new int[(x / 8) + 10];
			if (row != null)
			{
				for (int i = 0; i < row.length; ++i)
					newRow[i] = row[i];
			}
			row = newRow;
			m_matrix.set (y, row);
		}
		setBit (row, x);
	}

	public void take (int x, int y1, int y2)
	{
		if (m_matrix.size () <= y2)
		{
			int count = y2 - m_matrix.size () + 1;
			for (int i = 0; i < count; ++i)
			{
				m_matrix.add (null);
			}
		}
		for (int y = y1; y <= y2; ++y)
		{
			take (x, y);
		}
	}

	public boolean isTaken (int x, int y)
	{
		if (m_matrix.size () <= y)
		{
			return false;
		}
		int[] row = m_matrix.get (y);
		if (row == null || row.length <= x)
		{
			return false;
		}
		return isBitSet (row, x);
	}

	public boolean isTaken (int x, int y1, int y2)
	{
		for (int y = y1; y <= y2; ++y)
		{
			if (isTaken (x, y))
				return true;
		}
		return false;
	}

	private static boolean isBitSet (int[] arr, int bit)
	{
		return ((arr[bit / 8]) & (1 << (bit % 8))) != 0;
	}

	private static void setBit (int[] arr, int bit)
	{
		arr[bit / 8] |= (1 << (bit % 8));
	}
}
