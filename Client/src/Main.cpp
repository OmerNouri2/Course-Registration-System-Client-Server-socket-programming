//
// Created by spl211 on 31/12/2020.
//
#include <ConnectionHandler.h>
#include <ReadFromKeyBoard.h>
#include <ProtocolImpl.h>
#include <thread>
#include <mutex>
#include <condition_variable>
#include "boost/lexical_cast.hpp"

using namespace std;
int main (int argc, char *argv[]) {
    string host = argv[1];
    string portSt = argv[2];
    short port = stoi(argv[2]); // convert from string to short
    mutex m;
    condition_variable cv; // used in combination with a std::mutex to facilitate inter-thread communication
    ConnectionHandler connectionHandler(host, port);
    ReadFromKeyBoard readFromKeyBoard(connectionHandler, m, cv);
    ProtocolImpl protocolImpl(connectionHandler , readFromKeyBoard, m, cv);
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    thread threadKeyBoard(&ReadFromKeyBoard::run, &readFromKeyBoard); // thread which read from keyboard
    thread threadSrvProtocol(&ProtocolImpl::run, &protocolImpl); // thread which read from server
    threadKeyBoard.join();
    threadSrvProtocol.join();
    }

