package bgu.spl.net.impl;

import java.text.Collator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Course {
    int courseNum;
    String courseName;
    LinkedList<Integer> kdamCoursesList;
    int maxNumOfSeats; // max seats available in the course
    AtomicInteger numOfSeatsAvailable; // current seats available
    AtomicBoolean isAvailableSeats; // ture if there are seats available & false otherwise
    Collection<String> studentsRegistered;


    public Course(int courseNum, String courseName, LinkedList<Integer> kdamCoursesList, int numOfMaxStudents){
        this.courseNum = courseNum;
        this.courseName = courseName;
        this.kdamCoursesList = kdamCoursesList;
        this.maxNumOfSeats = numOfMaxStudents; // max seats available in this course
        this.numOfSeatsAvailable = new AtomicInteger(numOfMaxStudents); // current seats available in this course
        this.isAvailableSeats = new AtomicBoolean(numOfSeatsAvailable.intValue() > 0);
        this.studentsRegistered = new TreeSet<String>(Collator.getInstance()); // saves registered student by alphabetical order
    }

    public int getNumOfSeatsAvailable() {
        return numOfSeatsAvailable.intValue();
    }

    public String getCourseName() {
        return courseName;
    }

    public LinkedList<Integer> getKdamCoursesList() {
        return kdamCoursesList;
    }

    public int getMaxNumOfSeats() {
        return maxNumOfSeats;
    }

    public Collection<String> getStudentsRegistered() {
        return studentsRegistered;
    }

    public boolean isAvailable(){ // return true if there are available seats in this course
        return isAvailableSeats.get();
    }

    /* return true the given student can be added to the list of students registered to this course
       else return false
     */
    public synchronized boolean addStudent(User student){
        if(isAvailableSeats.get()){ // true if there are available seats in this course
            if(numOfSeatsAvailable.decrementAndGet() == 0){ // Atomically decrements by one the current value and compares to 0
                isAvailableSeats.set(false); // there is no more seats left in this course after the current student will be registered
            }
            studentsRegistered.add(student.getName()); // add the current student to the course
            return true; // student was added successfully to the list of students registered to this course
        }
        else
            return false; // student was not added to the list of students registered to this course
    }

    public void removeStudent(User user){
        numOfSeatsAvailable.incrementAndGet(); // Atomically increments by one the current value
        if(!isAvailableSeats.get()) // the seat that was belong to the student is now available
            isAvailableSeats.set(true);
        studentsRegistered.remove(user.getName()); //Removes the specified element from this set if it is present.
    }


}
