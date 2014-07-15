package jkanvas.io.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A JSON element.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public class JSONElement implements Iterable<JSONElement> {

  /**
   * The type of an {@link JSONElement}.
   *
   * @author Joschi <josua.krause@gmail.com>
   */
  public static enum JSONType {
    /** A simple string. */
    STRING,
    /** An array. */
    ARRAY,
    /** An object. */
    OBJECT,
    /** The null value. */
    NULL,
  }

  /** The name of the element or <code>null</code>. */
  private final String name;
  /** The type. */
  private final JSONType type;
  /** The string content or <code>null</code> if other type. */
  private final String str;
  /** The array or <code>null</code> if other type. */
  private final List<JSONElement> array;
  /** The object map or <code>null</code> if other type. */
  private final Map<String, JSONElement> object;

  /**
   * Creates a <code>null</code> value.
   *
   * @param name The name, may be <code>null</code>.
   */
  JSONElement(final String name) {
    this.name = name;
    type = JSONType.NULL;
    str = null;
    array = null;
    object = null;
  }

  /**
   * Creates a string element.
   *
   * @param name The name, may be <code>null</code>.
   * @param str The string content.
   */
  JSONElement(final String name, final String str) {
    this.name = name;
    type = JSONType.STRING;
    this.str = Objects.requireNonNull(str);
    array = null;
    object = null;
  }

  /**
   * Creates either an array or object.
   *
   * @param name The name of the element, may be <code>null</code>.
   * @param isObject Whether to create an object.
   */
  JSONElement(final String name, final boolean isObject) {
    this.name = name;
    str = null;
    if(isObject) {
      type = JSONType.OBJECT;
      object = new HashMap<>();
      array = null;
    } else {
      type = JSONType.ARRAY;
      array = new ArrayList<>();
      object = null;
    }
  }

  /**
   * Adds an element to the array.
   *
   * @param el The element to add.
   * @return The element.
   * @throws IllegalStateException If the type is not right.
   * @throws IllegalArgumentException If the element has a name.
   */
  JSONElement addElement(final JSONElement el) {
    if(array == null) throw new IllegalStateException(
        "attempt to add an array element to a non array");
    if(el.name() != null) throw new IllegalArgumentException(
        "el must be nameless: " + el.name());
    array.add(el);
    return el;
  }

  /**
   * Adds an element to the object.
   *
   * @param el The element.
   * @return The element.
   * @throws IllegalStateException If the type is not right.
   * @throws IllegalArgumentException If the element has no name.
   */
  JSONElement addValue(final JSONElement el) {
    if(object == null) throw new IllegalStateException(
        "attempt to add a value to a non object");
    final String key = el.name();
    if(key == null) throw new IllegalArgumentException("el must have name");
    if(object.containsKey(key)) throw new IllegalArgumentException(
        "duplicate key: " + key);
    object.put(key, el);
    return el;
  }

  /**
   * Ensures that the element has the given type.
   *
   * @param type The expected type.
   * @throws IllegalStateException When the type is not correct.
   */
  private void expectType(final JSONType type) {
    if(type() != type) throw new IllegalStateException(
        "wrong type: " + type + " expected: " + type());
  }

  /**
   * Expects the <code>null</code> type.
   *
   * @throws IllegalStateException When the type is not correct.
   */
  public void expectNull() {
    expectType(JSONType.NULL);
  }

  /**
   * Expects an object.
   *
   * @throws IllegalStateException When the type is not correct.
   */
  public void expectObject() {
    expectType(JSONType.OBJECT);
  }

  /**
   * Expects an array.
   *
   * @throws IllegalStateException When the type is not correct.
   */
  public void expectArray() {
    expectType(JSONType.ARRAY);
  }

  /**
   * Expects a string.
   *
   * @throws IllegalStateException When the type is not correct.
   */
  public void expectString() {
    expectType(JSONType.STRING);
  }

  /**
   * Whether the type of the element is the <code>null</code> type.
   *
   * @return Whether it is a <code>null</code> type.
   */
  public boolean isNull() {
    return type() == JSONType.NULL;
  }

  /**
   * Whether the type of the element is an object.
   *
   * @return Whether it is an object.
   */
  public boolean isObject() {
    return type() == JSONType.OBJECT;
  }

  /**
   * Whether the type of the element is an array.
   *
   * @return Whether it is an array.
   */
  public boolean isArray() {
    return type() == JSONType.ARRAY;
  }

  /**
   * Whether the type of the element is a string.
   *
   * @return Whether it is a string.
   */
  public boolean isString() {
    return type() == JSONType.STRING;
  }

  /**
   * Getter.
   *
   * @return The name of the element or <code>null</code> if it has none.
   */
  public String name() {
    return name;
  }

  /**
   * Getter.
   *
   * @return The type of the element.
   */
  protected JSONType type() {
    return type;
  }

  /**
   * Getter.
   *
   * @return The string content of the element.
   * @throws IllegalStateException If the type is not right.
   */
  public String string() {
    if(str == null) throw new IllegalStateException("not a string");
    return str;
  }

  /**
   * Getter.
   *
   * @return The number of elements in this element. The <code>null</code> type
   *         has the size 0.
   * @throws IllegalStateException If the type is not an array or an object.
   */
  public int size() {
    if(isString()) throw new IllegalStateException("not an array, object, or null");
    return array != null ? array.size() : object != null ? object.size() : 0;
  }

  /**
   * Getter.
   *
   * @param pos The position.
   * @return The element at the given position.
   * @throws IllegalStateException If the type is not right.
   */
  public JSONElement getAt(final int pos) {
    if(array == null) throw new IllegalStateException("not an array");
    return array.get(pos);
  }

  /**
   * Getter.
   *
   * @param key The key / name.
   * @return The element with the given name.
   * @throws IllegalStateException If the type is not right.
   */
  public JSONElement getValue(final String key) {
    if(object == null) throw new IllegalStateException("not an object");
    return object.get(key);
  }

  /**
   * Getter.
   *
   * @return The keys in this object.
   * @throws IllegalStateException If the type is not right.
   */
  public Set<String> getKeys() {
    if(object == null) throw new IllegalStateException("not an object");
    return Collections.unmodifiableSet(object.keySet());
  }

  /**
   * Whether the key is present.
   *
   * @param key The key / name.
   * @return Whether there exist a key with the given name.
   * @throws IllegalStateException If the type is not right.
   */
  public boolean hasValue(final String key) {
    return getValue(key) != null;
  }

  @Override
  public Iterator<JSONElement> iterator() {
    if(isString()) throw new IllegalStateException("not an array or object");
    if(isNull()) return Collections.emptyIterator();
    final Iterator<JSONElement> it =
        array != null ? array.iterator() : object.values().iterator();
    return new Iterator<JSONElement>() {

      @Override
      public boolean hasNext() {
        return it.hasNext();
      }

      @Override
      public JSONElement next() {
        return it.next();
      }

      // TODO #43 -- Java 8 simplification
      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }

    };
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    toString("", sb);
    return sb.toString();
  }

  /**
   * Appends the string representation of this element to the string builder.
   *
   * @param indent The indentation for lines.
   * @param sb The string builder.
   */
  private void toString(final String indent, final StringBuilder sb) {
    sb.append(indent);
    switch(type) {
      case ARRAY: {
        sb.append("[\n");
        final String innerIndent = indent + "  ";
        boolean first = true;
        for(final JSONElement el : this) {
          if(first) {
            first = false;
          } else {
            sb.append(",\n");
          }
          el.toString(innerIndent, sb);
        }
        sb.append("\n");
        sb.append(indent);
        sb.append("]");
        break;
      }
      case OBJECT: {
        sb.append("{\n");
        final String innerIndent = indent + "  ";
        boolean first = true;
        for(final JSONElement el : this) {
          if(first) {
            first = false;
          } else {
            sb.append(",\n");
          }
          sb.append(indent);
          str(sb, el.name());
          sb.append(":\n");
          el.toString(innerIndent, sb);
        }
        sb.append("\n");
        sb.append(indent);
        sb.append("}");
        break;
      }
      case STRING:
        str(sb, string());
        break;
      case NULL:
        sb.append("null");
        break;
      default:
        throw new AssertionError();
    }
  }

  /**
   * Appends an escaped string representation for the given string to the given
   * string builder.
   *
   * @param sb The string builder.
   * @param str The string.
   */
  private static void str(final StringBuilder sb, final String str) {
    sb.append('"');
    for(final char c : str.toCharArray()) {
      switch(c) {
        case '\n':
          sb.append("\\n");
          break;
        case '\r':
          sb.append("\\r");
          break;
        case '\t':
          sb.append("\\t");
          break;
        case '"':
          sb.append("\\\"");
          break;
        default:
          sb.append(c);
      }
    }
    sb.append('"');
  }

  // quick retrieving methods

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @param defaultValue The default value.
   * @return The double value of the given child.
   */
  public double getDouble(final String name, final double defaultValue) {
    if(!hasValue(name)) return defaultValue;
    return getDouble(name);
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @return The double value of the given child.
   */
  public double getDouble(final String name) {
    return Double.parseDouble(getString(name));
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @param defaultValue The default value.
   * @return The integer value of the given child.
   */
  public int getInt(final String name, final int defaultValue) {
    if(!hasValue(name)) return defaultValue;
    return getInt(name);
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @return The integer value of the given child.
   */
  public int getInt(final String name) {
    return Integer.parseInt(getString(name));
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @param defaultValue The default value.
   * @return The long value of the given child.
   */
  public long getLong(final String name, final long defaultValue) {
    if(!hasValue(name)) return defaultValue;
    return getLong(name);
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @return The long value of the given child.
   */
  public long getLong(final String name) {
    return Long.parseLong(getString(name));
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @param defaultValue The default value.
   * @return The string value of the given child.
   */
  public String getString(final String name, final String defaultValue) {
    if(!hasValue(name)) return defaultValue;
    return getString(name);
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @return The string value of the given child.
   */
  public String getString(final String name) {
    if(!hasValue(name)) throw new IllegalArgumentException("expected value for " + name);
    return getValue(name).string();
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @param defaultValue The default value.
   * @return The boolean value of the given child.
   */
  public boolean getBool(final String name, final boolean defaultValue) {
    if(!hasValue(name)) return defaultValue;
    return getBool(name);
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @return The boolean value of the given child.
   */
  public boolean getBool(final String name) {
    final String str = getString(name);
    if(str.equals("false")) return false;
    if(str.equals("true")) return true;
    throw new IllegalArgumentException("value must be either true or false: " + str);
  }

  /**
   * Whether the given child has a percentage value.
   *
   * @param name The name of the element.
   * @return Whether the given child has a percentage value.
   */
  public boolean hasPrecentage(final String name) {
    if(!hasValue(name)) return false;
    final JSONElement el = getValue(name);
    if(el.type() != JSONType.STRING) return false;
    return el.string().endsWith("%");
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @param defaultPercentage The default value.
   * @return The percentage value of the child as ratio between 0.0 and 1.0. A
   *         '%' is expected at the end of the string.
   */
  public double getPercentage(final String name, final double defaultPercentage) {
    if(!hasValue(name)) return defaultPercentage;
    return getPercentage(name);
  }

  /**
   * Getter.
   *
   * @param name The name of the value.
   * @return The percentage value of the child as ratio between 0.0 and 1.0. A
   *         '%' is expected at the end of the string.
   */
  public double getPercentage(final String name) {
    final String str = getString(name);
    if(!str.endsWith("%")) throw new IllegalArgumentException(
        "value must end in %: " + str);
    return Double.parseDouble(str.substring(0, str.length() - 1)) * 0.01;
  }

  /**
   * Creates a null element.
   * 
   * @param name The name of the element.
   * @return The element.
   */
  public static final JSONElement createNull(final String name) {
    return new JSONElement(name);
  }

  /**
   * Creates a text element.
   * 
   * @param name The name of the element.
   * @param text The text.
   * @return The text element.
   */
  public static final JSONElement createString(final String name, final String text) {
    return new JSONElement(name, text);
  }

  /**
   * Creates an array element.
   * 
   * @param name The name of the element.
   * @param elements The child elements.
   * @return The array element.
   */
  public static final JSONElement createArray(
      final String name, final Iterable<JSONElement> elements) {
    final JSONElement res = new JSONElement(name, false);
    for(final JSONElement e : elements) {
      res.addElement(e);
    }
    return res;
  }

  /**
   * Creates an object element.
   * 
   * @param name THe name of the element.
   * @param values The child elements.
   * @return The object element.
   */
  public static final JSONElement createObject(
      final String name, final Iterable<JSONElement> values) {
    final JSONElement res = new JSONElement(name, true);
    for(final JSONElement v : values) {
      res.addValue(v);
    }
    return res;
  }

}
