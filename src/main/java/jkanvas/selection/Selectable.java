package jkanvas.selection;

import java.awt.Shape;

import jkanvas.painter.Renderpass;

/**
 * An interface for arbitrary shaped selections.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface Selectable {

  /**
   * Selects the given shape.
   * 
   * @param selection The shape of the selection.
   * @param preview Whether the selection should only be a preview.
   */
  void select(Shape selection, boolean preview);

  /**
   * Getter.
   * 
   * @return The associated render pass.
   */
  Renderpass getRenderpass();

}
