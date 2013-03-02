package jkanvas.examples;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationTiming;
import jkanvas.groups.LinearGroup;
import jkanvas.groups.LinearGroup.Alignment;
import jkanvas.nodelink.DefaultNodeRealizer;
import jkanvas.nodelink.NodeLinkRenderpass;
import jkanvas.nodelink.NodeLinkView;
import jkanvas.painter.FrameRateHUD;
import jkanvas.painter.KanvasButton;

/**
 * An example application for animated bubbles.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class LayoutedNodesMain {

  /** No constructor. */
  private LayoutedNodesMain() {
    throw new AssertionError();
  }

  private static final class Node extends AnimatedPosition {

    private final Color color;

    private final double radius;

    public Node(final Point2D pos, final Color color, final double radius) {
      super(pos);
      this.color = color;
      this.radius = radius;
    }

    public Color getColor() {
      return color;
    }

    public double getRadius() {
      return radius;
    }

  }

  private static final class NodeDrawer extends DefaultNodeRealizer<Node> {

    public NodeDrawer() {
      // nothing to do
    }

    @Override
    public double getRadius(final Node node) {
      return node.getRadius();
    }

    @Override
    public Color getColor(final Node node) {
      return node.getColor();
    }

  }

  private static final class NodeView implements NodeLinkView<Node> {

    private static float rnd(final Random r) {
      return Math.min(Math.abs((float) r.nextGaussian()), 1);
    }

    private final List<Node> nodes;
    private final double w;
    private final double h;
    private final double maxR;

    public NodeView(final int numNodes, final double w, final double h, final double maxR) {
      this.w = w;
      this.h = h;
      this.maxR = maxR;
      nodes = new ArrayList<>();
      addNodes(numNodes);
    }

    public void addNodes(final int count) {
      final Random r = new Random();
      for(int i = 0; i < count; ++i) {
        final double x = r.nextDouble() * (w - 2 * maxR) + maxR;
        final double y = r.nextDouble() * (h - 2 * maxR) + maxR;
        nodes.add(new Node(new Point2D.Double(x, y),
            Color.getHSBColor(r.nextFloat() * 360f, rnd(r), rnd(r)), rnd(r) * maxR));
      }
    }

    @Override
    public int nodeCount() {
      return nodes.size();
    }

    @Override
    public String getName(final int index) {
      return "" + index;
    }

    @Override
    public boolean areConnected(final int a, final int b) {
      return false;
    }

    @Override
    public boolean isDirected() {
      return false;
    }

    @Override
    public Node getNode(final int index) {
      return nodes.get(index);
    }

    @Override
    public Iterable<Node> nodes() {
      return nodes;
    }

    @Override
    public Iterable<Integer> edgesTo(final int from) {
      return Collections.emptyList();
    }

  }

  public static void main(final String[] args) {
    final int w = 800;
    final int h = 600;
    final int maxR = 40;
    final int numNodes = 50;
    final NodeView view = new NodeView(numNodes, w, h, maxR);
    final NodeLinkRenderpass<Node> nl = new NodeLinkRenderpass<>(view);
    nl.setNodeRealizer(new NodeDrawer());
    final AnimatedPainter p = new AnimatedPainter();
    final LinearGroup group = new LinearGroup(p, true, 10, AnimationTiming.NO_ANIMATION);
    group.setAlignment(Alignment.RIGHT);
    final Rectangle2D bbox = new Rectangle2D.Double(0, 0, 100, 40);
    group.addRenderpass(new KanvasButton("Random Layout", bbox) {

      @Override
      protected void onClick() {
        final Random r = new Random();
        for(final Node n : view.nodes()) {
          final double x = r.nextDouble() * (w - 2 * maxR) + maxR;
          final double y = r.nextDouble() * (h - 2 * maxR) + maxR;
          n.startAnimationTo(new Point2D.Double(x, y), AnimationTiming.SMOOTH);
        }
        p.forceNextFrame();
      }

    });
    group.addRenderpass(new KanvasButton("Add Nodes", bbox) {

      @Override
      protected void onClick() {
        view.addNodes(10);
      }

    });
    p.addPass(nl);
    p.addPass(group);
    // configure canvas
    final Canvas c = new Canvas(p, w, h);
    c.setBackground(Color.WHITE);
    c.setFrameRateDisplayer(new FrameRateHUD());
    p.addRefreshable(c);
    c.setPaintLock(p.getAnimationLock());
    final JFrame frame = new JFrame("Node-Link") {

      @Override
      public void dispose() {
        c.dispose();
        super.dispose();
      }

    };
    // setup frame
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }
}
