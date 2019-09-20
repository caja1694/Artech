package caja1694.students.ju.se.artech;

public class Radiation {
    private double R;
    private double rc;
    private double pc;


    public Radiation(double R, double rc, double pc){
        this.R = R;
        this.rc = rc;
        this.pc = pc;
    }

    public double getR() {
        return R;
    }

    public void setR(double r) {
        this.R = r;
    }

    public double getPc() {
        return pc;
    }

    public void setPc(double pc) {
        this.pc = pc;
    }

    public double getRc() {
        return rc;
    }

    public void setRc(double rc) {
        this.rc = rc;
    }
    public double getUnitExposurePerSecond(){
        double E = (this.R*this.rc)/this.pc;
        return E;
    }
}
