package gov.usgs.earthquake.nshmp.eq;

import gov.usgs.earthquake.nshmp.Text;

/**
 * Tectonic setting identifier.
 * @author Peter Powers
 */
public enum TectonicSetting {

  /** Active shallow crust tectonic setting identifier. */
  ACTIVE_SHALLOW_CRUST,

  /** Stable shallow crust tectonic setting identifier. */
  STABLE_SHALLOW_CRUST,

  /** Subduction Interface tectonic setting identifier. */
  SUBDUCTION_INTERFACE,

  /** Subduction IntraSlab tectonic setting identifier. */
  SUBDUCTION_INTRASLAB,

  /** Volcanic tectonic setting identifier. */
  VOLCANIC;

  @Override
  public String toString() {
    return Text.enumLabelWithSpaces(this, true);
  }

}
