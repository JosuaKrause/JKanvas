package jkanvas.nodelink;

import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import jkanvas.Canvas;
import jkanvas.animation.Animated;
import jkanvas.nodelink.layout.AbstractLayouter;
import jkanvas.nodelink.layout.LayoutedView;
import jkanvas.util.BitSetIterable;
import jkanvas.util.ObjectDependencies;

/**
 * Creates a graph based on fields of objects referencing each other.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DependencyNodeLinkView implements LayoutedView<IndexedPosition>, Animated {

  /** The base object. */
  private final Object base;
  /** The mapping from objects to indices. */
  private final Map<Object, Integer> backMap;
  /** The objects. */
  private final List<Object> objects;
  /** The positions of the objects. */
  private final List<IndexedPosition> pos;
  /** The edges. */
  private final BitSet edges;
  /** The object dependency. */
  private final ObjectDependencies dependency;
  /** The canvas. */
  private final Canvas canvas;

  /**
   * Creates a node-link view of dependencies.
   * 
   * @param canvas The canvas.
   * @param base The base object.
   */
  public DependencyNodeLinkView(final Canvas canvas, final Object base) {
    this(canvas, base, new ObjectDependencies());
  }

  /**
   * Creates a node-link view of dependencies.
   * 
   * @param canvas The canvas.
   * @param base The base object.
   * @param dependency The object dependency handler.
   */
  public DependencyNodeLinkView(final Canvas canvas, final Object base,
      final ObjectDependencies dependency) {
    this.canvas = Objects.requireNonNull(canvas);
    this.dependency = Objects.requireNonNull(dependency);
    this.base = Objects.requireNonNull(base);
    backMap = new IdentityHashMap<>();
    objects = new ArrayList<>();
    pos = new ArrayList<>();
    edges = new BitSet();
  }

  /**
   * Fills all objects in.
   * 
   * @param o The base object.
   */
  private void fillObjects(final Object o) {
    backMap.clear();
    objects.clear();
    final Queue<Object> objs = new LinkedList<>();
    dependency.addRef(objs, o);
    while(!objs.isEmpty()) {
      if(objects.size() > 1000) return;
      final Object cur = objs.poll();
      if(backMap.containsKey(cur)) {
        continue;
      }
      final int pos = objects.size();
      objects.add(cur);
      backMap.put(cur, pos);
      final Set<Object> set = dependency.neighbors(cur);
      objs.addAll(set);
    }
  }

  /** Fills all edges in. */
  private void fillEdges() {
    edges.clear();
    for(final Object f : objects) {
      final Integer from = backMap.get(f);
      if(from == null) {
        continue;
      }
      final Set<Object> neighbors = dependency.neighbors(f);
      for(final Object n : neighbors) {
        final Integer to = backMap.get(n);
        if(to != null) {
          edges.set(edgePos(from, to));
        }
      }
    }
  }

  /** The last time of animation. */
  private long lastTime;

  @Override
  public boolean animate(final long currentTime) {
    if(lastTime != 0 && currentTime - lastTime < 5 * 60 * 1000) return false;
    pos.clear();
    fillObjects(base);
    fillEdges();
    for(int i = 0; i < objects.size(); ++i) {
      pos.add(new IndexedPosition(0, 0, i));
    }
    if(layouter != null) {
      layouter.layout(false);
    }
    lastTime = currentTime;
    return true;
  }

  @Override
  public int nodeCount() {
    return pos.size();
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The short name.
   */
  public String shortName(final int index) {
    final Object o = objects.get(index);
    final Class<? extends Object> clazz = o.getClass();
    return clazz.isAnonymousClass() ? clazz.getName() : clazz.getSimpleName();
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The info.
   */
  public String info(final int index) {
    final Object o = objects.get(index);
    final Class<? extends Object> clazz = o.getClass();
    final String arr;
    if(clazz.isArray() && !clazz.getComponentType().isPrimitive()) {
      final StringBuilder a = new StringBuilder("<br>[");
      final Object[] array = (Object[]) o;
      boolean first = true;
      for(final Object el : array) {
        if(first) {
          first = false;
        } else {
          a.append(", ");
        }
        a.append(System.identityHashCode(el));
      }
      a.append("]");
      arr = a.toString();
    } else {
      arr = "";
    }
    return "<html>" + clazz.getName() + "<br>("
        + System.identityHashCode(o) + ")<br>" + o.toString() + arr;
  }

  @Override
  public String getName(final int index) {
    return shortName(index);
  }

  /**
   * Computes the position of an edge.
   * 
   * @param from The from index.
   * @param to The to index.
   * @return The position in the bit set.
   */
  private int edgePos(final int from, final int to) {
    return from * objects.size() + to;
  }

  @Override
  public boolean areConnected(final int a, final int b) {
    return edges.get(edgePos(a, b));
  }

  @Override
  public Iterable<Integer> edgesFrom(final int from) {
    final BitSet set = edges;
    final int start = edgePos(from, 0);
    final int end = edgePos(from + 1, 0);
    final Iterable<Integer> iter = new Iterable<Integer>() {

      private final Iterable<Integer> iterable = new BitSetIterable(set, start, end);

      @Override
      public Iterator<Integer> iterator() {
        final Iterable<Integer> iterable = this.iterable;
        return new Iterator<Integer>() {

          private final Iterator<Integer> it = iterable.iterator();

          @Override
          public boolean hasNext() {
            return it.hasNext();
          }

          @Override
          public Integer next() {
            return it.next() - start;
          }

          // TODO #43 -- Java 8 simplification
          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
    return iter;
  }

  @Override
  public IndexedPosition getNode(final int index) {
    return pos.get(index);
  }

  @Override
  public Iterable<IndexedPosition> nodes() {
    return pos;
  }

  @Override
  public boolean isDirected() {
    return true;
  }

  /** The current layouter. */
  private AbstractLayouter<IndexedPosition> layouter;

  @Override
  public void setLayouter(final AbstractLayouter<IndexedPosition> layouter) {
    if(this.layouter != null) {
      this.layouter.deregister();
    }
    this.layouter = layouter;
    if(layouter != null) {
      layouter.register(canvas, this);
      layouter.layout(false);
    }
  }

  @Override
  public AbstractLayouter<IndexedPosition> getLayouter() {
    return layouter;
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
    Objects.requireNonNull(layouter);
    layouter.getBoundingBox(bbox);
  }

}
