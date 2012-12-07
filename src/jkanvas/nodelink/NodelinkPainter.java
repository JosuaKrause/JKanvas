package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jkanvas.KanvasContext;
import jkanvas.Refreshable;
import jkanvas.animation.AbstractAnimator;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;

/**
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T>
 */
public class NodelinkPainter<T extends AnimatedPosition> extends RenderpassPainter
implements Animator {

  private final Map<T, Set<T>> edges = new ConcurrentHashMap<>();

  private NodeRealizer<T> nodeRealizer;

  private EdgeRealizer<T> edgeRealizer;

  private AbstractAnimator animator;

  public NodelinkPainter() {
    addPass(new Renderpass() {

      @Override
      public void render(final Graphics2D gfx, final KanvasContext ctx) {
        renderEdges(gfx, ctx);
      }

      @Override
      public boolean isHUD() {
        return false;
      }

    });
    addPass(new Renderpass() {

      @Override
      public void render(final Graphics2D gfx, final KanvasContext ctx) {
        renderNodes(gfx, ctx);
      }

      @Override
      public boolean isHUD() {
        return false;
      }

    });
    animator = new AbstractAnimator() {

      @Override
      protected boolean step() {
        boolean needsRedraw = false;
        for(final T node : getNodes()) {
          node.animate();
          needsRedraw = needsRedraw || node.lazyInAnimation();
        }
        return needsRedraw;
      }

    };
  }

  protected Set<T> getNodes() {
    return edges.keySet();
  }

  @Override
  public void addRefreshable(final Refreshable r) {
    animator.addRefreshable(r);
  }

  @Override
  public void forceNextFrame() {
    animator.forceNextFrame();
  }

  @Override
  public void quickRefresh() {
    animator.quickRefresh();
  }

  public void setEdgeRealizer(final EdgeRealizer<T> edgeRealizer) {
    this.edgeRealizer = Objects.requireNonNull(edgeRealizer);
  }

  public EdgeRealizer<T> getEdgeRealizer() {
    return edgeRealizer;
  }

  public void setNodeRealizer(final NodeRealizer<T> nodeRealizer) {
    this.nodeRealizer = Objects.requireNonNull(nodeRealizer);
  }

  public NodeRealizer<T> getNodeRealizer() {
    return nodeRealizer;
  }

  public boolean hasEdge(final T from, final T to) {
    return hasEdge0(from, to) || !(edgeRealizer.isDirected() || !hasEdge0(to, from));
  }

  private boolean hasEdge0(final T from, final T to) {
    final Set<T> toNodes = edges.get(from);
    return toNodes != null && toNodes.contains(to);
  }

  public void addEdge(final T from, final T to) {
    if(!edgeRealizer.isDirected() && hasEdge(from, to)) return;
    addEdge0(from, to);
  }

  private void addEdge0(final T from, final T to) {
    Set<T> toNodes = edges.get(from);
    if(toNodes == null) {
      edges.put(from, toNodes = new HashSet<>());
    }
    toNodes.add(to);
    addNode(to);
  }

  public void removeEdge(final T from, final T to) {
    removeEdge0(from, to);
    if(!edgeRealizer.isDirected()) {
      removeEdge0(to, from);
    }
  }

  private void removeEdge0(final T from, final T to) {
    final Set<T> set = edges.get(from);
    if(set == null) return;
    set.remove(to);
  }

  public void addNode(final T node) {
    if(edges.containsKey(node)) return;
    edges.put(node, new HashSet<T>());
  }

  public void removeNode(final T node) {
    for(final Set<T> toSet : edges.values()) {
      toSet.remove(node);
    }
    edges.remove(node);
  }

  protected void renderEdges(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    for(final Entry<T, Set<T>> links : edges.entrySet()) {
      final T from = links.getKey();
      for(final T to : links.getValue()) {
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

  protected void renderNodes(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    for(final T node : edges.keySet()) {
      final Shape nodeShape = nodeRealizer.createNodeShape(node);
      if(!nodeShape.intersects(visible)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      nodeRealizer.drawNode(g, node);
      g.dispose();
    }
  }

  public T pick(final Point2D pos) {
    for(final T node : edges.keySet()) {
      final Shape shape = nodeRealizer.createNodeShape(node);
      if(shape.contains(pos)) return node;
    }
    return null;
  }

  public void dispose() {
    if(!animator.isDisposed()) {
      animator.dispose();
    }
  }

}
