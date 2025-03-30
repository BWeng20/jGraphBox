package com.bw.jgraph.ui;

import com.bw.jtools.svg.ShapeHelper;

import java.awt.*;
import java.awt.geom.Point2D;

public class DecoratorShape {

    public double getWidth() {
        return 0;
    }

    public int getNumberOfVariants() {
        return 0;
    }

    public void drawAtPoint(GraphicContext ctx, Point2D.Float p, int i) {

    }

    /**
     * @param firstVariantIndex Index of first shape to use. If negative,
     *                          counting from end (e-g- -2 results in index=N-3).
     * @param lastVariantIndex  Index of last used shape, if negative, counting
     *                          from end (e-g- -2 results in index=N-3).
     */
    public void paintAlong(GraphicContext ctx, ShapeHelper outline, double start, double end, int firstVariantIndex, int lastVariantIndex) {
    }

    public void paintAlong(GraphicContext ctx, Shape shape, double start, double end)
    {
        if (shape != null)
        {
            paintAlong(ctx, new ShapeHelper(shape), start, end, 0, -2);
        }
    }

    public int getDistance() {
        return 0;
    }
}
