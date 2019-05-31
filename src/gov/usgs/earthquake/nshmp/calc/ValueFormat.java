package gov.usgs.earthquake.nshmp.calc;

import gov.usgs.earthquake.nshmp.Text;

/**
 * Hazard curve value types.
 *
 * @author Peter Powers
 */
public enum ValueFormat {

  /** Annual-rate. */
  ANNUAL_RATE,

  /** Poisson probability. */
  POISSON_PROBABILITY;

  @Override
  public String toString() {
    return Text.enumLabelWithSpaces(this, true);
  }
}
