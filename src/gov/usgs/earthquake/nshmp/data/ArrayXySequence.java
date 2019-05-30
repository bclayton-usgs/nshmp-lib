package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkState;
import static gov.usgs.earthquake.nshmp.Text.NEWLINE;

import java.util.Arrays;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.base.Joiner;

/**
 * An immutable, array-backed implementation of {@code XySequence}.
 *
 * @author Peter Powers
 */
class ArrayXySequence implements XySequence {

  final double[] xs;
  final double[] ys;

  ArrayXySequence(double[] xs, double[] ys) {
    this.xs = xs;
    this.ys = ys;
  }

  ArrayXySequence(XySequence sequence, boolean clear) {
    /*
     * This constructor provides the option to 'clear' (or zero-out) the
     * y-values when copying, however, in practice, it is only ever used when
     * creating mutable instances.
     *
     * The covariant cast below is safe as all implementations descend from this
     * class.
     */
    ArrayXySequence s = (ArrayXySequence) sequence;
    xs = s.xs;
    ys = clear ? new double[xs.length] : Arrays.copyOf(s.ys, s.ys.length);
  }

  @Override
  public final double x(int index) {
    checkElementIndex(index, xs.length);
    return xUnchecked(index);
  }

  final double xUnchecked(int index) {
    return xs[index];
  }

  @Override
  public final double y(int index) {
    checkElementIndex(index, ys.length);
    return yUnchecked(index);
  }

  final double yUnchecked(int index) {
    return ys[index];
  }

  @Override
  public final int size() {
    return xs.length;
  }

  @Override
  public XyPoint min() {
    return new Point(0);
  }

  @Override
  public XyPoint max() {
    return new Point(size() - 1);
  }

  @Override
  public final boolean isClear() {
    return DoubleData.areZeroValued(ys);
  }

  @Override
  public XySequence trim() {
    checkState(!this.isClear(), "XySequence.trim() not permitted for 'clear' sequences");
    int minIndex = DoubleData.firstNonZeroIndex(ys);
    int maxIndex = DoubleData.lastNonZeroIndex(ys) + 1;
    return new ArrayXySequence(
        Arrays.copyOfRange(xs, minIndex, maxIndex),
        Arrays.copyOfRange(ys, minIndex, maxIndex));
  }

  @Override
  public final boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ArrayXySequence)) {
      return false;
    }
    ArrayXySequence that = (ArrayXySequence) obj;
    return Arrays.equals(this.xs, that.xs) && Arrays.equals(this.ys, that.ys);
  }

  @Override
  public final int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(xs);
    result = prime * result + Arrays.hashCode(ys);
    return result;
  }

  @Override
  public DoubleStream xValues() {
    return Arrays.stream(xs);
  }

  @Override
  public DoubleStream yValues() {
    return Arrays.stream(ys);
  }

  @Override
  public Stream<XyPoint> stream() {
    return IntStream.range(0, size()).mapToObj(Point::new);
  }

  /*
   * Check the x-value object references; if mismatched, compare values.
   */
  ArrayXySequence validateSequence(ArrayXySequence that) {
    checkArgument(this.xs.hashCode() == that.xs.hashCode() ||
        Arrays.equals(this.xs, that.xs));
    return that;
  }

  @Override
  public String toString() {
    return new StringBuilder(getClass().getSimpleName())
        .append(":")
        .append(NEWLINE)
        .append(Joiner.on(NEWLINE).join(stream().iterator()))
        .toString();
  }

  class Point implements XyPoint {

    final int index;

    Point(int index) {
      this.index = index;
    }

    @Override
    public double x() {
      return xUnchecked(index);
    }

    @Override
    public double y() {
      return yUnchecked(index);
    }

    @Override
    public void set(double y) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
      return "XyPoint: [" + x() + ", " + y() + "]";
    }
  }

}
