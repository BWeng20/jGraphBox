package com.bw.jgraph.ui.impl;

import com.bw.jgraph.graph.Node;
import com.bw.jgraph.ui.GraphicContext;
import com.bw.jgraph.ui.NodeDecorator;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Aggregates multiple node decorators.
 */
public class NodeDecoratorComposer implements NodeDecorator
{
	private final List<NodeDecorator> decorators = new ArrayList<>(2);

	@Override
	public void decorate(GraphicContext ctx, Node node)
	{
		for (NodeDecorator d : decorators)
			d.decorate(ctx, node);
	}

	@Override
	public Rectangle2D.Float getBounds(Node node)
	{
		Rectangle2D.Float r = null;
		for (NodeDecorator d : decorators)
		{
			if (r == null)
				r = d.getBounds(node);
			else
			{
				Rectangle2D.Float r2 = d.getBounds(node);
				if (r2 != null)
					Rectangle2D.union(r, r2, r);
			}
		}
		return r;
	}


	@Override
	public void install(Node node)
	{
		for (NodeDecorator d : decorators)
			d.install(node);
	}

	@Override
	public void uninstall(Node node)
	{
		for (NodeDecorator d : decorators)
			d.uninstall(node);
	}

	public NodeDecoratorComposer(NodeDecorator... d)
	{
		decorators.addAll(Arrays.asList(d));
	}

	public void addDecorator(NodeDecorator d)
	{
		decorators.remove(d);
		decorators.add(d);
	}

	public void removeDecorator(NodeDecorator d)
	{
		decorators.remove(d);
	}

	public int size()
	{
		return decorators.size();
	}
}
