package bgu.spl.net.impl;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl implements MessageEncoderDecoder<Operation> {

    private byte[] opcodeByte = new byte[2]; // the first 2 bytes arrive from client
    private byte[] paramBytes = new byte[1 << 10]; // 1KB byte array - rest of message (without the opcode)
    private int len = 0;
    private short opcode = -1;
    private String userName = null; // for operations 1/2/3
    private String passwd = null; // for operations 1/2/3

    // convert from byte array to short
    public short bytesToShort(byte[] byteArr)
    {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    // reset all relevant fields for next command which come from keyboard
    public void resetFields()
    {
        len = 0;
        opcode = -1;
        opcodeByte = new byte[2];
        userName = null;
        passwd = null;
    }

    // create the fitted operation for the specified opcode
    public Operation createOperation(byte nextByte) {
        if(opcode == 1 | opcode == 2 | opcode == 3) {
            return createUserNamePasswdOperation(nextByte);
        }
        if(opcode == 5 | opcode == 6 | opcode == 7 | opcode == 9 | opcode == 10) {
            return createCourseNumOperation(nextByte);
        }
        if(opcode == 8) {
            return createUserNameOperation(nextByte);
        }
        return null;
    }

    public Operation createUserNamePasswdOperation(byte nextByte) {
        // if userName was not yet initialized - first initialize userName & than password
        if (userName == null && nextByte == '\0') { // userName parameters are a sequence of bytes in UTF-8 terminated by a zero byte
            userName = popString(); // popString creates String from all the bytes received --> userName
            return null; // exit from function because we took care this byte
        }
        // initialize password (now userName should be already initialized) next '\0' will finish the request from client
        if (nextByte == '\0') { // password parameters are a sequence of bytes in UTF-8 terminated by a zero byte
            passwd = popString(); // popString creates String from all the bytes received --> password
            UserPasswdOperation userPasswdOperation = new UserPasswdOperation(opcode, userName, passwd); // after both userName & password were created
            resetFields(); // reset all fields for the next message
            return userPasswdOperation;
        }
        pushByte(nextByte); // the first parameter was not fully received so we still receive bytes
        return null;
    }

    public Operation createCourseNumOperation(byte nextByte) {
        pushByte(nextByte); // the first parameter was not fully received so we still receive bytes
        if (len == 2) { //after opcode was received the message should include 2 bytes which will be the courseNum
            short courseNum = bytesToShort(paramBytes);
            CourseNumOperation courseNumOperation = new CourseNumOperation(opcode, courseNum);
            resetFields(); // reset all fields for the next message
            return courseNumOperation;
        }
        return null;
    }

    public Operation createUserNameOperation(byte nextByte) {
        if (nextByte == '\0') { // userName parameters are a sequence of bytes in UTF-8 terminated by a zero byte
            userName = popString(); // popString creates String from all the bytes received --> userName
            UsernameOperation usernameOperation = new UsernameOperation(opcode, userName);
            resetFields(); // reset all fields for the next message
            return usernameOperation;
        }
        pushByte(nextByte); // the first parameter was not fully received so we still receive bytes
        return null;
    }

    @Override
    public Operation decodeNextByte(byte nextByte) {
        if(opcode == -1) {
            opcodeByte[len] = nextByte;
            len++;
            if (len == 2) {
                opcode = bytesToShort(opcodeByte); // new opcode
                len = 0; // reset len for the rest of the message
                if(opcode == 4 | opcode == 11) {
                    Operation operation = new Operation(opcode); // create operation with opcode 4/11
                    resetFields(); // reset all fields for the next message
                    return operation;
                }
                return null;
            }
        }
        return createOperation(nextByte);  // if null - message is not finished yet
    }

    @Override
    // check which type og message arrived from server & send to the right encode function
    public byte[] encode(Operation message) {
        if(message instanceof AckOperation) {
            AckOperation ackOperation = (AckOperation) message;
            return encode(ackOperation);
        }
        else {
            ErrorOperation errorOperation = (ErrorOperation) message;
            return encode(errorOperation);
        }
    }

    // @Override
    public byte[] encode(ErrorOperation errorOperation) { // function to handle the msg - error
        byte[] errorToReturn = new byte[4];
        byte[] errorOpcodeBytes = shortToBytes(errorOperation.getOpcode());
        byte[] errorMessageBytes = shortToBytes(errorOperation.getMessageOpcode());
        // concatenation of both arrays
        for (int i=0; i< errorOpcodeBytes.length; i++){
            errorToReturn[i] = errorOpcodeBytes[i];
        }
        for (int i=0; i< errorOpcodeBytes.length; i++){
            errorToReturn[i+2] = errorMessageBytes[i];
        }
        return errorToReturn;
    }

    public byte[] encode(AckOperation ackOperation) { // function to handle the msg - error
        byte[] ackOpcodeBytes = shortToBytes(ackOperation.getOpcode());
        byte[] ackMessageBytes = shortToBytes(ackOperation.getMessageOpcode());
        byte[] ackOptional;
        if(ackOperation.getOptional().equals("")){
            ackOptional = new byte[0];
        }
        else{
            ackOptional = (ackOperation.getOptional()+ "").getBytes();
        }
        // concatenation the 3 arrays into 1 byte[] array
        byte[] ackToReturn = new byte[ackOpcodeBytes.length + ackMessageBytes.length + ackOptional.length];
        for (int i=0; i< ackOpcodeBytes.length; i++){
            ackToReturn[i] = ackOpcodeBytes[i];
        }
        for (int i=0; i< ackMessageBytes.length; i++){
            ackToReturn[i+2] = ackMessageBytes[i];
        }
        for (int i=0; i< ackOptional.length; i++){
            ackToReturn[i+4] = ackOptional[i];
        }
        return ackToReturn;
    }

    // insert new byte to the array
    private void pushByte(byte nextByte) {
        if (len >= paramBytes.length) // in case array arrives to its limit
            paramBytes = Arrays.copyOf(paramBytes, len*2); // increase the array
        paramBytes[len++] = nextByte;
    }

    // create string of the message which received in bytes
    private String popString() {
        String result = new String(paramBytes, 0, len, StandardCharsets.UTF_8);
        len = 0; // reset the len for next decoding
        paramBytes = new byte[1 << 10]; // reset the paramBytes for next decoding
        return result;
    }

    // convert from short to byteArray
    public byte[] shortToBytes(short num)
    {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

}
