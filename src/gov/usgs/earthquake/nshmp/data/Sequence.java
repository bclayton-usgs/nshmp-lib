package gov.usgs.earthquake.nshmp.data;

import java.util.Map;

public class Sequence {

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

}
