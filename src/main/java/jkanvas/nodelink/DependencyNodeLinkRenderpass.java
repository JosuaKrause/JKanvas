package jkanvas.nodelink;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;

import jkanvas.animation.AnimationList;
import jkanvas.util.ArrowFactory;
import jkanvas.util.PaintUtil;
import jkanvas.util.StringDrawer;
import jkanvas.util.VecUtil;

/**
 * A render pass to show dependencies between objects.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DependencyNodeLinkRenderpass extends NodeLinkRenderpass<IndexedPosition> {

  /** The view. */
  private final DependencyNodeLinkView v;

  /**
   * Creates a dependency render pass.
   * 
   * @param base The base object.
   * @param allowdPkgs Allowed packages.
   */
  public DependencyNodeLinkRenderpass(final Object base, final String[] allowdPkgs) {
    this(new DependencyNodeLinkView(base, allowdPkgs));
  }

  /**
   * Creates a dependency render pass.
   * 
   * @param v The view.
   */
  public DependencyNodeLinkRenderpass(final DependencyNodeLinkView v) {
    super(v);
    this.v = v;
    setNodeRealizer(new DefaultNodeRealizer<IndexedPosition>() {

      @Override
      public void drawNode(final Graphics2D g, final IndexedPosition node) {
        super.drawNode(g, node);
        final Shape s = PaintUtil.createCircle(node.getX(), node.getY(), getRadius(node));
        g.setColor(Color.BLACK);
        StringDrawer.drawInto(g, v.shortName(node.getIndex()),
            PaintUtil.addPadding(s.getBounds2D(), -DefaultNodeRealizer.RADIUS * 0.2));
      }

    });
    setEdgeRealizer(new DefaultEdgeRealizer<IndexedPosition>() {

      private final ArrowFactory arrows =
          new ArrowFactory(ArrowFactory.ARROW_FULL, ArrowFactory.NONE, 10);

      @Override
      public Shape createLineShape(final IndexedPosition from, final IndexedPosition to) {
        final Point2D f = from.getPos();
        final Point2D t = to.getPos();
        final Point2D diff = VecUtil.subVec(t, f);
        final Point2D d = VecUtil.setLength(diff,
            VecUtil.getLength(diff) - DefaultNodeRealizer.RADIUS);
        return arrows.createArrow(f, VecUtil.addVec(f, d));
      }

      @Override
      public void drawLines(final Graphics2D g, final Shape edgeShape,
          final IndexedPosition from, final IndexedPosition to) {
        super.drawLines(g, edgeShape, from, to);
        g.draw(edgeShape);
      }

    });
  }

  @Override
  public void setAnimationList(final AnimationList list) {
    final AnimationList old = getAnimationList();
    if(old != null) {
      old.removeAnimated(v);
    }
    super.setAnimationList(list);
    list.addAnimated(v);
  }

  @Override
  public String getTooltip(final Point2D p) {
    final IndexedPosition pick = pick(p);
    return pick != null ? v.info(pick.getIndex()) : null;
  }

}
