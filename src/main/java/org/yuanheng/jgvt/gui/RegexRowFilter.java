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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

/**
 * @author	Heng Yuan
 */
public class RegexRowFilter extends TableRowFilter
{
	private final int m_flag;
    private Matcher m_matcher;

    public RegexRowFilter (int flag, int[] columns)
	{
    	super (columns);
    	m_flag = flag;
	}

    public void setRegEx (String p)
    {
    	if (p == null)
    	{
    		m_matcher = null;
    	}
    	else
    	{
			Pattern regex = Pattern.compile (p, m_flag);
			m_matcher = regex.matcher("");
    	}
    }

	@Override
	public boolean include (Entry<? extends TableModel, ? extends Object> value, int index)
	{
		if (m_matcher == null)
			return true;
		m_matcher.reset(value.getStringValue (index));
        return m_matcher.find ();
	}
}
