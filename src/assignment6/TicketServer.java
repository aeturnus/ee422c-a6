package assignment6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

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
	static int ticketCount = 50;
	public static void start(int portNumber) throws IOException {
		PORT = portNumber;
		
		//Create a serversocket for this server
		serverSocket = null;
		try{
			serverSocket = new ServerSocket(TicketServer.PORT);
		} catch (IOException ioe){
			ioe.printStackTrace();
		}
		
		serverSocketBarrier = new CyclicBarrier(MAXPARALLELTHREADS, new Runnable() {
			public void run(){
				//Use a barrier to wait until the others die to shut down the socket
				try{
					if(TicketServer.serverSocket != null){
						TicketServer.serverSocket.close();
						System.out.println("Server has closed port " + TicketServer.PORT);
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
}

class ThreadedTicketServer implements Runnable {
//class ThreadedTicketServer extends Thread{

	String hostname = "127.0.0.1";
	String threadname = "X";
	String testcase;
	TicketClient sc;
	ServerSocket serverSocket;	//Have a serversocket for us to listen to
	CyclicBarrier barrier;		//Barrier for closing the socket
	
	//Made a constructor so it actually has a name and a server socket
	ThreadedTicketServer(String name, ServerSocket ssocket, CyclicBarrier socketBarrier){
		threadname = name;
		serverSocket = ssocket;
		barrier = socketBarrier;
	}

	public void run() {
		// TODO 422C
		
		boolean running = true;
		while(TicketServer.ticketCount > 0)
		{
			try {
				//Listen for socket requests
				//Should run until no more seats
				Socket clientSocket = serverSocket.accept();	//Accept connections
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
				BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				out.println(threadname + ": " + TicketServer.ticketCount + " tickets left");
				TicketServer.ticketCount--;
				//Close the streams
				out.close();
				in.close();
				//Thread.currentThread().yield();
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