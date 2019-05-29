package gov.usgs.earthquake.nshmp.gmm;

import static gov.usgs.earthquake.nshmp.gmm.FaultStyle.REVERSE;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.MW;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.RAKE;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.RRUP;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.VS30;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

import java.util.Map;

import com.google.common.collect.Range;

import gov.usgs.earthquake.nshmp.Faults;
import gov.usgs.earthquake.nshmp.gmm.GmmInput.Constraints;

/**
 * Implementation of the ground motion model for shallow crustal earthquakes by
 * Sadigh et al. (1997). This implementation supports soil and rock sites, the
 * cutoff for which is vs30=750 m/s. This model was also used for subduction
 * interface sources in the 2007 Alaska NSHM, for which a custom magnitude
 * saturation at M=8.5 was added.
 *
 * <p><b>Note:</b> Direct instantiation of {@code GroundMotionModel}s is
 * prohibited. Use {@link Gmm#instance(Imt)} to retrieve an instance for a
 * desired {@link Imt}.
 *
 * <p><b>Reference:</b> Sadigh, K., Chang, C.-Y. , Egan, J.A., Makdisi, F., and
 * Youngs, R.R., 1997, Attenuation relationships for shallow crustal earthquakes
 * based on California strong motion data: Seismological Research Letters, v. 6,
 * n. 1, p. 180-189.
 *
 * <p><b>doi:</b> <a href="http://dx.doi.org/10.1785/gssrl.68.1.180"
 * target="_top">10.1785/gssrl.68.1.180</a>
 *
 * <p><b>Component:</b> geometric mean of two horizontal components
 *
 * @author Peter Powers
 */
public class SadighEtAl_1997 implements GroundMotionModel {

  /*
   * The Sadigh model provides different functional forms for soil and rock site
   * classes, has numerous magnitude and style-of-faulting coefficient variants.
   * This implementation nests style-of-faulting specific coefficents in the
   * coeff tables and keeps four uniform tables for the two site classes
   * supported with a low and high magnitude flavor of each. This yields some
   * redundancy in the coefficent tables but reduces the need for conditionals.
   */

  static final String NAME = "Sadigh et al. (1997)";

  static final Constraints CONSTRAINTS = Constraints.builder()
      .set(MW, Range.closed(5.0, 8.0))
      .set(RRUP, Range.closed(0.0, 100.0))
      .set(RAKE, Faults.RAKE_RANGE)
      .set(VS30, Range.closed(250.0, 760.0))
      .build();

  static final CoefficientContainer COEFFS_BC_LO, COEFFS_BC_HI, COEFFS_D_LO, COEFFS_D_HI;

  static {
    COEFFS_BC_LO = new CoefficientContainer("Sadigh97_BClo.csv");
    COEFFS_BC_HI = new CoefficientContainer("Sadigh97_BChi.csv");
    COEFFS_D_LO = new CoefficientContainer("Sadigh97_Dlo.csv");
    COEFFS_D_HI = new CoefficientContainer("Sadigh97_Dhi.csv");
  }

  private static final double VS30_CUT = 750.0;
  private static final double M_CUT = 8.5;

  private static final class Coefficients {

    final double c1r, c1ss, c2, c3, c4, c5, c6r, c6ss, c7, σ0, cM, σMax;

    Coefficients(Imt imt, CoefficientContainer cc) {
      Map<String, Double> coeffs = cc.get(imt);
      c1r = coeffs.get("c1r");
      c1ss = coeffs.get("c1ss");
      c2 = coeffs.get("c2");
      c3 = coeffs.get("c3");
      c4 = coeffs.get("c4");
      c5 = coeffs.get("c5");
      c6r = coeffs.get("c6r");
      c6ss = coeffs.get("c6ss");
      c7 = coeffs.get("c7");
      σ0 = coeffs.get("sig0");
      cM = coeffs.get("cM");
      σMax = coeffs.get("sigMax");
    }
  }

  private final Coefficients coeffs_bc_lo;
  private final Coefficients coeffs_bc_hi;
  private final Coefficients coeffs_d_lo;
  private final Coefficients coeffs_d_hi;

  SadighEtAl_1997(final Imt imt) {
    coeffs_bc_lo = new Coefficients(imt, COEFFS_BC_LO);
    coeffs_bc_hi = new Coefficients(imt, COEFFS_BC_LO);
    coeffs_d_lo = new Coefficients(imt, COEFFS_D_LO);
    coeffs_d_hi = new Coefficients(imt, COEFFS_D_LO);
  }

  @Override
  public final ScalarGroundMotion calc(final GmmInput in) {
    FaultStyle faultStyle = GmmUtils.rakeToFaultStyle_NSHMP(in.rake);

    double μ, σ;

    /* Modified to saturate above Mw=8.5 */
    double Mw = min(in.Mw, 8.5);

    if (in.vs30 > VS30_CUT) {
      /* Rock */
      Coefficients c = Mw <= 6.5 ? coeffs_bc_lo : coeffs_bc_hi;
      μ = calcRockMean(c, Mw, in.rRup, faultStyle);
      σ = calcStdDev(c, Mw);
    } else {
      /* Soil */
      Coefficients c = Mw <= 6.5 ? coeffs_d_lo : coeffs_d_hi;
      μ = calcSoilMean(c, Mw, in.rRup, faultStyle);
      σ = calcStdDev(c, Mw);
    }

    return DefaultScalarGroundMotion.create(μ, σ);
  }

  private static final double calcRockMean(final Coefficients c, final double Mw,
      final double rRup, final FaultStyle style) {

    /*
     * Rock site coeffs are not dependent on style-of-faulting so we just use
     * the rock flavor (c1r == c1ss)
     */

    double lnY =
        c.c1r +
            c.c2 * Mw +
            c.c3 * pow(M_CUT - Mw, 2.5) +
            c.c4 * log(rRup + exp(c.c5 + c.c6r * Mw)) +
            c.c7 * log(rRup + 2);

    /* Scale reverse amplitudes by 1.2; 0.18232 = ln(1.2) */
    return (style == REVERSE) ? lnY + 0.18232 : lnY;
  }

  private static final double calcSoilMean(final Coefficients c, final double Mw,
      final double rRup, final FaultStyle style) {

    double c1 = (style == REVERSE) ? c.c1r : c.c1ss;
    double c6 = (style == REVERSE) ? c.c6r : c.c6ss;

    return c1 +
        c.c2 * Mw -
        c.c3 * log(rRup + c.c4 * exp(c.c5 * Mw)) +
        c6 +
        c.c7 * pow(M_CUT - Mw, 2.5);
  }

  private static final double calcStdDev(final Coefficients c, final double Mw) {
    /*
     * mMax_bc = 7.21, mMax_d = 7.0, coeff tables were populated with maxSigma
     * for soil sites, maxSigma for rock were included in publication
     */
    return max(c.σ0 + c.cM * Mw, c.σMax);
  }

}
