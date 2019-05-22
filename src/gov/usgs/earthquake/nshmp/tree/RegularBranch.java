package gov.usgs.earthquake.nshmp.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import gov.usgs.earthquake.nshmp.data.DoubleData;;

/**
 * Basic logic tree branch implementation.
 *
 * @author Brandon Clayton
 */
class RegularBranch<T> implements Branch<T> {
  private final String id;
  private final T value;
  private final double weight;

  RegularBranch(String id, T value, double weight) {
    this.id = checkNotNull(id);
    this.value = checkNotNull(value);
    this.weight = DoubleData.checkWeight(weight);
  }

  @Override
  public String id() {
    return id;
  }

  @Override
  public T value() {
    return value;
  }

  @Override
  public double weight() {
    return weight;
  }

}
