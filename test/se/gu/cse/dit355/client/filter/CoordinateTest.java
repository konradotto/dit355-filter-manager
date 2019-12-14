package se.gu.cse.dit355.client.filter;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateTest {

    private Coordinate northPole = new Coordinate(90, 0);
    private Coordinate equator1 = new Coordinate(0, 0);
    private Coordinate equator2 = new Coordinate(0, 90);
    private Coordinate equator3 = new Coordinate(0, 180);
    private Coordinate equator4 = new Coordinate(0, -90);

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
}