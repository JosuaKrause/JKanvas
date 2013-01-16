package jkanvas.nodelink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jkanvas.animation.Position2D;

/**
 * Creates a simple view on a graph. Nodes and edges only can be added.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The position type.
 */
public class SimpleNodeLinkView<T extends Position2D> implements NodeLinkView<T> {

  /** Reverse map from nodes to ids. */
  private final Map<T, Integer> idMap = new HashMap<>();

  /** Dense list of nodes. */
  private final List<T> nodes = new ArrayList<>();

  /**
   * List containing edges. Edges always go from lower to higher indices. Nodes
   * are stored as integers.
   */
  private final List<List<Integer>> edges = new ArrayList<>();

  @Override
  public Iterable<T> nodes() {
    return nodes;
  }

  /** The empty list. */
  private final List<Integer> EMPTY_LIST = Collections.emptyList();

  @Override
  public Iterable<Integer> edgesTo(final int node) {
    if(node >= edges.size() || edges.get(node) == null) return EMPTY_LIST;
    return edges.get(node);
  }

  @Override
  public T getNode(final int id) {
    return nodes.get(id);
  }

  /**
   * Getter.
   * 
   * @param node The node.
   * @return The corresponding id.
   */
  private int getId(final T node) {
    return idMap.get(node);
  }

  @Override
  public int nodeCount() {
    return nodes.size();
  }

  /**
   * Adds an edge between two nodes.
   * 
   * @param from The first node.
   * @param to The second node.
   */
  public void addEdge(final T from, final T to) {
    addEdge(getId(from), getId(to));
  }

  /**
   * Adds an edge between two nodes.
   * 
   * @param from The id of the first node.
   * @param to The id of the second node.
   */
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

  @Override
  public boolean areConnected(final int from, final int to) {
    if(from == to) return false;
    if(from > to) return areConnected(to, from);
    assert from < to;
    final List<Integer> e = edges.get(from);
    if(e == null) return false;
    return e.contains(to);
  }

  /**
   * Adds a node.
   * 
   * @param node The node.
   */
  public void addNode(final T node) {
    if(idMap.containsKey(node)) throw new IllegalArgumentException("node " + node
        + " already added");
    idMap.put(node, nodes.size());
    nodes.add(node);
  }

  @Override
  public String getName(final int index) {
    return "Node " + index;
  }

}
