package jkanvas.nodelink.layout;

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

}
