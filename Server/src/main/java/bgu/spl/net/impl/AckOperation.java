package bgu.spl.net.impl;

public class AckOperation extends Operation {

    private short messageOpcode;
    private Object optional;

    public AckOperation(short opcode, short messageOpcode, Object optional) {
        super(opcode);
        this.messageOpcode = messageOpcode;
        this.optional = optional;
    }

    public short getMessageOpcode() {
        return messageOpcode;
    }

    public Object getOptional() {
        return optional;
    }
}
