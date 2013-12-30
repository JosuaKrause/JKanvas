package jkanvas.io.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class IdManager {

  private final Map<String, JSONThunk> thunks = new HashMap<>();

  public void setForId(final String id, final JSONThunk thunk) {
    Objects.requireNonNull(id);
    Objects.requireNonNull(thunk);
    if(thunks.put(id, thunk) != null) throw new IllegalArgumentException(
        "id already in use: " + id);
  }

  public JSONThunk getForId(final String id) {
    Objects.requireNonNull(id);
    if(!thunks.containsKey(id)) {
      setForId(id, new JSONThunk(this));
    }
    return thunks.get(id);
  }
}
