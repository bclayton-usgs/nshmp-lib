package gov.usgs.earthquake.nshmp.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.DoubleStream;

import com.google.common.primitives.Doubles;

/**
 * An immutable sequence of xy-value pairs that is iterable ascending in x.
 * 
 * <p>For mutable y-values, use the {@link MutableXySequence} sub-type. Methods
 * and classes in this package will always return a {@link MutableXySequence} if
 * the instance is, in fact, mutable. Mutable sequences should not be considered
 * thread safe.
 * 
 * <p>All data supplied to the factory methods in this interface is copied
 * unless it is not necessary to do so. Users should consider {@link #copyOf()}
 * where possible as x-values will never be replicated in memory.
 *
 * @author Peter Powers
 * @see MutableXySequence
 */
public interface XySequence extends Iterable<XyPoint> {

  /**
   * Create a new, immutable sequence from the supplied value arrays.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  static XySequence create(
      double[] xs,
      double[] ys) {

    return Sequences.construct(
        Arrays.copyOf(xs, xs.length),
        Arrays.copyOf(ys, ys.length));
  }

  /**
   * Create a new, immutable sequence from the supplied value collections.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  static XySequence create(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys) {

    return Sequences.construct(
        Doubles.toArray(xs),
        Doubles.toArray(ys));
  }

  /**
   * Create an immutable copy of the supplied {@code sequence}.
   *
   * @param sequence to copy
   */
  static XySequence copyOf(XySequence sequence) {
    return (sequence.getClass().equals(ArrayXySequence.class))
        ? sequence
        : new ArrayXySequence(sequence, false);
  }

  /**
   * Return the x-value at {@code index}.
   * @param index to retrieve
   * @throws IndexOutOfBoundsException if the index is out of range (
   *         {@code index < 0 || index >= size()})
   */
  double x(int index);

  /**
   * Return the y-value at {@code index}.
   * @param index to retrieve
   * @throws IndexOutOfBoundsException if the index is out of range (
   *         {@code index < 0 || index >= size()})
   */
  double y(int index);

  /**
   * Return an immutable {@code List} of the sequence x-values.
   */
  List<Double> xValues();

  /**
   * Return an immutable {@code List} of the sequence y-values.
   */
  List<Double> yValues();

  /**
   * Return the number of points in this sequence.
   */
  int size();

  /**
   * The first {@link XyPoint} in this sequence.
   */
  XyPoint min();

  /**
   * The last {@link XyPoint} in this sequence.
   */
  XyPoint max();

  /**
   * Returns {@code true} if all y-values are 0.0; {@code false} otherwise.
   */
  boolean isClear();

  /**
   * Returns a new sequence that has had all leading and trailing zero-valued
   * points ({@code y = 0}) removed. Any zero-valued points in the middle of
   * this sequence are ignored.
   * 
   * @throws IllegalStateException if {@link #isClear() this.isClear()} as empty
   *         sequences are not permitted
   */
  XySequence trim();

}
