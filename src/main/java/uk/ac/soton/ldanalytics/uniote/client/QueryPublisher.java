package uk.ac.soton.ldanalytics.uniote.client;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.jena.query.Query;
import org.zeromq.ZMQ;

import uk.ac.soton.ldanalytics.sparql2stream.parser.StreamQueryFactory;

public class QueryPublisher {
	public static void main(String[] a) {
		//  Prepare our context and publisher
        ZMQ.Context context = ZMQ.context(1);
        
        ZMQ.Socket publisher = context.socket(ZMQ.PUB);
        publisher.connect("tcp://localhost:5500");
        
        String clientAddress = "";
        try {
			clientAddress = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        int queryId = 1;
        String queryStr = "PREFIX om-owl: <http://knoesis.wright.edu/ssw/ont/sensor-observation.owl#>\n" + 
        		"PREFIX weather: <http://knoesis.wright.edu/ssw/ont/weather.owl#>\n" + 
        		"PREFIX wgs84_pos: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" + 
        		"PREFIX owl-time: <http://www.w3.org/2006/time#>\n" + 
        		"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
        		"\n" + 
        		"SELECT DISTINCT ?sensor ?value ?lat ?lon\n" + 
        		"FROM NAMED STREAM <http://www.cwi.nl/SRBench/observations> [RANGE 1h STEP]\n" +
        		"FROM NAMED <http://www.cwi.nl/SRBench/sdsd>\n" +
        		"WHERE {\n" + 
        		"  	?observation om-owl:procedure ?sensor ;\n" + 
        		"               a weather:RainfallObservation ;\n" + 
        		"               om-owl:result ?result.\n"+
        		"	?sensor om-owl:processLocation ?sensorLocation.	\n" +
        		"	?sensorLocation wgs84_pos:lat ?lat;	\n" +
        		"		wgs84_pos:long ?lon.	\n" +
        		"  	?result om-owl:floatValue ?value .\n" + 
        		"}";
        
        Query query = StreamQueryFactory.create(queryStr);
        
        for(int i=0;i<query.getNamedGraphURIs().size();i++) {
        	String[] uriStr = query.getNamedGraphURIs().get(i).split(";");
        	String uri = uriStr[0];
        	if(uriStr.length>1) { //named stream
        		publisher.sendMore(uri);
        		publisher.sendMore(Integer.toString(queryId));
        		publisher.sendMore(clientAddress);
            	publisher.send(queryStr, 0);
        	} else { //named graph
        		
        	}
        }                                                                   

        publisher.close ();
        
        ZMQ.Socket receiver = context.socket(ZMQ.PULL);
        receiver.bind("tcp://"+clientAddress+":5700");
        
        while (!Thread.currentThread ().isInterrupted ()) {
        	String qid = receiver.recvStr();
            String result = receiver.recvStr();
            System.out.println(result);
        }
        receiver.close();
        
        context.term ();
	}
}
