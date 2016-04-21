/*
 * EE 422C Assignment 6 Spring 2016
 * Brandon Nguyen (btn366)
 * Sharmistha Maity (sm47767)
 */

package assignment6;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

class ThreadedTicketClient implements Runnable {
	String hostname = "127.0.0.1";
	String threadname = "X";
	TicketClient sc;
	int serverPort;

	public ThreadedTicketClient(TicketClient sc, String hostname, String threadname, int serverPort) {
		this.sc = sc;
		this.hostname = hostname;
		this.threadname = threadname;
		this.serverPort = serverPort;
	}

	public void run() {
		System.out.flush();
		try {
			Socket echoSocket = new Socket(hostname, serverPort);
			// PrintWriter out =
			//new PrintWriter(echoSocket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
			BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
			System.out.println(in.readLine());
			echoSocket.close();
		}
		catch (UnknownHostException uhe){
			System.err.println(threadname + ": unknown host");
			uhe.printStackTrace();
		}
		catch (IOException ioe) {
			System.out.println("Sorry, tickets are not available at this time. The show is either sold out or there is no current show.");
		}
		
	}
}

public class TicketClient {
	ThreadedTicketClient tc;
	String result = "dummy";
	String hostName = "";
	String threadName = "";

	
	TicketClient(String hostname, String threadname, int serverPort) {
		tc = new ThreadedTicketClient(this, hostname, threadname,serverPort);
		hostName = hostname;
		threadName = threadname;
	}
	
	
	//These constructors will pick a random port from the ticket server for the connection
	TicketClient(String hostname, String threadname) {
		tc = new ThreadedTicketClient(this, hostname, threadname,TicketServer.getRandomPort());
		hostName = hostname;
		threadName = threadname;
	}

	TicketClient(String name) {
		this("localhost", name, TicketServer.getRandomPort());
	}

	TicketClient() {
		this("localhost", "unnamed client", TicketServer.getRandomPort());
	}

	void requestTicket() {
		// TODO thread.run()
		tc.run();
		//System.out.println(hostName + "," + threadName + " got one ticket");
	}

	void sleep() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
