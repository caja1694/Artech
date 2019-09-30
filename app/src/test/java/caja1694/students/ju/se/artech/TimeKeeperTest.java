package caja1694.students.ju.se.artech;

import org.junit.Test;

import static org.junit.Assert.*;

public class TimeKeeperTest {

    @Test
    public void timeLeftInHours() {
    }

    @Test
    public void timeLeftInMinutes() {
    }

    @Test
    public void timeLeftInSeconds_Should_Return_10() {
        //Arrange
        TimeKeeper timeKeeper = new TimeKeeper(10000);
        //Act
        int timeLeftInSeconds = timeKeeper.timeLeftInSeconds();
        // Assert
        assertEquals(timeLeftInSeconds, 10);

    }

}