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
   * @return The ids of the canvas. The string must be surrounded with spaces.
   */
  String getCanvasIds();

  /**
   * Processes the message for the canvas.
   * 
   * @param canvas The canvas.
   * @param msg The message to process.
   */
  void processMessage(Canvas canvas, String msg);

}
