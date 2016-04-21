package assignment6;

import static org.junit.Assert.fail;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import org.junit.Test;

public class TestTicketOffice {

	public static int score = 0;

	// @Test
	public void basicServerTest() {
		try {
			TicketServer.start(16789);
		} catch (Exception e) {
			fail();
		}
		TicketClient client = new TicketClient(16789);
		client.requestTicket();
		TicketServer.reset();
	}

	@Test
	public void testServerCachedHardInstance() {
		try {
			TicketServer.start(16790);
		} catch (Exception e) {
			fail();
		}
		TicketClient client1 = new TicketClient("localhost", "c1",16790);
		TicketClient client2 = new TicketClient("localhost", "c2",16790);
		client1.requestTicket();
		client2.requestTicket();
		TicketServer.reset();
	}

	@Test
	public void twoNonConcurrentServerTest() {
		try {
			TicketServer.start(16791);
		} catch (Exception e) {
			fail();
		}
		TicketClient c1 = new TicketClient("nonconc1",16791);
		TicketClient c2 = new TicketClient("nonconc2",16791);
		TicketClient c3 = new TicketClient("nonconc3",16791);
		c1.requestTicket();
		c2.requestTicket();
		c3.requestTicket();
		TicketServer.reset();
	}

	@Test
	public void twoConcurrentServerTest() {
		try {
			TicketServer.start(16792);
		} catch (Exception e) {
			fail();
		}
		final TicketClient c1 = new TicketClient("conc1",16792);
		final TicketClient c2 = new TicketClient("conc2",16792);
		final TicketClient c3 = new TicketClient("conc3",16792);
		Thread t1 = new Thread() {
			public void run() {
				c1.requestTicket();
			}
		};
		Thread t2 = new Thread() {
			public void run() {
				c2.requestTicket();
			}
		};
		Thread t3 = new Thread() {
			public void run() {
				c3.requestTicket();
			}
		};
		t1.start();
		t2.start();
		t3.start();
		try {
			t1.join();
			t2.join();
			t3.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
		TicketServer.reset();

	}
	
	@Test
	public void autoConcurrentServerTest() {
		int serverPort1 = 16793;
		int serverPort2 = 16794;
		int serverPort3 = 16795;
		try {
			TicketServer.reset();
			TicketServer.start(serverPort1);
			TicketServer.start(serverPort2);
			TicketServer.start(serverPort3);
		} catch (Exception e) {
			fail();
		}
		ArrayList<RequestThread> threadList = new ArrayList<RequestThread>();
		TicketClient tc;
		for(int i = 0; i < 1000; i++){
			tc = new TicketClient("conc #"+i,serverPort1 + i%3);	//use modulo to pick a port to connect to
			RequestThread thread = new RequestThread(tc);
			threadList.add(thread);
			thread.start();
		}
		try {
			for(int i = 0; i < threadList.size(); i++)
			{
				threadList.get(i).join();
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		try{
			//Sort the log
			File csv = new File("./log.csv");
			TicketServer.printLog(new PrintStream(csv));
			System.out.println("Ticket log written to " + "./log.csv");
			//System.exit(0);
		} catch (IOException ioe){
			//System.err.println("server failed to close server socket for port " + TicketServer.PORT);
			ioe.printStackTrace();
		}
		
		assertTrue(TicketServer.checkLogDoubles());
	}
}

class RequestThread extends Thread{
	TicketClient tc;
	RequestThread(TicketClient client){
		tc = client;
	}
	public void run(){
		tc.requestTicket();
	}
}
