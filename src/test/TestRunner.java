package test;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TestRunner
{
	public static void main(String[] args){
		Result results;
		
		results = JUnitCore.runClasses(TestHouseEnum.class);
		for(Failure failure : results.getFailures()){
			System.out.println(failure.toString());
		}
		System.out.println("******************************");
		System.out.println("Result of HorseEnum testing: " + (results.wasSuccessful()?"SUCCESS":"FAILURE"));
		System.out.println("******************************\n");
		
		results = JUnitCore.runClasses(TestSeat.class);
		for(Failure failure : results.getFailures()){
			System.out.println(failure.toString());
		}
		System.out.println("******************************");
		System.out.println("Result of Seat testing: " + (results.wasSuccessful()?"SUCCESS":"FAILURE"));
		System.out.println("******************************\n");
		
		results = JUnitCore.runClasses(TestTheater.class);
		for(Failure failure : results.getFailures()){
			System.out.println(failure.toString());
		}
		System.out.println("******************************");
		System.out.println("Result of Theater testing: " + (results.wasSuccessful()?"SUCCESS":"FAILURE"));
		System.out.println("******************************\n");
	}
}
