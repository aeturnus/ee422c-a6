/*
 * EE 422C Assignment 6 Spring 2016
 * Brandon Nguyen (btn366)
 * Sharmistha Maity (sm47767)
 */

package assignment6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import assignment6.theater.Seat;
import assignment6.theater.Theater;


/**
 * This class serves as a server for assigning tickets
 */
public class TicketServer {
	final static int MAXPARALLELTHREADS = 3;	//Maximum number of running servers
	static int serverCount = 0;					//Server count
	static Theater theater = new Theater();				//We have one theater
	static ArrayList<TicketLog> log = new ArrayList<TicketLog>();
	static final String logPath = "./log.csv";			//path for our log file
	static ArrayList<ThreadedTicketServer> servers = new ArrayList<ThreadedTicketServer>();	//Arraylist for the server objects
	static ArrayList<Thread> serverThreads = new ArrayList<Thread>();						//Arraylist for the threads they use
	
	static Lock logLock = new ReentrantLock();		//This lock is for accessing the log to prevent double adds
	static Lock theaterLock = new ReentrantLock();	//This lock is for accessing the theater through the public methods
													//with the unsynchronized methods
	public static void reset(){
		closeServers();		//shutdown servers
		theater.clear();	//empty the seats
		serverCount = 0;		//we have no more threads
		log.clear();		//we have to clear the log
		System.out.println("Server reset!");
	}
	
	/**
	 * Closes down all the servers
	 * Each server has 3 seconds to comply
	 * before being forcibly shutdown
	 */
	public static void closeServers(){
		//Shutdown our running servers
		for(int i = 0; i < servers.size(); i++){
			//Set their running flag to false
			servers.get(i).running = false;
			try{
				serverThreads.get(i).join(3000);			//Wait 3 seconds to finish up
				if(!servers.get(i).serverSocket.isClosed()){
					servers.get(i).serverSocket.close();	//Close its socket forcefully
				}
			}catch(InterruptedException ie){
				Thread.currentThread().interrupt();
			}catch(IOException ioe){
				System.err.println("Failed to close server socket on port " + servers.get(i).serverSocket.getLocalPort());
			}
		}
		//Clear our servers from the list
		servers.clear();
		serverThreads.clear();
	}
	
	public static void start(int portNumber) throws IOException {
		//Create a serversocket for the new server
		ServerSocket serverSocket = null;
		try{
			serverSocket = new ServerSocket(portNumber);
			//Create servers not exceeding the count of MAXPARALLELTHREADS
			if(serverCount < MAXPARALLELTHREADS){
				ThreadedTicketServer serverThread = new ThreadedTicketServer("Box Office " + Character.toString((char)('A' + serverCount))
																	,serverSocket);	//Get office letter with 'A' offset
				Thread t = new Thread(serverThread);
				servers.add(serverThread);
				serverThreads.add(t);
				serverCount++;
				t.start();
			}
		} catch (IOException ioe){
			System.err.println("Failure to create server on port " + portNumber);
			return;
		}
		
		
		
	}
	
	/**
	 * Picks a random port that a server is listening to and hands it back
	 * @return
	 */
	public static int getRandomPort(){
		int index = (int)Math.floor(Math.random()*serverCount);	//[0,3) is generated, floor for an index
		return servers.get(index).serverSocket.getLocalPort();
	}
	
	
	
	/**
	 * Logs a seat in the log
	 * @param timestamp
	 * @param seat
	 * @param office
	 */
	static void logSeat(long timestamp, Seat seat, String office){
		logLock.lock();
		log.add(new TicketLog(timestamp,seat,office));
		logLock.unlock();
	}
	
	/**
	 * Writes the log to a stream in CSV format
	 * @param stream
	 */
	static void printLogCSV(PrintStream stream){
		log.sort(null);
		int length = log.size();
		for(int i = 0; i < length; i++){
			stream.println(log.get(i).toString());
		}
	}
	
	/**
	 * This method checks if there are double printed tickets
	 * @return true if no double assignments
	 */
	static boolean checkLogDoubles(){
		log.sort(null);
		int length = log.size();
		for(int i = 0; i < length; i++){
			for(int j = i+1; j < length; j++){
				//Use compare to check for matches
				if(log.get(i).seat.compareTo(log.get(j).seat) == 0){
					System.err.println("checkLogDoubles: " + log.get(i)+ " and " + log.get(j)+ " conflict!");
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Check if seats assigned in the log were in order
	 * @return true if in order
	 */
	static boolean checkLogOrder(){
		log.sort(null);
		int length = log.size()-1;
		TicketLog one,two;
		for(int i = 0; i < length; i++){
			one = log.get(i);
			two = log.get(i+1);
			if(one.compareTo(two) >= 0){
				System.err.println("checkLogOrder: " + log.get(i)+ " and " + log.get(i+1)+ " time mismatch!");
				return false;
			}
		}
		return true;
	}
	
	////Apparently mandated functions to expose to the testcases?
	////They have primitive locking mechanisms for access
	/**
	 * TicketServer wrapper for best available seat
	 * @return best available seat
	 */
	public static Seat bestAvailableSeat(){
		//theaterLock.lock();
		Seat seat = theater.getBestAvailableSeat();
		//theaterLock.unlock();
		return seat;
	}
	
	/**
	 * TicketServer wrapper for marking a seat
	 * @return best available seat
	 */
	public static void markAvailableSeatTaken(Seat seat){
		//theaterLock.lock();
		seat.setTaken();
		//theaterLock.unlock();
	}
	
	/**
	 * TicketServer wrapper for printing a ticket
	 * @return ticket in string form
	 */
	public static String printTicket(Seat seat){
		return seat.toString();
	}
	////
	
	public static Seat getAndMarkBestAvailableSeat(){
		return theater.getAndMarkBestAvailableSeat();
	}
}

class ThreadedTicketServer implements Runnable {
//class ThreadedTicketServer extends Thread{

	String hostname = "127.0.0.1";
	String threadname = "X";
	String testcase;
	TicketClient sc;
	ServerSocket serverSocket;	//Have a serversocket for us to listen to
	static Lock seatLock = new ReentrantLock();	//Lock working with the seat; static because threads share it
	boolean running;			//Is it running the loop?
	
	//Made a constructor so it actually has a name and a server socket
	ThreadedTicketServer(String name, ServerSocket ssocket){
		threadname = name;
		serverSocket = ssocket;
		running = true;
	}

	public void run() {
		while(running)
		{
			try {
				//Listen for socket requests
				//Should run until no more seats
				Socket clientSocket = serverSocket.accept();	//Accept connections
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				
				long time = System.nanoTime();	//Timestamp for request
				//Critical start
				seatLock.lock();
				Seat seat = TicketServer.bestAvailableSeat();	//Get a seat as fast as possible	
				if(seat == null){	//If not a seat, then we're out. Finish up this thread and leave
					out.println(threadname + ": Sorry, we're out of seats!");
					running = false;
					seatLock.unlock();	//gotta unlock here too
					out.close();
					in.close();
					clientSocket.close();
					continue;
				}
				TicketServer.markAvailableSeatTaken(seat);
				seatLock.unlock();
				//Critical end
				TicketServer.logSeat(time, seat, threadname);	//Log our seat
				out.println(time + "\t: " +threadname + " has given you ticket " + TicketServer.printTicket(seat));	//Tabbed timestamp; sortable by time in excel
				
				//Close the streams
				out.close();
				in.close();
				clientSocket.close();
			} catch (IOException e) {
				System.err.println(threadname + " was closed forcefully");
			}
		}
		try{
			serverSocket.close();
		}catch(IOException ioe){
			System.err.println(threadname + ": has failed to close port " + serverSocket.getLocalPort());
		}
		
	}
}

/**
 * This class will serve as a log object
 */
class TicketLog implements Comparable<TicketLog>{
	long timestamp;
	Seat seat;
	String office;
	
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
			return seat.compareTo(other.seat);
		}
		return 1;
	}
	
	Seat getSeat(){
		return seat;
	}
}