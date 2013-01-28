package jkanvas.examples;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.EdgeRealizer;
import jkanvas.nodelink.NodeLinkRenderpass;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.SimpleNodeLinkView;
import jkanvas.painter.FrameRateHUD;
import jkanvas.util.Interpolator;
import jkanvas.util.PaintUtil;

/**
 * A small node-link example application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class NodeLinkMain extends NodeLinkRenderpass<AnimatedPosition> {

  /** The default node radius. */
  public static final double RADIUS = 20.0;

  /** The color for no selection. */
  private static final Color NO_SEL = new Color(247, 247, 247);

  /** The color for primary selection. */
  private static final Color PRIM_SEL = new Color(202, 0, 32);

  /** The color for secondary selection. */
  private static final Color SEC_SEL = new Color(5, 113, 176);

  /** The simple view in order to add nodes dynamically. */
  private final SimpleNodeLinkView<AnimatedPosition> simpleView;

  /**
   * Creates a node link diagram.
   * 
   * @param view The view on the graph.
   */
  public NodeLinkMain(final SimpleNodeLinkView<AnimatedPosition> view) {
    super(view);
    simpleView = view;
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The color of the node.
   */
  public Color getNodeColor(final AnimatedPosition node) {
    return primSel == node ? PRIM_SEL : (secSel == node ? SEC_SEL : NO_SEL);
  }

  /** The primary selection. */
  private AnimatedPosition primSel;

  /** The start x position of the current drag. */
  private double startX;

  /** The start y position of the current drag. */
  private double startY;

  /** The secondary selection. */
  private AnimatedPosition secSel;

  @Override
  public boolean click(final Point2D pos, final MouseEvent e) {
    if(!SwingUtilities.isRightMouseButton(e)) return false;
    final AnimatedPosition n = pick(pos);
    if(n == null) {
      // no node selected -- add new node
      simpleView.addNode(new AnimatedPosition(pos));
      return true;
    }
    if(secSel != null) {
      // when selecting a second node create edge
      simpleView.addEdge(secSel, n);
      secSel = null;
      return true;
    }
    // select node
    secSel = n;
    return true;
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    if(!SwingUtilities.isLeftMouseButton(e)) return false;
    final AnimatedPosition n = pick(p);
    if(n == null) return false;
    // initialize node dragging
    primSel = n;
    primSel.clearAnimation();
    startX = primSel.getX();
    startY = primSel.getY();
    return true;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    primSel.setPosition(startX + dx, startY + dy);
  }

  @Override
  public void endDrag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    drag(start, cur, dx, dy);
    primSel = null;
  }

  @Override
  public void setBoundingBox(final Rectangle2D bbox) {
    // super class supports setting bounding box -- since we compute the
    // bounding box by ourselves this method is ignored
    throw new UnsupportedOperationException();
  }

  @Override
  public Rectangle2D getBoundingBox() {
    final NodeRealizer<AnimatedPosition> n = getNodeRealizer();
    Rectangle2D bbox = null;
    for(final AnimatedPosition p : getPositions()) {
      final double x = p.getX();
      final double y = p.getY();
      final Shape shape = n.createNodeShape(p, x, y);
      final Rectangle2D b = shape.getBounds2D();
      if(bbox == null) {
        bbox = b;
      } else {
        bbox.add(b);
      }
      // include the end of the animation in the bounding box
      if(p.inAnimation()) {
        final double px = p.getPredictX();
        final double py = p.getPredictY();
        final Shape endShape = n.createNodeShape(p, px, py);
        bbox.add(endShape.getBounds2D());
      }
    }
    return bbox;
  }

  /**
   * Fills the given graph.
   * 
   * @param view The view on the graph.
   * @param width The allowed width.
   * @param height The allowed height.
   * @param nodes The number of nodes.
   * @param edges The number of edges.
   */
  private static void fillGraph(final SimpleNodeLinkView<AnimatedPosition> view,
      final int width, final int height, final int nodes, final int edges) {
    final Random rnd = new Random();
    for(int i = 0; i < nodes; ++i) {
      view.addNode(new AnimatedPosition(RADIUS + rnd.nextDouble() * (width - 2 * RADIUS),
          RADIUS + rnd.nextDouble() * (height - 2 * RADIUS)));
    }
    for(int i = 0; i < edges; ++i) {
      view.addEdge(rnd.nextInt(nodes), rnd.nextInt(nodes));
    }
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    final int w = 800;
    final int h = 600;
    final int nodes = 20;
    final int edges = 100;
    final SimpleNodeLinkView<AnimatedPosition> view = new SimpleNodeLinkView<>();
    fillGraph(view, w, h, nodes, edges);
    final AnimatedPainter p = new AnimatedPainter();
    final NodeLinkMain r = new NodeLinkMain(view);
    r.setEdgeRealizer(new EdgeRealizer<AnimatedPosition>() {

      @Override
      public Shape createLineShape(final AnimatedPosition from, final AnimatedPosition to) {
        return PaintUtil.createLine(from.getX(), from.getY(), to.getX(), to.getY(), 1.0);
      }

      @Override
      public void drawLines(final Graphics2D g, final Shape edgeShape,
          final AnimatedPosition from, final AnimatedPosition to) {
        g.setColor(Color.BLACK);
        g.fill(edgeShape);
      }

    });
    r.setNodeRealizer(new NodeRealizer<AnimatedPosition>() {

      /** A stroke with width one. */
      private final BasicStroke stroke = new BasicStroke(1f);

      /** The node radius. */
      private final double radius = RADIUS;

      @Override
      public Shape createNodeShape(final AnimatedPosition node,
          final double x, final double y) {
        return PaintUtil.createEllipse(x, y, radius + stroke.getLineWidth() * 0.5);
      }

      @Override
      public void drawNode(final Graphics2D g, final AnimatedPosition node) {
        g.setColor(r.getNodeColor(node));
        final Shape s = PaintUtil.createEllipse(node.getX(), node.getY(), radius);
        g.fill(s);
        g.setColor(Color.BLACK);
        g.fill(stroke.createStrokedShape(s));
      }

    });
    r.addToPainter(p);
    // configure Canvas
    final Canvas c = new Canvas(p, w, h);
    c.setBackground(Color.WHITE);
    c.setFrameRateDisplayer(new FrameRateHUD());
    p.addRefreshable(c);
    c.setPaintLock(p.getAnimationLock());
    final JFrame frame = new JFrame("Node-Link") {

      @Override
      public void dispose() {
        // The Canvas also disposes the animator, which terminates the animation
        // daemon
        c.dispose();
        super.dispose();
      }

    };
    // add actions
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    c.addAction(KeyEvent.VK_R, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final Rectangle2D rect = c.getVisibleCanvas();
        final double w = rect.getWidth();
        final double h = rect.getHeight();
        final double r = Math.min(w, h) / 2 - RADIUS;
        final int count = view.nodeCount();
        final double step = 2 * Math.PI / count;
        double angle = 0;
        for(int i = 0; i < count; ++i) {
          final double x = rect.getCenterX() + Math.sin(angle) * r;
          final double y = rect.getCenterY() + Math.cos(angle) * r;
          view.getNode(i).startAnimationTo(new Point2D.Double(x, y),
              Interpolator.SMOOTH, AnimatedPosition.NORMAL);
          angle += step;
        }
      }

    });
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

}
