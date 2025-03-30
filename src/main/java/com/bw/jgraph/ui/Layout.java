package com.bw.jgraph.ui;

import com.bw.jgraph.graph.Node;

public interface Layout
{
	public Geometry getGeometry();

	public void placeChildren(Node node);

}
