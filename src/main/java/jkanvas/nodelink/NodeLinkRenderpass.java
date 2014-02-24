package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationList;
import jkanvas.nodelink.layout.LayoutedView;
import jkanvas.painter.Renderpass;

/**
 * Paints a layouted node-link diagram.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type of nodes.
 */
public class NodeLinkRenderpass<T extends AnimatedPosition> extends Renderpass {

  /** The bounding box. */
  private final Rectangle2D bbox;

  /** The layout view if any. */
  private final LayoutedView<T> layout;

  /** The view on the graph. */
  protected final NodeLinkView<T> view;

  /** The node realizer. */
  private NodeRealizer<T> nodeRealizer;

  /** The edge realizer. */
  private EdgeRealizer<T> edgeRealizer;

  /** The animation list. */
  private AnimationList list;

  /**
   * Creates a node-link painter.
   * 
   * @param view The view on the graph.
   */
  public NodeLinkRenderpass(final LayoutedView<T> view) {
    this.view = Objects.requireNonNull(view);
    layout = view;
    bbox = null;
  }

  /**
   * Creates a node-link painter.
   * 
   * @param view The view on the graph.
   * @param bbox The bounding box of the render pass.
   */
  public NodeLinkRenderpass(final NodeLinkView<T> view, final Rectangle2D bbox) {
    this.view = Objects.requireNonNull(view);
    this.bbox = Objects.requireNonNull(bbox);
    layout = null;
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

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    renderEdges(gfx, ctx);
    renderNodes(gfx, ctx);
  }

  /** The node set. This set is used to detect whether a node is new. */
  private final Set<T> lastNodes = Collections.newSetFromMap(new IdentityHashMap<T, Boolean>());

  /**
   * Renders all nodes.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  private void renderNodes(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    int count = 0;
    for(final T node : view.nodes()) {
      ++count;
      // automatically adds new nodes to the animation list
      // this needs only to be done in the draw method
      if(!lastNodes.contains(node)) {
        list.addAnimated(node);
        lastNodes.add(node);
      }
      final double x = node.getX();
      final double y = node.getY();
      final Shape nodeShape = nodeRealizer.createNodeShape(node, x, y);
      if(!nodeShape.intersects(visible)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      nodeRealizer.drawNode(g, node);
      g.dispose();
    }
    if(count < lastNodes.size()) {
      // clear set when nodes got removed
      lastNodes.clear();
    }
  }

  /**
   * Renders all edges.
   * 
   * @param gfx The graphics context.
   * @param ctx The canvas context.
   */
  private void renderEdges(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final EdgeRealizer<T> edgeRealizer = getEdgeRealizer();
    for(int i = 0; i < view.nodeCount(); ++i) {
      final T from = view.getNode(i);
      for(final int toId : view.edgesFrom(i)) {
        final T to = view.getNode(toId);
        final Shape edgeShape = edgeRealizer.createLineShape(from, to);
        if(!edgeShape.intersects(visible)) {
          continue;
        }
        final Graphics2D g = (Graphics2D) gfx.create();
        edgeRealizer.drawLines(g, edgeShape, from, to);
        g.dispose();
      }
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
    T cur = null;
    for(final T node : view.nodes()) {
      final double x = node.getX();
      final double y = node.getY();
      final Shape shape = nodeRealizer.createNodeShape(node, x, y);
      if(shape.contains(pos)) {
        // return the last match -- ie topmost node
        cur = node;
      }
    }
    return cur;
  }

  @Override
  public void setAnimationList(final AnimationList list) {
    this.list = Objects.requireNonNull(list);
  }

  /**
   * Getter.
   * 
   * @return The animation list.
   */
  protected AnimationList getAnimationList() {
    return list;
  }

  @Override
  public boolean isChanging() {
    for(final T node : view.nodes()) {
      if(node.inAnimation()) return true;
    }
    return false;
  }

  @Override
  public void getBoundingBox(final RectangularShape rect) {
    if(layout == null) {
      rect.setFrame(bbox);
    } else {
      layout.getBoundingBox(rect);
    }
  }

}
