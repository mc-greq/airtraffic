package example.nio;

import java.io.Serializable;

public class Airport implements Serializable{

    private int xRunway;
    private int yRunway;
    private boolean runwayClear = true;

    public Airport(int xRunway, int yRunway) {
        this.xRunway = xRunway;
        this.yRunway = yRunway;
    }

    public synchronized boolean isRunwayClear() {
        return runwayClear;
    }

    public synchronized void setRunwayClear(boolean runwayClear) {
        this.runwayClear = runwayClear;

    }

    public int getxRunway() {
        return xRunway;
    }

    public int getyRunway() {
        return yRunway;
    }

    @Override
    public String toString() {
        return "x(" + xRunway + "), y(" + yRunway + ")";
    }
}
