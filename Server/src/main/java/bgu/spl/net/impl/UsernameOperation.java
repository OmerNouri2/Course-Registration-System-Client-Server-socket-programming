package bgu.spl.net.impl;

public class UsernameOperation extends Operation{

    private String username;

    public UsernameOperation(short opcode, String username) {
        super(opcode);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
