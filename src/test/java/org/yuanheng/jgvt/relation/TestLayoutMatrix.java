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

import org.junit.Assert;
import org.junit.Test;
import org.yuanheng.jgvt.relation.LayoutMatrix;

/**
 * @author	Heng Yuan
 */
public class TestLayoutMatrix
{
	@Test
	public void testTake () throws Exception
	{
		LayoutMatrix matrix = new LayoutMatrix ();
		Assert.assertFalse (matrix.isTaken (5, 5));
		matrix.take (5, 5);
		Assert.assertFalse (matrix.isTaken (5, 4));
		Assert.assertTrue (matrix.isTaken (5, 5));
		Assert.assertFalse (matrix.isTaken (4, 5));
	}

	@Test
	public void testTake2 () throws Exception
	{
		LayoutMatrix matrix = new LayoutMatrix ();
		Assert.assertFalse (matrix.isTaken (5, 1, 5));
		matrix.take (5, 10, 12);
		Assert.assertFalse (matrix.isTaken (5, 1, 5));
		Assert.assertTrue (matrix.isTaken (5, 11, 12));
		matrix.take (5, 5);
		Assert.assertTrue (matrix.isTaken (5, 1, 5));
	}
}
