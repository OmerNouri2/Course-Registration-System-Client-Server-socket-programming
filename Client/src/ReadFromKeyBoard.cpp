//
// Created by spl211 on 31/12/2020.
//

#include "ReadFromKeyBoard.h"
#include "boost/lexical_cast.hpp"

ReadFromKeyBoard::ReadFromKeyBoard(ConnectionHandler &connectionHandler, mutex &m, condition_variable &cv) :
connectionHandler(&connectionHandler), shouldTerminate(false), m(m), cv(cv){}

void ReadFromKeyBoard::run() {
    while (!shouldTerminate) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        vector<string> splittedString = splitString(line); // splittedString<-- is 1 record(line) slitted to an array
        encodeToSrv(splittedString);
    }
}
    // splitString func use to split a string by words
    vector<string> ReadFromKeyBoard::splitString(string s) {
    stringstream str(s);
    string word; // empty string
    vector<string> splittedStr;
    while (getline(str, word, ' ')) { // when arrive to the char ' '
        splittedStr.push_back(word); // push word to a new register in the vector
    }
    return splittedStr; // return an array of words
}

void shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

// check which command came from the keyboard & send it to the right function to handle the command
void ReadFromKeyBoard::encodeToSrv(vector<string> splittedStr) {
    if(splittedStr.empty())
        return;
    string operation = splittedStr[0];
    if((operation == "ADMINREG") | (operation == "STUDENTREG") | (operation == "LOGIN")){
        userPassOperation(splittedStr, operation);
    }
    if((operation == "COURSEREG") | (operation == "KDAMCHECK") | (operation == "COURSESTAT") |
            (operation == "ISREGISTERED") | (operation == "UNREGISTER")){
        courseNumOperation(splittedStr, operation);
    }
    if(operation == "STUDENTSTAT"){
        userOperation(splittedStr);
    }
    if((operation == "LOGOUT") | (operation == "MYCOURSES")){
        oper(splittedStr, operation);
    }
}

// send userPassOperation to the server
void ReadFromKeyBoard::userPassOperation(vector<string> vector, string operation) {
    short opcode;
    if(operation == "ADMINREG"){
        opcode = 1;
    }
    if(operation == "STUDENTREG"){
        opcode = 2;
    }
    if(operation == "LOGIN"){
        opcode = 3;
    }
    char bytesArr[2];
    shortToBytes(opcode,bytesArr);
    string userName = vector[1]; // if adminReg so the second word is the username.
    string password = vector[2]; // if adminReg so the third word is the password.
    char toSrv[userName.length() + password.length() + 4]; // the size should be enough to include opcode_userName_password
    char userNameArr[userName.length() + 1]; // declaring character array for userName
    strcpy(userNameArr, userName.c_str()); // copying the contents of the string to char array
    char passwdArr[password.length() + 1]; // declaring character array for userName
    strcpy(passwdArr, password.c_str()); // copying the contents of the string to char array
    // insert to the array 'toSrv' all the relevant params to handle event
    int indxOfToSrv = 0;
    for(size_t i = 0; i < sizeof(bytesArr); i++) {
        toSrv[indxOfToSrv] = bytesArr[i];
        indxOfToSrv++;
    }
    for(size_t i = 0; i < sizeof(userNameArr); i++) {
        toSrv[indxOfToSrv] = userNameArr[i];
        indxOfToSrv++;
    }
    for(size_t i = 0; i < sizeof(passwdArr); i++) {
        toSrv[indxOfToSrv] = passwdArr[i];
        indxOfToSrv++;
    }
    toSrv[indxOfToSrv] = '\0';
    connectionHandler->sendBytes(toSrv, sizeof(toSrv));

}

// send courseNumOperation to the server
void ReadFromKeyBoard::courseNumOperation(vector<string> vector, string operation) {
    short opcode;
    if(operation == "COURSEREG"){
        opcode = 5;
    }
    if(operation == "KDAMCHECK"){
        opcode = 6;
    }
    if(operation == "COURSESTAT"){
        opcode = 7;
    }
    if(operation == "ISREGISTERED"){
        opcode = 9;
    }
    if(operation == "UNREGISTER"){
        opcode = 10;
    }
    char bytesArr[2];
    shortToBytes(opcode,bytesArr);
    short courseNum = boost::lexical_cast<short>(vector[1]); // convert from char array to short
    char bytesArrCourseNum[2];
    shortToBytes(courseNum,bytesArrCourseNum); // convert from short to bytes
    char toSrv[sizeof(bytesArr)+ sizeof(bytesArrCourseNum)]; // the size should be enough to include opcode_courseNum
    // insert to the array 'toSrv' all the relevant params to handle event
    int indexOfToSrv = 0;
    for(size_t i = 0; i < sizeof(bytesArr); i++) {
        toSrv[indexOfToSrv] = bytesArr[i];
        indexOfToSrv++;
    }
    for(size_t i = 0; i < sizeof(bytesArrCourseNum); i++) {
        toSrv[indexOfToSrv] = bytesArrCourseNum[i];
        indexOfToSrv++;
    }
    connectionHandler->sendBytes(toSrv, sizeof(toSrv));
  }

// send userOperation to the server
void ReadFromKeyBoard::userOperation(vector<string> vector) {
    short opcode = 8;
    char bytesArr[2];
    shortToBytes(opcode,bytesArr);
    string userName = vector[1]; // if 'studentstat' so the second word is the username.
    char userNameArr[userName.length()]; // declaring character array for userName
    strcpy(userNameArr, userName.c_str()); // copying the contents of the string to char array
    char toSrv[sizeof(bytesArr) + sizeof(userNameArr) + 1]; // the size should be enough to include opcode_userName_'\0'
    // insert to the array 'toSrv' all the relevant params to handle event
    int indexOfToSrv = 0;
    for(size_t i = 0; i < sizeof(bytesArr); i++) {
        toSrv[indexOfToSrv] = bytesArr[i];
        indexOfToSrv++;
    }
    for(size_t i = 0; i < sizeof(userNameArr); i++) {
        toSrv[indexOfToSrv] = userNameArr[i];
        indexOfToSrv++;
    }
    toSrv[indexOfToSrv] = '\0';
    connectionHandler->sendBytes(toSrv, sizeof(toSrv));
 }

void ReadFromKeyBoard::oper(vector<string> vector, string operation) {
    short opcode;
    if(operation == "LOGOUT"){
        opcode = 4;
        char bytesArr[2];
        shortToBytes(opcode,bytesArr);
        connectionHandler->sendBytes(bytesArr, sizeof(bytesArr));
        unique_lock<std::mutex> locking(m); // Wait until ProtocolImpl sends data (answer for this command)
        cv.wait(locking); // wait until invoking getline method once a logout command was sent
    }
    else{// operation == "MYCOURSES"
        opcode = 11;
        char bytesArr[2];
        shortToBytes(opcode,bytesArr);
        connectionHandler->sendBytes(bytesArr, sizeof(bytesArr)); // this is when opcode - 11
    }
}

void ReadFromKeyBoard::terminate(){
    shouldTerminate = true;
}
