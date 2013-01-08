package jkanvas.examples;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.EdgeRealizer;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.NodelinkPainter;
import jkanvas.util.Interpolator;
import jkanvas.util.VecUtil;

/**
 * A small node-link example application.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class NodelinkMain extends NodelinkPainter<AnimatedPosition> implements
NodeRealizer<AnimatedPosition>, EdgeRealizer<AnimatedPosition> {

  /** The color for no selection. */
  private static final Color NO_SEL = new Color(247, 247, 247);

  /** The color for primary selection. */
  private static final Color PRIM_SEL = new Color(202, 0, 32);

  /** The color for secondary selection. */
  private static final Color SEC_SEL = new Color(5, 113, 176);

  /** The primary selection. */
  private AnimatedPosition primSel;

  /** The start x position of the current drag. */
  private double startX;

  /** The start y position of the current drag. */
  private double startY;

  /** The secondary selection. */
  private AnimatedPosition secSel;

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    final AnimatedPosition n = pick(p);
    if(!SwingUtilities.isRightMouseButton(e)) return false;
    if(n == null) {
      addNode(new AnimatedPosition(p));
    } else {
      if(secSel != null) {
        addEdge(secSel, n);
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
    final AnimatedPosition n = pick(p);
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

  /** A stroke with width one. */
  private final BasicStroke stroke = new BasicStroke(1f);

  /** The default node radius. */
  public static final double RADIUS = 20.0;

  /** The node radius. */
  private final double radius = RADIUS;

  @Override
  public Shape createNodeShape(final AnimatedPosition node) {
    return createEllipse(node, radius + stroke.getLineWidth() * 0.5);
  }

  /**
   * Creates a circle with the given radius.
   * 
   * @param node The node providing the position.
   * @param r The radius.
   * @return The circle.
   */
  private static Ellipse2D createEllipse(final AnimatedPosition node, final double r) {
    final double r2 = r * 2;
    return new Ellipse2D.Double(node.getX() - r, node.getY() - r, r2, r2);
  }

  @Override
  public void drawNode(final Graphics2D g, final AnimatedPosition node) {
    g.setColor(primSel == node ? PRIM_SEL : (secSel == node ? SEC_SEL : NO_SEL));
    final Shape s = createEllipse(node, radius);
    g.fill(s);
    g.setColor(Color.BLACK);
    g.fill(stroke.createStrokedShape(s));
  }

  @Override
  public boolean isDirected() {
    return false;
  }

  /**
   * Creates a line with a given width without using a stroke.
   * 
   * @param x1 The first x coordinate.
   * @param y1 The first y coordinate.
   * @param x2 The second x coordinate.
   * @param y2 The second y coordinate.
   * @param width The width of the line.
   * @return The shape of the line.
   */
  private static Shape createLine(final double x1, final double y1, final double x2,
      final double y2, final double width) {
    final Point2D ortho = VecUtil.setLength(new Point2D.Double(y1 - y2, x2 - x1),
        width * 0.5);
    final GeneralPath gp = new GeneralPath();
    gp.moveTo(x1 + ortho.getX(), y1 + ortho.getY());
    gp.lineTo(x2 + ortho.getX(), y2 + ortho.getY());
    gp.lineTo(x2 - ortho.getX(), y2 - ortho.getY());
    gp.lineTo(x1 - ortho.getX(), y1 - ortho.getY());
    gp.closePath();
    return gp;
  }

  @Override
  public Shape createLineShape(final AnimatedPosition from, final AnimatedPosition to) {
    return createLine(from.getX(), from.getY(), to.getX(), to.getY(), 1.0);
  }

  @Override
  public void drawLines(final Graphics2D g, final AnimatedPosition from,
      final AnimatedPosition to) {
    g.setColor(Color.BLACK);
    g.fill(createLineShape(from, to));
  }

  /**
   * Starts the example application.
   * 
   * @param args No args.
   */
  public static void main(final String[] args) {
    final int w = 800;
    final int h = 600;
    final int nodes = 20;
    final int edges = 100;
    final NodelinkMain p = new NodelinkMain();
    p.setEdgeRealizer(p);
    p.setNodeRealizer(p);
    final Random r = new Random();
    for(int i = 0; i < nodes; ++i) {
      p.addNode(new AnimatedPosition(RADIUS + r.nextDouble() * (w - 2 * RADIUS),
          RADIUS + r.nextDouble() * (h - 2 * RADIUS)));
    }
    for(int i = 0; i < edges; ++i) {
      p.addEdge(r.nextInt(nodes), r.nextInt(nodes));
    }
    final Canvas c = new Canvas(p, w, h);
    c.setBackground(Color.WHITE);
    p.addRefreshable(c);
    final JFrame frame = new JFrame("Nodelink") {

      @Override
      public void dispose() {
        p.dispose();
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
        final int count = p.nodeCount();
        final double step = 2 * Math.PI / count;
        double angle = 0;
        for(int i = 0; i < count; ++i) {
          final double x = rect.getCenterX() + Math.sin(angle) * r;
          final double y = rect.getCenterY() + Math.cos(angle) * r;
          p.getNode(i).startAnimationTo(new Point2D.Double(x, y),
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
