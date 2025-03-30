package com.bw.jgraph.ui.impl;

import com.bw.jgraph.geometry.ConvexHull;
import com.bw.jgraph.graph.GraphElement;
import com.bw.jgraph.graph.Node;
import com.bw.jgraph.ui.Geometry;
import com.bw.jgraph.ui.GeometryListener;
import com.bw.jgraph.ui.GraphicContext;
import com.bw.jgraph.ui.NodeDecorator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CloudNodeDecorator implements NodeDecorator, GeometryListener
{
	private int pointDistance = 50;
	private int gap = 3;
	private Stroke stroke = new BasicStroke(2);
	private Geometry geo;

	private static final class PathInfo
	{
		public Path2D.Float path;
		public boolean visible;

		PathInfo(Path2D.Float p, boolean v)
		{
			path = p;
			visible = v;
		}
	}

	private final Map<Integer, PathInfo> paths = new HashMap<>();

	public CloudNodeDecorator(Geometry geometry)
	{
		geo = geometry;
	}

	@Override
	public void install(Node node)
	{
		// All sub nodes affect the convex hull, so we depend on them
		geo.addDependency(this, node.getTreeNodes());
		paths.put(node.id, new PathInfo(new Path2D.Float(), geo.isVisible(node)));
		geometryUpdated(geo, node);
	}

	@Override
	public void uninstall(Node node)
	{
		paths.remove(node.id);
		geo.removeDependency(this, node.getTreeNodes());
	}


	@Override
	public void decorate(GraphicContext ctx, Node node)
	{
		PathInfo pi = paths.get(node.id);
		if (pi != null)
		{
			Path2D.Float p = pi.path;
			Graphics2D g2 = (Graphics2D) ctx.g2D_.create();
			try
			{
				g2.setPaint(Color.GRAY);
				g2.setStroke(stroke);
				g2.draw(p);
			}
			finally
			{
				g2.dispose();
			}
		}
	}

	@Override
	public Rectangle2D.Float getBounds(Node node)
	{
		PathInfo pi = paths.get(node.id);
		if (pi != null)
		{
			Rectangle2D.Float r = new Rectangle2D.Float();
			r.setRect(pi.path.getBounds2D());
			return r;
		}
		return null;
	}

	private void collectRelevantNodes(Set<Node> nodeWithPaths, Node inner)
	{
		if (paths.containsKey(inner.id))
		{
			nodeWithPaths.add(inner);
		}
		for (Iterator<Node> it = inner.parents(); it.hasNext(); )
			collectRelevantNodes(nodeWithPaths, it.next());
	}

	@Override
	public void geometryUpdated(Geometry geo, List<GraphElement> ea)
	{
		Set<Node> toUpdate = new HashSet<>();

		// for all changed nodes collect the parent nodes we decorate.
		for (GraphElement e : ea)
		{
			collectRelevantNodes(toUpdate, (Node) e);
		}

		for (Node node : toUpdate)
		{
			PathInfo pi = paths.get(node.id);
			if (geo.isVisible(node))
			{
				Point[] pts = ConvexHull.convex_hull_graham_andrew(geo.getTreePoints(node));
				if (pts != null && pts.length > 1)
				{
					Path2D.Float path = new Path2D.Float();

					final float offset = pointDistance * 0.1f;
					final int N = pts.length;
					float x1, y1, x3, y3, dx, dy, dyy, dxx;

					Point next = pts[0];
					Point last = pts[N - 1];
					Point tmp;

					float x0 = last.x - offset;
					float y0 = last.y + offset;
					float x2 = x0;
					float y2 = y0;

					path.moveTo(x0, y0);

					for (int i = 0; i < N; ++i)
					{
						x1 = next.x;
						y1 = next.y;

						if (i < (N - 1))
						{
							tmp = next;
							next = pts[i + 1];
							// To get a pretty flow around the corners, we check the slops of the line thought the neighbor points.
							dxx = next.x - last.x;
							dyy = next.y - last.y;
							last = tmp;

							if (dxx > 0)
								y1 -= offset;
							else if (dxx < 0)
								y1 += offset;
							if (dyy < 0)
								x1 -= offset;
							else if (dyy > 0)
								x1 += offset;

						}
						else
						{
							dxx = dyy = 0;
							x1 -= offset;
							y1 += offset;
						}

						dx = x1 - x0;
						dy = y1 - y0;

						final float length = Math.abs(dx) + Math.abs(dy);
						if (length > pointDistance)
						{
							final int jn = (int) (length / pointDistance);
							final float xf = pointDistance * (dx / length);
							final float yf = pointDistance * (dy / length);

							for (int j = 0; j < jn; ++j)
							{
								if ((j + 2) * pointDistance < length)
								{
									x3 = x0 + (j + 1) * xf;
									y3 = y0 + (j + 1) * yf;
								}
								else
								{
									x3 = x1;
									y3 = y1;
								}
								addQuad(path, x2, y2, x3, y3);
								x2 = x3;
								y2 = y3;
							}
						}
						else
						{
							addQuad(path, x2, y2, x1, y1);
							x2 = x1;
							y2 = y1;
						}
						x0 = x1;
						y0 = y1;
					}
					if (pi == null)
					{
						pi = new PathInfo(path, true);
						paths.put(node.id, pi);
					}
					else
					{
						pi.visible = true;
						pi.path = path;
					}
				}
			}
			else if (pi != null && pi.visible)
			{
				pi.visible = false;
			}
		}
	}

	private void addQuad(Path2D.Float path, float x0, float y0, float x1, float y1)
	{
		final float dx = x1 - x0;
		final float dy = y1 - y0;
		final float length = Math.abs(dx) + Math.abs(dy);
		if (length > 1f)
		{
			path.quadTo(x0 + .5f * dx + pointDistance * (dy / length),
					y0 + .5f * dy - pointDistance * (dx / length), x1, y1);
		}
	}
}
