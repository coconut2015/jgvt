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

import java.awt.Color;

/**
 * @author	Heng Yuan
 */
public class Defaults
{
	/**
	 * The jgvt subdirectory in .git/
	 */
	public final static String GIT_DIR_JGVT_DIR = "jgvt";
	/**
	 * The jgvt preference file
	 */
	public final static String FILE_PREFERENCE = ".jgvtconfig";
	/**
	 * The jgvt edit list file
	 */
	public final static String JGVT_EDITLIST = ".jgvt";

	/**
	 * The number of characters to show for a hash.
	 */
	public final static int DEFAULT_ABBREV_LEN = 6;

	/**
	 * The distance between two adjacent branches.
	 */
	public final static double BRANCH_SPACING = 140.0;
	public final static double MAX_BRANCH_SPACING = 1000.0;
	public final static double MIN_BRANCH_SPACING = 10.0;

	/**
	 * The distance to the immediate child in the same branch.
	 */
	public final static double CHILD_SPACING = 50.0;
	public final static double MAX_CHILD_SPACING = 1000.0;
	public final static double MIN_CHILD_SPACING = 10.0;

	/**
	 * The top left node X position;
	 */
	public final static double START_X = 100.0;
	public final static double MAX_START_X = 1000.0;
	public final static double MIN_START_X = 0;

	/**
	 * The top left node Y position;
	 */
	public final static double START_Y = 50.0;
	public final static double MAX_START_Y = 1000.0;
	public final static double MIN_START_Y = 0;
	/**
	 * Except a few algorithms, most algorithms should only treat
	 * parent 0 as trustworthy and thus join with left parent branch
	 * only.
	 */
	public final static boolean LEFT_ONLY = true;

	/**
	 * Change tree's added font color
	 */
	public final static Color COLOR_ADDED = new Color (36, 159, 64);
	/**
	 * Change tree's deleted font color
	 */
	public final static Color COLOR_DELETED = new Color (203, 36, 49);
	/**
	 * Change tree's renamed font color
	 */
	public final static Color COLOR_RENAMED = new Color (36, 203, 49);
	/**
	 * Change tree's lines added background color
	 */
	public final static Color BG_COLOR_LINE_ADDED = new Color (230, 255, 237);
	/**
	 * Change tree's lines added deleted color
	 */
	public final static Color BG_COLOR_LINE_DELETED = new Color (255, 238, 240);

	/**
	 * The bend angle for merge arrows
	 */
	public final static double DEFAULT_MERGE_ARROW_BEND_ANGLE = 15 * Math.PI / 180;
}
