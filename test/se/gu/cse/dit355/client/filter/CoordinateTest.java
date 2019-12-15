package se.gu.cse.dit355.client.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    private Coordinate northPole = new Coordinate(90, 0);
    private Coordinate equator1 = new Coordinate(0, 0);
    private Coordinate equator2 = new Coordinate(0, 90);
    private Coordinate equator3 = new Coordinate(0, 180);
    private Coordinate equator4 = new Coordinate(0, -90);
    private Coordinate equilibrium = new Coordinate(45, 45);

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
    }

    @org.junit.jupiter.api.Test
    void testEqualityWithReversedArguments() {
        double calc1 = northPole.calculateDistance(equator1);
        double calc2 = equator1.calculateDistance(northPole);
        double delta = 1e-8;

        assertEquals(calc1, calc2, delta, "Reversing the order of Coordinates should not change the result.");
    }

    @org.junit.jupiter.api.Test
    void calculateDistanceNorthPoleEquator() {
        double calculated = northPole.calculateDistance(equator1);
        double expected = 10010000;
        double dif = 5000; // set error margin to 5 km
        assertEquals(expected, calculated, dif, "Distance from North Pole to equator should be similar to " +
                "the value given by an online calculator.");
    }

    @org.junit.jupiter.api.Test
    void calculateEqualDistances() {
        double calc1 = northPole.calculateDistance(equator1);
        double calc2 = northPole.calculateDistance(equator2);
        double calc3 = northPole.calculateDistance(equator3);
        double calc4 = northPole.calculateDistance(equator4);
        double delta = 1e-8;

        assertEquals(calc1, calc2, delta, "Distance of two points on the equator to the NorthPole should be the same.");
        assertEquals(calc2, calc3, delta, "Distance of two points on the equator to the NorthPole should be the same.");
        assertEquals(calc3, calc4, delta, "Distance of two points on the equator to the NorthPole should be the same.");
        assertEquals(calc4, calc1, delta, "Distance of two points on the equator to the NorthPole should be the same.");

        double calc5 = equator1.calculateDistance(equator2);
        double calc6 = equator2.calculateDistance(equator3);
        double calc7 = equator3.calculateDistance(equator4);
        double calc8 = equator4.calculateDistance(equator1);

        assertEquals(calc5, calc6, delta, "Two 90 degree arches on the equator should have the same length.");
        assertEquals(calc6, calc7, delta, "Two 90 degree arches on the equator should have the same length.");
        assertEquals(calc7, calc8, delta, "Two 90 degree arches on the equator should have the same length.");
        assertEquals(calc8, calc1, delta, "Two 90 degree arches on the equator should have the same length.");
    }

    @Test
    void testToCartesianUnitVectorSpecialValues() {
        double delta = 1e-8;
        double[] calculated = northPole.toCartesianUnitVector();
        double[] expected = {0, 0, 1};
        assertArrayEquals(expected, calculated, delta, "North Pole should convert to (0, 0, 1)");

        calculated = equator1.toCartesianUnitVector();
        double[] expected1 = {1, 0, 0};
        assertArrayEquals(expected1, calculated, delta, "Point on 0-Meridian at equator should convert to (1, 0, 0)");

        calculated = equator2.toCartesianUnitVector();
        double[] expected2 = {0, 1, 0};
        assertArrayEquals(expected2, calculated, delta, "Point on 90-Meridian at equator should convert to (0, 1, 0)");

        calculated = equator3.toCartesianUnitVector();
        double[] expected3 = {-1, 0, 0};
        assertArrayEquals(expected3, calculated, delta, "Point on 180-Meridian at equator should convert to (-1, 0, 0)");

        calculated = equator4.toCartesianUnitVector();
        double[] expected4 = {0, -1, 0};
        assertArrayEquals(expected4, calculated, delta, "Point on -90-Meridian at equator should convert to (0, -1, 0)");
    }

    @Test
    void testToCartesianUnitEquilibrium() {
        double[] calculated = equilibrium.toCartesianUnitVector();
        double[] expected = {0.5, 0.5, Math.sqrt(0.5)};
        assertArrayEquals(expected, calculated, 1e-8, "Vector should be (0.5, 0.5, 0.5^0.5) for this coordinate.");
    }

    @Test
    void newCoordinateFromCartesianSimpleCases() {
        double[] cartesian = {0, 0, 1};
        Coordinate fromCartesian = Coordinate.newCoordinateFromCartesian(cartesian);
        assertTrue(fromCartesian.equals(northPole), "Latitude and longitude of " +
                "both coordinates should be almost identical but aren't.");

        double[] cartesian1 = {1, 0, 0};
        Coordinate fromCartesian1 = Coordinate.newCoordinateFromCartesian(cartesian1);
        assertTrue(fromCartesian1.equals(equator1), "Latitude and longitude of " +
                "both coordinates should be almost identical but aren't.");

        double[] cartesian2 = {0, 1, 0};
        Coordinate fromCartesian2 = Coordinate.newCoordinateFromCartesian(cartesian2);
        assertTrue(fromCartesian2.equals(equator2), "Latitude and longitude of " +
                "both coordinates should be almost identical but aren't.");

        double[] cartesian3 = {-1, 0, 0};
        Coordinate fromCartesian3 = Coordinate.newCoordinateFromCartesian(cartesian3);
        assertTrue(fromCartesian3.equals(equator3), "Latitude and longitude of " +
                "both coordinates should be almost identical but aren't.");

        double[] cartesian4 = {0, -1, 0};
        Coordinate fromCartesian4 = Coordinate.newCoordinateFromCartesian(cartesian4);
        assertTrue(fromCartesian4.equals(equator4), "Latitude and longitude of " +
                "both coordinates should be almost identical but aren't.");
    }

    @Test
    void newCoordinateFromCartesianComplicatedCases() {
        double epsilon = 1e-20;

        double[] cartesian = {0.5, 0.5, Math.sqrt(0.5)};
        Coordinate fromCartesian = Coordinate.newCoordinateFromCartesian(cartesian);
        assertTrue(fromCartesian.almostEquals(equilibrium, epsilon), "Latitude and longitude of " +
                "both coordinates should be almost identical but aren't.");
    }
}