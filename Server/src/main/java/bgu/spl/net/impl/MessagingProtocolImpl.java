package bgu.spl.net.impl;

import bgu.spl.net.api.MessagingProtocol;

import java.util.LinkedList;

public class MessagingProtocolImpl implements MessagingProtocol {
    Database database = Database.getInstance();
    AckOperation ackOperation;
    ErrorOperation errorOperation;
    short opcodeAck = 12;
    short opcodeError = 13;
    User currUser = null;
    boolean shouldTerminate = false;


    @Override
    // send to the right process according the received opcode
    public Object process(Object msg) {
        if(msg instanceof UserPasswdOperation){ // msg from type 1/2/3
            UserPasswdOperation userPasswdOperation = (UserPasswdOperation) msg;
            return process(userPasswdOperation);
        }
        if(msg instanceof CourseNumOperation) { // msg from type 5/6/7/9/10
            CourseNumOperation courseNumOperation = (CourseNumOperation) msg;
            return process(courseNumOperation);
        }
        if(msg instanceof UsernameOperation){ // msg from type 8
            UsernameOperation usernameOperation = (UsernameOperation) msg;
            return process(usernameOperation);
        }
        if(msg instanceof Operation) {// 4/11
            return process((Operation) msg); // msg from type Operation
        }
        return null;
    }

    private Operation process(UserPasswdOperation userPasswdOperation){ // for opcode 1/2/3
        short opcode = userPasswdOperation.getOpcode();
        String userName = userPasswdOperation.getUsername();
        String password = userPasswdOperation.getPasswd();
        if(currUser!=null){ // if client already logged-in with a user
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation; // it is not possible to de ADMINREG , STUDENTREG, LOGIN while already logged in
        }
        if(opcode == 1) { // message from type ADMINREG
            User user = new User(userName,password, true);
            if(database.insertUser(userName, user) == null){ // null means that the insertion succeed else - error
                ackOperation = new AckOperation(opcodeAck, opcode,"");
                return ackOperation;
            }
            else{ //the username is already registered
                errorOperation = new ErrorOperation(opcodeError, opcode);
                return errorOperation;
            }
        }
        if(opcode == 2) { // message from type STUDENTREG
            User user = new User(userName,password, false);
            if(database.insertUser(userName, user) == null){ // null means that the insertion succeed
                ackOperation = new AckOperation(opcodeAck, opcode,"");
                return ackOperation;
            }
            else{ //the username is already registered
                errorOperation = new ErrorOperation(opcodeError, opcode);
                return errorOperation;
            }
        }
        else{ // (opcode == 3) - message from type LOGIN
            User user = database.getUser(userName);
            //user doesn’t exist in the table or client already logged-in with a user or the password doesn’t match or the user is already logged in
            if(user == null || (currUser!=null) || ((!user.getPassword().equals(password)) | user.isLoggedIn())) {
                errorOperation = new ErrorOperation(opcodeError, opcode);
                return errorOperation;
            }
            else if (user.getPassword().equals(password)) { // make sure the given password is matched the password of the user
                currUser = user;
                currUser.setLoggedIn(true); // mark the current user as logged in
                ackOperation = new AckOperation(opcodeAck, opcode, "");
                return ackOperation;
            }
        }
        return null;
    }

    // private function which created to take care of - 'COURSEREG' command
    private boolean didKdamCourses(int courseNum){
        Course course = database.getCourse(courseNum);
        LinkedList <Integer> kdamCourses = course.getKdamCoursesList();
        for (int kdamCourseNum : kdamCourses) {
            if(!currUser.getRegisteredCourses().contains(kdamCourseNum))
                return false; // FALSE - if the student doesn't have the kdamCourses which needed for 'COURSEREG'
        }
        return true; // true - if the student has the kdamCourses which needed for 'COURSEREG'
    }

    private Operation processCourseReg(short opcode, int courseNumber, Course course){
        // if the no user is logged-in | the request came from an admin | no seats are available in this course| didn't do all kdam courses
        if ((currUser == null) ||(currUser.isAdmin) || (!course.isAvailable() | (!didKdamCourses(courseNumber)))) {
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation;
        }
        else {
            boolean isStudentAdded = course.addStudent(currUser); // true if the student was added to the list of registered students for this course, otherwise false
            if(isStudentAdded) { //if the student was added to the list of registered students of the specified course
                currUser.registerCourse(courseNumber); // add to the list of courses of the user the specified course
                ackOperation = new AckOperation(opcodeAck, opcode, "");
                return ackOperation;
            }
            else { //if the student couldn't added to the list of registered students of the specified course
                errorOperation = new ErrorOperation(opcodeError, opcode);
                return errorOperation;
            }
        }
    }

    private Operation processKdamCheck(short opcode, Course course){
        LinkedList <Integer> kdamCourses = course.getKdamCoursesList();
        ackOperation = new AckOperation(opcodeAck, opcode, kdamCourses.toString()+ "\0"); // the server returns the list of the KDAM courses
        return ackOperation;
    }

    private Operation processCourseStat(short opcode, int courseNumber, Course course){
        if((currUser == null) || (!currUser.isAdmin())) { // if no user is currently logged in & only admin user can send this type of message
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation;
        }
        String toReturn = "Course: ("+ courseNumber + ") "+ course.getCourseName() + "\n" +
                "Seats Available: " + course.getNumOfSeatsAvailable() + "/" + course.getMaxNumOfSeats() + "\n" +
                "Students Registered: " + course.getStudentsRegistered().toString().replaceAll("\\s+", "") + "\0";
        ackOperation = new AckOperation(opcodeAck, opcode, toReturn); // the server returns the state of the course
        return ackOperation;
    }

    private Operation processIsRegistered(short opcode, int courseNumber){
        String toReturn;
        if(currUser ==  null || currUser.isAdmin()){ // if no user is logged in & if the user is the admin
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation;
        }
        if(currUser.getRegisteredCourses().contains(courseNumber)){
            toReturn = "REGISTERED" + "\0"; // the student registered to the course
        }
        else {
            toReturn = "NOT REGISTERED" + "\0"; // the student is not registered to the course
        }
        ackOperation = new AckOperation(opcodeAck, opcode, toReturn); // the server returns “REGISTERED” if the student is already registered, otherwise -  “NOT REGISTERED”
        return ackOperation;
    }

    private Operation processUnRegistered(short opcode, int courseNumber, Course course){
        // no user is logged in | the request came from an admin | the user was not register to this course
        if ((currUser ==  null) || (currUser.isAdmin) || (!currUser.getRegisteredCourses().contains(courseNumber))) {
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation;
        }
        else { // need to unregister the student from this course
            currUser.unRegisterCourse(courseNumber); // remove the course from the list of courses of the student
            course.removeStudent(currUser); // remove the student from this course
            ackOperation = new AckOperation(opcodeAck, opcode, "");
            return ackOperation;
        }
    }

    private Operation process(CourseNumOperation courseNumOperation) {
        short opcode = courseNumOperation.getOpcode();
        int courseNumber = courseNumOperation.getCourseNum();
        Course course = database.getCourse(courseNumber);
        if (course == null | currUser == null) { // no such course is exist Or user is not logged in
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation;
        }
        if (opcode == 5) { // message from type COURSEREG
            return processCourseReg(opcode, courseNumber, course);
        }
        if (opcode == 6) { // message from type KDAMCHECK
            return processKdamCheck(opcode, course);
        }
        if (opcode == 7) { // message from type COURSESTAT
            return processCourseStat(opcode, courseNumber, course);
        }
        if (opcode == 9) { // message from type ISREGISTERED
            return processIsRegistered(opcode, courseNumber);
        }
        if (opcode == 10) { // message from type UNREGISTER
            return processUnRegistered(opcode, courseNumber, course);
        }
        return null;
    }

    private Operation process(UsernameOperation usernameOperation){ // msg from type STUDENTSTAT
        short opcode = usernameOperation.getOpcode();
        String userName = usernameOperation.getUsername();
        // if no user was logged in OR current user is not an admin OR the request is regarding an admin user
        if(currUser == null || (!currUser.isAdmin()) || database.getUser(userName).isAdmin()) {
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation;
        }
        else{
            User user = database.getUser(userName);
            String toReturn = "Student: " + userName + "\n" +
                   "Courses: " + user.getregisterCoursesOrderedLikeDB().toString().replaceAll("\\s+","") + "\0";
            ackOperation = new AckOperation(opcodeAck, opcode, toReturn);
            return ackOperation;
        }
    }

    private Operation process(Operation operation){ // msg from type LOGOUT or MYCOURSES
        short opcode = operation.getOpcode();
        if(currUser == null){ // if before logout there is no user logged in --> error
            errorOperation = new ErrorOperation(opcodeError, opcode);
            return errorOperation;
        }
        if(opcode == 4){ //msg from type LOGOUT
            currUser.setLoggedIn(false); // logout this user from the system
            currUser = null;
          //  shouldTerminate = true;
            ackOperation = new AckOperation(opcodeAck, opcode, "");
            return ackOperation;
        }
        else {// opcode = 11 - msg from type MYCOURSES
            if(currUser.isAdmin()){ // if the user which logged in is admin
                errorOperation = new ErrorOperation(opcodeError, opcode);
                return errorOperation;
            }
            else {
                String toReturn = currUser.getRegisteredCoursesLL().toString().replaceAll("\\s+","") + "\0";
                ackOperation = new AckOperation(opcodeAck, opcode, toReturn);
                return ackOperation;
            }
        }
    }


    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
