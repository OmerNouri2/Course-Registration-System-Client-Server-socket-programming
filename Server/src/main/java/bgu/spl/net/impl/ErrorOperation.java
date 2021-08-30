package bgu.spl.net.impl;

public class ErrorOperation extends Operation {

    private short messageOpcode;

    public ErrorOperation(short opcode, short messageOpcode) {
        super(opcode);
        this.messageOpcode = messageOpcode;
    }

    public short getMessageOpcode() {
        return messageOpcode;
    }
}
