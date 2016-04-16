package test;

import static org.junit.Assert.*;

import org.junit.Test;

import assignment6.theater.HouseEnum;

public class TestHouseEnum
{

	/**
	 * This test case tests if the HouseEnum toString()'s correctly
	 */
	@Test
	public void testToString()
	{
		assert("M".equals(HouseEnum.MIDDLE.toString()));
		assert("L".equals(HouseEnum.LEFT.toString()));
		assert("R".equals(HouseEnum.RIGHT.toString()));
	}

}
