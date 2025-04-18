package com.bw.jgraph.ui;

import javax.swing.*;
import java.awt.event.HierarchyEvent;

/**
 * Label to show the Fframe Per Second, measured by using delta of graph-panels {@link GraphPanel#paintCount_}.
 * The counter is set back on each measurement, so their can be only ONE such label for a graph-panel.
 */
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
