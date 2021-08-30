package bgu.spl.net.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Passive object representing the Database where all courses and users are stored.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You can add private fields and methods to this class as you see fit.
 */
public class Database {

	private LinkedHashMap<Integer,Course> coursesByNum; // a LinkedHashMap which map course-number to the Course object & maintain its insertion order
	private ConcurrentHashMap<String,User> usersByName; // a hashTable which map user name to the User object

	public static class SingletonHolder{
		private static Database database = new Database();
	}

	//to prevent user from creating new Database
	private Database() {
		coursesByNum = new LinkedHashMap<Integer,Course>();
		usersByName = new ConcurrentHashMap<String,User>();
		initialize("Courses.txt");
	}

	/**
	 * Retrieves the single instance of this class.
	 */
	public static Database getInstance() {
		return SingletonHolder.database;
	}

	/**
	 * loades the courses from the file path specified
	 * into the Database, returns true if successful.
	 */
	boolean initialize(String coursesFilePath) {
		Scanner scan = null; // Initialize Scanner object
		try {
			scan = new Scanner(new File(coursesFilePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		scan.useDelimiter("\n"); // initialize the string delimiter for splitting the file by line

		String currCourseData; // <-- String that saves all data regarding current course
		String[] courseDataArr; // <-- Array that saves all data regarding current course
		int courseNum;
		String courseName;
		int maxStudents;
		LinkedList<Integer> kdamCourses;

		while(scan.hasNext()){ // as long as there are more courses execute:
			currCourseData = scan.next();
			courseDataArr = currCourseData.split("\\|");
			int j=0;
			courseNum = Integer.parseInt(courseDataArr[j]);
			courseName = courseDataArr[++j]; // ++j increments the number before the current expression is evaluated
			String kdamAsString = courseDataArr[++j]; // kdamAsString <-- all kdam courses of curr course are saved as String
			kdamAsString = kdamAsString.substring(1, kdamAsString.length()-1); // omit from the string first char - '[' & last char - ']'
			String[] kdamCoursesArr = kdamAsString.split(","); // kdamStArr <-- array with all the kdam courses
			kdamCourses = new LinkedList<Integer>();
			for (String kdam: kdamCoursesArr) {
				if(kdam.equals("")) // there is no kdam course
					break;
				kdamCourses.push(Integer.parseInt(kdam)); // parsing kdam courses from String to an int & put ir in a linked list
			}
			String maxStudentSt = courseDataArr[++j].trim(); // ++j increments the number before the current expression is evaluated
			maxStudents = Integer.parseInt(maxStudentSt); // conver maxStudent from String to an int
			Course currCourse = new Course(courseNum,courseName,kdamCourses,maxStudents); // create course
			coursesByNum.put(courseNum, currCourse);
		}
		scan.close(); // closing the scanner stream
		return true;
	}

	// insert new user to the DB
	public User insertUser(String userName, User user){
		 return usersByName.putIfAbsent(userName, user); // if the specified userName is not already associated with a User associates it with the given value.
	}

	public User getUser(String userName){
		return usersByName.get(userName); // Returns the User object to which the specified user is mapped, or null if this map contains no mapping for the userName.
	}

	public Course getCourse(int courseNumber){
		return coursesByNum.get(courseNumber); // Returns the Course object with the specified number, or null if this map contains no mapping for the course number.
	}

	public LinkedHashMap<Integer, Course> getCoursesByNum() {
		return coursesByNum;
	}
}
