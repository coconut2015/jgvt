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
package org.yuanheng.jgvt.relation.discovery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author	Heng Yuan
 */
public class DiscoveryAlgorithmManager
{
	private final static DiscoveryAlgorithmManager s_instance = new DiscoveryAlgorithmManager ();

	static
	{
		s_instance.add (0, new SingleChildAlgorithm ());
		s_instance.add (0, new SideMergeSingleChildAlgorithm ());
		s_instance.add (0, new SideBranchMergeChildAlgorithm ());	// TODO: not as safe as I original thought
		s_instance.add (0, new ParentSideBranchParentAlgorithm ());
		s_instance.add (0, new TwoChildrenAlgorithm ());
		s_instance.add (0, new MultipleChildrenAlgorithm ());
		s_instance.add (0, new RepeatMergeAlgorithm ());
		s_instance.add (0, new MergeParentAlgorithm ());
		s_instance.add (0, new MergePullRequestAlgorithm ());
		s_instance.add (0, new MergeOutMergeInAlgorithm ());
		s_instance.add (0, new GrandParentSideMergeAlgorithm ());
		s_instance.add (0, new TwoChildrenOneMergeParentAlgorithm ());
		s_instance.add (0, new ParallelParentsAlgorithm ());


		s_instance.add (1, new DiamondMergeLeftParentAlgorithm ());
		s_instance.add (1, new SwapParentsLongerChainAlgorithm ());
		s_instance.add (1, new DiamondSwapParentsTwoLongChainAlgorithm ());
	}

	public static DiscoveryAlgorithmManager getInstance ()
	{
		return s_instance;
	}

	private final HashMap<Integer, List<DiscoveryAlgorithm>> m_algorithmMap = new HashMap<Integer, List<DiscoveryAlgorithm>> ();

	private DiscoveryAlgorithmManager ()
	{
	}

	public void add (int level, DiscoveryAlgorithm algorithm)
	{
		List<DiscoveryAlgorithm> list = m_algorithmMap.get (level);
		if (list == null)
		{
			list = new ArrayList<DiscoveryAlgorithm> ();
			m_algorithmMap.put (level, list);
		}
		list.add (algorithm);
	}

	public List<DiscoveryAlgorithm> getList (int level)
	{
		return  m_algorithmMap.get (level);
	}
}
