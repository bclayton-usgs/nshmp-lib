package gov.usgs.earthquake.nshmp.gmm;

import gov.usgs.earthquake.nshmp.Text;

/**
 * Style-of-faulting identifier.
 * @author Peter Powers
 */
public enum FaultStyle {

  // TODO can this be reconciled with FocalMech

  /** Strike-slip fault identifier. */
  STRIKE_SLIP,

  /** Normal fault identifier. */
  NORMAL,

  /** Reverse fault identifier. */
  REVERSE,

  /** Reverse-oblique identifier. */
  REVERSE_OBLIQUE,

  /** Unknown fault style identifier. */
  UNKNOWN;

  @Override
  public String toString() {
    return Text.enumLabelWithDashes(this, true);
  }

}
