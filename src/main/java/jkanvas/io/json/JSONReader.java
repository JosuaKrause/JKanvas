package jkanvas.io.json;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Objects;

/**
 * Reads JSON content.
 * 
 * @author Joschi <josua.krause@gmail.com>
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
        next();
        try {
          if(r != null) {
            r.close();
            r = null;
          }
          throw new IllegalStateException("unexpected character: " + context(1));
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
        final String word = readWord();
        if("true".equals(word) || "false".equals(word)) return new JSONElement(name, word);
        if("null".equals(word)) return new JSONElement(name);
        if(!word.isEmpty()) throw new IllegalStateException(
            "unexpected word: " + context(word.length()));
        next();
        throw new IllegalStateException("unexpected character: " + context(1));
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
            throw new IllegalStateException("illegal escape: " + context(2));
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
   * Reads a single word without spaces or delimiter characters.
   * 
   * @return The read word.
   * @throws IOException I/O Exception.
   */
  private String readWord() throws IOException {
    final StringBuilder sb = new StringBuilder();
    while(!isWhitespace(peek())) {
      final char c = peek();
      if(",]}:".indexOf(c) >= 0) {
        break;
      }
      if("\\\"[{".indexOf(c) >= 0) {
        next();
        throw new IllegalStateException("unexpected character in word: " + context(1));
      }
      sb.append(next());
    }
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
      throw new IllegalStateException("not a number: " + context(str.length()));
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

  /** Whether the current character was read before. */
  private boolean again;

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
      again = true;
    }
    return c;
  }

  /** The number of characters used for context. */
  private static final int CONTEXT_SIZE = 16;

  /** Stores the current parsing context. */
  private final StringBuilder context = new StringBuilder();

  /**
   * Returns the context of the failing character or word. All faulty characters
   * must have been consumed.
   * 
   * @param length The length of the faulty word.
   * @return The context.
   */
  private String context(final int length) {
    final int l = context.length() - (again ? 1 : 0);
    final int start = Math.max(l - length - CONTEXT_SIZE, 0);
    final int end = Math.max(l - length, start);
    final String pre = context.substring(start, end);
    final String word = context.substring(end, l);
    final StringBuilder post = new StringBuilder();
    try {
      for(int i = 0; i < CONTEXT_SIZE; ++i) {
        if(isEOF() || "\r\n".indexOf(peek()) >= 0) {
          break;
        }
        post.append(next());
      }
    } catch(final IOException e) {
      // we are about to print a more important error message anyway
    }
    return (pre + " >" + word + "< " + post).trim();
  }

  /**
   * Reads the next character.
   * 
   * @return The next character.
   * @throws IOException I/O Exception.
   */
  private char next() throws IOException {
    final boolean isNew = !again;
    again = false;
    if(!isEOF()) {
      final int c = r.read();
      if(c >= 0) {
        if(isNew) {
          if("\r\n".indexOf(c) >= 0) {
            context.setLength(0);
          } else {
            context.append((char) c);
          }
        }
        return (char) c;
      }
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
        "expected '" + expect + "' got '" + c + "': " + context(1));
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
