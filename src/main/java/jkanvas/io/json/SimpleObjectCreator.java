package jkanvas.io.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple implementation of an {@link ObjectCreator}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class SimpleObjectCreator implements ObjectCreator {

  /** The fields. */
  private final Map<String, JSONThunk> setters = new HashMap<>();

  @Override
  public boolean hasField(final String name) {
    return setters.containsKey(name);
  }

  @Override
  public void addField(final String name, final JSONThunk thunk) {
    setters.put(name, thunk);
  }

  @Override
  public void callSetters(final Object o) throws IOException {
    JSONThunk.callSetters(o, setters);
  }

  @Override
  public boolean hasType() {
    return true;
  }

  @Override
  public boolean hasConstructor() {
    return true;
  }

  @Override
  public void setType(final String type) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setConstructor(final String args) {
    throw new UnsupportedOperationException();
  }

}
