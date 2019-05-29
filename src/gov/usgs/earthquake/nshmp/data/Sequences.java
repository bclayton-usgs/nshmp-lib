package gov.usgs.earthquake.nshmp.data;

import static com.google.common.base.Preconditions.checkArgument;
import static gov.usgs.earthquake.nshmp.data.DoubleData.areMonotonic;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.google.common.primitives.Doubles;

public class Sequences {

  /**
   * Adds {@code this} sequence to any exisiting sequence for {@code key} in the
   * supplied {@code map}. If {@code key} does not exist in the {@code map},
   * method puts a mutable copy of {@code this} in the map.
   * 
   * @param key for sequence to add
   * @param map of sequences to add to
   * @throws IllegalArgumentException if the x-values of added sequences to not
   *         match those of existing sequences
   */
  public static <E extends Enum<E>> void addToMap(
      E key,
      Map<E, MutableXySequence> map,
      XySequence sequence) {
    if (map.containsKey(key)) {
      map.get(key).add(sequence);
    } else {
      map.put(key, (MutableXySequence) XySequence.copyOf(sequence));
    }
  }

  static XySequence create(
      Collection<? extends Number> xs,
      Collection<? extends Number> ys,
      boolean mutable) {

    return construct(
        Doubles.toArray(xs),
        (ys == null) ? new double[xs.size()] : Doubles.toArray(ys),
        mutable);
  }

  static XySequence create(double[] xs, double[] ys, boolean mutable) {
    return Sequences.construct(
        Arrays.copyOf(xs, xs.length),
        (ys == null) ? new double[xs.length] : Arrays.copyOf(ys, ys.length),
        mutable);
  }

  static XySequence construct(double[] xs, double[] ys, boolean mutable) {
    checkArgument(xs.length > 0, "x-values may not be empty");
    checkArgument(xs.length == ys.length, "x- and y-values are different sizes");
    if (xs.length > 1) {
      checkArgument(areMonotonic(true, true, xs), "x-values do not increase monotonically");
    }
    return mutable ? new MutableArrayXySequence(xs, ys) : new ArrayXySequence(xs, ys);
  }

}
