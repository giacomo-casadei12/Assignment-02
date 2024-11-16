package sap.ass02.gui.GUI;

import sap.ass02.gui.utils.Pair;
import sap.ass02.gui.utils.Triple;

import javax.swing.*;
import java.awt.*;
import java.io.Serial;
import java.util.Map;

/**
 * The main Panel that displays the bikes in their coordinates,
 * their statuses and battery charge as well as the current user.
 */
public class VisualiserPanel extends JPanel {
    private final long dx;
    private final long dy;
    private final EBikeApp app;
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Visualiser panel.
     *
     * @param w   the width in pixel
     * @param h   the height in pixel
     * @param app the EBikeApp
     */
    public VisualiserPanel(int w, int h, EBikeApp app){
        setSize(w,h);
        dx = w/2 - 20;
        dy = h/2 - 20;
        this.app = app;
    }

    @Override
    public void paint(Graphics g){
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        g2.clearRect(0,0,this.getWidth(),this.getHeight());

        Map<Integer, Triple<Pair<Integer, Integer>, Integer, String>> bhm = app.getEBikes();
        if (bhm != null) {
            for (Integer key : bhm.keySet()) {
                var p = bhm.get(key);
                int x0 = (int) (dx + p.first().first());
                int y0 = (int) (dy - p.first().second());
                g2.drawOval(x0, y0, 20, 20);
                g2.drawString(key.toString(), x0, y0 + 35);
                g2.drawString("(" + p.first().first() + "," + p.first().second() + ")", x0, y0 + 50);
                g2.drawString("Battery level: " + p.second() + " % ", x0, y0 + 65);
                g2.drawString("Status: " + p.third(), x0, y0 + 77);
            }
        }

        Triple<String, Integer, Boolean> hm = app.getUser();
        if (hm != null) {
            int userId = app.getUserId();
            var credit = hm.second();
            g2.drawRect(10, 20, 20, 20);
            g2.drawString("#" + userId + " " + hm.first() + " - credit: " + credit, 35, 35);
        }
    }

    /**
     * Refresh the panel.
     */
    public void refresh(){
        repaint();
    }
}
