package jkanvas.examples;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.io.IOException;
import java.util.Random;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationTiming;
import jkanvas.io.json.JSONManager;
import jkanvas.io.json.JSONSetup;
import jkanvas.nodelink.DefaultEdgeRealizer;
import jkanvas.nodelink.DefaultNodeRealizer;
import jkanvas.nodelink.NodeLinkRenderpass;
import jkanvas.nodelink.NodeRealizer;
import jkanvas.nodelink.SimpleNodeLinkView;
import jkanvas.nodelink.layout.AbstractLayouter;
import jkanvas.nodelink.layout.BouncingLayouter;
import jkanvas.nodelink.layout.CircleLayouter;
import jkanvas.nodelink.layout.ForceDirectedLayouter;
import jkanvas.nodelink.layout.RandomLayouter;
import jkanvas.nodelink.layout.SimpleLayoutedView;
import jkanvas.optional.MDSLayouter;
import jkanvas.optional.MDSProjector;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.util.Resource;

/**
 * A small node-link example application.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class NodeLinkMain extends NodeLinkRenderpass<AnimatedPosition> {

  /** The color for no selection. */
  private static final Color NO_SEL = new Color(247, 247, 247);

  /** The color for primary selection. */
  private static final Color PRIM_SEL = new Color(202, 0, 32);

  /** The color for secondary selection. */
  private static final Color SEC_SEL = new Color(5, 113, 176);

  /** The simple view in order to add nodes dynamically. */
  private final SimpleLayoutedView<AnimatedPosition> simpleView;

  /**
   * Creates a node-link diagram.
   * 
   * @param view The view on the graph.
   */
  public NodeLinkMain(final SimpleLayoutedView<AnimatedPosition> view) {
    super(view, new Rectangle2D.Double(0, 0, 800, 600));
    simpleView = view;
    setIds("nl");
  }

  @Override
  protected void processMessage(final String msg) {
    switch(msg) {
      case "random":
        simpleView.setLayouter(new RandomLayouter<>());
        break;
      case "circle":
        simpleView.setLayouter(new CircleLayouter<>());
        break;
      case "force":
        simpleView.setLayouter(new ForceDirectedLayouter<>());
        break;
      case "bounce":
        simpleView.setLayouter(new BouncingLayouter<>());
        break;
      case "mds": {
        // estimate node size
        final Shape s = getNodeRealizer().createNodeShape(view.getNode(0), 0, 0);
        final double radius = s.getBounds2D().getWidth() * 0.5;
        simpleView.setLayouter(new MDSLayouter<>(radius));
        break;
      }
    }
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
  public boolean click(final Camera cam, final Point2D pos, final MouseEvent e) {
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
    } else {
      // select node
      secSel = n;
    }
    return true;
  }

  @Override
  public String getTooltip(final Point2D p) {
    final AnimatedPosition n = pick(p);
    if(n == null) return null;
    return "x: " + n.getX() + " y: " + n.getY();
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
  public void getBoundingBox(final RectangularShape bbox) {
    final NodeRealizer<AnimatedPosition> n = getNodeRealizer();
    bbox.setFrame(0, 0, 0, 0);
    for(final AnimatedPosition p : view.nodes()) {
      final double x = p.getX();
      final double y = p.getY();
      final Shape shape = n.createNodeShape(p, x, y);
      final Rectangle2D b = shape.getBounds2D();
      RenderpassPainter.addToRect(bbox, b);
      // include the end of the animation in the bounding box
      if(p.inAnimation()) {
        final double px = p.getPredictX();
        final double py = p.getPredictY();
        final Shape endShape = n.createNodeShape(p, px, py);
        RenderpassPainter.addToRect(bbox, endShape.getBounds2D());
      }
    }
  }

  /**
   * Fills the given graph.
   * 
   * @param view The view on the graph.
   * @param nodes The number of nodes.
   * @param edges The number of edges.
   */
  private static void fillGraph(final SimpleNodeLinkView<AnimatedPosition> view,
      final int nodes, final int edges) {
    for(int i = 0; i < nodes; ++i) {
      view.addNode(new AnimatedPosition(0, 0));
    }
    final int numMain = 5;
    final int factorMore = 10000;
    final Random rnd = new Random();
    for(int i = 0; i < edges; ++i) {
      final int start = rnd.nextInt(nodes);
      int end = rnd.nextInt(nodes + factorMore * numMain);
      if(end >= nodes) {
        end = end % numMain;
      }
      view.addEdge(start, end);
    }
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   * @throws IOException I/O Exception.
   */
  public static void main(final String[] args) throws IOException {
    // Canvas.DEBUG_BBOX = true;
    final int nodes = 20;
    final int edges = 100;
    final JSONManager mng = new JSONManager();
    JSONSetup.setupCanvas("Node-Link", mng, Resource.getFor("nodelink.json"), true);
    final Canvas c = mng.getForId("canvas", Canvas.class);
    final SimpleLayoutedView<AnimatedPosition> view = new SimpleLayoutedView<>(c, false);
    fillGraph(view, nodes, edges);
    final NodeLinkMain r = new NodeLinkMain(view);
    r.setEdgeRealizer(new DefaultEdgeRealizer<>());
    // TODO #43 -- Java 8 simplification
    r.setNodeRealizer(new DefaultNodeRealizer<AnimatedPosition>() {

      @Override
      public Color getColor(final AnimatedPosition node) {
        return r.getNodeColor(node);
      }

    });
    final RenderpassPainter p = mng.getForId("painter", RenderpassPainter.class);
    p.addPass(r);
    final AbstractLayouter<AnimatedPosition> layout;
    if(MDSProjector.hasMDSJ()) {
      layout = new MDSLayouter<>(DefaultNodeRealizer.RADIUS);
      c.addMessageAction(KeyEvent.VK_5, "nl#mds");
      final SimpleTextHUD info = mng.getForId("info", SimpleTextHUD.class);
      info.insertLine(4, "5: MDS Layout");
    } else {
      layout = new RandomLayouter<>();
    }
    layout.setTiming(AnimationTiming.NO_ANIMATION);
    view.setLayouter(layout);
  }

}
