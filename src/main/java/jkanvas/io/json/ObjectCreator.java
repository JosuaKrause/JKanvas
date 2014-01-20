package jkanvas.io.json;

import java.io.IOException;

/**
 * Some sort of thunk or lazy object creator.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface ObjectCreator {

  /**
   * Getter.
   * 
   * @return Whether the thunk has already a type.
   */
  boolean hasType();

  /**
   * Sets the creation type of the object. The type cannot be set when the
   * creation string is present.
   * 
   * @param type The fully qualified type name.
   * @throws IOException I/O Exception.
   */
  void setType(String type) throws IOException;

  /**
   * Getter.
   * 
   * @return Whether a non default constructor is already assigned.
   */
  boolean hasConstructor();

  /**
   * Sets the constructor. Arguments are split via '{@code ,}'. The empty
   * (default) constructor is implicit and must not be defined with this method.
   * The constructor can only be set once.
   * 
   * @param args The constructor string.
   */
  void setConstructor(String args);

  /**
   * Getter.
   * 
   * @param name The name.
   * @return Whether the field already exists.
   */
  boolean hasField(String name);

  /**
   * Adds a field to the thunk.
   * 
   * @param name The name of the field.
   * @param thunk The thunk.
   */
  void addField(String name, JSONThunk thunk);

  /**
   * Calls all setters.
   * 
   * @param o The object to call the setters on.
   * @throws IOException I/O Exception.
   */
  void callSetters(Object o) throws IOException;

}
