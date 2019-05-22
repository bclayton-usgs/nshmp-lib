package gov.usgs.earthquake.nshmp.tree;

import static com.google.common.base.Preconditions.checkNotNull;

import gov.usgs.earthquake.nshmp.data.DoubleData;

/**
 * Id value branch were id and value are equal.
 */
class IdValueBranch<T> implements Branch<T> {

  private final T value;
  private final double weight;

  public IdValueBranch(T value, double weight) {
    this.value = checkNotNull(value);
    this.weight = DoubleData.checkWeight(weight);
  }

  @Override
  public String id() {
    return (String) value;
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
