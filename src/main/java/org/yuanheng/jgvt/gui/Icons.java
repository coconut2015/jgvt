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

import java.awt.Color;
import java.awt.Insets;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
/**
 * @author	Heng Yuan
 */
class Icons
{
	public Icon SEARCH;
	public Icon ABOUT;

	public Icons ()
	{
		JMenuItem item = new JMenuItem ("M");
		int menuHeight = item.getPreferredSize ().height;
		Insets menuInsets = item.getInsets ();
		// infer the menu font height from menu size.
		int iconSize = menuHeight - menuInsets.top - menuInsets.bottom;
		IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
		ABOUT = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.HELP, iconSize, new Color(0x9b, 0, 0xff));
		SEARCH = IconFontSwing.buildIcon(GoogleMaterialDesignIcons.SEARCH, iconSize, new Color(0x9b, 0, 0xff));
	}
}
