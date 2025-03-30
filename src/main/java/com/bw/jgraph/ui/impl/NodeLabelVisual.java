package com.bw.jgraph.ui.impl;

import com.bw.jgraph.graph.Node;
import com.bw.jgraph.ui.Geometry;
import com.bw.jgraph.ui.GraphicContext;
import com.bw.jgraph.ui.Layout;
import com.bw.jgraph.ui.VisualSettings;
import com.bw.jgraph.ui.VisualState;

import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Visual that manages a JLabel for each node. Inheritances should override
 * createRenderer to use different labels.
 */
public class NodeLabelVisual extends NodeVisualBase
{

	protected Map<Integer, JLabel> renderer = new HashMap<>();

	public NodeLabelVisual(Layout layout, VisualSettings settings)
	{
		super(layout, settings);
	}

	@Override
	public void paint(GraphicContext ctx, Node node)
	{
		final VisualState s = geo.getVisualState(node);
		final Rectangle r = Geometry.toRect(s.boundingBox);

		final Graphics2D g = ctx.g2D_;
		if (ctx.debug_)
		{
			if (s.boundingBox != null)
			{
				g.setPaint(ctx.debugPaint_);
				g.setStroke(ctx.debugStroke_);
				g.draw(s.boundingBox);
			}
		}

		paintBorder(ctx, node, s, r);

		JLabel l = renderer.get(node.id);
		if (l != null)
		{
			String text = (String) node.getAttribute(NODE_TEXT);
			if (!text.equals(l.getText()))
			{
				l.setText(text);
			}
			l.setForeground(Color.BLACK);
			r.x += margin_x2;
			r.y += margin_y2;
			r.width -= margin_x << 2;
			r.height -= margin_y << 2;
			l.setBounds(r);

			Graphics cg = g.create(r.x, r.y, r.width, r.height);
			try
			{
				l.paint(cg);
			}
			finally
			{
				cg.dispose();
			}

		}
	}

	@Override
	public void updateGeometry(Graphics2D g, Node node)
	{
		geo.beginUpdate();

		String text = (String) node.getAttribute(NODE_TEXT);

		JLabel l = renderer.get(node.id);
		if (l == null)
		{
			l = createRenderer();
			renderer.put(node.id, l);
		}
		l.setText(text);
		l.setFont(g.getFont());
		Dimension ps = l.getPreferredSize();
		l.setSize(ps);
		geo.setBounds(node, new Rectangle2D.Float(0, 0, ps.width + (margin_x << 2), ps.height + (margin_y << 2)));
		updateVisibility(node);
		geo.endUpdate();
	}

	protected JLabel createRenderer()
	{
		JLabel r = new JLabel();
		// visual cares about background.
		r.setOpaque(false);
		return r;
	}

}
