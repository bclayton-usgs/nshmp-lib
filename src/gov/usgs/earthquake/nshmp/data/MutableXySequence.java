package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import com.google.common.primitives.Doubles;

/**
 * Mutable variant of {@code XySequence}.
 * 
 * <p>Methods and classes in this package will always return a
 * {@link MutableXySequence} if the instance is, in fact, mutable. Mutable
 * sequences should not be considered thread safe.
 * 
 * <p>The mutator methods ({@link #set(int, double)}, {@link #add(double),
 * etc...)} of this class return a reference to the {@code this} sequence for
 * use inline.
 *
 * <p>All data supplied to the factory methods in this interface is copied
 * unless it is not necessary to do so. Users should consider {@link #copyOf()}
 * where possible as x-values will never be replicated in memory.
 * 
 * @author Peter Powers
 */
public interface MutableXySequence extends XySequence {

  /**
   * Create a new sequence with mutable y-values from the supplied value arrays.
   * If the supplied y-value array is null, y-values are initialized to 0.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with; may be null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  static MutableXySequence create(double[] xs, double[] ys) {
    return Sequences.constructMutable(
        Arrays.copyOf(xs, xs.length),
        (ys == null)
            ? new double[xs.length]
            : Arrays.copyOf(ys, ys.length));
  }

  /**
   * Create a new sequence with mutable y-values from the supplied value
   * collections. If the supplied y-value collection is null, y-values are
   * initialized to 0.
   *
   * @param xs x-values to initialize sequence with
   * @param ys y-values to initialize sequence with; may be null
   * @throws IllegalArgumentException if {@code xs} and {@code ys} are not the
   *         same size
   * @throws IllegalArgumentException if {@code xs} does not increase
   *         monotonically or contains repeated values
   */
  static MutableXySequence create(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys) {

    return Sequences.constructMutable(
        Doubles.toArray(xs),
        (ys == null)
            ? new double[xs.size()]
            : Doubles.toArray(ys));
  }

  /**
   * Create a mutable copy of the supplied {@code sequence}.
   *
   * @param sequence to copy
   */
  static MutableXySequence copyOf(XySequence sequence) {
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
  static MutableXySequence emptyCopyOf(XySequence sequence) {
    return new MutableArrayXySequence(checkNotNull(sequence), true);
  }

  /**
   * Sets the y-{@code value} at {@code index}.
   * @param index of y-{@code value} to set.
   * @param value to set
   * @return {@code this} sequence, for use inline
   * @throws IndexOutOfBoundsException if the index is out of range (
   *         {@code index < 0 || index >= size()})
   */
  MutableXySequence set(int index, double value);

  /**
   * Add a {@code term} to the y-values of this sequence in place.
   *
   * @param term to add
   * @return {@code this} sequence, for use inline
   */
  MutableXySequence add(double term);

  /**
   * Add the supplied y-values to the y-values of this sequence in place.
   *
   * @param ys y-values to add
   * @return {@code this} sequence, for use inline
   */
  MutableXySequence add(double[] ys);

  /**
   * Add the y-values of a sequence to the y-values of this sequence in place.
   *
   * @param sequence to add
   * @return {@code this} sequence, for use inline
   * @throws IllegalArgumentException if
   *         {@code sequence.xValues() != this.xValues()}
   */
  MutableXySequence add(XySequence sequence);

  /**
   * Multiply ({@code scale}) the y-values of this sequence in place.
   *
   * @param scale factor
   * @return {@code this} sequence, for use inline
   */
  MutableXySequence multiply(double scale);

  /**
   * Multiply the y-values of this sequence by the y-values of another sequence
   * in place.
   *
   * @param sequence to multiply {@code this} sequence by
   * @return {@code this} sequence, for use inline
   * @throws IllegalArgumentException if
   *         {@code sequence.xValues() != this.xValues()}
   */
  MutableXySequence multiply(XySequence sequence);

  /**
   * Set the y-values of this sequence to their complement in place [
   * {@code 1 - y}]. Assumes this is a probability distribution limited to the
   * domain [0..1].
   *
   * @return {@code this} sequence, for use inline
   */
  MutableXySequence complement();

  /**
   * Sets all y-values to 0.
   *
   * @return {@code this} sequence, for use inline
   */
  MutableXySequence clear();

  @Override
  MutableXySequence trim();

  /**
   * Transforms all y-values in place using the supplied {@link Function}.
   *
   * @param function for transform
   * @return {@code this} sequence, for use inline
   */
  default MutableXySequence transform(Function<Double, Double> function) {
    throw new UnsupportedOperationException();
  }

}
