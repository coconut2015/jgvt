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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

/**
 * @author	Heng Yuan
 */
class SwingUtils
{
	public static Dimension getDimension (JComponent comp, FontMetrics fm, int length)
	{
		Rectangle2D rect = fm.getMaxCharBounds (comp.getGraphics ());
		return new Dimension ((int)rect.getWidth () * length, (int)rect.getHeight ());
	}

	public static int getWidth (Graphics g, Font font, String[] strs)
	{
		FontMetrics fm = g.getFontMetrics (font);
		int width = 0;
		for (String str : strs)
		{
			int w = fm.stringWidth (str);
			if (width < w)
				width = w;
		}
		return width;
	}

	public static int getWidth (Graphics g, Font font, int length)
	{
		FontMetrics fm = g.getFontMetrics (font);
		return fm.stringWidth ("M") * length;
	}
}
