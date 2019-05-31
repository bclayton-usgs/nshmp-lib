package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.primitives.Doubles.asList;
import static gov.usgs.earthquake.nshmp.Text.NEWLINE;
import static gov.usgs.earthquake.nshmp.data.DoubleData.checkDelta;
import static java.util.Collections.unmodifiableList;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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

  static abstract class AbstractArray implements IntervalArray {

    final double rowMin;
    final double rowMax;
    final double rowΔ;
    final double[] rows;

    private AbstractArray(IntervalArray.Builder builder) {
      this.rowMin = builder.rowMin();
      this.rowMax = builder.rowMax();
      this.rowΔ = builder.rowΔ();
      this.rows = builder.rows();
    }

    @Override
    public double get(double rowValue) {
      int iRow = indexOf(rowMin, rowΔ, rowValue, rows.length);
      return get(iRow);
    }

    @Override
    public List<Double> rows() {
      return unmodifiableList(asList(rows));
    }

    @Override
    public double rowMin() {
      return rowMin;
    }

    @Override
    public double rowMax() {
      return rowMax;
    }

    @Override
    public double rowΔ() {
      return rowΔ;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      appendArrayKeys(sb, "", rows());
      appendArrayValues(sb, values().yValues().boxed().collect(Collectors.toList()));
      return sb.toString();
    }
  }

  static final class DefaultArray extends AbstractArray {

    final double[] data;

    DefaultArray(IntervalArray.Builder builder) {
      super(builder);
      data = builder.data();
    }

    @Override
    public double get(int rowIndex) {
      return data[rowIndex];
    }

    @Override
    public XySequence values() {
      return new ArrayXySequence(rows, data);
    }

    @Override
    public double sum() {
      return DoubleData.sum(data);
    }

    @Override
    public int minIndex() {
      return Indexing.minIndex(data);
    }

    @Override
    public int maxIndex() {
      return Indexing.maxIndex(data);
    }
  }

  static abstract class AbstractTable implements IntervalTable {

    final double rowMin;
    final double rowMax;
    final double rowΔ;
    final double[] rows;

    final double columnMin;
    final double columnMax;
    final double columnΔ;
    final double[] columns;

    private AbstractTable(IntervalTable.Builder builder) {
      rowMin = builder.rowMin();
      rowMax = builder.rowMax();
      rowΔ = builder.rowΔ();
      rows = builder.rows();

      columnMin = builder.columnMin();
      columnMax = builder.columnMax();
      columnΔ = builder.columnΔ();
      columns = builder.columns();
    }

    @Override
    public double get(double rowValue, double columnValue) {
      int iRow = indexOf(rowMin, rowΔ, rowValue, rows.length);
      int iColumn = indexOf(columnMin, columnΔ, columnValue, columns.length);
      return get(iRow, iColumn);
    }

    @Override
    public XySequence row(double rowValue) {
      int rowIndex = indexOf(rowMin, rowΔ, rowValue, rows.length);
      return row(rowIndex);
    }

    @Override
    public List<Double> rows() {
      return unmodifiableList(asList(rows));
    }

    @Override
    public List<Double> columns() {
      return unmodifiableList(asList(columns));
    }

    @Override
    public double rowMin() {
      return rowMin;
    }

    @Override
    public double rowMax() {
      return rowMax;
    }

    @Override
    public double rowΔ() {
      return rowΔ;
    }

    @Override
    public double columnMin() {
      return columnMin;
    }

    @Override
    public double columnMax() {
      return columnMax;
    }

    @Override
    public double columnΔ() {
      return columnΔ;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      List<Double> rows = rows();
      appendArrayKeys(sb, "          ", columns());
      for (int i = 0; i < rows.size(); i++) {
        sb.append(String.format(KEY_WITH_BRACKETS, rows.get(i)));
        appendArrayValues(sb, row(i).yValues().boxed().collect(Collectors.toList()));
      }
      return sb.toString();
    }
  }

  static final class DefaultTable extends AbstractTable {

    final double[][] data;

    DefaultTable(IntervalTable.Builder builder) {
      super(builder);
      data = builder.data();
    }

    private DefaultTable(IntervalTable.Builder builder, double[][] data) {
      super(builder);
      this.data = data;
    }

    @Override
    public double get(int rowIndex, int columnIndex) {
      return data[rowIndex][columnIndex];
    }

    @Override
    public XySequence row(int rowIndex) {
      return new ArrayXySequence(columns, data[rowIndex]);
    }

    @Override
    public IntervalArray collapse() {
      return IntervalArray.Builder
          .withRows(rowMin, rowMax, rowΔ)
          .add(DoubleData.collapse(data))
          .build();
    }

    @Override
    public int[] minIndex() {
      return Indexing.minIndex(data);
    }

    @Override
    public int[] maxIndex() {
      return Indexing.maxIndex(data);
    }
  }

  static abstract class AbstractVolume implements IntervalVolume {

    final double rowMin;
    final double rowMax;
    final double rowΔ;
    final double[] rows;

    final double columnMin;
    final double columnMax;
    final double columnΔ;
    final double[] columns;

    final double levelMin;
    final double levelMax;
    final double levelΔ;
    final double[] levels;

    private AbstractVolume(IntervalVolume.Builder builder) {
      rowMin = builder.rowMin();
      rowMax = builder.rowMax();
      rowΔ = builder.rowΔ();
      rows = builder.rows();

      columnMin = builder.columnMin();
      columnMax = builder.columnMax();
      columnΔ = builder.columnΔ();
      columns = builder.columns();

      levelMin = builder.levelMin();
      levelMax = builder.levelMax();
      levelΔ = builder.levelΔ();
      levels = builder.levels();
    }

    @Override
    public double get(double rowValue, double columnValue, double levelValue) {
      int iRow = indexOf(rowMin, rowΔ, rowValue, rows.length);
      int iColumn = indexOf(columnMin, columnΔ, columnValue, columns.length);
      int iLevel = indexOf(levelMin, levelΔ, levelValue, levels.length);
      return get(iRow, iColumn, iLevel);
    }

    @Override
    public XySequence column(double rowValue, double columnValue) {
      int iRow = indexOf(rowMin, rowΔ, rowValue, rows.length);
      int iColumn = indexOf(columnMin, columnΔ, columnValue, columns.length);
      return column(iRow, iColumn);
    }

    @Override
    public List<Double> rows() {
      return unmodifiableList(asList(rows));
    }

    @Override
    public List<Double> columns() {
      return unmodifiableList(asList(columns));
    }

    @Override
    public List<Double> levels() {
      return unmodifiableList(asList(levels));
    }

    @Override
    public double rowMin() {
      return rowMin;
    }

    @Override
    public double rowMax() {
      return rowMax;
    }

    @Override
    public double rowΔ() {
      return rowΔ;
    }

    @Override
    public double columnMin() {
      return columnMin;
    }

    @Override
    public double columnMax() {
      return columnMax;
    }

    @Override
    public double columnΔ() {
      return columnΔ;
    }

    @Override
    public double levelMin() {
      return levelMin;
    }

    @Override
    public double levelMax() {
      return levelMax;
    }

    @Override
    public double levelΔ() {
      return levelΔ;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      List<Double> rows = rows();
      List<Double> columns = columns();
      appendArrayKeys(sb, "                    ", levels());
      for (int i = 0; i < rows.size(); i++) {
        for (int j = 0; j < columns.size(); j++) {
          sb.append(String.format(KEY_WITH_BRACKETS, rows.get(i)));
          sb.append(String.format(KEY_WITH_BRACKETS, columns.get(j)));
          appendArrayValues(sb, column(i, j).yValues().boxed().collect(Collectors.toList()));
        }
      }
      return sb.toString();
    }
  }

  static final class DefaultVolume extends AbstractVolume {

    final double[][][] data;

    DefaultVolume(IntervalVolume.Builder builder) {
      super(builder);
      data = builder.data();
    }

    @Override
    public double get(int rowIndex, int columnIndex, int levelIndex) {
      return data[rowIndex][columnIndex][levelIndex];
    }

    @Override
    public XySequence column(int rowIndex, int columnIndex) {
      return new ArrayXySequence(levels, data[rowIndex][columnIndex]);
    }

    @Override
    public IntervalTable collapse() {
      IntervalTable.Builder builder = new IntervalTable.Builder()
          .rows(rowMin, rowMax, rowΔ)
          .columns(columnMin, columnMax, columnΔ);

      return new DefaultTable(builder, DoubleData.collapse(data));
    }

    @Override
    public int[] minIndex() {
      return Indexing.minIndex(data);
    }

    @Override
    public int[] maxIndex() {
      return Indexing.maxIndex(data);
    }
  }

  /* String utilities */

  private static void appendArrayKeys(StringBuilder sb, String prefix, Collection<Double> values) {
    sb.append(prefix);
    sb.append(Text.toString(values, KEY_FORMAT, DELIMITER, true, true));
    sb.append(NEWLINE);
  }

  private static void appendArrayValues(StringBuilder sb, Collection<Double> values) {
    String dataLine = Text.toString(values, DATA_FORMAT, DELIMITER, true, true);
    dataLine = dataLine.replace("0.0,", "     0.0,");
    dataLine = dataLine.replace("0.0]", "     0.0]");
    sb.append(dataLine);
    sb.append(NEWLINE);
  }

  private static final String KEY_FORMAT = "%8.2f";
  private static final String KEY_WITH_BRACKETS = "[%7.2f] ";
  private static final String DATA_FORMAT = "%7.2e";
  private static final String DELIMITER = ", ";

}
