package gov.usgs.earthquake.nshmp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.CaseFormat;
import com.google.common.base.CharMatcher;
import com.google.common.base.Converter;
import com.google.common.base.Enums;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.StandardSystemProperty;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Miscellaneous {@code String} utilities.
 *
 * @author Peter Powers
 */
public class Text {

  /** System specific newline string. */
  public static final String NEWLINE = StandardSystemProperty.LINE_SEPARATOR.value();

  /** Null string ("null"). */
  public static final String NULL = "null";

  /** The column on which to align values in a log entry. */
  public static final int LOG_VALUE_COLUMN = 32;

  private static final int LOG_INDENT_SIZE = 8;

  /** A newline plus the number of spaces to indent multiline log entries. */
  public static final String LOG_INDENT = NEWLINE + Strings.repeat(" ", LOG_INDENT_SIZE);

  /**
   * Whitespace {@link Splitter splitter} that omits empty strings. Shortcut
   * for: {@code Splitter.on(CharMatcher.whitespace()).omitEmptyStrings();}
   */
  public static final Splitter WHITESPACE_SPLITTER =
      Splitter.on(CharMatcher.whitespace()).omitEmptyStrings();

  /**
   * Verifies that the supplied {@code String} is neither {@code null} or empty.
   * Method returns the supplied value and can be used inline.
   *
   * @param name to verify
   * @throws IllegalArgumentException if name is {@code null} or empty
   */
  public static String validateName(String name) {
    checkArgument(
        !Strings.nullToEmpty(name).trim().isEmpty(),
        "Name may not be empty or null");
    return name;
  }

  /**
   * Returns a {@code Converter} for use with enums whose serialized form is a
   * format other than {@link CaseFormat#UPPER_UNDERSCORE}. This converter
   * assumes the values of any supplied enum are declared using
   * {@code UPPER_UNDERSCORE}; results are not guaranteed if they are not.
   * 
   * @param enumType to convert
   * @param caseFormat for conversion
   */
  public static <E extends Enum<E>> Converter<E, String> enumStringConverter(
      Class<E> enumType,
      CaseFormat caseFormat) {

    return new EnumStringConverter<>(
        checkNotNull(enumType),
        checkNotNull(caseFormat));
  }

  private static final class EnumStringConverter<E extends Enum<E>> extends Converter<E, String> {

    final Class<E> type;
    final Converter<String, String> delegate;

    private EnumStringConverter(Class<E> type, CaseFormat stringFormat) {
      this.type = type;
      this.delegate = CaseFormat.UPPER_UNDERSCORE.converterTo(stringFormat);
    }

    @Override
    protected String doForward(E e) {
      return delegate.convert(e.name());
    }

    @Override
    protected E doBackward(String s) {
      return Enum.valueOf(type, delegate.reverse().convert(s));
    }
  }

  /**
   * Returns a Gson {@code TypeAdapter} (serializer, deserializer) for enum
   * fields that uses the specified {@code Converter}.
   * 
   * @param converter to use for {@code CaseFormat} conversions
   */
  public static <E extends Enum<E>> TypeAdapter<E> enumSerializer(
      Converter<E, String> converter) {
    return new EnumAdapter<>(checkNotNull(converter));
  }

  /* Serialize using the enum Converter. */
  static final class EnumAdapter<E extends Enum<E>> extends TypeAdapter<E> {

    private final Converter<E, String> converter;

    EnumAdapter(Converter<E, String> converter) {
      this.converter = converter;
    }

    @Override
    public void write(JsonWriter out, E value) throws IOException {
      out.value(converter.convert(value));
    }

    @Override
    public E read(JsonReader in) throws IOException {
      return converter.reverse().convert(in.nextString());
    }
  }

  /**
   * Returns a string containing the string representation of each of
   * {@code parts} joined with {@code delimiter}.
   *
   * @param parts the objects to join
   * @param delimiter the {@link Delimiter} to join on
   * @see Joiner
   */
  public static String join(Iterable<?> parts, Delimiter delimiter) {
    return delimiter.joiner().join(parts);
  }

  /**
   * Split a {@code sequence} into string components and make them available
   * through a (possibly-lazy) {@code Iterator}.
   *
   * @param sequence the sequence of characters to split
   * @param delimiter the {@link Delimiter} to split on
   * @see Splitter
   */
  public static Iterable<String> split(CharSequence sequence, Delimiter delimiter) {
    return delimiter.splitter().split(sequence);
  }

  /**
   * Split a {@code sequence} into string components and make them available
   * through an immutable {@code List}.
   *
   * @param sequence the sequence of characters to split
   * @param delimiter the {@link Delimiter} to split on
   */
  public static List<String> splitToList(CharSequence sequence, Delimiter delimiter) {
    return delimiter.splitter().splitToList(sequence);
  }

  /**
   * Split {@code sequence} into {@code Double} components and make them
   * available through an immutable {@code List}.
   *
   * @param sequence the sequence of characters to split
   * @param delimiter the {@link Delimiter} to split on
   */
  public static List<Double> splitToDoubleList(CharSequence sequence, Delimiter delimiter) {
    return FluentIterable
        .from(split(sequence, delimiter))
        .transform(Doubles.stringConverter())
        .toList();
  }

  /**
   * Delimiter identifiers, each of which can provide a {@link Joiner} and
   * {@link Splitter}.
   */
  public enum Delimiter {

    /** Colon (':') delimiter. */
    COLON(':'),

    /** Comma (',') delimiter. */
    COMMA(','),

    /** Dash ('-') delimiter. */
    DASH('-'),

    /** Period ('.') delimiter. */
    PERIOD('.'),

    /** Forward-slash ('/') delimiter. */
    SLASH('/'),

    /**
     * Whitespace (' ') delimiter.
     * @see CharMatcher#whitespace()
     */
    SPACE(' ', CharMatcher.whitespace()),

    /** Underscore ('_') delimiter. */
    UNDERSCORE('_');

    private Joiner joiner;
    private Splitter splitter;

    private Delimiter(char separator) {
      joiner = Joiner.on(separator).skipNulls();
      splitter = Splitter.on(separator).omitEmptyStrings().trimResults();
    }

    private Delimiter(char joinSeparator, CharMatcher splitMatcher) {
      joiner = Joiner.on(joinSeparator).skipNulls();
      splitter = Splitter.on(splitMatcher).omitEmptyStrings().trimResults();
    }

    /**
     * Returns a null-skipping {@link Joiner} on this {@code Delimiter}.
     * @see Joiner#skipNulls()
     */
    public Joiner joiner() {
      return joiner;
    }

    /**
     * Returns an empty-string-omitting and result-trimming {@link Splitter} on
     * this {@code Delimiter}.
     *
     * @see Splitter#omitEmptyStrings()
     * @see Splitter#trimResults()
     */
    public Splitter splitter() {
      return splitter;
    }
  }

  /**
   * Return a {@code String} representation of an {@code Iterable<Enum>} where
   * {@code Enum.name()} is used instead of {@code Enum.toString()}.
   *
   * @param iterable to process
   * @param enumClass
   */
  public static <E extends Enum<E>> String enumsToString(Iterable<E> iterable,
      Class<E> enumClass) {
    return addBrackets(FluentIterable
        .from(iterable)
        .transform(Enums.stringConverter(enumClass).reverse())
        .join(Delimiter.COMMA.joiner()));
  }

  /**
   * Convert an {@code Enum.name()} to a space-delimited presentation-friendly
   * string.
   *
   * @param e the {@code Enum} to generate label for
   * @param capitalize true if first letter of each word should be capitalized;
   *        false if letters should all be lowercase
   */
  public static String enumLabelWithSpaces(Enum<? extends Enum<?>> e, boolean capitalize) {
    return join(splitEnum(e, capitalize), Delimiter.SPACE);
  }

  /**
   * Convert an {@code Enum.name()} to a dash-delimited presentation-friendly
   * string.
   *
   * @param e the {@code Enum} to generate label for
   * @param capitalize true if fist letter of each word should be capitalized;
   *        false if letters should all be lowercase
   */
  public static String enumLabelWithDashes(Enum<? extends Enum<?>> e, boolean capitalize) {
    return join(splitEnum(e, capitalize), Delimiter.DASH);
  }

  /* Splits and possibly capitalizes an enum.name() */
  private static List<String> splitEnum(Enum<? extends Enum<?>> e, boolean capitalize) {
    Iterable<String> sources = split(e.name(), Delimiter.UNDERSCORE);
    List<String> results = Lists.newArrayList();
    for (String s : sources) {
      results.add(capitalize ? capitalize(s) : s.toLowerCase());
    }
    return results;
  }

  /**
   * Capitalize supplied string by converting the first {@code char} to
   * uppercase and all subsequent {@code char}s to lowercase.
   *
   * @param s the string to capitalize
   */
  public static String capitalize(String s) {
    return Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
  }

  /**
   * Convert a {@code Collection<Double>} to a string of the same format
   * returned by {@link Arrays#toString(double[])} and
   * {@link AbstractCollection#toString()} , but will format the values using
   * the supplied format string. The supplied {@code format} should match that
   * expected by {@code String.format(String, Object...)}
   *
   * @param values the values to convert
   * @param format a format string
   * @param delimiter the delimiter to use
   * @param brackets {@code true} if hard brackets should be added to start and
   *        end, {@code false} otherwise
   */
  public static String toString(Collection<Double> values, String format, String delimiter,
      boolean brackets, boolean cleanZeros) {
    String base = Joiner.on(delimiter).join(
        Iterables.transform(
            values,
            new FormatDoubleFunction(format, cleanZeros)::apply));
    return brackets ? addBrackets(base) : base;
  }

  /*
   * Adds brackets to the supplied string.
   */
  static String addBrackets(String s) {
    return '[' + s + ']';
  }

  private static class FormatDoubleFunction implements Function<Double, String> {
    private String format;
    private boolean cleanZeros;

    private FormatDoubleFunction(String format, boolean cleanZeros) {
      this.format = format;
      this.cleanZeros = cleanZeros;
    }

    @Override
    public String apply(Double value) {
      return (cleanZeros && value == 0.0) ? "0.0" : String.format(format, value);
    }
  }

}
