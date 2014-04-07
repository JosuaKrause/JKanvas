package jkanvas.nodelink.layout;

import java.awt.geom.RectangularShape;

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
   * @param bbox The rectangle in which the bounding box will be stored.
   */
  void getBoundingBox(RectangularShape bbox);

}
