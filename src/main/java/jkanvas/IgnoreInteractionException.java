package jkanvas;

/**
 * This exception may be thrown when interactions of the same type from peers in
 * a {@link KanvasInteraction} and {@link HUDInteraction} (and therefore in a
 * {@link jkanvas.painter.Renderpass} and {@link jkanvas.painter.HUDRenderpass})
 * should be prevented.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class IgnoreInteractionException extends RuntimeException {

  /** Creates a new ignore interaction exception. */
  public IgnoreInteractionException() {
    // nothing to do
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    return this;
  }

}
