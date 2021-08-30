//
// Created by spl211 on 28/12/2020.
//
#include <string>
#include <iostream>
#include <vector>
#include "ConnectionHandler.h"
#include "ReadFromKeyBoard.h"

#ifndef BOOST_ECHO_CLIENT_PROTOCOLIMPLE_H
#define BOOST_ECHO_CLIENT_PROTOCOLIMPLE_H

class ProtocolImpl {
private:
    ConnectionHandler* connectionHandler;
    ReadFromKeyBoard& readFromKeyBoard;
    bool running;
    mutex& m;
    condition_variable& cv;

public:
    ProtocolImpl(ConnectionHandler &handler , ReadFromKeyBoard &readFromKeyBoard,  mutex &m, condition_variable &cv);
    void run();
    short bytesToShort(char* bytesArr);
    void processAck();
    void processError();
    void optional(short messageOp);
    void logout();
    void printOptional();
};

#endif //BOOST_ECHO_CLIENT_PROTOCOLIMPLE_H
