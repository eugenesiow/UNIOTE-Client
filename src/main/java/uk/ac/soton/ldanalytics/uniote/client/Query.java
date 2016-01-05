package uk.ac.soton.ldanalytics.uniote.client;

import org.zeromq.ZMQ;

public class Query {
	public static void main(String[] a) {
		//  Prepare our context and publisher
        ZMQ.Context context = ZMQ.context(1);

        ZMQ.Socket publisher = context.socket(ZMQ.PUB);
        publisher.connect("tcp://localhost:5500");

        while (!Thread.currentThread ().isInterrupted ()) {
            //  Send message to broker
        	publisher.sendMore("http://www.cwi.nl/SRBench/observations");
            publisher.send("blah blah", 0);
            publisher.sendMore("http://www.cwi.nl/SRBench/sensors");
            publisher.send("something else", 0);
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }

        publisher.close ();
        context.term ();
	}
}
