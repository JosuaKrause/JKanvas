package jkanvas;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.EdgeRealizer;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.NodelinkPainter;
import jkanvas.util.VecUtil;

/**
 * @author Joschi <josua.krause@googlemail.com>
 */
public class NodelinkMain extends NodelinkPainter<AnimatedPosition> implements
NodeRealizer<AnimatedPosition>, EdgeRealizer<AnimatedPosition> {

  /** The color for no selection. */
  private static final Color NO_SEL = new Color(247, 247, 247);

  /** The color for primary selection. */
  private static final Color PRIM_SEL = new Color(202, 0, 32);

  /** The color for secondary selection. */
  private static final Color SEC_SEL = new Color(5, 113, 176);

  private AnimatedPosition primSel;

  private double startX;

  private double startY;

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

  private final BasicStroke stroke = new BasicStroke(1f);

  private final double radius = 20.0;

  @Override
  public Shape createNodeShape(final AnimatedPosition node) {
    return createEllipse(node, radius + stroke.getLineWidth() * 0.5);
  }

  private Ellipse2D createEllipse(final AnimatedPosition node, final double r) {
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
  private Shape createLine(final double x1, final double y1, final double x2,
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
    g.draw(createLineShape(from, to));
  }

  public static void main(final String[] args) {
    final NodelinkMain p = new NodelinkMain();
    p.setEdgeRealizer(p);
    p.setNodeRealizer(p);
    final Canvas c = new Canvas(p, 800, 600);
    c.setBackground(Color.WHITE);
    p.addRefreshable(c);
    final JFrame frame = new JFrame("Nodelink") {

      @Override
      public void dispose() {
        p.dispose();
        super.dispose();
      }

    };
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

}
