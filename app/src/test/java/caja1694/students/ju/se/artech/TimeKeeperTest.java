package caja1694.students.ju.se.artech;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TimeKeeperTest {
    TimeKeeper timeKeeper;

    @Test
    public void timeLeftInMillis_should_return_100(){
        //Arrange
        timeKeeper = new TimeKeeper(100);

        //Act
        int timeleft = (int)timeKeeper.getTimeLeftInMillis();

        //Assert
        assertEquals(100, timeleft);
    }
    @Test
    public void getTimePast_should_return_zero(){
        // Arrange
        timeKeeper = new TimeKeeper(10000);
        // Act
        long timePast = timeKeeper.getTimePastInMillis();
        // Assert
        assertEquals(0, timePast);
    }
}
