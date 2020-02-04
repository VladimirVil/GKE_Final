package nodesV2;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;

import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.ASAPMessageReceivedListener;
import net.sharksystem.asap.apps.ASAPMessages;
import net.sharksystem.util.localloop.BufferedStream;

public class GKENode_Impl {
	
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    //claire is not used right now
    public static final String CLAIRE = "Claire";

	
	public static void main (String[] args) throws ASAPException, IOException {

	final CharSequence APP = "GKE";

	GKENode alice;
	GKENode bob;
	GKENode claire;

	
	CharSequence uriAlice = "gke://alice";
	CharSequence uriBob = "gke://bob";
	CharSequence uriClaire = "gke://claire";

	
	Collection<CharSequence> formats = new HashSet<>();
	formats.add(APP);
	String filePath = new File("").getAbsolutePath();
	String folder1 = "root/" + ALICE;
	//File file1 = new File(filePath +"/" + folder1 + "/*.*");
	String folder2 = "root/" + BOB;
	String folder3 = "root/" + CLAIRE;

	Collection<CharSequence> recipentsBob = new HashSet<>();
	Collection<CharSequence> recipentsAlice = new HashSet<>();
	Collection<CharSequence> recipentsClaire = new HashSet<>();

	recipentsAlice.add(BOB);
	recipentsBob.add(ALICE);



	BigInteger pub1 = new BigInteger("11111");
	BigInteger pub2 = new BigInteger("222222");
	//BigInteger pub3 = new BigInteger("33333");

	ASAPMessageReceivedListener aliceListener = new ASAPMessageReceivedListener() {
		
		@Override
		public void asapMessagesReceived(ASAPMessages messages) {			
			ASAPMessages m = messages;
			try {
	            System.out.println(" received "
	                    + m.getMessages().toString());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	};
	ASAPMessageReceivedListener bobListener = new ASAPMessageReceivedListener() {
		
		@Override
		public void asapMessagesReceived(ASAPMessages messages) {
			ASAPMessages m = messages;
			try {
	            System.out.println(" received "
	                    + m.getMessages().toString());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
	};
	

	System.out.println("GKENode_Impl: Create GKENodes:");
	alice = new GKENode(pub1, ALICE, folder1, formats, recipentsAlice);
	bob = new GKENode(pub2, BOB, folder2, formats, recipentsBob);
	//claire = new GKENode(pub3, CLAIRE, folder3, formats, recipentsClaire);

    // create connections for both sides
	BufferedStream alice2bobChannel = new BufferedStream("alice2bob");
	BufferedStream bob2aliceChannel = new BufferedStream("bob2alice");

	alice.handleConnection(bob2aliceChannel.getInputStream(), alice2bobChannel.getOutputStream());
	bob.handleConnection(alice2bobChannel.getInputStream(), bob2aliceChannel.getOutputStream());
	
	


	
	System.out.println("GKENode_Impl: Set Received Listeners:");
	alice.setASAPMessageReceivedListener(APP, aliceListener);
	bob.setASAPMessageReceivedListener(APP, bobListener);
	//claire.setASAPMessageReceivedListener(APP, claireListener);

	

	System.out.println("GKENode_Impl: Send ASAP Message:");

	
	alice.sendASAPMessage(APP, uriBob, recipentsAlice, "Hello bob whatever bye".getBytes());
	
	bob.sendASAPMessage(APP, uriAlice, recipentsBob, "Hello alice whatever bye".getBytes());
	
	
	System.out.println("End");
	while (true) {}
	}
	

	
	
	
}
