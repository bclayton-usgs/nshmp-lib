package gov.usgs.earthquake.nshmp.gmm;

import static gov.usgs.earthquake.nshmp.gmm.FaultStyle.NORMAL;
import static gov.usgs.earthquake.nshmp.gmm.FaultStyle.REVERSE;
import static gov.usgs.earthquake.nshmp.gmm.FaultStyle.STRIKE_SLIP;
import static gov.usgs.earthquake.nshmp.gmm.FaultStyle.UNKNOWN;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.DIP;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.MW;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.RAKE;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.RJB;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.VS30;
import static gov.usgs.earthquake.nshmp.gmm.GmmInput.Field.WIDTH;
import static gov.usgs.earthquake.nshmp.gmm.Imt.PGA;
import static java.lang.Math.exp;
import static java.lang.Math.log;

import java.util.Map;

import com.google.common.collect.Range;

import gov.usgs.earthquake.nshmp.Earthquakes;
import gov.usgs.earthquake.nshmp.Faults;
import gov.usgs.earthquake.nshmp.gmm.GmmInput.Constraints;

/**
 * Implementation of the Boore & Atkinson (2008) next generation attenuation
 * relationship for active crustal regions developed as part of <a
 * href="http://peer.berkeley.edu/ngawest/" target="_top">NGA West I</a>.
 *
 * <p><b>Note:</b> Direct instantiation of {@code GroundMotionModel}s is
 * prohibited. Use {@link Gmm#instance(Imt)} to retrieve an instance for a
 * desired {@link Imt}.
 *
 * <p><b>Reference:</b> Boore, D.M., and Atkinson, G.M., 2008, Ground-motion
 * prediction equations for the average horizontal component of PGA, PGV, and
 * 5%-damped PSA at spectral periods between 0.01s and 10.0s: Earthquake
 * Spectra, v. 24, n. 1, pp. 99-138.
 *
 * <p><b>doi:</b><a href="http://dx.doi.org/10.1193/1.2830434" target="_top">
 * http://dx.doi.org/10.1193/1.2830434</a>
 *
 * <p><b>Component:</b> GMRotI50 (geometric mean)
 *
 * @author Peter Powers
 * @see Gmm#BA_08
 */
public final class BooreAtkinson_2008 implements GroundMotionModel {

  static final String NAME = "Boore & Atkinson (2008)";

  static final Constraints CONSTRAINTS = Constraints.builder()
      .set(MW, Range.closed(5.0, 8.0))
      .set(RJB, Range.closed(0.0, 200.0))
      .set(DIP, Faults.DIP_RANGE)
      .set(WIDTH, Earthquakes.CRUSTAL_WIDTH_RANGE)
      .set(RAKE, Faults.RAKE_RANGE)
      .set(VS30, Range.closedOpen(180.0, 1300.0))
      .build();

  static final CoefficientContainer COEFFS = new CoefficientContainer("BA08.csv");

  private static final double PGAlo = 0.06;
  private static final double A2 = 0.09;
  private static final double A1 = 0.03;
  private static final double V1 = 180.0;
  private static final double V2 = 300.0;
  private static final double Vref = 760;
  private static final double Mref = 4.5;
  private static final double Rref = 1;

  static final class Coefficients {

    final double b_lin, b1, b2,
        c1, c2, c3,
        e1, e2, e3, e4, e5, e6, e7,
        h, mh, s,
        t_u, s_tu, t_m, s_tm;

    Coefficients(Imt imt, CoefficientContainer cc) {
      Map<String, Double> coeffs = cc.get(imt);
      b_lin = coeffs.get("b_lin");
      b1 = coeffs.get("b1");
      b2 = coeffs.get("b2");
      c1 = coeffs.get("c1");
      c2 = coeffs.get("c2");
      c3 = coeffs.get("c3");
      e1 = coeffs.get("e1");
      e2 = coeffs.get("e2");
      e3 = coeffs.get("e3");
      e4 = coeffs.get("e4");
      e5 = coeffs.get("e5");
      e6 = coeffs.get("e6");
      e7 = coeffs.get("e7");
      h = coeffs.get("h");
      mh = coeffs.get("mh");
      s = coeffs.get("s");
      t_u = coeffs.get("t_u");
      s_tu = coeffs.get("s_tu");
      t_m = coeffs.get("t_m");
      s_tm = coeffs.get("s_tm");
    }
  }

  private final Coefficients coeffs;
  private final Coefficients coeffsPGA;

  BooreAtkinson_2008(final Imt imt) {
    coeffs = new Coefficients(imt, COEFFS);
    coeffsPGA = new Coefficients(PGA, COEFFS);
  }

  @Override
  public final ScalarGroundMotion calc(final GmmInput in) {
    return calc(coeffs, coeffsPGA, in);
  }

  // TODO not sure how to test this or make backwards compatible version for
  // comparisons. In 2008, the NSHMP mistakenly would supply fractional
  // weights
  // to the different fault styles whereas the 4 different fault styles
  // should be booleans and only ever have one of their values set to 1.

  private static final ScalarGroundMotion calc(final Coefficients c, final Coefficients cPGA,
      final GmmInput in) {

    FaultStyle style = GmmUtils.rakeToFaultStyle_NSHMP(in.rake);
    double pga4nl = exp(calcPGA4nl(cPGA, in.Mw, in.rJB, style));

    double μ = calcMean(c, style, pga4nl, in);
    double σ = calcStdDev(c, style);

    return DefaultScalarGroundMotion.create(μ, σ);
  }

  // Mean ground motion model
  private static final double calcMean(final Coefficients c, final FaultStyle style,
      final double pga4nl, final GmmInput in) {

    // Source/Event Term
    double Fm = calcSourceTerm(c, in.Mw, style);

    // Path Term
    double Fd = calcPathTerm(c, in.Mw, in.rJB);

    // Site term
    double Fs = calcSite(c, pga4nl, in.vs30);

    // Total Model
    return Fm + Fd + Fs;
  }

  /**
   * Package visible site amplification model. This model is appropriate for use
   * with other GMMs where the reference rock site condition has Vs30=760.
   */
  double siteAmp(double lnPga, double vs30) {
    return calcSite(coeffs, lnPga, vs30);
  }

  /* Site term */
  private static double calcSite(Coefficients c, double lnPga, double vs30) {

    double Flin = c.b_lin * log(vs30 / Vref);

    double bnl = 0.0; // vs30 >= 760 case
    if (vs30 < Vref) {
      if (vs30 > V2) {
        bnl = c.b2 * log(vs30 / Vref) / log(V2 / Vref);
      } else if (vs30 > V1) {
        bnl = (c.b1 - c.b2) * log(vs30 / V2) / log(V1 / V2) + c.b2;
      } else {
        bnl = c.b1;
      }
    }

    double Fnl = 0.0;
    if (lnPga <= A1) {
      Fnl = bnl * log(PGAlo / 0.1);
    } else if (lnPga <= A2) {
      double dX = log(A2 / A1);
      double dY = bnl * log(A2 / PGAlo);
      double _c = (3.0 * dY - bnl * dX) / (dX * dX);
      double d = -(2.0 * dY - bnl * dX) / (dX * dX * dX);
      double p = log(lnPga / A1);
      Fnl = bnl * log(PGAlo / 0.1) + (_c * p * p) + (d * p * p * p);
    } else {
      Fnl = bnl * log(lnPga / 0.1);
    }

    return Flin + Fnl;
  }

  // Median PGA for ref rock (Vs30=760m/s); always called with PGA coeffs
  private static final double calcPGA4nl(final Coefficients c, final double Mw, final double rJB,
      final FaultStyle style) {

    // Source/Event Term
    double Fm = calcSourceTerm(c, Mw, style);

    // Path Term
    double Fd = calcPathTerm(c, Mw, rJB);

    return Fm + Fd;
  }

  // Source/Event Term
  private static final double calcSourceTerm(final Coefficients c, final double Mw,
      final FaultStyle style) {
    double Fm = (style == STRIKE_SLIP) ? c.e2
        : (style == NORMAL) ? c.e3 : (style == REVERSE) ? c.e4 : c.e1; // else
    // unkown
    double MwMh = Mw - c.mh;
    Fm += (Mw <= c.mh) ? c.e5 * MwMh + c.e6 * MwMh * MwMh : c.e7 * MwMh;
    return Fm;
  }

  // Path Term
  private static final double calcPathTerm(final Coefficients c, final double Mw,
      final double rJB) {
    double r = Math.sqrt(rJB * rJB + c.h * c.h);
    return (c.c1 + c.c2 * (Mw - Mref)) * log(r / Rref) + c.c3 *
        (r - Rref);
  }

  // Aleatory uncertainty model
  private static final double calcStdDev(final Coefficients c, final FaultStyle style) {
    // independent values for tau and sigma are available in coeffs
    return style == UNKNOWN ? c.s_tu : c.s_tm;
  }

}
