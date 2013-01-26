package jkanvas.selection;

import java.awt.Shape;

import jkanvas.painter.Renderpass;

/**
 * An interface for arbitrary shaped selections.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public interface SelectableRenderpass extends Renderpass {

  /**
   * Selects the given shape.
   * 
   * @param selection The shape of the selection.
   * @param preview Whether the selection should only be a preview.
   */
  void select(Shape selection, boolean preview);

}
