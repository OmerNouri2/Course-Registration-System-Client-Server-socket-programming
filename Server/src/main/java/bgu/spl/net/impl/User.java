package bgu.spl.net.impl;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class User {
    Database database;
    String name;
    String password;
    boolean isAdmin;
    boolean isLoggedIn; // isLoggedIn marks if user is already logged in
    HashSet<Integer> registeredCourses;
    LinkedList<Integer> registeredCoursesLL;



    public User(String name, String password, boolean isAdmin){
        database = Database.getInstance();
        this.name = name;
        this.password = password;
        this.isAdmin = isAdmin;
        isLoggedIn = false;
        registeredCourses = new HashSet<Integer>();
        registeredCoursesLL = new LinkedList<Integer>();
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    //for myCourses
    public LinkedList<Integer> getRegisteredCoursesLL() {
        return registeredCoursesLL;
    }


    public HashSet<Integer> getRegisteredCourses() {
        return registeredCourses;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    // Add the specified course to the list of registeredCourses of the student
    public void registerCourse(int courseNum){
        registeredCourses.add(courseNum);
        registeredCoursesLL.add(courseNum);
    }

    public void unRegisterCourse(int courseNum){
        registeredCourses.remove(courseNum); // Removes the specified course from this set if it is present.
        registeredCoursesLL.remove((Integer)courseNum); //Removes the first occurrence of the specified course from this list, if it is present.
    }

    public LinkedList<Integer> getregisterCoursesOrderedLikeDB(){
        LinkedList <Integer> registerCoursesOrderedLikeDB = new LinkedList<Integer>();
        Iterator<Integer> it = database.getCoursesByNum().keySet().iterator(); // iterate through the key (number of courses) of the 'coursesByNum' LinkedHashMap
        while (it.hasNext()) {
            int currCourse = it.next();
            if(registeredCourses.contains(currCourse)){ // if the user register to the curr course
                registerCoursesOrderedLikeDB.add(currCourse); // add it to the ordered courses list
            }
        }
        return registerCoursesOrderedLikeDB;
    }

}
