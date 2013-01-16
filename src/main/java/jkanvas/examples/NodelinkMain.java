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
import java.util.Objects;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.EdgeRealizer;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.NodelinkLayouter;
import jkanvas.nodelink.SimpleNodeLinkView;
import jkanvas.util.Interpolator;
import jkanvas.util.PaintUtil;

/**
 * A small node-link example application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class NodelinkMain extends AnimatedPainter {

  /** The default node radius. */
  public static final double RADIUS = 20.0;

  /** The color for no selection. */
  private static final Color NO_SEL = new Color(247, 247, 247);

  /** The color for primary selection. */
  private static final Color PRIM_SEL = new Color(202, 0, 32);

  /** The color for secondary selection. */
  private static final Color SEC_SEL = new Color(5, 113, 176);

  /** The node link layouter. */
  private final NodelinkLayouter<AnimatedPosition> nodelink;

  /** A view on the graph. */
  private final SimpleNodeLinkView<AnimatedPosition> view;

  /**
   * Creates a node link diagram.
   * 
   * @param nodelink The layouter.
   * @param view The view on the graph.
   */
  public NodelinkMain(final NodelinkLayouter<AnimatedPosition> nodelink,
      final SimpleNodeLinkView<AnimatedPosition> view) {
    this.view = view;
    this.nodelink = Objects.requireNonNull(nodelink);
    nodelink.setEdgeRealizer(new EdgeRealizer<AnimatedPosition>() {

      @Override
      public Shape createLineShape(final AnimatedPosition from, final AnimatedPosition to) {
        return PaintUtil.createLine(from.getX(), from.getY(), to.getX(), to.getY(), 1.0);
      }

      @Override
      public void drawLines(final Graphics2D g, final AnimatedPosition from,
          final AnimatedPosition to) {
        g.setColor(Color.BLACK);
        g.fill(createLineShape(from, to));
      }

    });
    nodelink.setNodeRealizer(new NodeRealizer<AnimatedPosition>() {

      /** A stroke with width one. */
      private final BasicStroke stroke = new BasicStroke(1f);

      /** The node radius. */
      private final double radius = RADIUS;

      @Override
      public Shape createNodeShape(final AnimatedPosition node) {
        return PaintUtil.createEllipse(node.getX(), node.getY(),
            radius + stroke.getLineWidth() * 0.5);
      }

      @Override
      public void drawNode(final Graphics2D g, final AnimatedPosition node) {
        g.setColor(getNodeColor(node));
        final Shape s = PaintUtil.createEllipse(node.getX(), node.getY(), radius);
        g.fill(s);
        g.setColor(Color.BLACK);
        g.fill(stroke.createStrokedShape(s));
      }

    });
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
    final Point2D p = nodelink.getRealPosition(pos);
    final AnimatedPosition n = nodelink.pick(p);
    if(!SwingUtilities.isRightMouseButton(e)) return false;
    if(n == null) {
      view.addNode(new AnimatedPosition(p));
    } else {
      if(secSel != null) {
        view.addEdge(secSel, n);
        secSel = null;
      } else {
        secSel = n;
      }
    }
    quickRefresh();
    return true;
  }

  @Override
  public boolean acceptDrag(final Point2D p) {
    final AnimatedPosition n = nodelink.pick(nodelink.getRealPosition(p));
    if(n == null) return false;
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
    quickRefresh();
  }

  @Override
  public void endDrag(final Point2D start, final Point2D cur, final double dx,
      final double dy) {
    drag(start, cur, dx, dy);
    primSel = null;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    final NodeRealizer<AnimatedPosition> n = nodelink.getNodeRealizer();
    Rectangle2D bbox = null;
    for(final AnimatedPosition p : nodelink.getPositions()) {
      final Shape shape = n.createNodeShape(p);
      final Rectangle2D b = shape.getBounds2D();
      if(bbox == null) {
        bbox = b;
      } else {
        bbox.add(b);
      }
    }
    return bbox;
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
    final Random r = new Random();
    for(int i = 0; i < nodes; ++i) {
      view.addNode(new AnimatedPosition(RADIUS + r.nextDouble() * (w - 2 * RADIUS),
          RADIUS + r.nextDouble() * (h - 2 * RADIUS)));
    }
    for(int i = 0; i < edges; ++i) {
      view.addEdge(r.nextInt(nodes), r.nextInt(nodes));
    }
    final NodelinkLayouter<AnimatedPosition> nodelink = new NodelinkLayouter<>(view);
    final NodelinkMain p = new NodelinkMain(nodelink, view);
    nodelink.setBoundingBox(p.getBoundingBox());
    p.addLayouter(nodelink);
    p.addPass(nodelink.getNodePass());
    p.addPass(nodelink.getEdgePass());
    final Canvas c = new Canvas(p, w, h);
    c.setBackground(Color.WHITE);
    p.addRefreshable(c);
    final JFrame frame = new JFrame("Nodelink") {

      @Override
      public void dispose() {
        c.dispose();
        super.dispose();
      }

    };
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
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

}
