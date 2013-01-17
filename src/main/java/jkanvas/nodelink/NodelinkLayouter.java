package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedLayouter;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimatedPosition;
import jkanvas.painter.AbstractRenderpass;
import jkanvas.painter.Renderpass;

/**
 * Paints a layouted node link diagram.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The type of nodes.
 */
public class NodelinkLayouter<T extends AnimatedPosition> implements AnimatedLayouter {

  /** The node realizer. */
  private NodeRealizer<T> nodeRealizer;

  /** The edge realizer. */
  private EdgeRealizer<T> edgeRealizer;

  /** The node render pass. */
  private final AbstractRenderpass nodePass;

  /** The edge render pass. */
  private final AbstractRenderpass edgePass;

  /** The view on the graph. */
  private final NodeLinkView<T> view;

  /**
   * Creates a node link painter.
   * 
   * @param view The view on the graph.
   */
  public NodelinkLayouter(final NodeLinkView<T> view) {
    this.view = view;
    nodePass = new AbstractRenderpass(false) {

      @Override
      public void render(final Graphics2D gfx, final KanvasContext ctx) {
        renderEdges(gfx, ctx);
      }

      @Override
      public double getOffsetX() {
        return NodelinkLayouter.this.getOffsetX();
      }

      @Override
      public double getOffsetY() {
        return NodelinkLayouter.this.getOffsetY();
      }

      @Override
      public Rectangle2D getBoundingBox() {
        return NodelinkLayouter.this.getBoundingBox();
      }

    };
    edgePass = new AbstractRenderpass(false) {

      @Override
      public void render(final Graphics2D gfx, final KanvasContext ctx) {
        renderNodes(gfx, ctx);
      }

      @Override
      public Rectangle2D getBoundingBox() {
        return NodelinkLayouter.this.getBoundingBox();
      }

      @Override
      public double getOffsetX() {
        return NodelinkLayouter.this.getOffsetX();
      }

      @Override
      public double getOffsetY() {
        return NodelinkLayouter.this.getOffsetY();
      }

    };
  }

  /**
   * Getter.
   * 
   * @return The node render pass.
   */
  public Renderpass getNodePass() {
    return nodePass;
  }

  /**
   * Getter.
   * 
   * @return The edge render pass.
   */
  public Renderpass getEdgePass() {
    return edgePass;
  }

  /**
   * Setter.
   * 
   * @param edgeRealizer The edge realizer.
   */
  public void setEdgeRealizer(final EdgeRealizer<T> edgeRealizer) {
    this.edgeRealizer = Objects.requireNonNull(edgeRealizer);
  }

  /**
   * Getter.
   * 
   * @return The current edge realizer.
   */
  public EdgeRealizer<T> getEdgeRealizer() {
    return edgeRealizer;
  }

  /**
   * Setter.
   * 
   * @param nodeRealizer The node realizer.
   */
  public void setNodeRealizer(final NodeRealizer<T> nodeRealizer) {
    this.nodeRealizer = Objects.requireNonNull(nodeRealizer);
  }

  /**
   * Getter.
   * 
   * @return The current node realizer.
   */
  public NodeRealizer<T> getNodeRealizer() {
    return nodeRealizer;
  }

  /**
   * Renders all edges.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  protected void renderEdges(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final EdgeRealizer<T> edgeRealizer = getEdgeRealizer();
    for(int i = 0; i < view.nodeCount(); ++i) {
      final T from = view.getNode(i);
      for(final int toId : view.edgesTo(i)) {
        final T to = view.getNode(toId);
        final Shape edgeShape = edgeRealizer.createLineShape(from, to);
        if(!edgeShape.intersects(visible)) {
          continue;
        }
        final Graphics2D g = (Graphics2D) gfx.create();
        edgeRealizer.drawLines(g, from, to);
        g.dispose();
      }
    }
  }

  /**
   * Renders all nodes.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  protected void renderNodes(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    for(final T node : view.nodes()) {
      final Shape nodeShape = nodeRealizer.createNodeShape(node);
      if(!nodeShape.intersects(visible)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      nodeRealizer.drawNode(g, node);
      g.dispose();
    }
  }

  /**
   * Finds a node at the given position.
   * 
   * @param pos The position.
   * @return The node or <code>null</code> if there is no node at the given
   *         position.
   */
  public T pick(final Point2D pos) {
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    for(final T node : view.nodes()) {
      final Shape shape = nodeRealizer.createNodeShape(node);
      if(shape.contains(pos)) return node;
    }
    return null;
  }

  /**
   * Converts a position to the real position in this layouter.
   * 
   * @param pos The position.
   * @return The real position.
   */
  public Point2D getPositionInLayouter(final Point2D pos) {
    return new Point2D.Double(pos.getX() - offX, pos.getY() - offY);
  }

  @Override
  public Iterable<? extends AnimatedPosition> getPositions() {
    return view.nodes();
  }

  /** The bounding box. */
  protected Rectangle2D bbox;

  /**
   * Getter.
   * 
   * @return The bounding box of this node-link diagram or <code>null</code>.
   */
  public Rectangle2D getBoundingBox() {
    return bbox;
  }

  /**
   * Setter.
   * 
   * @param bbox Sets the optional bounding box.
   */
  public void setBoundingBox(final Rectangle2D bbox) {
    this.bbox = bbox;
  }

  /** The x offset. */
  private double offX;

  /** The y offset. */
  private double offY;

  /**
   * Setter.
   * 
   * @param x The x offset.
   * @param y The y offset.
   */
  public void setOffset(final double x, final double y) {
    offX = x;
    offY = y;
  }

  /**
   * Getter.
   * 
   * @return The x offset.
   */
  public double getOffsetX() {
    return offX;
  }

  /**
   * Getter.
   * 
   * @return The y offset.
   */
  public double getOffsetY() {
    return offY;
  }

  /**
   * Adds this layouter to the given painter.
   * 
   * @param p The painter.
   */
  public void addToPainter(final AnimatedPainter p) {
    p.addPass(getNodePass());
    p.addPass(getEdgePass());
    p.addLayouter(this);
  }

}
