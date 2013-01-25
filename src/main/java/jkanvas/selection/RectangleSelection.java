package jkanvas.selection;

import java.awt.Graphics2D;

import jkanvas.Canvas;
import jkanvas.KanvasContext;

/**
 * A rectangle selection.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class RectangleSelection extends AbstractSelector {

  /**
   * Creates a rectangular selection.
   * 
   * @param canvas The canvas the selection operates on.
   */
  public RectangleSelection(final Canvas canvas) {
    super(canvas);
  }

  @Override
  public void drawHUD(final Graphics2D gfx, final KanvasContext ctx) {
    // TODO needs drag methods -- argh!
  }

}
