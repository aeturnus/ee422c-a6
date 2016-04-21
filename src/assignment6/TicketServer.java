package assignment6;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import assignment6.theater.Seat;
import assignment6.theater.Theater;

/**
 * This class serves as a server for assigning tickets
 */

/*
 * We're going to need a data structure to store seats -Brandon 4/14/2016
 */

public class TicketServer {
	//static int PORT = 2222; we can't use this
	// EE422C: no matter how many concurrent requests you get,
	// do not have more than three servers running concurrently
	final static int MAXPARALLELTHREADS = 3;	//Maximum number of running servers
	static int numThread = 0;					//Server count
	static Theater theater = new Theater();				//We have one theater
	static ArrayList<TicketLog> log;
	static final String logPath = "./log.csv";			//path for our log file
	static ArrayList<ThreadedTicketServer> servers = new ArrayList<ThreadedTicketServer>();	//Arraylist for the server objects
	static ArrayList<Thread> serverThreads = new ArrayList<Thread>();						//Arraylist for the threads they use
	
	public static void reset(){
		//Shutdown our running servers
		for(int i = 0; i < servers.size(); i++){
			//Set their running flag to false
			servers.get(i).running = false;
			try{
				serverThreads.get(i).join();
			}catch(InterruptedException ie){
				Thread.currentThread().interrupt();
			}
		}
		//Clear our servers from the list
		servers.clear();
		serverThreads.clear();
		theater.clear();
		numThread = 0;
		log.clear();
		System.out.println("Server reset!");
	}
	
	public static void start(int portNumber) throws IOException {
		log = new ArrayList<TicketLog>();
		
		//Create a serversocket for the new server
		ServerSocket serverSocket = null;
		try{
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException ioe){
			ioe.printStackTrace();
			return;
		}
		
		//Create servers not exceeding the count of MAXPARALLELTHREADS
		//threadList = new ArrayList<ThreadedTicketServer>();
		if(numThread < MAXPARALLELTHREADS){
			ThreadedTicketServer serverThread = new ThreadedTicketServer("Box Office " + Character.toString((char)('A' + numThread))
																,serverSocket);	//Get office letter with 'A' offset
			Thread t = new Thread(serverThread);
			numThread++;
			t.start();
		}
		
	}
	
	/**
	 * TODO: Finish this
	 * TicketServer wrapper for best available seat
	 * @return best available seat
	 */
	static Seat bestAvailableSeat(){
		return theater.getBestAvailableSeat();
	}
	
	/**
	 * TODO: Finish this
	 * TicketServer wrapper for marking a seat
	 * @return best available seat
	 */
	static void markAvailableSeatTaken(Seat seat){
		seat.setTaken();
	}
	
	/**
	 * TODO: Finish this
	 * TicketServer wrapper for printing a ticket
	 * @return ticket in string form
	 */
	static String printTicket(Seat seat){
		return seat.toString();
	}
	
	static void logSeat(long timestamp, Seat seat, String office){
		log.add(new TicketLog(timestamp,seat,office));
	}
	
	static void printLog(PrintStream stream){
		log.sort(null);
		int length = log.size();
		for(int i = 0; i < length; i++){
			stream.println(log.get(i).toString());
		}
	}
	
	/**
	 * This method checks if there are double printed tickets
	 * @return no doubles
	 */
	static boolean checkLogDoubles(){
		log.sort(null);
		int length = log.size();
		for(int i = 0; i < length; i++){
			for(int j = i+1; j < length; j++){
				//Use compare to check for matches
				if(log.get(i).compareTo(log.get(j)) == 0){
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Check if seats assigned in the log were in order
	 * @return
	 */
	static boolean checkLogOrder(){
		log.sort(null);
		int length = log.size()-1;
		TicketLog one,two;
		for(int i = 0; i < length; i++){
			one = log.get(i);
			two = log.get(i+1);
			if(one.compareTo(two) >= 0){
				return false;
			}
		}
		return true;
	}
}

class ThreadedTicketServer implements Runnable {
//class ThreadedTicketServer extends Thread{

	String hostname = "127.0.0.1";
	String threadname = "X";
	String testcase;
	TicketClient sc;
	ServerSocket serverSocket;	//Have a serversocket for us to listen to
	static Lock seatLock = new ReentrantLock();
	boolean running;	//Is it running the loop?
	
	//Made a constructor so it actually has a name and a server socket
	ThreadedTicketServer(String name, ServerSocket ssocket){
		threadname = name;
		serverSocket = ssocket;
		running = true;
	}

	public void run() {
		// TODO 422C
		while(running)
		{
			try {
				//Listen for socket requests
				//Should run until no more seats
				Socket clientSocket = serverSocket.accept();	//Accept connections
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				//As it stands, we have race conditions; oh fun!
				//Locations to target: accessing seat status, setting seat status; should lock seat status access
				seatLock.lock();
				long time = System.nanoTime();	//Timestamp for request
				//Critical section
				Seat seat = TicketServer.bestAvailableSeat();	//Get a seat as fast as possible	
				if(seat == null){	//If not a seat, then we're out. Finish up this thread and leave
					out.println(threadname + ": Sorry, we're out of seats!");
					running = false;
					seatLock.unlock();
					out.close();
					in.close();
					clientSocket.close();
					continue;
				}
				TicketServer.markAvailableSeatTaken(seat);	//Set it taken
				TicketServer.logSeat(time,seat,threadname);
				seatLock.unlock();
				//Critical end
				
				
				out.println(time + "\t: " +threadname + " has given you ticket " + TicketServer.printTicket(seat));	//Tabbed timestamp; sortable by time in excel
				
				//Close the streams
				out.close();
				in.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(threadname + " went oopsie");
				e.printStackTrace();
			}
		}
		try{
			serverSocket.close();
			TicketServer.numThread--;
		}catch(IOException ioe){
			System.err.println(threadname + ": has failed to close port " + serverSocket.getLocalPort());
		}
		
	}
}

/**
 * This class will serve as a log object
 */
class TicketLog implements Comparable<TicketLog>{
	private long timestamp;
	private Seat seat;
	private String office;
	
	TicketLog(long timestamp, Seat seat, String office){
		this.timestamp = timestamp;
		this.seat = seat;
		this.office = office;
	}
	
	//CSV'able entry
	public String toString(){
		String output = timestamp + "," + seat.toString() + "," + office;
		return output;
	}
	
	/**
	 * Compares timestamps;
	 */
	public int compareTo(TicketLog other){
		long comp = timestamp - other.timestamp;
		if(comp < 0){
			return -1;
		}
		else if(comp == 0){
			return 0;
		}
		return 1;
	}
	
	Seat getSeat(){
		return seat;
	}
}