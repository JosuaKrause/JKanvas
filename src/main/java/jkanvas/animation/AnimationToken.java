package jkanvas.animation;

import java.util.List;

/**
 * An animation token is responsible for executing {@link AnimationAction
 * animation actions}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface AnimationToken {

  /**
   * Registers a list that will be filled with actions on this token. Lists are
   * held with weak references so unregister operations are unnecessary.
   * 
   * @param list The list to register.
   */
  void register(List<AnimationAction> list);

  /**
   * Executes the animation actions in all registered action lists. The lists
   * are cleared afterwards. No concurrent changes of the lists may happen
   * during this method.
   */
  void executeAndClear();

}
