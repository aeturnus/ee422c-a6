package assignment6.theater;

import java.util.ArrayList;

public class Theater
{
	ArrayList<Seat> seats;
	
	public Theater(){
		generateSeats();
	}
	
	/**
	 * This method creates the seats; highest priority seats in at the beginning
	 */
	private void generateSeats(){
		seats = new ArrayList<Seat>();
		//Generate the seats: put them in priority order
		for(int r = 0; r <= 26; r++){
			//Middle seats
			if(r <= 23){
				//Only rows A-X have middle house
				for(int s = 108; s <= 121; s++){
					seats.add(new Seat(r, s, HouseEnum.MIDDLE));
				}
			}
			
			//House right seats
			if(r == 26){
				//AA has only two house right seats
				seats.add(new Seat(r, 127, HouseEnum.RIGHT));
				seats.add(new Seat(r, 128, HouseEnum.RIGHT));
			}
			else{
				//All the other rows go from 122-128
				for(int s = 122; s <= 128; s++){
					seats.add(new Seat(r, s, HouseEnum.RIGHT));
				}
			}
			
			//House left seats
			if(r == 26){
				//row AA has a weird house left
				for(int s = 101; s <= 104; s++){
					seats.add(new Seat(r, s, HouseEnum.LEFT));
				}
				for(int s = 116; s <= 118; s++){
					seats.add(new Seat(r, s, HouseEnum.LEFT));
				}
			}
			else if(r > 2){
				//D - Z have 101-107 seats
				for(int s = 101; s <= 107; s++){
					seats.add(new Seat(r, s, HouseEnum.LEFT));
				}
			}
			else if (r == 2){
				//C has 1-106 seats
				//rows A and B have no house left
				for(int s = 101; s <= 106; s++){
					seats.add(new Seat(r, s, HouseEnum.LEFT));
				}
			}
		}
	}
	
	public String toString(){
		String output = "";
		int length = seats.size();
		for(int i = 0; i < length; i++)
		{
			output += seats.get(i) + "\n";
		}
		return output;
	}
}