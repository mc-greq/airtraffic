package example.nio;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;


public class FlightBoard extends JPanel {

    private static Image genericIcon = new ImageIcon("kropelka.gif").getImage();

    private final Object lock = new Object();

    private ThreadGroup threadGroup = new ThreadGroup("Airplane group");
    private ArrayList<Airport> airportsList = new ArrayList<>();
    private ArrayList<Airplane> airplanesList = new ArrayList<>();
    private ArrayList<AirplaneRunnable> runnableList = new ArrayList<>();
    private ArrayList<Thread> threadList = new ArrayList<>();

    public FlightBoard() {
        Airport airport1 = new Airport(50, 50);
        Airport airport2 = new Airport(50, 250);
        Airport airport3 = new Airport(400, 200);
        Airport airport4 = new Airport(80, 400);
        Airport airport5 = new Airport(500, 500);
        Airport airport6 = new Airport(10, 500);

        //Airports list population
        airportsList.add(airport1);
        airportsList.add(airport2);
        airportsList.add(airport3);
        airportsList.add(airport4);
        airportsList.add(airport5);
        airportsList.add(airport6);

        Airplane airplane = new Airplane("airplane1", airport1, new ImageIcon("airplane1.gif"));
        Airplane airplane2 = new Airplane("airplane2", airport1, new ImageIcon("airplane2.gif"));
        Airplane airplane3 = new Airplane("airplane3", airport1, new ImageIcon("airplane3.gif"));
        Airplane airplane4 = new Airplane("airplane4", airport1, new ImageIcon("airplane4.gif"));
        Airplane airplane5 = new Airplane("airplane5", airport1, new ImageIcon("airplane2.gif"));
        Airplane airplane6 = new Airplane("airplane6", airport1, new ImageIcon("airplane3.gif"));
        Airplane airplane7 = new Airplane("airplane7", airport1, new ImageIcon("airplane1.gif"));

        //Planes list population
        airplanesList.add(airplane);
        airplanesList.add(airplane2);
        airplanesList.add(airplane3);
        airplanesList.add(airplane4);
        airplanesList.add(airplane5);
        airplanesList.add(airplane6);
        airplanesList.add(airplane7);

        //creating threads for airplanes
        makeThread(airplane, false);
        makeThread(airplane2, false);
        makeThread(airplane3, false);
        makeThread(airplane4, false);
        makeThread(airplane5, false);
        makeThread(airplane6, false);
        makeThread(airplane7, false);
    }

    public void addAirplane(){
        String name = "airplane" + (airplanesList.size() + 1);
        Airplane airplane = new Airplane(name, airportsList);
        airplanesList.add(airplane);
        makeThread(airplane, false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Airplane airplane: airplanesList) {
            if(!(airplane.getState() == Airplane.State.CRASHED)) {
//                g.drawImage(airplane.getAirplaneIcon().getImage(), airplane.getX(), airplane.getY(), null);
                g.fillRect(airplane.getX(), airplane.getY(), 5, 5);
            }
        }

        for(Airport airport: airportsList){
//            g.drawImage(genericIcon, airport.getxRunway(), airport.getyRunway(), null);
            if(!airport.isRunwayClear()) {
                g.setColor(Color.RED);
            }else{
                g.setColor(Color.BLUE);
            }
            g.fillRect(airport.getxRunway(),airport.getyRunway(),5,5);

        }


    }

    /**
     * Creating thread for airplane
     * @param airplane provided airplane object to start thread
     * @param loaded switch to indicate if airplane was created after loading from file
     */
    private void makeThread(Airplane airplane, Boolean loaded){
        AirplaneRunnable airplaneRunnable = new AirplaneRunnable(airplane);
        if(loaded){
            airplaneRunnable.loaded();
        }
        runnableList.add(airplaneRunnable);
        Thread thread = new Thread(threadGroup, airplaneRunnable);
        threadList.add(thread);
        thread.start();
    }

    /**
     * Saving airplanes to file data.txt
     */
    public void saveComponents(){
        try(ObjectOutputStream outS = new ObjectOutputStream(new FileOutputStream("data.txt"))){
            for(Airplane airplane: airplanesList){
                outS.writeObject(airplane);
            }

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Loading airplanes from data.txt
     */
    public void loadComponents(){
        boolean firstFound = true;
        //stops all running threads before loading
        for(AirplaneRunnable thread: runnableList){
            thread.stop();
        }

        //clearing airplanesList and repainting graphics
        airplanesList.clear();
        repaint();
        revalidate();

        try(ObjectInputStream inS = new ObjectInputStream(new FileInputStream("data.txt"))){
            while (true) {
                Airplane airplane = (Airplane) inS.readObject();
                airplanesList.add(airplane);
                //After loading state when currentDestination.runwayClear() is FALSE planes waiting for start WILL NEVER start.
                //One of the planes has to be pushed through starting procedure without checking for clear runway.
                if(firstFound && airplane.getState() == Airplane.State.WAITING){
                    System.out.println(airplane.getName());
                    makeThread(airplane, true);
                    firstFound = false;
                }
                //All other planes can now use standard starting procedure
                makeThread(airplane, false);
            }
        }catch (IOException | ClassNotFoundException e){
            e.getMessage();
        }

    }

    public ArrayList<Airplane> getAirplanesList() {
        return airplanesList;
    }

    /**
     * Waits for threads to end by creating anonymous class with new Thread.
     * Required to listen for exit event without interrupting Swing thread.
     */
    public void waitAndExit(){
        for(AirplaneRunnable airplaneRunnable: runnableList){
            airplaneRunnable.exit();
        }

        new Thread(() -> {
            for(Thread thread: threadList){
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.exit(0);
        }).start();
    }


    private class AirplaneRunnable implements Runnable{

        private Airplane airplane;
        private boolean flying = true;
        private boolean loaded = false;
        private boolean exiting = false;

        AirplaneRunnable(Airplane airplane) {
            this.airplane = airplane;
        }

        public boolean isFlying() {
            return flying;
        }

        @Override
        public void run() {
            Airplane.State previousState = airplane.getState();

            while(flying){

                if(airplane.getState() != previousState){
                    System.out.println(airplane.getName() + " changed to " + airplane.getState().toString());
                    previousState = airplane.getState();
                }

                switch (airplane.getState()) {

                    case FLYING:
                        this.airplane.flyAirplane();
                        repaint();
                        threadSleep(airplane.getSpeed());
//                        debugging for fast airplanes
//                        threadSleep(2);
                        break;

                    case LANDED:
//                        if (airplane.getCurrentAirport().equals(airport3)) {
//                            airplane.setDestination(airport1);
//                        } else {
//                            airplane.setDestination(airport3);
//                        }
                        if(!exiting) {
                            airplane.setDestination(airportsList);
                            airplane.startTaxi(loaded);
                        } else {
                            flying = false;
                        }
                        break;

                    case LANDING:
                        airplane.getDestination().setRunwayClear(false);
                        threadSleep(1000);
                        airplane.getDestination().setRunwayClear(true);

                        airplane.land();
                        airplane.refuel();
                        break;

                    case TAKINGOFF:
                        if(!loaded) {
                            if (airplane.getCurrentAirport().isRunwayClear()) {
                                airplane.getCurrentAirport().setRunwayClear(false);
                                threadSleep(1000);
                                airplane.getCurrentAirport().setRunwayClear(true);
                                airplane.takeOff();
                            } else {
                                airplane.setState(Airplane.State.WAITING);
                            }
                        } else{
                            airplane.getCurrentAirport().setRunwayClear(false);
                            threadSleep(1000);
                            airplane.getCurrentAirport().setRunwayClear(true);
                            airplane.takeOff();
                            loaded = false;
                        }
                        break;

                    case WAITING:
                        airplane.startTaxi(loaded);
                        break;
                }

                //plane crashing if fuel is spent
                if (this.airplane.getCurrentFuel() == 0) {
                    airplane.crash();
                    repaint();
                    revalidate();
                    flying = false;
                    System.out.println(airplane.getName() + " crashed");
                }

            }
        }

        void exit(){
            exiting = true;
        }

        void stop(){
            flying = false;
        }

        void loaded(){
            loaded = true;
        }

        private void threadSleep(int milis){
            try {
                Thread.sleep(milis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
