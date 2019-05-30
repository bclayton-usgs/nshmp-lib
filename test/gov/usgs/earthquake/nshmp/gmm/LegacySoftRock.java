package gov.usgs.earthquake.nshmp.gmm;

import static gov.usgs.earthquake.nshmp.gmm.Gmm.AS_97;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.BJF_97;
import static gov.usgs.earthquake.nshmp.gmm.Gmm.CB_03;
import static gov.usgs.earthquake.nshmp.gmm.Imt.PGA;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA0P2;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA1P0;
import static gov.usgs.earthquake.nshmp.gmm.Imt.SA2P0;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@SuppressWarnings("javadoc")
@RunWith(Parameterized.class)
public class LegacySoftRock extends GmmTest {

  /*
   * Tests for older active crust ground motion models that have limited
   * parameterizations and only support soft-rock sites. These models are
   * currently used in the 2007 Alaska NSHM.
   */

  private static String GMM_INPUTS = "ceus-vs760-inputs.csv";
  private static String GMM_RESULTS = "legacy-vs760-results.csv";

  @Parameters(name = "{index}: {0} {2} {1}")
  public static Collection<Object[]> data() throws IOException {
    return loadResults(GMM_RESULTS);
  }

  public LegacySoftRock(int index, Gmm gmm, Imt imt, double exMedian, double exSigma) {
    super(index, gmm, imt, exMedian, exSigma, GMM_INPUTS);
  }

  /* Result generation sets */
  private static Set<Gmm> gmms = EnumSet.of(AS_97, BJF_97, CB_03);
  private static Set<Imt> imts = EnumSet.of(PGA, SA0P2, SA1P0, SA2P0);

  public static void main(String[] args) throws IOException {
    GmmTest.generateResults(gmms, imts, GMM_INPUTS, GMM_RESULTS);
  }

}
