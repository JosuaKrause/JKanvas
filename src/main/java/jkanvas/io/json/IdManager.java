package jkanvas.io.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A manager for ids.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class IdManager {

  /** The id map. */
  private final Map<String, JSONThunk> thunks = new HashMap<>();

  /**
   * Sets the thunk for the given id.
   * 
   * @param id The id.
   * @param thunk The thunk.
   */
  private void setForId(final String id, final JSONThunk thunk) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(thunk);
    if(thunks.put(id, thunk) != null) throw new IllegalArgumentException(
        "id already in use: " + id);
  }

  /**
   * Gets the thunk associated with the given id. If the id is unused a new
   * thunk is created and associated with the id.
   * 
   * @param id The id.
   * @return The associated thunk.
   */
  public JSONThunk getForId(final String id) {
    Objects.requireNonNull(id);
    if(!thunks.containsKey(id)) {
      setForId(id, new JSONThunk(this));
    }
    return thunks.get(id);
  }

}
