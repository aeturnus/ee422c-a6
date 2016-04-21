# EE 422C Spring 2016 Assignment 6 (Alternate)
## Brandon Nguyen (btn366)
## Sharmistha Maity (sm47767)

### github URL: https://github.com/aeturnus/ee422c-a6
There are three packages: **assignment6**, **assignment6.theater**, and **test**:

**assignment6** includes the **TestTicketOffice** class where the main tests that can run are located

**assignment6.theater** includes the **Theater**, **Seat**, and **HouseEnum** classes that organize the seating structure for the theater

**test** includes JUnit test cases that test the classes under **assignment6.theater** to make sure they are working correctly

## Notes:
We built this solution around the server being able to be reset. the *TicketServer.reset()* will close all open servers and their ports, unreserve the seats, and reset the server count. This allows an entire JUnit test case to run through all the methods, instead of having to run each separately in its own JVM instance. With reset, TicketServer will wait for the servers to finish up their last connection with a timeout in case they are blocking trying to accept() incoming connections.
This is helped by the *TicketServer.closeServers()* method which will handle closing the servers. Driver methods can also call this directly to just shut down the servers without performing a reset.

## PLEASE READ THIS
We eschewed the use of the PDF defined *bestAvailableSeat()* and *markAvailableSeatTaken()* methods (they are implemented however, with a primitive locking paradigm) in favor of a more robust method for finding available seats. This is implemented in **assignment6.theater** as *getAndMarkBestAvailableSeat*, which is able to find an available seat, mark it as taken, and return that seat. This is helped by the *testAndSet()* method of **assignment6.Seat** that, in a critical section, sets as taken and returns the old status; this method is in the vein of the x86 cmpxchg instruction with a LOCK prefix.

We believe that this method exemplifies concurrent access: the only locking occurs upon accessing a particular seat. The different servers are free to traverse the data structure containing the seats, but only one server at a time will be able to test-and-set a particular seat, setting the taken status in a critical section and awarding the particular server that test-and-set the seat to be returned to the calling methods.

If this is unsatisfactory, we have provided the alternate code, using the PDF mandated *bestAvailableSeat()* and *markAvailableSeatTaken()* methods to get and mark the seats in a critical section, under *./alternate*, where the same source directory structure is provided. In addition, a branch will be provided on GitHub that shows it.