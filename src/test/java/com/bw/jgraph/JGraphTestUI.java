package com.bw.jgraph;

import com.bw.jgraph.graph.*;
import com.bw.jgraph.ui.*;
import com.bw.jgraph.ui.impl.*;
import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.util.SystemInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class JGraphTestUI extends JFrame {

    GraphPanel gpanel;

    static Node node(String text)
    {
        return GraphUtil.createTextNode(text);
    }

    public JGraphTestUI() {

        JPanel main = new JPanel(new BorderLayout());

        gpanel = new GraphPanel();

        Layout layout = new TreeLayout(new TreeRectangleGeometry());

        VisualSettings settings = new VisualSettings();
        DecoratorNodeVisual v = new DecoratorNodeVisual(new NodeLabelVisual(layout, settings));
        EdgeVisualBase ev = new EdgeVisualBase(layout, settings);

        gpanel.setNodeVisual(v);
        gpanel.setEdgeVisual(ev);

        Graph g = gpanel.getGraph();


        Node root = node("Grandson");
        Node son = node("<html><b>Son</b><br><p style='color:Blue;'>Brotie</p></html>");
        Node father = node("Father");
        Node mother = node("<html><b>Mother</b><br><p style='color:Blue;'>Gitte</p></html>");

        g.addEdge(root, son);
        g.addEdge(son, father);
        g.addEdge(son, mother);
        g.addEdge(mother, node("<html><b>Grandmother</b><br><p style='color:Blue;'>Ursel</p></html>"));
        g.addEdge(mother, node("<html><b>Grandfather</b><br><p style='color:Blue;'>Hugo</p></html>"));
        g.addEdge(father, node("Grandmother"));
        g.addEdge(father, node("Grandfather"));
        v.addDecorator(son, new CloudNodeDecorator(v.getGeometry()));


        JScrollPane graphPanel = new JScrollPane(gpanel);

        main.add(BorderLayout.CENTER, graphPanel);

        JPanel status = new JPanel(new BorderLayout());
        status.add( BorderLayout.WEST, new FpsStatus(gpanel));
        main.add(BorderLayout.SOUTH, status);

        v.getGeometry().beginUpdate();
        root = node("Grandson");
        generate( root, 3, 10, v);
        g.setRoot(root);

        v.getGeometry().endUpdate();

        main.setPreferredSize(new Dimension(400,300));
        setContentPane(main);

        JMenuBar menu = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menu.add( fileMenu );

        fileMenu.add(new AbstractAction("Generate") {
            @Override
            public void actionPerformed(ActionEvent e) {
                Node root = node("A ROOT BOX");
                generate( root, 3, 10, v);

                v.getGeometry().beginUpdate();
                g.setRoot(root);
                v.getGeometry().endUpdate();
                SwingUtilities.invokeLater(() -> {
                    repaint();
                });
            }
        });

        setJMenuBar(menu);

    }

    static void generate(Node root, int depth, int width, NodeVisual visual) {
        for (int i=0 ; i<width ; i++) {
            Node child = node( (depth+1)+"."+(i+1));
            Edge e = new Edge(root, child);
            visual.expand(e, true);
            if ( depth > 0)
                generate( child, depth-1, width, visual);
        }
    }


    public static void main(String[] args) {

        FlatRobotoFont.installLazy();
        FlatLaf.setPreferredFontFamily( FlatRobotoFont.FAMILY );
        FlatLaf.setPreferredLightFontFamily( FlatRobotoFont.FAMILY_LIGHT );
        FlatLaf.setPreferredSemiboldFontFamily( FlatRobotoFont.FAMILY_SEMIBOLD );

        if( SystemInfo.isMacOS ) {
            System.setProperty( "apple.laf.useScreenMenuBar", "true" );
            System.setProperty( "apple.awt.application.name", "FlatLaf Demo" );
            System.setProperty( "apple.awt.application.appearance", "system" );
        }

        if( SystemInfo.isLinux ) {
            JFrame.setDefaultLookAndFeelDecorated( true );
            JDialog.setDefaultLookAndFeelDecorated( true );
        }

        FlatDarculaLaf.setup();

        JGraphTestUI ui = new JGraphTestUI();
        ui.pack();
        ui.setLocationByPlatform(true);
        ui.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ui.setVisible(true);
    }
}
