package assignment6;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
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
		TicketClient client = new TicketClient();
		client.requestTicket();
	}

	@Test
	public void testServerCachedHardInstance() {
		try {
			TicketServer.start(16790);
		} catch (Exception e) {
			fail();
		}
		TicketClient client1 = new TicketClient("localhost", "c1");
		TicketClient client2 = new TicketClient("localhost", "c2");
		client1.requestTicket();
		client2.requestTicket();
		
	}

	@Test
	public void twoNonConcurrentServerTest() {
		try {
			TicketServer.start(16791);
		} catch (Exception e) {
			fail();
		}
		TicketClient c1 = new TicketClient("nonconc1");
		TicketClient c2 = new TicketClient("nonconc2");
		TicketClient c3 = new TicketClient("nonconc3");
		c1.requestTicket();
		c2.requestTicket();
		c3.requestTicket();
	}

	@Test
	public void twoConcurrentServerTest() {
		try {
			TicketServer.start(16792);
		} catch (Exception e) {
			fail();
		}
		final TicketClient c1 = new TicketClient("conc1");
		final TicketClient c2 = new TicketClient("conc2");
		final TicketClient c3 = new TicketClient("conc3");
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

	}
	
	@Test
	public void autoConcurrentServerTest() {
		try {
			TicketServer.start(16793);
		} catch (Exception e) {
			fail();
		}
		ArrayList<RequestThread> threadList = new ArrayList<RequestThread>();
		TicketClient tc;
		for(int i = 0; i < 1000; i++){
			tc = new TicketClient("conc #"+i);
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
		assert(TicketServer.checkLog());
	}
	
	@Test
	public void autoConcurrentServerTestTiming() {
		try {
			TicketServer.start(16794);
		} catch (Exception e) {
			fail();
		}
		ArrayList<RequestThread> threadList = new ArrayList<RequestThread>();
		TicketClient tc;
		for(int i = 0; i < 1000; i++){
			tc = new TicketClient("conc #"+i);
			RequestThread thread = new RequestThread(tc);
			threadList.add(thread);
			try{
				Thread.sleep((int)(Math.random() * 10));	//sleep for a bit to simulate next client delay
			} catch(InterruptedException ie){
				Thread.currentThread().interrupt();
			}
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
		assert(TicketServer.checkLog());
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
