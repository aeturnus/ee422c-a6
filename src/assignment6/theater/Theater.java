/*
 * EE 422C Assignment 6 Spring 2016
 * Brandon Nguyen (btn366)
 * Sharmistha Maity (sm47767)
 */

package assignment6.theater;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class serves the data structure holding the seats
 */
public class Theater
{
	//ArrayList<Seat> seats;
	Seat[] seats;
	
	public Theater(){
		seats = generateSeats();
	}
	
	
	/**
	 * Unmarks all the seats
	 */
	public void clear(){
		int length = seats.length;
		for(int i = 0; i < length; i++){
			seats[i].setOpen();
		}
	}
	
	/**
	 * This will return the best available seat
	 * (This might have to have synchronization)
	 * @return Seat reference if there is one, null if there isn't
	 */
	public Seat getBestAvailableSeat(){
		Seat output = null;
		Seat temp;
		int length = seats.length;
		for(int i = 0; i < length && output == null; i++){
			temp = seats[i];
			if(!temp.isTaken()){
				output = temp;
			}
		}
		return output;
	}
	
	
	/**
	 * This method creates the seats; highest priority seats in at the beginning
	 */
	private Seat[] generateSeats(){
		ArrayList<Seat> seatList = new ArrayList<Seat>();
		//Generate the seats: put them in priority order
		for(int r = 0; r <= 26; r++){
			//Middle seats
			if(r <= 23){
				//Only rows A-X have middle house
				for(int s = 108; s <= 121; s++){
					seatList.add(new Seat(r, s, HouseEnum.MIDDLE));
				}
			}
			
			//House right seats
			if(r == 26){
				//AA has only two house right seats
				seatList.add(new Seat(r, 127, HouseEnum.RIGHT));
				seatList.add(new Seat(r, 128, HouseEnum.RIGHT));
			}
			else{
				//All the other rows go from 122-128
				for(int s = 122; s <= 128; s++){
					seatList.add(new Seat(r, s, HouseEnum.RIGHT));
				}
			}
			
			//House left seats
			if(r == 26){
				//row AA has a weird house left
				for(int s = 101; s <= 104; s++){
					seatList.add(new Seat(r, s, HouseEnum.LEFT));
				}
				for(int s = 116; s <= 118; s++){
					seatList.add(new Seat(r, s, HouseEnum.LEFT));
				}
			}
			else if(r > 2){
				//D - Z have 101-107 seatList
				for(int s = 101; s <= 107; s++){
					seatList.add(new Seat(r, s, HouseEnum.LEFT));
				}
			}
			else if (r == 2){
				//C has 1-106 seatList
				//rows A and B have no house left
				for(int s = 101; s <= 106; s++){
					seatList.add(new Seat(r, s, HouseEnum.LEFT));
				}
			}
		}
		Seat[] newArray = new Seat[seatList.size()];
		newArray = seatList.toArray(newArray);
		return newArray;
	}
	
	public String toString(){
		String output = "";
		int length = seats.length;
		for(int i = 0; i < length; i++)
		{
			output += seats[i] + "\n";
		}
		return output;
	}
}