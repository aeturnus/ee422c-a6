package test;

import static org.junit.Assert.*;

import org.junit.Test;

import assignment6.theater.HouseEnum;
import assignment6.theater.Seat;

public class TestSeat
{

	@Test
	public void testSeat()
	{
		Seat seat = new Seat(0, 100, HouseEnum.MIDDLE);
		assertTrue(seat != null);
	}

	@Test
	public void testSetTaken()
	{
		Seat seat = new Seat(0, 100, HouseEnum.MIDDLE);
		assertTrue(!seat.isTaken());
		seat.setTaken();
		assertTrue(seat.isTaken());
	}

	@Test
	public void testIsTaken()
	{
		Seat seat = new Seat(0, 100, HouseEnum.MIDDLE);
		assertTrue(!seat.isTaken());
	}

	@Test
	public void testToString()
	{
		Seat seat = new Seat(0, 100, HouseEnum.MIDDLE);
		String string = seat.toString();
		assertTrue(string.equals("HM, 100A"));
		
		seat = new Seat(25, 108, HouseEnum.RIGHT);
		string = seat.toString();
		assertTrue(string.equals("HR, 108Z"));
		
		seat = new Seat(26, 122, HouseEnum.LEFT);
		string = seat.toString();
		assertTrue(string.equals("HL, 122AA"));
	}
	
	@Test
	public void testCompareTo()
	{
		Seat frontMid = new Seat(0, 100, HouseEnum.MIDDLE);
		Seat frontLeft = new Seat(0, 100, HouseEnum.LEFT);
		Seat frontRight = new Seat(0, 100, HouseEnum.RIGHT);
		Seat backMid = new Seat(1, 100, HouseEnum.MIDDLE);
		Seat backLeft = new Seat(1, 100, HouseEnum.LEFT);
		Seat backRight = new Seat(1, 100, HouseEnum.RIGHT);
		
		assertTrue(frontMid.compareTo(frontLeft) < 0);
		assertTrue(frontMid.compareTo(frontRight) < 0);
		assertTrue(frontMid.compareTo(backMid) < 0);
		assertTrue(frontMid.compareTo(frontMid) == 0);
		
		assertTrue(frontLeft.compareTo(frontMid) > 0);
		assertTrue(frontRight.compareTo(frontMid) > 0);
		assertTrue(backMid.compareTo(frontMid) > 0);
		assertTrue(frontMid.compareTo(frontMid) == 0);
	}
}
