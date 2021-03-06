package gameClient;

import api.directed_weighted_graph;
import api.edge_data;
import api.geo_location;
import api.node_data;
import gameClient.util.Point3D;
import gameClient.util.Range;
import gameClient.util.Range2D;
import gameClient.util.Range2Range;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the game board view, and game occurrence.
 * All information about the shape of the graph, the location of the agents,
 * the location of the Pokemon and more, is taken from the arena and displayed in this class.
 */
public class GameView extends JPanel {

    private Arena _ar;
    private Range2Range _w2f;
    private static long _startT;

    public GameView() {
        super();
        _startT = System.currentTimeMillis();
    }



    public void set_ar(Arena ar) {
        _ar = ar;
        updateFrame();
    }

    /**
     * Updates the size and boundaries of the frame according to the given graph.
     */
    private void updateFrame() {
        Range rx = new Range(40, this.getWidth() - 40);
        Range ry = new Range(this.getHeight() - 40, 40);
        Range2D frame = new Range2D(rx, ry);
        directed_weighted_graph g = _ar.get_graph();
        _w2f = Arena.w2f(g, frame);
    }

    /**
     * Paint with double buffers, to create a smooth displayץ
     * @param g Graphics of this JPanel
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int w = getWidth();
        int h = getHeight();

        Image buffer_image = createImage(w, h);
        Graphics buffer_graphics = buffer_image.getGraphics();
        if (_ar != null) {
            paintComponents(buffer_graphics);
            g.drawImage(buffer_image, 0, 0, null);
        }
    }

    /**
     * Paint the current state of the game on Graphics g.
     * @param g Graphics of buffer image
     */
    @Override
    public void paintComponents(Graphics g) {
        drawGraph(g);
        drawPokemons(g, _ar, _w2f);
        drawAgents(g);
        drawTimeLine(g);
        updateFrame();
    }

    private void drawGraph(Graphics g) {
        directed_weighted_graph graph = _ar.get_graph();
        for (node_data i : graph.getV()) {
            drawNode(i, g);
            for (edge_data e : graph.getE(i.getKey())) {
                drawEdge(e, g);
            }
        }
    }

    private void drawNode(node_data n, Graphics g) {
        int radius = 6;
        geo_location pos = n.getLocation();
        geo_location fp = _w2f.world2frame(pos);
        nodeIcon(g, radius, fp);
        g.setColor(Color.BLACK);
        g.drawString("" + n.getKey(), (int) fp.x(), (int) fp.y() - 2 * radius);
    }

    private void drawEdge(edge_data e, Graphics g) {
        // get location info
        directed_weighted_graph gg = _ar.get_graph();
        geo_location s = gg.getNode(e.getSrc()).getLocation();
        geo_location d = gg.getNode(e.getDest()).getLocation();
        geo_location s0 = _w2f.world2frame(s);
        geo_location d0 = _w2f.world2frame(d);

        // draw edge line
        g.setColor(new Color(0x000099));
//        g.drawLine((int) s0.x(), (int) s0.y(), (int) d0.x(), (int) d0.y());
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.draw(new Line2D.Float((int) s0.x(), (int) s0.y(), (int) d0.x(), (int) d0.y()));

        // print weight
        g.setColor(Color.black);
        g.setFont(new Font("Courier", Font.PLAIN, 13));
        String t = String.format("%.2f", e.getWeight());
        int x = (int) ((s0.x() + d0.x()) / 2);
        int y = (int) ((s0.y() + d0.y()) / 2) - 3;
        if (e.getSrc() < e.getDest()) y += 15;
//        g.drawString(t, x, y);
    }

    protected void nodeIcon(Graphics g, int radius, geo_location fp) {
        g.setColor(new Color(0x000099));
        g.fillOval((int) fp.x() - radius, (int) fp.y() - radius, 2 * radius, 2 * radius);
    }

    protected void drawPokemons(Graphics g, Arena ar, Range2Range _w2f) {
        List<Pokemon> fs = new ArrayList<>(_ar.getPokemons());
        if (fs.isEmpty())
            return;
        for (Pokemon f : fs) {
            if (f == null) continue;
            Point3D c = f.get_pos();
            int radius = 10;
            g.setColor(Color.green);
            if (f.get_type() < 0) {
                g.setColor(Color.orange);
            }
            if (c != null) {
                geo_location fp = _w2f.world2frame(c);
                pokIcon(g, radius, fp, 0);
                g.setColor(Color.BLACK);
                g.setFont(new Font(null, Font.BOLD, 12));
                g.drawString("" + (int) f.get_value(), (int) fp.x(), (int) fp.y() + 2);
            }
        }
    }

    protected void pokIcon(Graphics g, int radius, geo_location fp, int flag) {
        g.fillOval((int) fp.x() - radius, (int) fp.y() - radius, 2 * radius, 2 * radius);

    }

    private void drawAgents(Graphics g) {
        List<Agent> rs = _ar.getAgents();
        for (Agent a : rs) {
            geo_location loc = a.getPos();
            int r = 8;
            geo_location fp = _w2f.world2frame(loc);
            agentIcon(g, r, fp, a.getId());
            String v = (int) a.getValue() + "";
            g.setColor(Color.BLACK);
            g.setFont(new Font(null, Font.BOLD, 12));
            g.drawString(v, (int) fp.x() + 10, (int) fp.y() + 10);
        }
    }

    protected void agentIcon(Graphics g, int r, geo_location fp, int id) {
        g.setColor(new Color(150, 60, 90));
        g.fillOval((int) fp.x() - r, (int) fp.y() - r, 2 * r, 2 * r);
    }

    private void drawTimeLine(Graphics g) {
        g.setColor(new Color(0xCD1818));
        double ts = (double) _ar.get_timeStart();
        long curT = System.currentTimeMillis()-_startT;
        double dt = curT / ts;

        double w = getWidth();
        g.fillRoundRect(0, 0, (int) (w * dt), 10, 10, 10);
    }
    public static void set_startT(long _startT1) {
    _startT = _startT1;
    }
}