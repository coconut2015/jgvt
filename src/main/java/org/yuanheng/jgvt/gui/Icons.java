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
	public Icon ZOOMIN;
	public Icon ZOOMIN_SMALL;
	public Icon ZOOMOUT;
	public Icon ZOOMOUT_SMALL;
	public Icon ZOOMRESET;
	public Icon ZOOMRESET_SMALL;
	public Icon ABOUT;
	public Icon ABOUT_SMALL;

	private Icons ()
	{
		SAVE = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/save.png"));
		SAVE_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/save_small.png"));
		SEARCH = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/search.png"));
		SEARCH_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/search_small.png"));
		SEARCHBRANCH = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/searchbranch.png"));
		SEARCHBRANCH_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/searchbranch_small.png"));
		SEARCHTAG = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/searchtag.png"));
		SEARCHTAG_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/searchtag_small.png"));
		ZOOMIN = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/zoomin.png"));
		ZOOMIN_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/zoomin_small.png"));
		ZOOMOUT = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/zoomout.png"));
		ZOOMOUT_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/zoomout_small.png"));
		ZOOMRESET = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/zoomreset.png"));
		ZOOMRESET_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/zoomreset_small.png"));
		ABOUT = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/about.png"));
		ABOUT_SMALL = new ImageIcon (ClassLoader.getSystemResource ("META-INF/jgvt/icons/about_small.png"));
	}
}
