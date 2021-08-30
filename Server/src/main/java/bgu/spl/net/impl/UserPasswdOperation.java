package bgu.spl.net.impl;

public class UserPasswdOperation extends Operation{

    private String username;
    private String passwd;

    public UserPasswdOperation(short opcode, String username, String passwd) {
        super(opcode);
        this.username = username;
        this.passwd = passwd;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswd() {
        return passwd;
    }
}
