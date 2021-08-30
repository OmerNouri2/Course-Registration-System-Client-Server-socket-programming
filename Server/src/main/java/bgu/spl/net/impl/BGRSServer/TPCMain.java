package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.Database;
import bgu.spl.net.impl.MessageEncoderDecoderImpl;
import bgu.spl.net.impl.MessagingProtocolImpl;
import bgu.spl.net.impl.User;
import bgu.spl.net.srv.BaseServer;
import bgu.spl.net.srv.BlockingConnectionHandler;
import bgu.spl.net.srv.Server;

public class TPCMain {
    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        Server tpcServer = new BaseServer(port, () -> new MessagingProtocolImpl(), () -> new MessageEncoderDecoderImpl()) {
            @Override
            protected void execute(BlockingConnectionHandler handler) {
                new Thread(handler).start();
            }
        };
        tpcServer.serve();
    }
}
