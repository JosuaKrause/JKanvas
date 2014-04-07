package jkanvas;

/**
 * This exception may be thrown when interactions of the same type from peers in
 * a {@link KanvasInteraction} and {@link HUDInteraction} (and therefore in a
 * {@link jkanvas.painter.Renderpass} and {@link jkanvas.painter.HUDRenderpass})
 * should be prevented.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
class IgnoreInteractionException extends RuntimeException {

  /** The only instance. */
  static final IgnoreInteractionException INSTANCE = new IgnoreInteractionException();

  /** Creates a new ignore interaction exception. */
  private IgnoreInteractionException() {
    // nothing to do
  }

  @Override
  public synchronized Throwable fillInStackTrace() {
    if(Canvas.ALLOW_INTERACTION_DIAGNOSTIC) return super.fillInStackTrace();
    // we wont use the stack trace so we save the costs of creating it
    return this;
  }

}
