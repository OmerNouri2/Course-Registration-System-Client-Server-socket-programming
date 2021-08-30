package bgu.spl.net.impl.BGRSServer;

import bgu.spl.net.impl.Database;
import bgu.spl.net.impl.MessageEncoderDecoderImpl;
import bgu.spl.net.impl.MessagingProtocolImpl;
import bgu.spl.net.srv.Reactor;
import bgu.spl.net.srv.Server;

public class ReactorMain {

    public static void main(String[] args) {
        int port = Integer.parseInt(args[0]);
        int numOfThread = Integer.parseInt(args[1]);

        Server reactor = new Reactor(numOfThread, port, () -> new MessagingProtocolImpl(), () -> new MessageEncoderDecoderImpl());
        reactor.serve();
    }
}
