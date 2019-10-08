package caja1694.students.ju.se.artech;

public class Radiation {
    private double reactorOutputPerSecond;
    private double roomCoefficient;
    private double protectionCoefficient;
    final static private double radioationLimit = 500000;
    private double todaysExposure;

    public  Radiation(){}

    public Radiation(double reactorOutputPerSecond, double roomCoefficient, double protectionCoefficient){
        this.reactorOutputPerSecond = reactorOutputPerSecond;
        this.roomCoefficient = roomCoefficient;
        this.protectionCoefficient = protectionCoefficient;
    }

    public double getUnitExposurePerSecond(){
        return (this.reactorOutputPerSecond * this.roomCoefficient) / this.protectionCoefficient;
    }
    public double getUnitExposurePerMilliSecond(){
        return (this.reactorOutputPerSecond * this.roomCoefficient) / (this.protectionCoefficient*1000);
    }
    public double getRadioationLimit(){
        return this.radioationLimit;
    }

    public void setTodaysExposure(long timePast) {
        this.todaysExposure = getUnitExposurePerMilliSecond()*(double)timePast;
    }

    public double getTodaysExposure() {
        return todaysExposure;
    }

    public double getReactorOutputPerSecond() {
        return reactorOutputPerSecond;
    }

    public void setReactorOutputPerSecond(double reactorOutputPerSecond) {
        this.reactorOutputPerSecond = reactorOutputPerSecond;
    }

    public double getMilliSecondsUntilLimit(){
        return this.getRadioationLimit()/this.getUnitExposurePerMilliSecond();
    }

    public double getProtectionCoefficient() {
        return protectionCoefficient;
    }

    public void setProtectionCoefficient(double protectionCoefficient) {
        this.protectionCoefficient = protectionCoefficient;
    }

    public double getRoomCoefficient() {
        return roomCoefficient;
    }

    public void setRoomCoefficient(double roomCoefficient) {
        this.roomCoefficient = roomCoefficient;
    }
    public String toString(double radiation){
        return String.valueOf(radiation);
    }
}
