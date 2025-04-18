package com.bw.jgraph.ui;

import com.bw.jtools.svg.ShapeHelper;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public abstract class DecoratorPainter {

    public double getWidth() {
        return 0;
    }

    public int getNumberOfVariants() {
        return 0;
    }

    /**
     * Draw at some specific point, used e.g. for end points.
     * @param ctx The context.
     * @param point The point
     */
    public void drawAtPoint(GraphicContext ctx, Point2D.Float point) {
        if (point != null)
        {
            GraphicContext lctx = new GraphicContext(ctx);
            try
            {
                lctx.g2D_.translate(point.x, point.y);
                paint(lctx.g2D_);
            }
            finally
            {
                lctx.dispose();
            }
        }
    }

    /**
     */
    public void paintAlong(GraphicContext ctx, ShapeHelper outline, double start, double end) {
        final GraphicContext gl = new GraphicContext(ctx);
        try
        {
            final AffineTransform t = gl.g2D_.getTransform();

            if (ctx.debug_)
            {
                // Debugging: Shows the path
                gl.g2D_.setPaint(GraphicContext.debugPaint_);
                gl.g2D_.setStroke(GraphicContext.debugStroke_);
                gl.g2D_.draw(outline.getShape());
            }

            double pos = start;
            if (pos < 0)
            {
                pos = 0;
            }
            final double D = getDistance();
            
            ShapeHelper.PointOnPath pop1 = outline.pointAtLength(pos);
            if ( pop1 == null)
                return;
            if ( gl.currentColor_ != null)
                gl.g2D_.setPaint(gl.currentColor_);

            while (pos < end)
            {
                pos += D;
                ShapeHelper.PointOnPath pop2 = outline.pointAtLength(pos);
                gl.g2D_.setTransform(t);
                gl.g2D_.translate(pop1.x_, pop1.y_);
                if (pop2 == null)
                {
                    gl.g2D_.rotate(pop1.angle_);
                }
                else
                {
                    gl.g2D_.rotate(Math.atan2(pop2.y_ - pop1.y_, pop2.x_ - pop1.x_));
                }

                paint(gl.g2D_);

                pop1 = pop2;
            }
        }
        finally
        {
            gl.dispose();
        }

    }

    public void paintAlong(GraphicContext ctx, Shape shape, double start, double end)
    {
        if (shape != null)
        {
            paintAlong(ctx, new ShapeHelper(shape), start, end);
        }
    }

    /**
     * Get the distance between two painted instances along the shape.
     * @return The distance.
     */
    public abstract int getDistance();

    /**
     * Paint the shape at position (0,0).
     * @param g2D The graphic context.
     */
    protected abstract void paint(Graphics2D g2D);

}
