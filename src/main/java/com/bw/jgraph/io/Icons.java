package com.bw.jgraph.io;

import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.ui.ShapeIcon;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Icons {

    private static Map<String, Icon> iconCache = new HashMap<>();


    public static synchronized Icon get(String name) {
        Icon ic = iconCache.get(name);
        if ( ic == null) {
            InputStream is = null;
            try {
                is = Icons.class.getResourceAsStream("/icons/"+name+".svg");
                if (is != null ) {
                    ic = new ShapeIcon(SVGConverter.convert(is));
                } else {
                    is = Icons.class.getResourceAsStream("/icons/" + name + ".png");
                    if (is != null) {
                        ic = new ImageIcon(ImageIO.read(is));
                    } else
                        ic = UIManager.getIcon("OptionPane.errorIcon");
                }
                iconCache.put(name, ic );
                return ic;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
                // Possible e.g. in headless environment.
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
