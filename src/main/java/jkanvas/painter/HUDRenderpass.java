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

  /**
   * Getter.
   * 
   * @return The ids associated with this render pass. Multiple ids may be
   *         separated with space '<code>&#20;</code>'.
   */
  String getIds();

  /**
   * Setter.
   * 
   * @param ids The ids associated with this render pass. Multiple ids may be
   *          separated with space '<code>&#20;</code>'.
   */
  void setIds(String ids);

}
