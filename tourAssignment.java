/*
Possathorn Sujipisut 6480274
*/

package Project2_6480929;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

//Tour class
class Tour{
    //Variables
    private String name;
    private int totalCapacity;
    private int currentCapacity;
    
    //Constructor
    public Tour(String name, int capacity){
        this.name = name;
        this.totalCapacity = capacity;
    }
    
    //Removing capacity from tour
    public synchronized void removeCapacity(int numPeople){
        currentCapacity = currentCapacity- numPeople;
    }
    
    //Reseting current capacity to total capacity
    public void resetCapacity(){
        currentCapacity = totalCapacity;
    }
    
    //Return tour name (For adding them into agenecy)
    public String getName(){
        return name;
    }
    
    //Return capcaity
    public int getCapcity(){
        return totalCapacity;
    }
}

//Agency class
class AgencyThread extends Thread{
    //Variables
    //Note: Using protected sometimes because I need to update the MainThread subclass
    private String agency;
    protected int currentCustomers = 0;
    protected int simulationDays;
    protected int maxCustomers;
    protected ArrayList<Tour> tourGroup;
    protected String tourName;
    protected int numAgencies;
    protected CyclicBarrier pauseBarrier;
    
    //Constructor to fill in variables
    public AgencyThread(String name, int days, int numAgen, 
            int maxCus, ArrayList<Tour> tour, String tName, CyclicBarrier barrier){
        //Fill in arguements into variables
        super(name);
        agency = name;
        simulationDays = days;
        numAgencies = numAgen;
        maxCustomers = maxCus;
        tourGroup = tour;
        pauseBarrier = barrier;
        this.tourName = tName;
        //System.out.println(tourName);
    }
    
    //Return agency name and tour group
    public String getAgencyName(){
        return agency;
    }
    
    public String getTourName(){
        return tourName;
    }
    
    @Override
    public void run(){
        
    }
    
    /*
    //Find the right tour group that is assigned to the agency
    for (int j = 0; j < numTours; j++){
                        if (agencyTourGroup == tour.get(j).getName()){
    
    */
}

//Main class (for main thread)
/*
Why is the scanning of info in the main thread rather than just doing it at the start
and then start the main thread?

A: Because that is what the instructions says, not to mention in the exanple output
it clearly states that all output lines are labeled with thread name using:
(Thread.currentThread().getName())
*/
class MainThread extends AgencyThread{
    //Variables
    String path = "src/main/java/Project2_6480929/";
    String file = "config.txt";
    
    //Constructor
    public MainThread(String name, int days, int numAgencies, int maxCustomers, ArrayList<Tour> tour, String tName, CyclicBarrier barrier){
        super(name, days, numAgencies, maxCustomers, tour, tName, barrier);
    }

    //Getting info from config.txt
    public String scanInfo(String path, String file, 
            ArrayList<Tour> tour, ArrayList<AgencyThread> agency){
        //Variables to temperary store variables to be added into constructors
        //Tour variables
        String tourName;
        int numTours = 0;
        int tourCapacity;
        boolean scannedTour = false;
        
        //Agency variables
        String agencyName;
        int simDays = 0;
        int numAgen = 0;
        String agencyTourGroup;
        int maxArrival = 0;
        CyclicBarrier barrier = new CyclicBarrier(1);

        try{
            //Setup scanner
            Scanner scan = new Scanner(new File(path + file));
            
            //While loop for scanning
            while(scan.hasNext()){
                //First line (days of simulation)
                String line = scan.nextLine();
                
                //Scan for num of days of the simulation
                if (simDays == 0){
                   simDays = Integer.parseInt(line);
                   this.simulationDays = simDays;
                   continue;
                }
                
                //Scan for max num of customers per day
                if (maxArrival == 0){
                    maxArrival = Integer.parseInt(line);
                    this.maxCustomers = maxArrival;
                    continue;
                }

                //If we have past the first two lines (thanks to the if loops)
                //Then we go on to scanning tours and agencies
                
                //Scan for num of tour groups
                if (numTours == 0){
                    numTours = Integer.parseInt(line);
                    continue;
                }
                
                //Scan tour groups in ArrayList
                if (scannedTour == false){
                    String [] buf;
                    
                    //For loop to scan number of lines based on numTours
                    for (int i = 0; i < numTours; i++){
                        //Get the tour name and capacity
                        buf = line.split("  ");
                        tourName = buf[0];
                        tourCapacity = Integer.parseInt(buf[1]);
                        
                        //Insert name and capacity into tour object, then into ArrayList
                        tour.add(new Tour(tourName, tourCapacity));
                        
                        //Scan next line (this if loop so it excludes the last tour)
                        if (i < numTours - 1){
                            line = scan.nextLine();
                        }
                    }
                    
                    //Change scannedTour into true since we scanned all the tours
                    this.tourGroup = tour;
                    scannedTour = true;
                    continue;
                }

                //Scan for num of agenecies
                if (numAgen == 0){
                    numAgen = Integer.parseInt(line);
                    
                    //Setting new barrier
                    barrier = new CyclicBarrier(numAgen);
                    this.pauseBarrier = barrier;
                    this.numAgencies = numAgen;
                    continue;
                }
                
                //Scan agencies (no if statement since last information needed to scan
                String [] buf = line.split("  ");
                agencyName = buf[0];
                agencyTourGroup = buf[1];
                agency.add(new AgencyThread(agencyName,simDays,numAgen,
               maxArrival, tour, agencyTourGroup, barrier));
            }
            scan.close();

        } catch(FileNotFoundException e) {
            //Bog standard exception handling
            System.err.println(e.toString());
            Scanner getFile = new Scanner(System.in);
            System.out.printf("Thread%-6s>> enter config file = \n", 
                    Thread.currentThread().getName());
            String newfile = getFile.nextLine();
            scanInfo(path, newfile, tour, agency);
        }
        
        //Return updated file (If file was not found)
        return file;
    }
    
    //What the main thread will be doing
    @Override
    public void run() {
        //Make the ArrayList for agency
        ArrayList<AgencyThread> agencyArrayList = new ArrayList<AgencyThread>();
        
        //Read config.txt file
        file = scanInfo(path, file, tourGroup, agencyArrayList);
        System.out.printf("\nThread %-6s>> %s%s%s\n\n",Thread.currentThread().getName(),
                "read parameters from file ", path, file);
        
        //Print out config.txt file

        System.out.printf("Thread %-6s>> %-25s = %d\n",Thread.currentThread().getName(),
                "days of simulation", simulationDays);
        System.out.printf("Thread %-6s>> %-25s = %d\n",Thread.currentThread().getName(),
                "maximum daily arrival", maxCustomers);
        
        //Printing out all the tours
        System.out.printf("Thread %-6s>> %-25s = ",Thread.currentThread().getName(),
                "(tour, daily capacity)");
        for (int i = 0; i < tourGroup.size(); i++){
            //Send to new line if current line too large
            int j = 3;
            if (i == j){
                System.out.printf("\n");
                System.out.printf("%44s", "");
                j = j + 4;
            }
            
            //Printing out tours
            System.out.printf("(%s,%4d)    ", tourGroup.get(i).getName(),
                    tourGroup.get(i).getCapcity());
        }
        
        //Printing out all the agencies
        System.out.printf("\nThread %-6s>> %-25s = ",Thread.currentThread().getName(),
                "(thread, tour)");
        
        for (int i = 0; i < agencyArrayList.size(); i++){
            //Send to new line if current line too large
            int j = 3;
            if (i == j){
                System.out.printf("\n");
                System.out.printf("%44s", "");
                j = j + 4;
            }
            
            //Printing out agencies and their tours
            String name = agencyArrayList.get(i).getTourName();
            System.out.printf("(%s, %s%-5s   ", agencyArrayList.get(i).getAgencyName(),
                    name, ")");
        }
        
        
        //Start simulation
        for (int i = 0; i < simulationDays; i++){
            
        }
        
    }
}

public class tourAssignment {
    public static void main(String[] args) {
        //Start the main thread
        
        //Make dummy ArrayList and CyclicBarrier (will get replaced in MainThread anyways
        ArrayList<Tour> dummyTourArrayList = new ArrayList<Tour>();
        CyclicBarrier dummyCyclicBarrier = new CyclicBarrier(1);
        
        //Making the main thread
        MainThread M1 = new MainThread("main", 0, 0, 0, 
                dummyTourArrayList, "dummy", dummyCyclicBarrier);
        
        //Starting the thread;
        M1.start();
    }
}
