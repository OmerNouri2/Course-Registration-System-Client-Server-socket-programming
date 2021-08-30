package bgu.spl.net.impl;

public class Operation {
    private short opcode;

    public Operation(short opcode){
        this.opcode = opcode;
    }
    public short getOpcode(){
        return opcode;
    }
}
