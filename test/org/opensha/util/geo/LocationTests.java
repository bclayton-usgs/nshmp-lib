package org.opensha.util.geo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opensha.util.Maths;

import com.google.common.base.Converter;
import com.google.common.collect.Lists;

@SuppressWarnings("javadoc")
public class LocationTests {

  private static final double V = 10.0;
  Location location;

  @Before
  public void setUp() throws Exception {
    location = Location.create(V, V, V);
  }

  @Test
  public final void create() {
    Location loc = new Location(V, V);
    assertEquals(loc.longitude, V, 0);
    assertEquals(loc.latitude, V, 0);
    assertEquals(loc.depth, 0, 0);
    loc = Location.create(V, V, V);
    assertEquals(loc.longitude, V, 0);
    assertEquals(loc.latitude, V, 0);
    assertEquals(loc.depth, V, 0);
    assertEquals(loc.latRad, V * Maths.TO_RADIANS, 0);
    assertEquals(loc.lonRad, V * Maths.TO_RADIANS, 0);
    loc = Location.create(V, V);
    assertEquals(loc.depth, 0, 0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void create_IAE1() {
    Location.create(90.1, 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void create_IAE2() {
    Location.create(-90.1, 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void create_IAE3() {
    Location.create(0.0, 360.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void create_IAE4() {
    Location.create(0.0, -360.0);
  }

  @Test
  public final void fromString() {
    String s = "10.0,10.0,10.0";
    assertEquals(Location.fromString(s), location);
  }

  @Test(expected = NumberFormatException.class)
  public final void fromString_NFE() {
    String s = "10.0,x,10.0";
    Location.fromString(s);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public final void fromString_IOOBE() {
    String s = "10.0,10.0";
    Location.fromString(s);
  }

  @Test
  public final void toStringTest() {
    Location loc = Location.create(30, 20, 10);
    String s = String.format("%.5f,%.5f,%.5f", loc.longitude, loc.latitude, loc.depth);
    assertEquals(loc.toString(), s);
  }

  @Test
  public final void stringConverter() {
    Converter<Location, String> c1 = Location.stringConverter();
    Converter<Location, String> c2 = Location.stringConverter();
    assertSame(c1, c2);
  }

  @Test
  public final void equalsTest() {
    assertEquals(location, location);
    assertNotEquals(location, null);
    assertNotEquals(location, "test");
    // same values
    Location loc = Location.create(V, V, V);
    assertEquals(location, loc);
    // different values
    loc = Location.create(V, V + 0.1, V);
    assertNotEquals(location, loc);
    loc = Location.create(V + 0.1, V, V);
    assertNotEquals(location, loc);
    loc = Location.create(V, V, V + 0.1);
    assertNotEquals(location, loc);
  }

  @Test
  public final void hashCodeTest() {
    Location other = Location.create(V, V, V);
    assertEquals(location.hashCode(), other.hashCode());
    Location locA = Location.create(90, 45, 25);
    Location locB = Location.create(45, 90, 25);
    assertNotEquals(locA.hashCode(), locB.hashCode());
  }

  @Test
  public final void compareToTest() {
    Location l0 = Location.create(20, -30);
    Location l1 = Location.create(20, -50);
    Location l2 = Location.create(-10, 10);
    Location l3 = Location.create(-10, 30);
    Location l4 = Location.create(-10, 30);
    Location l5 = Location.create(40, 10);
    List<Location> locList = Lists.newArrayList(l0, l1, l2, l3, l4, l5);
    Collections.sort(locList);
    assertTrue(locList.get(0) == l2);
    assertTrue(locList.get(1) == l3);
    assertTrue(locList.get(2) == l4);
    assertTrue(locList.get(3) == l1);
    assertTrue(locList.get(4) == l0);
    assertTrue(locList.get(5) == l5);
  }

}
