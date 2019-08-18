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

import javax.swing.Icon;
import javax.swing.ImageIcon;
/**
 * @author	Heng Yuan
 */
public class Icons
{
	private final static Icons s_icons = new Icons ();

	public static Icons getInstance ()
	{
		return s_icons;
	}

	public Icon SAVE;
	public Icon SAVE_SMALL;
	public Icon SEARCH;
	public Icon SEARCH_SMALL;
	public Icon SEARCHBRANCH;
	public Icon SEARCHBRANCH_SMALL;
	public Icon SEARCHTAG;
	public Icon SEARCHTAG_SMALL;
	public Icon ABOUT;

	private Icons ()
	{
		SAVE = new ImageIcon (Icons.class.getResource ("icons/save.png"));
		SAVE_SMALL = new ImageIcon (Icons.class.getResource ("icons/save_small.png"));
		SEARCH = new ImageIcon (Icons.class.getResource ("icons/search.png"));
		SEARCH_SMALL = new ImageIcon (Icons.class.getResource ("icons/search_small.png"));
		SEARCHBRANCH = new ImageIcon (Icons.class.getResource ("icons/searchbranch.png"));
		SEARCHBRANCH_SMALL = new ImageIcon (Icons.class.getResource ("icons/searchbranch_small.png"));
		SEARCHTAG = new ImageIcon (Icons.class.getResource ("icons/searchtag.png"));
		SEARCHTAG_SMALL = new ImageIcon (Icons.class.getResource ("icons/searchtag_small.png"));
		ABOUT = new ImageIcon (Icons.class.getResource ("icons/about.png"));
	}
}
