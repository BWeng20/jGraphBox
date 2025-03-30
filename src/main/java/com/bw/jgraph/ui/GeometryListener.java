package com.bw.jgraph.ui;

import com.bw.jgraph.graph.GraphElement;

import java.util.Arrays;
import java.util.List;

public interface GeometryListener
{
	public void geometryUpdated(Geometry geo, List<GraphElement> e);

	public default void geometryUpdated(Geometry geo, GraphElement... e)
	{
		geometryUpdated(geo, Arrays.asList(e));
	}
}
