package jkanvas.io.json;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * Reads JSON content.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class JSONReader {

  /** The push-back reader. */
  private PushbackReader r;
  /** The root element. */
  private JSONElement root;

  /**
   * Creates a JSON reader for the given reader.
   * 
   * @param r The reader.
   */
  public JSONReader(final Reader r) {
    this.r = new PushbackReader(Objects.requireNonNull(r));
    root = null;
  }

  /**
   * Getter. This method automatically closes the input stream.
   * 
   * @return The root element of the JSON document.
   * @throws IOException I/O Exception.
   */
  public JSONElement get() throws IOException {
    if(r != null) {
      root = read(null);
      eatWhitespace();
      if(!isEOF()) {
        final char c = next();
        try {
          if(r != null) {
            r.close();
            r = null;
          }
          throw new IllegalStateException("unexpected character: '" + c + "'");
        } catch(final IOException e) {
          throw new IllegalStateException("EOF not reached", e);
        }
      }
    }
    return root;
  }

  /**
   * Reads an JSON element.
   * 
   * @param name The name for the element.
   * @return The element.
   * @throws IOException I/O Exception.
   */
  private JSONElement read(final String name) throws IOException {
    eatWhitespace();
    ensureOpen();
    switch(peek()) {
      case '{':
        return readObj(name);
      case '[':
        return readArr(name);
      case '"':
        return new JSONElement(name, readStr());
      default:
        if(isNumberStart(peek())) return readNumber(name);
        throw new IllegalStateException("unexpected character: " + next());
    }
  }

  /**
   * Reads an object.
   * 
   * @param name The name for the element.
   * @return The element.
   * @throws IOException I/O Exception.
   */
  private JSONElement readObj(final String name) throws IOException {
    expect('{');
    final JSONElement res = new JSONElement(name, true);
    for(;;) {
      eatWhitespace();
      if(peek() == '}') {
        break;
      }
      final String elName = readStr();
      eatWhitespace();
      expect(':');
      final JSONElement el = read(elName);
      res.addValue(el);
      eatWhitespace();
      if(peek() == '}') {
        break;
      }
      expect(',');
    }
    expect('}');
    return res;
  }

  /**
   * Reads an array.
   * 
   * @param name The name for the element.
   * @return The element.
   * @throws IOException I/O Exception.
   */
  private JSONElement readArr(final String name) throws IOException {
    expect('[');
    final JSONElement res = new JSONElement(name, false);
    for(;;) {
      eatWhitespace();
      if(peek() == ']') {
        break;
      }
      final JSONElement el = read(null);
      res.addElement(el);
      eatWhitespace();
      if(peek() == ']') {
        break;
      }
      expect(',');
    }
    expect(']');
    return res;
  }

  /**
   * Reads a in quotes enclosed string.
   * 
   * @return The string read.
   * @throws IOException I/O Exception.
   */
  private String readStr() throws IOException {
    expect('"');
    final StringBuilder sb = new StringBuilder();
    while(peek() != '"') {
      final char c = next();
      if(c == '\\') {
        final char seq = next();
        switch(seq) {
          case 'n':
            sb.append('\n');
            break;
          case 'r':
            sb.append('\r');
            break;
          case 't':
            sb.append('\t');
            break;
          case '"':
            sb.append('"');
            break;
          default:
            throw new IllegalStateException("illegal escape: '\\" + seq + "'");
        }
      } else {
        sb.append(c);
      }
      ensureOpen();
    }
    expect('"');
    return sb.toString();
  }

  /**
   * Reads a plain number.
   * 
   * @param name The name for the element.
   * @return The element.
   * @throws IOException I/O Exception.
   */
  private JSONElement readNumber(final String name) throws IOException {
    final StringBuilder sb = new StringBuilder();
    out: while(!isWhitespace(peek())) {
      switch(peek()) {
        case ',':
        case ']':
        case '}':
          break out;
      }
      sb.append(next());
    }
    final String str = sb.toString();
    try {
      Double.parseDouble(str);
    } catch(final NumberFormatException e) {
      throw new IllegalStateException("not a number: \"" + str + "\"");
    }
    return new JSONElement(name, str);
  }

  /**
   * Checks whether EOF is reached.
   * 
   * @return Whether EOF is reached.
   */
  private boolean isEOF() {
    return r == null;
  }

  /** Ensures that the reader is still open. */
  private void ensureOpen() {
    if(isEOF()) throw new IllegalStateException("early EOF!");
  }

  /**
   * Peeks at the next character.
   * 
   * @return The next character.
   * @throws IOException I/O Exception.
   */
  private char peek() throws IOException {
    final char c = next();
    if(!isEOF()) {
      r.unread(c);
    }
    return c;
  }

  /**
   * Reads the next character.
   * 
   * @return The next character.
   * @throws IOException I/O Exception.
   */
  private char next() throws IOException {
    if(!isEOF()) {
      final int c = r.read();
      if(c >= 0) return (char) c;
      r.close();
      r = null;
    }
    return ' ';
  }

  /**
   * Expects the given character.
   * 
   * @param expect The expected character.
   * @throws IOException I/O Exception.
   */
  private void expect(final char expect) throws IOException {
    final char c = next();
    if(c != expect) throw new IllegalArgumentException(
        "expected '" + expect + "' got '" + c + "'");
  }

  /**
   * Checks whether the given character is a whitespace character.
   * 
   * @param c The character.
   * @return Whether it is a whitespace character.
   */
  private static boolean isWhitespace(final char c) {
    return " \t\r\n".indexOf(c) >= 0;
  }

  /**
   * Checks whether the given character can start a number.
   * 
   * @param c The character.
   * @return Whether it can start a number.
   */
  private static boolean isNumberStart(final char c) {
    return "0123456789-.".indexOf(c) >= 0;
  }

  /**
   * Reads until a non-whitespace character is found or EOF is reached.
   * 
   * @throws IOException I/O Exception.
   */
  private void eatWhitespace() throws IOException {
    while(!isEOF()) {
      if(!isWhitespace(peek())) return;
      next();
    }
  }

  /**
   * JSON test application.
   * 
   * @param args Ignored.
   * @throws IOException I/O Exception.
   */
  public static void main(final String[] args) throws IOException {
    final String s = "{" +
        " \"foo\": 3, " +
        " \"bar\"   :\"text\"     ,\"baz\":[\"a\",3], \"obj\": {}" +
        "}";
    System.out.println("input:");
    System.out.println(s);
    System.out.println();
    System.out.println("output:");
    final String out = new JSONReader(new StringReader(s)).get().toString();
    System.out.println(out);
    System.out.println("idempotence:");
    System.out.println(new JSONReader(new StringReader(out)).get());
  }

}
