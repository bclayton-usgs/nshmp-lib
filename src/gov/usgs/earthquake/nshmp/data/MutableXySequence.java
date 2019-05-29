package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;

/**
 * Mutable variant of {@code XySequence}.
 *
 * @author Peter Powers
 */
public interface MutableXySequence extends XySequence {

  /**
   * Create a new sequence with mutable y-values from the supplied value arrays.
   * If the supplied y-value array is null, all y-values are initialized to 0.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with; may be null
   * @return a mutable, {@code double[]}-backed sequence
   * @throws NullPointerException if {@code xs} are null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  public static XySequence create(double[] xs, double[] ys) {
    return Sequences.create(xs, ys, true);
  }

  /**
   * Create a new sequence with mutable y-values from the supplied value
   * collections. If the y-value collection is null, all y-values are
   * initialized to 0.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with; may be null
   * @return a mutable, {@code double[]}-backed sequence
   * @throws NullPointerException if {@code xs} are null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  public static XySequence create(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys) {
    return Sequences.create(xs, ys, true);
  }

  /**
   * Create a mutable copy of the supplied {@code sequence}.
   *
   * @param sequence to copy
   * @return a mutable copy of the supplied {@code sequence}
   * @throws NullPointerException if the supplied {@code sequence} is null
   */
  public static XySequence copyOf(XySequence sequence) {
    return new MutableArrayXySequence(checkNotNull(sequence), false);
  }

  /**
   * Create a mutable copy of the supplied {@code sequence} with all y-values
   * reset to zero.
   *
   * @param sequence to copy
   * @return a mutable copy of the supplied {@code sequence}
   * @throws NullPointerException if the supplied {@code sequence} is null
   */
  public static XySequence emptyCopyOf(XySequence sequence) {
    return new MutableArrayXySequence(checkNotNull(sequence), true);
  }

  void set(int index, double value);

  XySequence add(double term);

  XySequence add(double[] ys);

  XySequence add(XySequence sequence);

  XySequence multiply(double scale);

  XySequence multiply(XySequence sequence);

  XySequence complement();

  XySequence clear();

}
