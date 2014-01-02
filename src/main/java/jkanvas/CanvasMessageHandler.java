package jkanvas;

/**
 * Handles messages addressed to the canvas.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface CanvasMessageHandler {

  /**
   * Getter.
   * 
   * @return The id of the canvas.
   */
  String getCanvasId();

  /**
   * Processes the message for the canvas.
   * 
   * @param canvas The canvas.
   * @param msg The message to process.
   */
  void processMessage(Canvas canvas, String msg);

}
