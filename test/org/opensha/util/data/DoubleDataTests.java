package org.opensha.util.data;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.google.common.primitives.Doubles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("javadoc")
public final class DoubleDataTests {

  private static final double[] VALUES = { 1.0, 10.0, 100.0 };

  private static double[] valueArray() {
    return Arrays.copyOf(VALUES, VALUES.length);
  }

  private static List<Double> valueList() {
    return Doubles.asList(valueArray());
  }

  static final void testArrayAndList(
      double[] expectArray,
      double[] actualArray,
      List<Double> actualList) {

    assertArrayEquals(expectArray, actualArray, 0.0);
    List<Double> expectList = Doubles.asList(expectArray);
    assertEquals(expectList, actualList);
  }

  /* * * * * * * * * * * * * OPERATORS * * * * * * * * * * * * */

  @Test
  public final void testAddTerm() {
    // 1D
    double[] expectArray = { 2.0, 11.0, 101.0 };
    double[] actualArray = DoubleData.add(1.0, valueArray());
    List<Double> actualList = DoubleData.add(1.0, valueList());
    testArrayAndList(expectArray, actualArray, actualList);
    // 2D
    double[][] d2_expect = { expectArray, expectArray };
    double[][] d2_input = { valueArray(), valueArray() };
    double[][] d2_actual = DoubleData.add(1.0, d2_input);
    for (int i = 0; i < d2_expect.length; i++) {
      assertArrayEquals(d2_expect[i], d2_actual[i], 0.0);
    }
    // 3D
    double[][][] d3_expect = { { expectArray, expectArray }, { expectArray, expectArray } };
    double[][][] d3_input = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    double[][][] d3_actual = DoubleData.add(1.0, d3_input);
    for (int i = 0; i < d3_expect.length; i++) {
      for (int j = 0; j < d3_expect[1].length; j++) {
        assertArrayEquals(d3_expect[i][j], d3_actual[i][j], 0.0);
      }
    }
  }

  @Test
  public final void testAddArrays() {
    // 1D array and list
    double[] expectArray = { 2.0, 20.0, 200.0 };
    double[] actualArray = DoubleData.add(valueArray(), valueArray());
    List<Double> actualList = DoubleData.add(valueList(), valueList());
    testArrayAndList(expectArray, actualArray, actualList);
    // 2D primitive arrays
    double[][] d2_expect = { { 2.0, 20.0, 200.0 }, { 2.0, 20.0, 200.0 } };
    double[][] d2_1 = { valueArray(), valueArray() };
    double[][] d2_2 = { valueArray(), valueArray() };
    double[][] d2_actual = DoubleData.add(d2_1, d2_2);
    for (int i = 0; i < d2_expect.length; i++) {
      assertArrayEquals(d2_expect[i], d2_actual[i], 0.0);
    }
    // 3D primitive arrays
    double[][][] d3_expect = {
        { { 2.0, 20.0, 200.0 }, { 2.0, 20.0, 200.0 } },
        { { 2.0, 20.0, 200.0 }, { 2.0, 20.0, 200.0 } } };
    double[][][] d3_1 = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    double[][][] d3_2 = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    double[][][] d3_actual = DoubleData.add(d3_1, d3_2);
    for (int i = 0; i < d3_expect.length; i++) {
      for (int j = 0; j < d3_expect[1].length; j++) {
        assertArrayEquals(d3_expect[i][j], d3_actual[i][j], 0.0);
      }
    }
  }

  @Test
  public final void testAddArraysUnchecked() {
    // 1D checked covariant passes through to unchecked
    // 2D primitive arrays
    double[][] d2_expect = { { 2.0, 20.0, 200.0 }, { 2.0, 20.0, 200.0 } };
    double[][] d2_1 = { valueArray(), valueArray() };
    double[][] d2_2 = { valueArray(), valueArray() };
    double[][] d2_actual = DoubleData.uncheckedAdd(d2_1, d2_2);
    for (int i = 0; i < d2_expect.length; i++) {
      assertArrayEquals(d2_expect[i], d2_actual[i], 0.0);
    }
    // 3D primitive arrays
    double[][][] d3_expect = {
        { { 2.0, 20.0, 200.0 }, { 2.0, 20.0, 200.0 } },
        { { 2.0, 20.0, 200.0 }, { 2.0, 20.0, 200.0 } } };
    double[][][] d3_1 = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    double[][][] d3_2 = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    double[][][] d3_actual = DoubleData.uncheckedAdd(d3_1, d3_2);
    for (int i = 0; i < d3_expect.length; i++) {
      for (int j = 0; j < d3_expect[1].length; j++) {
        assertArrayEquals(d3_expect[i][j], d3_actual[i][j], 0.0);
      }
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAddLists1D_IAE() {
    DoubleData.add(valueList(), new ArrayList<Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAddArrays1D_IAE() {
    DoubleData.add(valueArray(), new double[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAddArray2D1_IAE() {
    double[][] d2_1 = { valueArray(), valueArray() };
    // 1st level lengths different
    double[][] d2_2 = { valueArray() };
    DoubleData.add(d2_1, d2_2);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAddArray2D2_IAE() {
    double[][] d2_1 = { valueArray(), valueArray() };
    // 2nd level lengths different
    double[][] d2_2 = { valueArray(), {} };
    DoubleData.add(d2_1, d2_2);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAddArray3D1_IAE() {
    double[][][] d3_1 = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    // 1st level lengths different
    double[][][] d3_2 = { { valueArray(), valueArray() } };
    DoubleData.add(d3_1, d3_2);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAddArray3D2_IAE() {
    double[][][] d3_1 = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    // 2nd level lengths different
    double[][][] d3_2 = { { valueArray(), valueArray() }, { valueArray() } };
    DoubleData.add(d3_1, d3_2);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAddArray3D3_IAE() {
    double[][][] d3_1 = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    // 3rd level lengths different
    double[][][] d3_2 = { { valueArray(), valueArray() }, { valueArray(), {} } };
    DoubleData.add(d3_1, d3_2);
  }

  @Test
  public final void testAddMap() {
    Map<TimeUnit, Double> m1 = new EnumMap<>(TimeUnit.class);
    m1.put(TimeUnit.DAYS, 0.01);
    m1.put(TimeUnit.MINUTES, 0.5);
    Map<TimeUnit, Double> m2 = new EnumMap<>(TimeUnit.class);
    m2.put(TimeUnit.DAYS, 0.01);
    m2.put(TimeUnit.HOURS, 0.2);
    m2.put(TimeUnit.MINUTES, 0.5);
    Map<TimeUnit, Double> mExpect = new EnumMap<>(TimeUnit.class);
    mExpect.put(TimeUnit.DAYS, 0.02);
    mExpect.put(TimeUnit.HOURS, 0.2);
    mExpect.put(TimeUnit.MINUTES, 1.0);
    Map<TimeUnit, Double> m1p2 = DoubleData.add(m1, m2);
    assertEquals(mExpect, m1p2);
  }

  @Test
  public final void testSubtract() {
    // 1D array and list
    double[] expectArray = { 0.0, 0.0, 0.0 };
    double[] actualArray = DoubleData.subtract(valueArray(), valueArray());
    List<Double> actualList = DoubleData.subtract(valueList(), valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testSubtractLists1D_IAE() {
    DoubleData.subtract(valueList(), new ArrayList<Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testSubtractArrays1D_IAE() {
    DoubleData.subtract(valueArray(), new double[0]);
  }

  @Test
  public final void testMultiplyTerm() {
    // 1D
    double[] expectArray = { 5.0, 50.0, 500.0 };
    double[] actualArray = DoubleData.multiply(5.0, valueArray());
    List<Double> actualList = DoubleData.multiply(5.0, valueList());
    testArrayAndList(expectArray, actualArray, actualList);
    // 2D
    double[][] d2_expect = { expectArray, expectArray };
    double[][] d2_input = { valueArray(), valueArray() };
    double[][] d2_actual = DoubleData.multiply(5.0, d2_input);
    for (int i = 0; i < d2_expect.length; i++) {
      assertArrayEquals(d2_expect[i], d2_actual[i], 0.0);
    }
    // 3D
    double[][][] d3_expect = { { expectArray, expectArray }, { expectArray, expectArray } };
    double[][][] d3_input = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    double[][][] d3_actual = DoubleData.multiply(5.0, d3_input);
    for (int i = 0; i < d3_expect.length; i++) {
      for (int j = 0; j < d3_expect[1].length; j++) {
        assertArrayEquals(d3_expect[i][j], d3_actual[i][j], 0.0);
      }
    }
  }

  @Test
  public final void testMultiplyArrays() {
    // 1D array and list
    double[] expectArray = { 1.0, 100.0, 10000.0 };
    double[] actualArray = DoubleData.multiply(valueArray(), valueArray());
    List<Double> actualList = DoubleData.multiply(valueList(), valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testMultiplyLists1D_IAE() {
    DoubleData.multiply(valueList(), new ArrayList<Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testMultiplyArrays1D_IAE() {
    DoubleData.multiply(valueArray(), new double[0]);
  }

  @Test
  public final void testDivideArrays() {
    // 1D array and list
    double[] expectArray = { 1.0, 1.0, 1.0 };
    double[] actualArray = DoubleData.divide(valueArray(), valueArray());
    List<Double> actualList = DoubleData.divide(valueList(), valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testDivideLists1D_IAE() {
    DoubleData.divide(valueList(), new ArrayList<Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testDivideArrays1D_IAE() {
    DoubleData.divide(valueArray(), new double[0]);
  }

  @Test
  public final void testAbs() {
    double[] expectArray = valueArray();
    double[] absArray = DoubleData.multiply(-1, valueArray());
    List<Double> absList = DoubleData.multiply(-1, valueList());
    double[] actualArray = DoubleData.abs(absArray);
    List<Double> actualList = DoubleData.abs(absList);
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testExp() {
    double[] expectArray = new double[3];
    for (int i = 0; i < 3; i++) {
      expectArray[i] = Math.exp(VALUES[i]);
    }
    double[] actualArray = DoubleData.exp(valueArray());
    List<Double> actualList = DoubleData.exp(valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testLn() {
    double[] expectArray = new double[3];
    for (int i = 0; i < 3; i++) {
      expectArray[i] = Math.log(VALUES[i]);
    }
    double[] actualArray = DoubleData.ln(valueArray());
    List<Double> actualList = DoubleData.ln(valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testPow10() {
    double[] expectArray = new double[3];
    for (int i = 0; i < 3; i++) {
      expectArray[i] = Math.pow(10, VALUES[i]);
    }
    double[] actualArray = DoubleData.pow10(valueArray());
    List<Double> actualList = DoubleData.pow10(valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testLog() {
    double[] expectArray = new double[3];
    for (int i = 0; i < 3; i++) {
      expectArray[i] = Math.log10(VALUES[i]);
    }
    double[] actualArray = DoubleData.log(valueArray());
    List<Double> actualList = DoubleData.log(valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testFlip() {
    double[] expectArray = { -1.0, -10.0, -100.0 };
    double[] actualArray = DoubleData.flip(valueArray());
    List<Double> actualList = DoubleData.flip(valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testSum() {
    double expect = 111.0;
    assertEquals(expect, DoubleData.sum(valueArray()), 0.0);
    assertEquals(expect, DoubleData.sum(valueList()), 0.0);
  }

  @Test
  public final void testCollapse() {
    // 2D
    double[] d2_expect = { 111.0, 111.0 };
    double[][] d2_input = { valueArray(), valueArray() };
    double[] d2_actual = DoubleData.collapse(d2_input);
    assertArrayEquals(d2_expect, d2_actual, 0.0);
    // 3D
    double[][] d3_expect = { { 111.0, 111.0 }, { 111.0, 111.0 } };
    double[][][] d3_input = { { valueArray(), valueArray() }, { valueArray(), valueArray() } };
    double[][] d3_actual = DoubleData.collapse(d3_input);
    for (int i = 0; i < d3_expect.length; i++) {
      assertArrayEquals(d3_expect[i], d3_actual[i], 0.0);
    }
  }

  @Test
  public final void testTransform() {
    Function<Double, Double> fn = new Function<Double, Double>() {
      @Override
      public Double apply(Double input) {
        return input + 1;
      }
    };
    double[] expectArray = { 2.0, 11.0, 101.0 };
    double[] actualArray = DoubleData.transform(fn, valueArray());
    List<Double> actualList = DoubleData.transform(fn, valueList());
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testNormalize() {
    double[] expectArray = { 0.2, 0.3, 0.5 };
    double[] inputArray = { 20, 30, 50 };
    List<Double> inputList = Doubles.asList(Arrays.copyOf(inputArray, inputArray.length));
    double[] actualArray = DoubleData.normalize(inputArray);
    List<Double> actualList = DoubleData.normalize(inputList);
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testRound() {
    double[] expectArray = { 0.23, 1.32 };
    double[] inputArray = { 0.23449999, 1.3150001 };
    List<Double> inputList = Doubles.asList(Arrays.copyOf(inputArray, inputArray.length));
    double[] actualArray = DoubleData.round(2, inputArray);
    List<Double> actualList = DoubleData.round(2, inputList);
    testArrayAndList(expectArray, actualArray, actualList);
  }

  @Test
  public final void testPositivize() {
    double[] empty = {};
    assertArrayEquals(empty, DoubleData.positivize(empty), 0.0);
    double[] values = valueArray();
    assertArrayEquals(values, DoubleData.positivize(values), 0.0);
    double[] expect = { 99.0, 90.0, 0.0 };
    double[] actual = DoubleData.positivize(DoubleData.flip(values));
    assertArrayEquals(expect, actual, 0.0);
  }

  @Test
  public final void testDiff() {
    double[] increasing_dupes = { -10, -1, 0, 0, 1, 10 };
    double[] increasing_nodupes = { -10, -1, 0, 1, 10 };
    double[] decreasing_dupes = { 10, 1, 0, 0, -1, -10 };
    double[] decreasing_nodupes = { 10, 1, 0, -1, -10 };
    double[] expect = new double[] { 9, 1, 0, 1, 9 };
    assertArrayEquals(expect, DoubleData.diff(increasing_dupes), 0.0);
    assertArrayEquals(DoubleData.flip(expect), DoubleData.diff(decreasing_dupes), 0.0);
    expect = new double[] { 9, 1, 1, 9 };
    assertArrayEquals(expect, DoubleData.diff(increasing_nodupes), 0.0);
    assertArrayEquals(DoubleData.flip(expect), DoubleData.diff(decreasing_nodupes), 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testDiff_IAE() {
    DoubleData.diff(new double[1]);
  }

  @Test
  public final void testPercentDiff() {
    assertEquals(5.0, DoubleData.percentDiff(95.0, 100.0), 0.0);
    assertEquals(5.0, DoubleData.percentDiff(105.0, 100.0), 0.0);
    assertEquals(0.0, DoubleData.percentDiff(0.0, 0.0), 0.0);
    assertEquals(Double.POSITIVE_INFINITY, DoubleData.percentDiff(1.0, 0.0), 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testPercentDiffTest_IAE() {
    DoubleData.percentDiff(Double.NaN, 1.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testPercentDiffTarget_IAE() {
    DoubleData.percentDiff(1.0, Double.NaN);
  }

  /* * * * * * * * * * * * * * STATE * * * * * * * * * * * * * */

  @Test
  public final void testState() {
    // isPositiveAndReal
    assertTrue(DoubleData.isPositiveAndReal(10.0));
    assertFalse(DoubleData.isPositiveAndReal(0.0));
    assertFalse(DoubleData.isPositiveAndReal(Double.POSITIVE_INFINITY));
    assertFalse(DoubleData.isPositiveAndReal(Double.NaN));
    // arePositiveAndReal
    assertTrue(DoubleData.arePositiveAndReal(valueArray()));
    assertFalse(DoubleData.arePositiveAndReal(DoubleData.flip(valueArray())));
    assertTrue(DoubleData.arePositiveAndReal(valueList()));
    assertFalse(DoubleData.arePositiveAndReal(DoubleData.flip(valueList())));
    assertFalse(DoubleData.arePositiveAndReal(0));
    // isPositiveAndRealOrZero
    assertTrue(DoubleData.isPositiveAndRealOrZero(10.0));
    assertTrue(DoubleData.isPositiveAndRealOrZero(0.0));
    assertFalse(DoubleData.isPositiveAndRealOrZero(Double.POSITIVE_INFINITY));
    assertFalse(DoubleData.isPositiveAndRealOrZero(Double.NaN));
    // arePositiveAndRealOrZero
    assertTrue(DoubleData.arePositiveAndRealOrZero(valueArray()));
    assertFalse(DoubleData.arePositiveAndRealOrZero(DoubleData.flip(valueArray())));
    assertTrue(DoubleData.arePositiveAndRealOrZero(valueList()));
    assertFalse(DoubleData.arePositiveAndRealOrZero(DoubleData.flip(valueList())));
    assertTrue(DoubleData.arePositiveAndRealOrZero(0));
    // areZeroValued
    assertTrue(DoubleData.areZeroValued(new double[] { 0, 0 }));
    assertFalse(DoubleData.areZeroValued(new double[] { 0, 1 }));
    assertTrue(DoubleData.areZeroValued(Lists.<Double> newArrayList(0.0, 0.0)));
    assertFalse(DoubleData.areZeroValued(Lists.<Double> newArrayList(0.0, 1.0)));
    // areMonotonic
    double[] increasing_dupes = { -10, -1, 0, 0, 1, 10 };
    double[] increasing_nodupes = { -10, -1, 0, 1, 10 };
    double[] increasing_bad = { -10, -1, 0, -1, -10 };
    double[] decreasing_dupes = { 10, 1, 0, 0, -1, -10 };
    assertTrue(DoubleData.areMonotonic(true, false, increasing_dupes));
    assertFalse(DoubleData.areMonotonic(true, true, increasing_dupes));
    assertTrue(DoubleData.areMonotonic(true, true, increasing_nodupes));
    assertFalse(DoubleData.areMonotonic(true, false, increasing_bad));
    assertTrue(DoubleData.areMonotonic(false, false, decreasing_dupes));
    assertFalse(DoubleData.areMonotonic(false, true, decreasing_dupes));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testArePositiveAndRealArray_IAE() {
    DoubleData.arePositiveAndReal();
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testArePositiveAndRealList_IAE() {
    DoubleData.arePositiveAndReal(new ArrayList<Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testArePositiveAndRealOrZeroArray_IAE() {
    DoubleData.arePositiveAndReal();
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testArePositiveAndRealOrZeroList_IAE() {
    DoubleData.arePositiveAndReal(new ArrayList<Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAreFiniteArray_IAE() {
    DoubleData.arePositiveAndReal();
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAreFiniteList_IAE() {
    DoubleData.arePositiveAndReal(new ArrayList<Double>());
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAreZeroValuedArray_IAE() {
    DoubleData.areZeroValued();
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testAreZeroValuedList_IAE() {
    DoubleData.areZeroValued(new ArrayList<Double>());
  }

  /* * * * * * * * * * * * PRECONDITIONS * * * * * * * * * * * */

  @Test
  public final void testCheckDelta() {
    assertEquals(2.0, DoubleData.checkDelta(0.0, 10.0, 2.0), 0.0);
    assertEquals(0.0, DoubleData.checkDelta(10.0, 10.0, 0.0), 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckDeltaMinOverMax_IAE() {
    DoubleData.checkDelta(10.0, 0.0, 2.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckDeltaNegativeDelta_IAE() {
    DoubleData.checkDelta(0.0, 10.0, -2.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckDeltaZeroDelta_IAE() {
    DoubleData.checkDelta(0.0, 10.0, 0.0);
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckDeltaSize_IAE() {
    DoubleData.checkDelta(0.0, 10.0, 11.0);
  }

  @Test
  public final void testCheckFinite() {
    // also tests checkFinite(double)
    double[] expectArray = { 5.0, 2.0 };
    assertArrayEquals(expectArray, DoubleData.checkFinite(expectArray), 0.0);
    assertSame(expectArray, DoubleData.checkFinite(expectArray));
    List<Double> expectCollect = Doubles.asList(expectArray);
    assertEquals(expectCollect, DoubleData.checkFinite(expectCollect));
    assertSame(expectCollect, DoubleData.checkFinite(expectCollect));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckFiniteArray_IAE() {
    DoubleData.checkFinite(new double[] { 0, Double.NaN });
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckFiniteCollect_IAE() {
    DoubleData.checkFinite(Doubles.asList(0, Double.NEGATIVE_INFINITY));
  }

  @Test
  public final void testCheckInRange() {
    // also tests checkInRange(double)
    double[] expectArray = { 5.0, 2.0 };
    Range<Double> r = Range.open(0.0, 10.0);
    assertArrayEquals(expectArray, DoubleData.checkInRange(r, expectArray), 0.0);
    assertSame(expectArray, DoubleData.checkInRange(r, expectArray));
    List<Double> expectCollect = Doubles.asList(expectArray);
    assertEquals(expectCollect, DoubleData.checkInRange(r, expectCollect));
    assertSame(expectCollect, DoubleData.checkInRange(r, expectCollect));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckInRangeArray_IAE() {
    Range<Double> r = Range.open(0.0, 10.0);
    DoubleData.checkInRange(r, new double[] { -1.0 });
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckInRangeCollect_IAE() {
    Range<Double> r = Range.open(0.0, 10.0);
    DoubleData.checkInRange(r, Doubles.asList(-1.0));
  }

  /*
   * checkSize overloads are checked via operator tests
   */

  @Test
  public final void testCheckWeight() {
    assertEquals(0.5, DoubleData.checkWeight(0.5), 0.0);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public final void testCheckWeight_IAE() {
    DoubleData.checkWeight(0.0);
  }

  @Test
  public final void testCheckWeights() {
    double[] wtArray = { 0.4, 0.6001 };
    List<Double> wtList = Doubles.asList(wtArray);
    assertEquals(wtList, DoubleData.checkWeights(wtList));
    assertSame(wtList, DoubleData.checkWeights(wtList));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckWeightsBadHi_IAE() {
    double[] wtArrayBadValueHi = { 1.0001 };
    DoubleData.checkWeights(Doubles.asList(wtArrayBadValueHi));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckWeightsBadLo_IAE() {
    double[] wtArrayBadValueLo = { 0.0 };
    DoubleData.checkWeights(Doubles.asList(wtArrayBadValueLo));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testCheckWeightsBadSum_IAE() {
    double[] wtArrayBadSum = { 0.4, 0.6002 };
    DoubleData.checkWeights(Doubles.asList(wtArrayBadSum));
  }

  /* * * * * * * * 2D & 3D ARRAYS EXTENSIONS * * * * * * * * */

  @Test
  public final void testCopyOf() {
    // 2D
    double[][] expect2D = { valueArray(), valueArray(), {}, { 1.0, Double.NaN } };
    double[][] actual2D = DoubleData.copyOf(expect2D);
    assertNotSame(expect2D, actual2D);
    for (int i = 0; i < expect2D.length; i++) {
      assertArrayEquals(expect2D[i], actual2D[i], 0.0);
    }
    // 3D
    double[][][] expect3D = {
        { valueArray(), valueArray() },
        { valueArray() },
        { {}, { 1.0, Double.NaN } } };
    double[][][] actual3D = DoubleData.copyOf(expect3D);
    for (int i = 0; i < expect3D.length; i++) {
      for (int j = 0; j < expect3D[i].length; j++) {
        assertArrayEquals(expect3D[i][j], actual3D[i][j], 0.0);
      }
    }
  }

  @Test
  public final void testToString() {
    // 2D
    double[][] expect2D = { valueArray(), valueArray(), {}, { 1.0, Double.NaN } };
    String expect2Dstr = "[[1.0, 10.0, 100.0],\n" +
        " [1.0, 10.0, 100.0],\n" +
        " [],\n" +
        " [1.0, NaN]]";
    assertEquals(expect2Dstr, DoubleData.toString(expect2D));
    // 3D
    double[][][] expect3D = {
        { valueArray(), valueArray() },
        { valueArray() },
        { {}, { 1.0, Double.NaN } } };
    String expect3Dstr = "[[[1.0, 10.0, 100.0],\n" +
        "  [1.0, 10.0, 100.0]],\n" +
        " [[1.0, 10.0, 100.0]],\n" +
        " [[],\n" +
        "  [1.0, NaN]]]";
    assertEquals(expect3Dstr, DoubleData.toString(expect3D));
  }

}
