package caja1694.students.ju.se.artech;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RadiationTest {
    Radiation radiation;
    final static double BREAK_ROOM = 0.1;
    final static double CONTROL_ROOM = 0.5;
    final static double REACTOR_ROOM = 1.6;
    final static double NO_SUIT = 1;
    final static double HAZMAT_SUIT = 5;
    final static double STANDARD_RADIATION_OUTPUT = 30;

    @Test
    public void getUnitExposurePerMilliSecond_should_return_point003(){
        //Arrange
        radiation = new Radiation(STANDARD_RADIATION_OUTPUT, BREAK_ROOM, NO_SUIT);

        //Act
        double exposureperMilliSecond = radiation.getUnitExposurePerMilliSecond();

        //Assert
        assertEquals(0.003, exposureperMilliSecond, 0);
    }

    @Test
    public void getUnitExposurePerSecond_should_return_9point6(){
        //Arrange
        radiation  = new Radiation(STANDARD_RADIATION_OUTPUT, REACTOR_ROOM, HAZMAT_SUIT);

        //Act
        double exposureperSecond = radiation.getUnitExposurePerSecond();

        //Assert
        assertEquals(9.6, exposureperSecond, 0);
    }
    @Test
    public void getMillisUntilRadiationLimit_should_be_166666666(){
        //Arange
        radiation = new Radiation(STANDARD_RADIATION_OUTPUT, BREAK_ROOM, NO_SUIT);

        //Act
        double millisLeft = radiation.getMilliSecondsUntilLimit();

        //Assert
        assertEquals(166666666, millisLeft, 1);
    }
}
