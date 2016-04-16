package test;

import org.junit.Test;

import assignment6.theater.Theater;

public class TestTheater
{

	@Test
	public void test()
	{
		Theater theater = new Theater();
		System.out.println(theater.toString());
		assert(true);
		//fail("Not yet implemented");
	}

}
