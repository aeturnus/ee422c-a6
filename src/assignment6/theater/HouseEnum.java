/*
 * EE 422C Assignment 6 Spring 2016
 * Brandon Nguyen (btn366)
 * Sharmistha Maity (sm47767)
 */

package assignment6.theater;

/**
 * Enum to represent house
 */
public enum HouseEnum{
	MIDDLE,
	RIGHT,
	LEFT;
	
	public String toString(){
		switch(this){
		case MIDDLE:
			return "M";
		case RIGHT:
			return "R";
		case LEFT:
			return "L";
		default:
			return "BADHOUSE";
		}
	}
}
