package com.bw.jgraph.ui.impl;

import com.bw.jgraph.graph.Node;
import com.bw.jgraph.ui.DecoratorPainter;
import com.bw.jgraph.ui.Geometry;
import com.bw.jgraph.ui.GraphicContext;
import com.bw.jgraph.ui.NodeDecorator;

import java.awt.geom.Rectangle2D;


public class ShapeNodeDecorator implements NodeDecorator
{
	public ShapeNodeDecorator(Geometry geometry, DecoratorPainter shape)
	{
		geo = geometry;
		this.shape = shape;
	}

	protected Geometry geo;
	protected DecoratorPainter shape;

	@Override
	public void install(Node node)
	{
	}

	@Override
	public void uninstall(Node node)
	{
	}


	@Override
	public void decorate(GraphicContext ctx, Node node)
	{
		shape.paintAlong(ctx, geo.getBounds(node), 0, -shape.getDistance());
	}

	@Override
	public Rectangle2D.Float getBounds(Node node)
	{
		Rectangle2D.Float r = new Rectangle2D.Float();
		r.setRect(geo.getBounds(node));
		double w = (double) shape.getDistance() / 2;
		r.x -= (float) w;
		r.y -= (float) w;
		r.width += (float) (2 * w);
		r.height += (float) (2 * w);
		return r;
	}


}
