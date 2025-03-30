package com.bw.jgraph.ui;

import com.bw.jtools.shape.AbstractPainterBase;

import javax.swing.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowEvent;

public class FpsStatus extends JLabel {

    GraphPanel graphPanel;
    Timer timer;
    long millis = 0;

    public FpsStatus(GraphPanel graphPanel) {
        super("...");
        this.graphPanel = graphPanel;
        timer = new Timer(1000, e ->
        {
            long newMillis = System.currentTimeMillis();
            if (graphPanel.paintCount_ > 0)
            {
                long delta = newMillis - millis;
                setText( String.format("%.1f fps", 1000d * (graphPanel.paintCount_ / ((double)delta)) ));
            } else
            {
                setText("...");
            }
            graphPanel.paintCount_ = 0;
            millis = newMillis;
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
                if (isDisplayable()) {
                    millis = System.currentTimeMillis();
                    setText("...");
                    timer.start();
                } else {
                    timer.stop();
                }
            }
        });
    }

}
