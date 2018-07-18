package example.nio;

import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class Airplane implements Serializable {

    private ImageIcon airplaneIcon;
    private String name;
    private int speed = ThreadLocalRandom.current().nextInt(2, 10);
    private int maxFuel = ThreadLocalRandom.current().nextInt(500, 800);
    private int currentFuel;
    private State state = State.LANDED;
    private int x;
    private int y;
    private int dx = 1;
    private int dy = 1;
    private int xSamolotu;
    private int ySamolotu;
    private Airport destination;
    private Airport currentAirport;

    enum State{
        LANDED,
        FLYING,
        LANDING,
        TAKINGOFF,
        WAITING,
        CRASHED
    }

    public State getState() {
        return state;
    }

    public void setState(State landed) {
        this.state = landed;
    }

    /**
     * Creating airplane object with provided starting airport
     * @param name - name of created plane
     * @param startingAirport - airport for first take off
     */
    public Airplane(String name, Airport startingAirport, ImageIcon airplaneIcon) {
        System.out.println(name + ": speed " + speed + ", max fuel: " + maxFuel);
        this.name = name;
        this.currentFuel = maxFuel;
        this.currentAirport = startingAirport;
        this.x = startingAirport.getxRunway();
        this.y = startingAirport.getyRunway();
        this.airplaneIcon = airplaneIcon;
        this.xSamolotu = airplaneIcon.getIconWidth();
        this.ySamolotu = airplaneIcon.getIconHeight();
    }

    /**
     * Creating airplane object with random starting airport out of provided airports list
     * @param name - name of created plane
     * @param startingAirports - list of airports for take off, one chosen randomly
     */
    public Airplane(String name, ArrayList<Airport> startingAirports) {
        System.out.println(name + ": speed " + speed + ", max fuel: " + maxFuel);
        this.name = name;
        this.currentFuel = maxFuel;
        Airport startingAirport = startingAirports.get(ThreadLocalRandom.current().nextInt(0, startingAirports.size()));
        currentAirport = startingAirport;
        this.x = startingAirport.getxRunway();
        this.y = startingAirport.getyRunway();
        int iconNr = ThreadLocalRandom.current().nextInt(1,4);
        this.airplaneIcon = new ImageIcon("airplane" + Integer.toString(iconNr) + ".gif");
    }

    /**
     * Choosing random destination
     * @param airportArrayList - list of destinations to choose from
     */
    public void setDestination(ArrayList<Airport> airportArrayList) {
        ArrayList<Airport> destinations = new ArrayList<>(airportArrayList);
        destinations.remove(currentAirport);
        int destI = ThreadLocalRandom.current().nextInt(0, destinations.size());
        this.destination = destinations.get(destI);
        setCourse();

//        System.out.println("x: " + this.x + " y: " + this.y + " dx: " + dx + " dy: "
//                + dy + " current: " + currentAirport + " destination: " + destination);
    }

    /**
     * Setting destination airport as provided
     * @param destination - destination airport for airplane object
     */
    public void setDestination(Airport destination){
        this.destination = destination;
        setCourse();
    }

    /**
     * Moving airplane across the board, landing if airport is reached
     */
    public void flyAirplane(){

        currentFuel--;

        if(this.x != destination.getxRunway()){
            x += dx;
        }

        if(this.y != destination.getyRunway()){
            y += dy;
        }

        //landing if airplane is at the start of runway
        if(this.x == destination.getxRunway() && this.y == destination.getyRunway() && destination.isRunwayClear()){
            state = State.LANDING;
        } else if(this.x == destination.getxRunway() && this.y == destination.getyRunway() && !destination.isRunwayClear()){
            state = State.FLYING;
            x -=dx;
            y -=dy;
        }
    }

    public void startTaxi(boolean loaded){
        if(!loaded) {
            if (currentAirport.isRunwayClear()) {
                state = State.TAKINGOFF;
            } else {
                state = State.WAITING;
            }
        } else {
            state = State.TAKINGOFF;
        }
    }

    public String getName() {
        return name;
    }

    public void land(){
        state = State.LANDED;
        currentAirport = destination;
        System.out.println(name + " landed, " + " fuel: " + currentFuel + " maxfuel: " + maxFuel);
    }

    public void takeOff(){
        this.state = State.FLYING;
        System.out.println(name + " taking off, fuel " + currentFuel + " dx:" + dx + " dy:" + dy + " c: " + currentAirport + " d: " + destination);
    }

    public int getCurrentFuel() {
        return currentFuel;
    }

    public void refuel() {
        this.currentFuel = this.maxFuel;
    }

    public ImageIcon getAirplaneIcon() {
        return airplaneIcon;
    }

    public void crash(){
        state = State.CRASHED;
    }

    /**
     * Setting proper dx and dy depending on destination to move across the board
     */
    private void setCourse(){
        if(this.destination.getxRunway() <= this.x){
            dx = -1;
        }

        if(this.destination.getxRunway() >= this.x){
            dx = 1;
        }

        if(this.destination.getyRunway() <= this.y){
            dy = -1;
        }

        if(this.destination.getyRunway() >= this.y){
            dy = 1;
        }
    }

    public int getX() {
        return x;
    }

    public Airport getDestination() {
        return destination;
    }

    public int getY() {
        return y;
    }

    public int getxSamolotu() {
        return xSamolotu;
    }

    public int getySamolotu() {
        return ySamolotu;
    }

    public int getSpeed() {
        return speed;
    }

    public Airport getCurrentAirport() {
        return currentAirport;
    }

    @Override
    public String toString() {
        return this.name + " fuel: " + this.currentFuel + " destination: " + this.destination + " state: " + this.state;
    }
}
