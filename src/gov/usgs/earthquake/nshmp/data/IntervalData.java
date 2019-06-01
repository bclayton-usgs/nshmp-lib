package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkState;
import static gov.usgs.earthquake.nshmp.Text.NEWLINE;
import static gov.usgs.earthquake.nshmp.data.DoubleData.checkDelta;

import java.util.Collection;

import javax.xml.crypto.Data;

import gov.usgs.earthquake.nshmp.Text;

/**
 * Static utilities for working with and concrete implementations of 1-, 2- and
 * 3-dimensional interval data containers.
 *
 * @author Peter Powers
 * @see IntervalArray
 * @see IntervalTable
 * @see IntervalVolume
 */
public final class IntervalData {

  /**
   * Create key values for use in an {@link IntervalArray},
   * {@link IntervalTable} or {@link IntervalVolume}. These classes call this
   * method directly when initializing their backing arrays. It is exposed for
   * convenience as there are circumstances where a reference to the row or
   * column keys is helpful to have when working with the builders for these
   * classes. Internally, this method calls
   * {@link Data#buildCleanSequence(double, double, double, boolean, int)} with
   * a precision value of 4 decimal places. This may change in the future.
   *
   * <p><b>Example:</b> {@code keys(5.0, 8.0, 1.0)} returns [5.5, 6.5, 7.5]
   *
   * @param min lower edge of lowermost bin
   * @param max upper edge of uppermost bin
   * @param Δ bin width
   */
  public static double[] keys(double min, double max, double Δ) {
    return keyArray(min, max, checkDelta(min, max, Δ));
  }

  /*
   * Create clean sequence of keys. Precision is curently set to 4 decimal
   * places.
   */
  private static double[] keyArray(double min, double max, double Δ) {
    double Δby2 = Δ / 2.0;
    return DoubleData.buildCleanSequence(
        min + Δby2,
        max - Δby2,
        Δ, true, 4);
  }

  /**
   * Compute an index from a minimum value, a value and an interval.
   *
   * @param min value
   * @param delta interval (i.e. bin width)
   * @param value for which to compute index
   * @param size of array or collection for which index is to be used
   * @throws IndexOutOfBoundsException if the index of {@code value} falls
   *         outside the allowed index range of {@code [0, size-1]}.
   * @throws IllegalArgumentException if {@code size} is negative
   */
  public static int indexOf(double min, double delta, double value, int size) {
    // casting to int floors value
    return checkElementIndex((int) ((value - min) / delta), size);
  }

  private static void checkDataState(double[] data, String label) {
    checkState(data != null, "%s data have not yet been fully specified", label);
  }

  /*
   * Ensure rows have been specified
   */
  static void checkDataState(double[] rows) {
    checkDataState(rows, "Row");
  }

  /*
   * Ensure rows and columns have been specified
   */
  static void checkDataState(double[] rows, double[] columns) {
    checkDataState(rows);
    checkDataState(columns, "Column");
  }

  /*
   * Ensure rows, columns, and levels have been specified
   */
  static void checkDataState(double[] rows, double[] columns, double[] levels) {
    checkDataState(rows, columns);
    checkDataState(levels, "Level");
  }

  /* String utilities */

  static void appendArrayKeys(StringBuilder sb, String prefix, Collection<Double> values) {
    sb.append(prefix);
    sb.append(Text.toString(values, KEY_FORMAT, DELIMITER, true, true));
    sb.append(NEWLINE);
  }

  static void appendArrayValues(StringBuilder sb, Collection<Double> values) {
    String dataLine = Text.toString(values, DATA_FORMAT, DELIMITER, true, true);
    dataLine = dataLine.replace("0.0,", "     0.0,");
    dataLine = dataLine.replace("0.0]", "     0.0]");
    sb.append(dataLine);
    sb.append(NEWLINE);
  }

  static final String KEY_WITH_BRACKETS = "[%7.2f] ";
  private static final String KEY_FORMAT = "%8.2f";
  private static final String DATA_FORMAT = "%7.2e";
  private static final String DELIMITER = ", ";

}
