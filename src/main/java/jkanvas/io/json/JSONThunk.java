package jkanvas.io.json;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

/**
 * A lazy evaluated JSON object.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class JSONThunk {

  /** The id manager. */
  private final IdManager mng;
  /** All used setters. */
  private final Map<String, JSONThunk> setters = new HashMap<>();
  /** The thunks for the constructor arguments. */
  private final Map<String, JSONThunk> constructorLookup = new HashMap<>();
  /** The constructor arguments. */
  private final List<String> constructor = new ArrayList<>();
  /** The creation type of the thunk if it is not a simple string. */
  private Class<?> type;
  /** The actual object. */
  private Object obj;
  /** The creation string or <code>null</code>. */
  private String str;
  /**
   * Whether the thunk is currently evaluating. This is used to detect circular
   * dependencies.
   */
  private boolean eval;

  /**
   * Creates a complex thunk.
   * 
   * @param mng The manager.
   */
  public JSONThunk(final IdManager mng) {
    this.mng = Objects.requireNonNull(mng);
  }

  /**
   * Creates a simple thunk.
   * 
   * @param mng The manager.
   * @param str The creation string.
   */
  public JSONThunk(final IdManager mng, final String str) {
    this.str = Objects.requireNonNull(str);
    this.mng = Objects.requireNonNull(mng);
  }

  /**
   * Sets the creation type of the object. The type cannot be set when the
   * creation string is present.
   * 
   * @param type The fully qualified type name.
   * @throws IOException I/O Exception.
   */
  public void setType(final String type) throws IOException {
    try {
      setType(Class.forName(type));
    } catch(IllegalStateException | IllegalArgumentException | ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  /**
   * Sets the creation type of the object. The type cannot be set when the
   * creation string is present.
   * 
   * @param type The type.
   */
  public void setType(final Class<?> type) {
    if(str != null) throw new IllegalStateException("cannot define type for primitives");
    if(this.type != null) throw new IllegalStateException("type already defined");
    Objects.requireNonNull(type);
    this.type = type;
  }

  /**
   * Sets the constructor. Arguments are split via '{@code ,}'. The empty
   * (default) constructor is implicit and must not be defined with this method.
   * The constructor can only be set once.
   * 
   * @param construct The constructor string.
   */
  public void setConstructor(final String construct) {
    final String[] fields = construct.split(",");
    if(fields.length == 0) throw new IllegalArgumentException(
        "default constructor is implicit");
    if(!constructor.isEmpty()) throw new IllegalStateException("constructor already set");
    for(final String f : fields) {
      constructor.add(f);
      final JSONThunk thunk = setters.remove(f);
      if(thunk != null) {
        constructorLookup.put(f, thunk);
      }
    }
  }

  /**
   * Adds a field to the thunk.
   * 
   * @param name The name of the field.
   * @param id The id reference.
   */
  public void addField(final String name, final String id) {
    final JSONThunk thunk = mng.getForId(id);
    addField(name, thunk);
  }

  /**
   * Adds a field to the thunk.
   * 
   * @param name The name of the field.
   * @param thunk The thunk.
   */
  public void addField(final String name, final JSONThunk thunk) {
    if(constructor.contains(name)) {
      if(constructorLookup.containsKey(name)) throw new IllegalArgumentException(
          name + " already in use");
      constructorLookup.put(name, thunk);
    } else {
      if(setters.containsKey(name)) throw new IllegalArgumentException(
          name + " already in use");
      setters.put(name, thunk);
    }
  }

  /**
   * Casts the object to the given type.
   * 
   * @param <T> The type.
   * @param o The object.
   * @param type The type to cast.
   * @return The cast object.
   * @throws IOException I/O Exception.
   */
  @SuppressWarnings("unchecked")
  private static <T> T cast(final Object o, final Class<T> type) throws IOException {
    if(!type.isAssignableFrom(o.getClass())) throw new IOException(
        "invalid type: " + type.getSimpleName() + " <=!= "
            + o.getClass().getSimpleName());
    return (T) o;
  }

  /**
   * Evaluates the thunk and returns the content. If the thunk is already
   * evaluated the content is returned directly.
   * 
   * @param <T> The expected type.
   * @param type The expected type of the content.
   * @return The content.
   * @throws IOException I/O Exception.
   */
  public <T> T get(final Class<T> type) throws IOException {
    if(obj != null) return cast(obj, type);
    if(eval) throw new IOException("cyclic dependency!");
    eval = true;
    if(str != null) {
      try {
        if(type.isAssignableFrom(Long.class)) {
          obj = Long.parseLong(str);
          return cast(obj, type);
        }
        if(type.isAssignableFrom(Integer.class)) {
          obj = Integer.parseInt(str);
          return cast(obj, type);
        }
        if(type.isAssignableFrom(Short.class)) {
          obj = Short.parseShort(str);
          return cast(obj, type);
        }
        if(type.isAssignableFrom(Byte.class)) {
          obj = Byte.parseByte(str);
          return cast(obj, type);
        }
        if(type.isAssignableFrom(Double.class)) {
          obj = Double.parseDouble(str);
          return cast(obj, type);
        }
        if(type.isAssignableFrom(Float.class)) {
          obj = Float.parseFloat(str);
          return cast(obj, type);
        }
        if(type.isAssignableFrom(String.class)) {
          obj = str;
          return cast(obj, type);
        }
      } catch(final NumberFormatException e) {
        throw new IOException(
            "could not interpret \"" + str + "\" as " + type.getName(), e);
      }
      // string is ID
      obj = mng.getForId(str).get(type);
      return cast(obj, type);
    }
    obj = eval(type);
    eval = false;
    return cast(obj, type);
  }

  /**
   * Finds a constructor for the given type with the given number of arguments.
   * 
   * @param type The type.
   * @param args The number of arguments.
   * @return The first matching constructor.
   * @throws IOException I/O Exception.
   */
  private static Constructor<?> findConstructor(final Class<?> type, final int args)
      throws IOException {
    for(final Constructor<?> c : type.getConstructors()) {
      if(c.getParameterTypes().length == args) return c;
    }
    throw new IOException("could not find constructor: " + type.getSimpleName());
  }

  /**
   * Creates an object of the given type. The specified constructor is used.
   * 
   * @param type The type.
   * @return The object.
   * @throws IOException I/O Exception.
   */
  private Object create(final Class<?> type) throws IOException {
    try {
      if(constructor.isEmpty()) return type.newInstance();
      final Constructor<?> c = findConstructor(type, constructor.size());
      final Class<?>[] classes = c.getParameterTypes();
      final Object[] args = new Object[classes.length];
      for(int i = 0; i < classes.length; ++i) {
        final JSONThunk thunk = constructorLookup.get(constructor.get(i));
        args[i] = thunk.get(classes[i]);
      }
      return c.newInstance(args);
    } catch(IllegalAccessException | InstantiationException | SecurityException
        | IllegalArgumentException | InvocationTargetException e) {
      throw new IOException(e);
    }
  }

  /**
   * Constructs the setter name for the given field.
   * 
   * @param name The field name.
   * @return The name of the setter method.
   */
  private static String getSetter(final String name) {
    return "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  /**
   * Finds a method with one argument and a setter typical name for the given
   * field.
   * 
   * @param type The type.
   * @param name The field name.
   * @return The setter method.
   * @throws IOException I/O Exception.
   */
  private static Method findSetter(final Class<?> type, final String name)
      throws IOException {
    final String setter = getSetter(name);
    for(final Method m : type.getDeclaredMethods()) {
      if(setter.equals(m.getName()) && m.getParameterTypes().length == 1) return m;
    }
    throw new IOException("could not find setter for: " + name);
  }

  /**
   * Calls all setters.
   * 
   * @param o The object to call the setters on.
   * @throws IOException I/O Exception.
   */
  private void callSetters(final Object o) throws IOException {
    callSetters(o, setters);
  }

  /**
   * Calls setters on an object.
   * 
   * @param o The object.
   * @param setters The setters.
   * @throws IOException I/O Exception.
   */
  public static final void callSetters(
      final Object o, final Map<String, JSONThunk> setters) throws IOException {
    final Class<?> type = o.getClass();
    for(final Entry<String, JSONThunk> el : setters.entrySet()) {
      try {
        final Method method = findSetter(type, el.getKey());
        method.invoke(o, el.getValue().get(method.getParameterTypes()[0]));
      } catch(SecurityException | IllegalAccessException
          | IllegalArgumentException | InvocationTargetException e) {
        throw new IOException(e);
      }
    }
  }

  /**
   * Evaluates the thunk.
   * 
   * @param type The expected type.
   * @return The object.
   * @throws IOException I/O Exception.
   */
  private Object eval(final Class<?> type) throws IOException {
    final Class<?> t;
    if(this.type != null) {
      if(!type.isAssignableFrom(this.type)) throw new IOException(
          "cannot convert " + this.type.getSimpleName() + " to " + type.getSimpleName());
      t = this.type;
    } else {
      t = type;
    }
    final Object res = create(t);
    callSetters(res);
    if(res instanceof JSONFinished) {
      ((JSONFinished) res).onSetupFinish();
    }
    return res;
  }

  /**
   * Reads a thunk from JSON input.
   * 
   * @param el The root element.
   * @param mng The id manager.
   * @return The thunk.
   * @throws IOException I/O Exception.
   */
  public static final JSONThunk readJSON(final JSONElement el, final IdManager mng)
      throws IOException {
    if(el.isString()) return new JSONThunk(mng, el.string());
    el.expectObject();
    final JSONThunk thunk;
    if(el.hasValue("id")) {
      thunk = mng.getForId(el.getString("id", null));
    } else {
      thunk = new JSONThunk(mng);
    }
    loop: for(final String k : el.getKeys()) {
      switch(k) {
        case "id":
          continue loop;
        case "type":
          thunk.setType(el.getString(k, null));
          continue loop;
        case "args":
          thunk.setConstructor(el.getString(k, null));
          continue loop;
      }
      final JSONElement v = el.getValue(k);
      thunk.addField(k, readJSON(v, mng));
    }
    return thunk;
  }

}
