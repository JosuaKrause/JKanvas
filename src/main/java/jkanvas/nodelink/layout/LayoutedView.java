package jkanvas.nodelink.layout;

import java.awt.geom.Rectangle2D;

import jkanvas.animation.AnimatedPosition;
import jkanvas.nodelink.NodeLinkView;

/**
 * A layouted view.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The node position type.
 */
public interface LayoutedView<T extends AnimatedPosition> extends NodeLinkView<T> {

  /**
   * Setter.
   * 
   * @param layouter The layouter.
   */
  void setLayouter(AbstractLayouter<T> layouter);

  /**
   * Getter.
   * 
   * @return The current layouter.
   */
  AbstractLayouter<T> getLayouter();

  /**
   * Getter.
   * 
   * @return The bounding box of the layout.
   */
  Rectangle2D getBoundingBox();

}
