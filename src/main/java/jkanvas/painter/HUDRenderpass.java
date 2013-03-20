package jkanvas.painter;

import jkanvas.HUDInteraction;

/**
 * A HUD render pass is always in component coordinates.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface HUDRenderpass extends HUDInteraction {

  /**
   * Getter.
   * 
   * @return Whether this pass is currently visible.
   */
  boolean isVisible();

}
