/*
Possathorn Sujipisut    6480274
Supakorn Unjindamanee   6480279
Phakkhapon Kaewmanee    6480929
Jawit Poopradit         6480087
*/

package Project2_6480279;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.*;

public class Project2_6480279 {
    public static void main(String[] args) {
        //Making the main thread
        MainThread M1 = new MainThread("main");
        
        //Starting the thread;
        M1.start();
    }
}

//Tour class
class Tour{
    //Variables
    final private String name;
    final private int totalCapacity;
    private int currentCapacity = 0;
    
    //Constructor
    public Tour(String name, int capacity){
        this.name = name;
        this.totalCapacity = capacity;
    }
    
    //Removing capacity from tour
    public synchronized int removeCapacity(int numPeople){
        //If current capcacity can handle all new customers, return all of numPeople;
        if (currentCapacity - numPeople >= 0){
            currentCapacity = currentCapacity - numPeople;            
            return numPeople;
        } else {
            //If cannot handle all new customers, set currentCapacity to 0 and
            //return leftover customers
            int leftovers = currentCapacity;
            currentCapacity = 0;
            return leftovers;
        }       
    }
    
    //Reseting current capacity to total capacity
    public void resetCapacity(){
        this.currentCapacity = this.totalCapacity;
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

//Agency class (extends Main Thread below)
class AgencyThread extends MainThread{
    //Variables
    final private String agency;
    final private String tourName;
    private Tour assignedTourGroup;
    private int currentCustomers = 0;
    private int totalArrival = 0;
    private int totalSuccess = 0;
    
    
    //Constructor to fill in variables
    public AgencyThread(String name, int days, int numAgen, int maxCus, ArrayList<Tour> tour, String tName, CyclicBarrier barrier){
        //Fill in arguements into variables
        super(name);
        agency = name;
        simulationDays = days;
        numAgencies = numAgen;
        maxCustomers = maxCus;
        tourGroup = tour;
        pauseBarrier = barrier;
        tourName = tName; 
        this.setTour();
    }
    
    //Return agency name
    public String getAgencyName(){
        return agency;
    }
    
    //Return tourName
    public String getTourName(){
        return tourName;
    }
    
    //Find the correct tour
    public void setTour(){
        for (int j = 0; j < tourGroup.size(); j++){
            String temp = tourGroup.get(j).getName();
            if (tourName.compareTo(temp) == 0){
                assignedTourGroup = tourGroup.get(j);
                
            }
        }
    }
    
    //Return total arrival
    public int getTotalArrival(){
        return totalArrival;
    }
    
    //Return total success
    public int getTotalSuccess(){
        return totalSuccess;
    }
    
    //What the agency thread will be running
    @Override
    public void run(){         
        for (int i = 0; i < simulationDays; i++){
            //Wait for main to print out line seperation and day number
            try{
                pauseBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println(e);
            }
            
            //Geting random amount of customers
            Random rand = new Random();
            int newCustomers = rand.nextInt(maxCustomers);
            currentCustomers = currentCustomers + newCustomers;
            System.out.printf("\nThread %-9s >> new arrival = %-15dremainging customers = %2d", 
                              Thread.currentThread().getName(), newCustomers,currentCustomers);
            
            //Save total arrival
            totalArrival = totalArrival + newCustomers;

            //Wait for every agency thread to get all new arrivals
            try{
                pauseBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println(e);
            }

            //Put customers onto tours
            int customersServiced = assignedTourGroup.removeCapacity(currentCustomers);
            currentCustomers = currentCustomers - customersServiced;
            System.out.printf("\nThread %-9s >> puts %2d customers on %s",
                              Thread.currentThread().getName(), customersServiced, tourName);
            
            //Save total customers serviced
            totalSuccess = totalSuccess + customersServiced;

            //Wait for every thread to put customers into tour
            try{
                pauseBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println(e);
            }
        }
    }
}

//Main thread
class MainThread extends Thread{
    //Variables
    final private String path = "src/main/java/Project2_6480279/";
    final private String startFile = "configg.txt";
    
    //Variables that will be shared to AgencyThread subclass   
    protected int simulationDays;
    protected int maxCustomers;
    protected ArrayList<Tour> tourGroup;    
    protected int numAgencies;
    protected CyclicBarrier pauseBarrier;
    
    //Constructor
    public MainThread(String name){
        super(name);
    }

    //Getting info from config.txt
    public void scanInfo(String path, String file, 
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
                    
                    //Setting new barrier (+1 because we also have main thread)
                    barrier = new CyclicBarrier(numAgen + 1);
                    this.pauseBarrier = barrier;
                    this.numAgencies = numAgen;
                    continue;
                }
                
                //Scan agencies (no if statement since last information needed to scan
                String [] buf = line.split("  ");
                agencyName = buf[0];
                agencyTourGroup = buf[1];
                agency.add(new AgencyThread(agencyName, simDays, numAgen, maxArrival, tour, agencyTourGroup, barrier));
            }
            System.out.printf("Thread %-9s >> %s%s%s\n\n",Thread.currentThread().getName(), 
                              "read parameters from file ", path, file);
            scan.close();
                        
        } catch(FileNotFoundException e) {
            //Bog standard exception handling
            System.out.printf("\nThread %-9s >> ", Thread.currentThread().getName());
            System.err.printf(e.toString());
            Scanner getFile = new Scanner(System.in);
            System.out.printf("\nThread %-9s >> enter config file = \n", 
                              Thread.currentThread().getName());
            String newfile = getFile.nextLine();
            scanInfo(path, newfile, tour, agency);
        }      
    }
    
    //What the main thread will be doing
    @Override
    public void run() {
        //Make the ArrayList for agency
        ArrayList<AgencyThread> agencyArrayList = new ArrayList<AgencyThread>();
        ArrayList<Tour> tourArrayList = new ArrayList<Tour>();
        
        //Read config.txt file
        scanInfo(path, startFile, tourArrayList, agencyArrayList);
        
        //Print out config.txt file

        System.out.printf("Thread %-9s >> %-25s = %d\n",Thread.currentThread().getName(),
                          "days of simulation", simulationDays);
        System.out.printf("Thread %-9s >> %-25s = %d\n",Thread.currentThread().getName(),
                          "maximum daily arrival", maxCustomers);
        
        //Printing out all the tours
        System.out.printf("Thread %-9s >> %-25s = ",Thread.currentThread().getName(),
                          "(tour, daily capacity)");
        for (int i = 0; i < tourGroup.size(); i++){
            //Send to new line if current line too large
            int j = 3;
            if (i == j){
                System.out.printf("\n");
                System.out.printf("%48s", "");
                j = j + 4;
            }
            
            //Printing out tours
            System.out.printf("(%s,%4d)            ", tourGroup.get(i).getName(),
                              tourGroup.get(i).getCapcity());
        }
        
        //Printing out all the agencies
        System.out.printf("\nThread %-9s >> %-25s = ",Thread.currentThread().getName(),
                          "(thread, tour)");
        
        for (int i = 0; i < agencyArrayList.size(); i++){
            //Send to new line if current line too large
            int j = 3;
            if (i == j){
                System.out.printf("\n");
                System.out.printf("%48s", "");
                j = j + 4;
            }
            
            //Printing out agencies and their tours
            String name = agencyArrayList.get(i).getTourName();
            System.out.printf("(%s, %s%-5s   ", 
                              agencyArrayList.get(i).getAgencyName(), name, ")");
        }
        
        //Extra formating
        System.out.println();
        
        
        //Start threads
        for (AgencyThread a : agencyArrayList){                
                    a.start();
        }
                
        for (int i = 0; i < simulationDays; i++){
            //Reset the current capacity of all tours as its the next day
            for (Tour t :tourGroup){
                t.resetCapacity();
            }
            
            //Formating
            System.out.printf("\nThread %-9s >> ",Thread.currentThread().getName());
            System.out.printf("-".repeat(80));
            System.out.printf("\nThread %-9s >> Day %-2d",
                              Thread.currentThread().getName(), i + 1);
            
            //Let the other threads start arrivals
            try{
                pauseBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println(e);
            }
            
            //Used so that other threads start putting customers onto tours
            try{
                pauseBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println(e);
            }
            
            //Wait for the other threads to finsih putting customers onto tours
            try{
                pauseBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                System.out.println(e);
            }           
        }   
        
        //Print out stats
        System.out.printf("\n\nThread %-9s >> ",Thread.currentThread().getName());
        System.out.printf("-".repeat(80));
        System.out.printf("\nThread %-9s >> Agency summary",
                          Thread.currentThread().getName());
        for (AgencyThread a : agencyArrayList){   
            System.out.printf("\nThread %-9s >> %-15stotal arrival = %-6dtotal success = %d",
                              Thread.currentThread().getName(), a.getAgencyName(), 
                              a.getTotalArrival(), a.getTotalSuccess());
        }
    }
}
