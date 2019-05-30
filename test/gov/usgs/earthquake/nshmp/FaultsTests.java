package gov.usgs.earthquake.nshmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

import gov.usgs.earthquake.nshmp.geo.Location;
import gov.usgs.earthquake.nshmp.geo.LocationList;
import gov.usgs.earthquake.nshmp.geo.Locations;

@SuppressWarnings("javadoc")
public final class FaultsTests {

  @Test
  public final void testCheckFaultDipRakeStrike() {
    assertEquals(45.0, Faults.checkDip(45.0), 0.0);
    assertEquals(45.0, Faults.checkRake(45.0), 0.0);
    assertEquals(45.0, Faults.checkStrike(45.0), 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckDipLo_IAE() {
    Faults.checkDip(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckDipHi_IAE() {
    Faults.checkDip(90.1);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public final void testCheckRakeLo_IAE() {
    Faults.checkRake(-180.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckRakeHi_IAE() {
    Faults.checkRake(180.1);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public final void testCheckStrikeLo_IAE() {
    Faults.checkStrike(-0.1);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckStrikeHi_IAE() {
    Faults.checkStrike(360.0);
  }
  
  @Test
  public final void testCheckTrace() {
    LocationList expect = LocationList.of(
        Location.create(0.0, 0.0),
        Location.create(1.0, 1.0));
    assertSame(expect, Faults.checkTrace(expect));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckTrace_IAE() {
    Faults.checkTrace(LocationList.of(Location.create(0.0, 0.0)));
  }
  
  @Test
  public final void testHypoDepth() {
    double hypoDepthExpected = 6.0;
    assertEquals(hypoDepthExpected, Faults.hypocentralDepth(90.0, 10.0, 1.0), 0.0);
    hypoDepthExpected = 1.0 + Math.sin(60.0 * Maths.TO_RADIANS) * 10.0 / 2.0;
    assertEquals(hypoDepthExpected, Faults.hypocentralDepth(60.0, 10.0, 1.0), 0.0);
  }
  
  /* radian degrees conversion tolerance */
  private static final double RAD_CONV_TOL = 1e-15;
  
  @Test
  public final void testDipDirection() {
    Location p1 = Location.create(0.0, 0.0);
    Location p2 = Location.create(1.0, 0.0);
    Location p3 = Location.create(1.0, 1.0);
    LocationList locs = LocationList.of(p1, p2, p3);
    
    double strikeExpect = Locations.azimuth(p1, p3);
    double strikeRadExpect = Locations.azimuthRad(p1, p3);
    
    assertEquals(strikeExpect, Faults.strike(locs), 0.0);
    assertEquals(strikeExpect, Faults.strike(p1, p3), 0.0);
    assertEquals(strikeRadExpect, Faults.strikeRad(locs), 0.0);
    assertEquals(strikeRadExpect, Faults.strikeRad(p1, p3), 0.0);
    
    double dipDirExpect = 70.0;
    double dipDirRadExpect = dipDirExpect * Maths.TO_RADIANS;
    
    assertEquals(dipDirExpect, Faults.dipDirection(340.0), 0.0);
    assertEquals(
        dipDirRadExpect,
        Faults.dipDirectionRad(340.0 * Maths.TO_RADIANS), RAD_CONV_TOL);
        
    dipDirExpect = strikeExpect + 90.0;
    dipDirRadExpect = strikeRadExpect + Maths.PI_BY_2;
    
    assertEquals(dipDirExpect, Faults.dipDirection(locs), 0.0);
    assertEquals(dipDirExpect, Faults.dipDirection(p1, p3), 0.0);
    assertEquals(dipDirRadExpect, Faults.dipDirectionRad(locs), 0.0);
    assertEquals(dipDirRadExpect, Faults.dipDirectionRad(p1, p3), 0.0);
  }



}
