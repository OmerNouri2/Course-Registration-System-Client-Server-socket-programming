//
// Created by spl211 on 31/12/2020.
//

#include <ConnectionHandler.h>
#include <mutex>
#include <condition_variable>


#ifndef BOOST_ECHO_CLIENT_READFROMKEYBOARD_H
#define BOOST_ECHO_CLIENT_READFROMKEYBOARD_H

using namespace std;
class ReadFromKeyBoard {
public:
    ReadFromKeyBoard(ConnectionHandler &connectionHandler, mutex &m, condition_variable &cv);
    void run();
    void terminate();

private:
    ConnectionHandler *connectionHandler;
    vector<string> splitString(string s);
    bool shouldTerminate;
    mutex& m;
    condition_variable& cv;

    void encodeToSrv(vector<string> vector);
    void userPassOperation(vector<string> vector, string operation);
    void courseNumOperation(vector<string> vector, string basicString);
    void userOperation(vector<string> vector);
    void oper(vector<string> vector, string basicString);

};


#endif //BOOST_ECHO_CLIENT_READFROMKEYBOARD_H
