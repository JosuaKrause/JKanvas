package jkanvas.io.json;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A manager for ids and templates in a JSON document.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class JSONManager {

  /** The template map. */
  private final Map<String, JSONElement> templates = new HashMap<>();

  /**
   * Adds a template.
   * 
   * @param name The name of the template.
   * @param el The template object.
   */
  public void addTemplate(final String name, final JSONElement el) {
    el.expectObject();
    if(templates.put(name, el) != null) throw new IllegalArgumentException(
        "duplicate entry: " + name);
  }

  /**
   * Getter.
   * 
   * @param name The name of the template.
   * @return The template object.
   */
  public JSONElement getTemplate(final String name) {
    return Objects.requireNonNull(templates.get(name), name);
  }

  /**
   * Getter.
   * 
   * @param el The object.
   * @return The template object or <code>null</code>.
   */
  public JSONElement getTemplateOf(final JSONElement el) {
    final String tmpl = el.getString("template", null);
    if(tmpl == null) return null;
    return getTemplate(tmpl);
  }

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
      setForId(id, new JSONThunk(this, id, true));
    }
    return thunks.get(id);
  }

  /**
   * Gets the content associated with the given id.
   * 
   * @param <T> The type.
   * @param id The id.
   * @param type The type.
   * @return The content.
   * @throws IOException I/O Exception.
   */
  public <T> T getForId(final String id, final Class<T> type) throws IOException {
    Objects.requireNonNull(id);
    if(!thunks.containsKey(id)) throw new IllegalArgumentException("unknown id: " + id);
    return getForId(id).get(type);
  }

  /**
   * Adds a raw object to the id list.
   * 
   * @param id The id.
   * @param obj The raw object.
   */
  public void addRawId(final String id, final Object obj) {
    Objects.requireNonNull(id);
    if(thunks.put(id, new JSONThunk(obj, this, id)) != null) throw new IllegalArgumentException(
        "id already in use: " + id);
  }

  /**
   * Calls all setters for the object.
   * 
   * @param o The object.
   * @throws IOException I/O Exception.
   */
  public void fillObject(final Object o) throws IOException {
    JSONThunk.callSetters(o, thunks);
  }

}
