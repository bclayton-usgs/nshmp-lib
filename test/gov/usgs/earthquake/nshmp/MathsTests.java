package gov.usgs.earthquake.nshmp;

import static org.junit.Assert.assertEquals;

import java.math.RoundingMode;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class MathsTests {

  @Test
  public void hypot() {
    /* 2- and 3-arg flavors. */
    assertEquals(Maths.hypot(3, 4), 5.0, 0.0);
    assertEquals(Maths.hypot(3, 4, 0), 5.0, 0.0);
    /* Variadic flavor. */
    assertEquals(Maths.hypot(2), 2.0, 0.0);
    assertEquals(Maths.hypot(3, 4, 0, 0), 5.0, 0.0);
  }

  @Test
  public void epsilon() {
    assertEquals(1.5, Maths.epsilon(3.0, 2.0, 6.0), 0);
  }
  
  @Test
  public void round() {
    assertEquals(3.14, Maths.round(Math.PI, 2), 0);
    assertEquals(3.14, Maths.round(Math.PI, 2, RoundingMode.HALF_DOWN), 0);
  }

}
