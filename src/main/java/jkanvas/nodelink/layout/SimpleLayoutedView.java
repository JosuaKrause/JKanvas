package jkanvas.nodelink.layout;

import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.SimpleNodeLinkView;

/**
 * A simple layouted view.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The node position type.
 */
public class SimpleLayoutedView<T extends AnimatedPosition> extends SimpleNodeLinkView<T>
    implements LayoutedView<T> {

  /** The canvas. */
  private final Canvas canvas;
  /** The layouter. */
  private AbstractLayouter<T> layouter;

  /**
   * Creates a layouted view.
   * 
   * @param canvas The canvas.
   * @param isDirected Whether the graph is directed.
   */
  public SimpleLayoutedView(final Canvas canvas, final boolean isDirected) {
    super(isDirected);
    this.canvas = Objects.requireNonNull(canvas);
    layouter = null;
  }

  @Override
  protected void onChange() {
    super.onChange();
    if(layouter != null) {
      layouter.layout(false);
    }
  }

  @Override
  public void setLayouter(final AbstractLayouter<T> layouter) {
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
  public AbstractLayouter<T> getLayouter() {
    return layouter;
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
    Objects.requireNonNull(layouter);
    layouter.getBoundingBox(bbox);
  }

}
