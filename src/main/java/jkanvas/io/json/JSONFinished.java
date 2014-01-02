package jkanvas.io.json;

/**
 * Marks a class to be JSON finish able. This means that
 * {@link #onSetupFinish()} is called after the thunk creating the object has
 * finished evaluating.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface JSONFinished {

  /**
   * This method is called on the newly created object after all setters have
   * been called. Objects that rely on this behavior must call this method
   * directly when they are not loaded via
   * {@link JSONThunk#readJSON(JSONElement, JSONManager)}.
   */
  void onSetupFinish();

}
