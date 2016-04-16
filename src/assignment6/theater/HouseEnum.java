package assignment6.theater;

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
