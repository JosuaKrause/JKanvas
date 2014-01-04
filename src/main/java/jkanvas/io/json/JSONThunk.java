package jkanvas.io.json;

import java.io.IOException;
import java.lang.reflect.Array;
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
  private final JSONManager mng;
  /** All used setters. */
  private final Map<String, JSONThunk> setters = new HashMap<>();
  /** The thunks for the constructor arguments. */
  private final Map<String, JSONThunk> constructorLookup = new HashMap<>();
  /** The constructor arguments. */
  private final List<String> constructor = new ArrayList<>();
  /** The creation string or <code>null</code>. */
  private final String str;
  /** The thunk array or <code>null</code>. */
  private final JSONThunk[] arr;
  /** The creation type of the thunk if it is not a simple string. */
  private Class<?> type;
  /** The actual object. */
  private Object obj;
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
  public JSONThunk(final JSONManager mng) {
    this.mng = Objects.requireNonNull(mng);
    str = null;
    arr = null;
  }

  /**
   * Creates a simple thunk.
   * 
   * @param mng The manager.
   * @param str The creation string.
   */
  public JSONThunk(final JSONManager mng, final String str) {
    this.mng = Objects.requireNonNull(mng);
    this.str = Objects.requireNonNull(str);
    arr = null;
  }

  /**
   * Creates an array thunk.
   * 
   * @param mng The manager.
   * @param arr The array.
   */
  public JSONThunk(final JSONManager mng, final JSONThunk[] arr) {
    this.mng = Objects.requireNonNull(mng);
    this.arr = Objects.requireNonNull(arr);
    str = null;
  }

  /**
   * Creates an already evaluated thunk.
   * 
   * @param obj The evaluated object.
   * @param mng The manager.
   */
  JSONThunk(final Object obj, final JSONManager mng) {
    this.mng = Objects.requireNonNull(mng);
    this.obj = Objects.requireNonNull(obj);
    str = null;
    arr = null;
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
    if(obj != null) throw new IllegalStateException("object already evaluated");
    if(str != null) throw new IllegalStateException("cannot define type for primitives");
    if(arr != null) throw new IllegalStateException("cannot define type for arrays");
    if(this.type != null) throw new IllegalStateException("type already defined");
    Objects.requireNonNull(type);
    this.type = type;
  }

  /**
   * Getter.
   * 
   * @return Whether the thunk has already a type.
   */
  public boolean hasType() {
    return str != null || arr != null || type != null;
  }

  /**
   * Sets the constructor. Arguments are split via '{@code ,}'. The empty
   * (default) constructor is implicit and must not be defined with this method.
   * The constructor can only be set once.
   * 
   * @param construct The constructor string.
   */
  public void setConstructor(final String construct) {
    if(obj != null) throw new IllegalStateException("object already evaluated");
    if(str != null) throw new IllegalStateException(
        "cannot define constructor for primitives");
    if(arr != null) throw new IllegalStateException(
        "cannot define constructor for arrays");
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
   * Getter.
   * 
   * @return Whether a non default constructor is already assigned.
   */
  public boolean hasConstructor() {
    return !constructor.isEmpty();
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
    if(obj != null) throw new IllegalStateException("object already evaluated");
    if(str != null) throw new IllegalStateException("cannot add field for primitives");
    if(arr != null) throw new IllegalStateException("cannot add field type for arrays");
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
   * Getter.
   * 
   * @param name The name.
   * @return Whether the field already exists.
   */
  public boolean hasField(final String name) {
    return constructorLookup.containsKey(name) || setters.containsKey(name);
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
    final Class<?> oc = o.getClass();
    if(!type.isAssignableFrom(oc)) throw new IOException(
        "invalid type: " + type.getSimpleName() + " <=!= " + oc.getSimpleName());
    return (T) o;
  }

  /**
   * Evaluates the thunk and returns the content. If the thunk is already
   * evaluated the content is returned directly.
   * 
   * @param <T> The expected type.
   * @param <C> The component type if it is an array.
   * @param type The expected type of the content.
   * @return The content.
   * @throws IOException I/O Exception.
   */
  public <T, C> T get(final Class<T> type) throws IOException {
    if(eval) throw new IOException("cyclic dependency!");
    if(obj != null) return cast(obj, type);
    eval = true;
    if(arr != null) {
      if(!type.isArray()) throw new IOException(
          "expected array type: " + type.getSimpleName());
      final Class<C> comp = (Class<C>) type.getComponentType();
      final C[] res = (C[]) Array.newInstance(comp, arr.length);
      for(int i = 0; i < res.length; ++i) {
        final C cur = arr[i].get(comp);
        res[i] = cast(cur, comp);
      }
      obj = res;
    } else if(str != null) {
      try {
        if(type.isAssignableFrom(Long.class)) {
          obj = Long.parseLong(str);
        } else if(type.isAssignableFrom(Integer.class)) {
          obj = Integer.parseInt(str);
        } else if(type.isAssignableFrom(Short.class)) {
          obj = Short.parseShort(str);
        } else if(type.isAssignableFrom(Byte.class)) {
          obj = Byte.parseByte(str);
        } else if(type.isAssignableFrom(Double.class)) {
          obj = Double.parseDouble(str);
        } else if(type.isAssignableFrom(Float.class)) {
          obj = Float.parseFloat(str);
        } else if(type.isAssignableFrom(String.class)) {
          obj = str;
        } else {
          // string is ID
          obj = mng.getForId(str).get(type);
        }
      } catch(final NumberFormatException e) {
        throw new IOException(
            "could not interpret \"" + str + "\" as " + type.getName(), e);
      }
    } else {
      obj = eval(type);
    }
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
   * @param mng The manager.
   * @return The thunk.
   * @throws IOException I/O Exception.
   */
  public static final JSONThunk readJSON(final JSONElement el, final JSONManager mng)
      throws IOException {
    if(el.isString()) return new JSONThunk(mng, el.string());
    if(el.isArray()) {
      final JSONThunk[] arr = new JSONThunk[el.size()];
      for(int i = 0; i < arr.length; ++i) {
        arr[i] = readJSON(el.getAt(i), mng);
      }
      return new JSONThunk(mng, arr);
    }
    el.expectObject();
    final JSONThunk thunk;
    if(el.hasValue("id")) {
      thunk = mng.getForId(el.getString("id", null));
    } else {
      thunk = new JSONThunk(mng);
    }
    JSONElement cur = el;
    while(cur != null) {
      JSONElement tmpl = null;
      loop: for(final String k : cur.getKeys()) {
        switch(k) {
          case "id":
            if(cur != el) throw new IOException("id in template not allowed");
            continue loop;
          case "type":
            if(cur != el && thunk.hasType()) {
              continue loop;
            }
            thunk.setType(cur.getString(k, null));
            continue loop;
          case "args":
            if(cur != el && thunk.hasConstructor()) {
              continue loop;
            }
            thunk.setConstructor(cur.getString(k, null));
            continue loop;
          case "template":
            if(tmpl != null) throw new IOException("double template definition");
            tmpl = mng.getTemplate(cur.getString(k, null));
            continue loop;
        }
        if(cur != el && thunk.hasField(k)) {
          continue loop;
        }
        final JSONElement v = cur.getValue(k);
        thunk.addField(k, readJSON(v, mng));
      }
      cur = tmpl;
      tmpl = null;
    }
    return thunk;
  }

}
