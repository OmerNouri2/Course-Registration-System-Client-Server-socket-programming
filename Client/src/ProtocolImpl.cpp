//
// Created by spl211 on 28/12/2020.
//

#include <ProtocolImpl.h>
using namespace std;

ProtocolImpl::ProtocolImpl(ConnectionHandler &handler, ReadFromKeyBoard &readFromKeyBoard, mutex &m, condition_variable &cv):
connectionHandler(&handler), readFromKeyBoard(readFromKeyBoard), running(true), m(m), cv(cv) {
}

void ProtocolImpl::run() {
    while (running) {
        char resOp[2];
        if (!connectionHandler->getBytes(resOp, sizeof(resOp))) {
            std::cout << "Disconnected. Exiting...\n" << std::endl;
            break;
        }
        short opcode = bytesToShort(resOp); // opcode indicate if it ack msg(12) or error msg(13)
        if (opcode == 12){ // ack message was received
            processAck();
        }
        if (opcode == 13){ // error message was received
            processError();
        }
        memset(resOp, 0, sizeof resOp);
    }
}

// convert from char array to short
short ProtocolImpl::bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
};

// handle message opcode which has optional to return
void ProtocolImpl::optional(short messageOp){
    if(messageOp == 4){ // message opcode was - LOGOUT
        logout();
    }
    // if there is an optional to print  - for opcodes 6/7/8/9/11
    if((messageOp == 6) | (messageOp == 7) | (messageOp == 8) | (messageOp == 9) | (messageOp == 11)){
        printOptional();
    }
}

void ProtocolImpl::printOptional(){ // print the list of the KDAM courses
    string optional;
    if (!connectionHandler->getLine(optional)) {
        std::cout << "Disconnected. Exiting...\n" << std::endl;
        return;
    }
    cout << optional << endl;
}

void ProtocolImpl::logout(){
    readFromKeyBoard.terminate(); // means that the 'ReadFromKeyBoard' should terminate
    unique_lock<std::mutex> locking(m); // same lock as in ReadFromKeyBoard
    cv.notify_one();  // notify to ReadFromKeaBoard that it can not keep listening to the user
    running = false; // means that the 'ProtocolImpl' should terminate
}

void ProtocolImpl::processAck(){
    char msgOp[2];
    if (!connectionHandler->getBytes(msgOp, sizeof(msgOp))) {
        std::cout << "Disconnected. Exiting...\n" << std::endl;
        return;
    }
    short messageOpcode = bytesToShort(msgOp); // opcode indicate which message the ack for
    cout << "ACK " << messageOpcode << endl;
    optional(messageOpcode); // is msg has optional - handle it
}

void ProtocolImpl::processError() {
    char msgOp[2];
    if (!connectionHandler->getBytes(msgOp, sizeof(msgOp))) {
        std::cout << "Disconnected. Exiting...\n" << std::endl;
        return;
    }
    short messageOpcode = bytesToShort(msgOp); // opcode indicate which message the error for
    if(messageOpcode == 4){ // the messageOpcode is "LOGOUT"
        unique_lock<std::mutex> locking(m); // same lock as in ReadFromKeyBoard
        cv.notify_one(); // notify to ReadFromKeaBoard that it can keep listening to the user
    }
    cout << "ERROR " << messageOpcode << endl;
}
