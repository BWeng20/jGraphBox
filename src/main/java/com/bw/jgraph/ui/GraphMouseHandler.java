package com.bw.jgraph.ui;

import com.bw.jgraph.graph.GraphElement;
import com.bw.jgraph.graph.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

public class GraphMouseHandler extends MouseAdapter
{

	/**
	 * The current node that is dragged.
	 */
	Node nodeDragged;

	/**
	 * The current element of the dragged node.
	 * If null the whole node is dragged.
	 */
	GraphElement elementDragged;

	Point org;
	boolean moved = false;
	Cursor cursor;
	Cursor moveCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
	final GraphPanel gpanel;

	public GraphMouseHandler(GraphPanel gpanel)
	{
		this.gpanel = gpanel;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if ( e.isControlDown()) {
			double amount = e.getPreciseWheelRotation();
			gpanel.setScale(gpanel.getScale() + (float) (amount / 10));
		} else {
			Component c = gpanel.getParent();
			c.dispatchEvent(SwingUtilities.convertMouseEvent(gpanel, e, c));
		}
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		moved = false;
		nodeDragged = gpanel.getNodeAt(e.getPoint());
		org = e.getPoint();
		elementDragged = null;
		if (nodeDragged != null)
		{
			System.out.println("Click on node " + nodeDragged.id + " " + org.x + "," + org.y);

			NodeVisual nv = gpanel.getNodeVisual();

			Point2D.Float p = gpanel.getNodeLocation(nodeDragged);

			float scale = nv.getVisualSettings().scale_;
			p.x = (e.getX() - p.x) / scale;
			p.y = (e.getY() - p.y) / scale;
			System.out.println("mousePressed at " + p);

			elementDragged = nv.pressed(nodeDragged, p);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		System.out.println("Mouse released " + e.getPoint());
		if (moved)
		{
			gpanel.endNodeDrag();
			moved = false;
		}
		if (cursor != null)
		{
			gpanel.setCursor(cursor);
			cursor = null;
		}

		gpanel.getNodeVisual()
			  .released();

	}

	@Override
	public void mouseDragged(MouseEvent e)
	{
		if (cursor == null)
		{
			cursor = gpanel.getCursor();
			gpanel.setCursor(moveCursor);
		}

		Point p = e.getPoint();

		int dx = p.x - org.x;
		int dy = p.y - org.y;
		org.x = p.x;
		org.y = p.y;

		if (!moved)
		{
			gpanel.startNodeDrag();
			moved = true;
		}

		if (nodeDragged == null)
			gpanel.moveOrigin(dx, dy);
		else if (elementDragged == null)
			gpanel.moveNode(nodeDragged, dx, dy, !e.isControlDown());
		else
		{
			gpanel.moveElementAlongShape(elementDragged, gpanel.getNodeVisual()
															   .getVisualBounds(nodeDragged), new Point2D.Double(p.x, p.y));
		}
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		Node node = nodeDragged;
		nodeDragged = null;
		elementDragged = null;
		if (node != null)
		{

			System.out.println("Click @ Node " + node.id);

			Point2D.Float p = gpanel.getNodeLocation(node);
			if (p != null)
			{
				float scale = gpanel.getNodeVisual()
									.getVisualSettings().scale_;
				p.x = (e.getX() - p.x) / scale;
				p.y = (e.getY() - p.y) / scale;
				gpanel.getNodeVisual()
					  .click(node, p);
				gpanel.repaint();
			}
		}
	}
}
