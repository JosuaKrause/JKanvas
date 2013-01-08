package jkanvas.nodelink;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.Refreshable;
import jkanvas.animation.AbstractAnimator;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.Animator;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;

/**
 * Paints a node link diagram.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The type of nodes.
 */
public class NodelinkPainter<T extends AnimatedPosition> extends RenderpassPainter
implements Animator {

  private final Map<T, Integer> idMap = new HashMap<>();

  private final List<T> nodes = new ArrayList<>();

  private final List<List<Integer>> edges = new ArrayList<>();

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

  protected List<T> getNodes() {
    return nodes;
  }

  protected T getNode(final int id) {
    return nodes.get(id);
  }

  protected int getId(final T node) {
    return idMap.get(node);
  }

  private final List<Integer> EMPTY_LIST = Collections.emptyList();

  protected List<Integer> getEdges(final int node) {
    if(node >= edges.size() || edges.get(node) == null) return EMPTY_LIST;
    return edges.get(node);
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

  public void addEdge(final T from, final T to) {
    addEdge(getId(from), getId(to));
  }

  public void addEdge(final int from, final int to) {
    if(from == to) return;
    if(from > to) {
      addEdge(to, from);
      return;
    }
    assert from < to;
    while(edges.size() <= from) {
      edges.add(null);
    }
    if(edges.get(from) == null) {
      edges.set(from, new ArrayList<Integer>());
    }
    final List<Integer> fromList = edges.get(from);
    if(!fromList.contains(to)) {
      fromList.add(to);
    }
  }

  public void addNode(final T node) {
    if(idMap.containsKey(node)) return;
    idMap.put(node, nodes.size());
    nodes.add(node);
  }

  protected void renderEdges(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D visible = ctx.getVisibleCanvas();
    final EdgeRealizer<T> edgeRealizer = getEdgeRealizer();
    for(int i = 0; i < nodes.size(); ++i) {
      final T from = getNode(i);
      for(final int toId : getEdges(i)) {
        final T to = getNode(toId);
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
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    for(final T node : getNodes()) {
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
    final NodeRealizer<T> nodeRealizer = getNodeRealizer();
    for(final T node : getNodes()) {
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
