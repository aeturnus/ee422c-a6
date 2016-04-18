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
	static int PORT = 2222;
	// EE422C: no matter how many concurrent requests you get,
	// do not have more than three servers running concurrently
	final static int MAXPARALLELTHREADS = 3;
	static ServerSocket serverSocket = null;
	static CyclicBarrier serverSocketBarrier = null;	//Have a barrier kill the socket when the server threads are done
	static Theater theater = null;
	static ArrayList<TicketLog> log;
	public static void start(int portNumber) throws IOException {
		PORT = portNumber;
		theater = new Theater();	//Get ourselves a theater!
		log = new ArrayList<TicketLog>();
		
		//Create a serversocket for this server
		serverSocket = null;
		try{
			serverSocket = new ServerSocket(TicketServer.PORT);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		
		//Barrier code for shutdown
		serverSocketBarrier = new CyclicBarrier(MAXPARALLELTHREADS, new Runnable() {
			public void run(){
				//Use a barrier to wait until the server threads die to shut down the socket
				try{
					if(TicketServer.serverSocket != null){
						TicketServer.serverSocket.close();
						System.out.println("Server has closed port " + TicketServer.PORT);
						//Sort the log
						log.sort(null);
						File csv = new File("./log.csv");
						printLog(new PrintStream(csv));
					}
				} catch (IOException ioe){
					System.err.println("server failed to close server socket for port " + TicketServer.PORT);
					ioe.printStackTrace();
				}
			}
		});
		
		//Create servers not exceeding the count of MAXPARALLELTHREADS
		//threadList = new ArrayList<ThreadedTicketServer>();
		for(int i = 0; i < MAXPARALLELTHREADS; i++){
			Runnable serverThread = new ThreadedTicketServer("Box Office " + Character.toString((char)('A' + i))
																,serverSocket
																,serverSocketBarrier);	//Get office letter with 'A' offset
			Thread t = new Thread(serverThread);
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
	
	static void logSeat(long timestamp, Seat seat){
		log.add(new TicketLog(timestamp,seat));
	}
	
	static void printLog(PrintStream stream){
		int length = log.size();
		for(int i = 0; i < length; i++){
			stream.println(log.get(i).toString());
		}
	}
	
	static boolean checkLog(){
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
	CyclicBarrier barrier;		//Barrier for closing the socket
	static Lock seatLock = new ReentrantLock();
	
	//Made a constructor so it actually has a name and a server socket
	ThreadedTicketServer(String name, ServerSocket ssocket, CyclicBarrier socketBarrier){
		threadname = name;
		serverSocket = ssocket;
		barrier = socketBarrier;
	}

	public void run() {
		// TODO 422C
		
		boolean running = true;
		while(running)
		{
			try {
				//Listen for socket requests
				//Should run until no more seats
				Socket clientSocket = serverSocket.accept();	//Accept connections
				long time = System.nanoTime();	//Timestamps for request
				
				//As it stands, we have race conditions; oh fun!
				//Locations to target: accessing seat status, setting seat status; should lock seat status access
				seatLock.lock();
				//Critical section
				Seat seat = TicketServer.bestAvailableSeat();	//Get a seat as fast as possible	
				if(seat == null){	//If not a seat, then we're out. Finish up this thread and leave
					System.err.println("no seat");
					clientSocket.close();
					running = false;
					seatLock.unlock();
					continue;
				}
				TicketServer.markAvailableSeatTaken(seat);	//Set it taken
				TicketServer.logSeat(time,seat);
				seatLock.unlock();
				//Critical end
				
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out.println(time + "\t: " +threadname + " has given you ticket " + TicketServer.printTicket(seat));	//Tabbed timestamp; sortable by time in excel
				
				//Close the streams
				clientSocket.close();
				out.close();
				in.close();
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println(threadname + " went oopsie");
				e.printStackTrace();
			}
		}
		
		//We use a barrier to handle the closing of the port
		try{
			barrier.await();
		} catch (InterruptedException ioe){
			Thread.currentThread().interrupt();	//politely notify that we've been interrupted
		} catch (BrokenBarrierException bbe){
			System.err.println(threadname + ": barrier is broken :(");
		}
		
	}
}

/**
 * This class will serve as a log object
 */
class TicketLog implements Comparable<TicketLog>{
	private long timestamp;
	private Seat seat;
	
	TicketLog(long timestamp, Seat seat){
		this.timestamp = timestamp;
		this.seat = seat;
	}
	
	//CSV'able entry
	public String toString(){
		String output = timestamp + "," + seat.toString();
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