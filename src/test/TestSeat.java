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
		assert(seat != null);
	}

	@Test
	public void testSetTaken()
	{
		Seat seat = new Seat(0, 100, HouseEnum.MIDDLE);
		assert(!seat.isTaken());
		seat.setTaken();
		assert(seat.isTaken());
	}

	@Test
	public void testIsTaken()
	{
		Seat seat = new Seat(0, 100, HouseEnum.MIDDLE);
		assert(!seat.isTaken());
	}

	@Test
	public void testToString()
	{
		Seat seat = new Seat(0, 100, HouseEnum.MIDDLE);
		String string = seat.toString();
		assert(string.equals("HM, 100A"));
		
		seat = new Seat(25, 108, HouseEnum.RIGHT);
		string = seat.toString();
		assert(string.equals("HR, 122Z"));
		
		seat = new Seat(26, 122, HouseEnum.LEFT);
		string = seat.toString();
		assert(string.equals("HL, 122AA"));
	}

}
