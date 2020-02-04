package basicTestsSimpleInterfaceVersion;

import net.sharksystem.asap.ASAPEngineFS;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.*;
import net.sharksystem.asap.util.ASAPEngineThread;
import net.sharksystem.cmdline.TCPChannel;
import nodesV2.GKENode;
import nodesV2.GKENode_Impl;
import nodesV2.GKE_Listener;
import nodesV2.GKEMessage;
import nodesV2.GKEMessage_Impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

public class GKETestsV2 {
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    public static final String CLAIRE = "Claire";
    public static final String TESTS_ROOT_FOLDER = "tests2/";
    public static final String ALICE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Alice";
    public static final String BOB_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Bob";
    public static final String CLAIRE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Claire";

    private static final CharSequence APP_FORMAT = "TEST_FORMAT";



    private static final int PORT7783 = 7783;
    private static final int PORT7784 = 7784;
    private static final int PORT7785 = 7785;
    private static final BigInteger PUB1 = new BigInteger("11111");



    @Test
    public void usageTest2() throws IOException, ASAPException, InterruptedException {
        final String TESTMESSAGEString1 = "Hallo  Bob Alice here WHatever Bye Bye ";
        final String TESTMESSAGEString2 = "Hallo  Claire Bob here Bye ";
        final String TESTMESSAGEString3 = "Hallo   Alice Claire here Bye Bye ";
        
        GKEMessage TESTMESSAGE1 = new GKEMessage_Impl("Alice", TESTMESSAGEString1, new Date());
        GKEMessage TESTMESSAGE2 = new GKEMessage_Impl("Bob", TESTMESSAGEString2, new Date());
        GKEMessage TESTMESSAGE3 = new GKEMessage_Impl("Claire", TESTMESSAGEString3, new Date());
        
        
        ASAPEngineFS.removeFolder(TESTS_ROOT_FOLDER);
        Collection<CharSequence> formats = new HashSet<>();
        formats.add(APP_FORMAT);

        Collection<CharSequence> recipients = new HashSet<>();
        recipients.add(BOB);
        
        GKENode asapJavaApplicationAlice =new GKENode(PUB1, ALICE, ALICE_ROOT_FOLDER, formats);



        asapJavaApplicationAlice.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE1.getSerializedMessage().toString().getBytes());
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, new GKE_Listener());

        // create bob engine
        GKENode asapJavaApplicationBob =
                new GKENode(PUB1, BOB, BOB_ROOT_FOLDER, formats);

        GKENode asapJavaApplicationClaire =
                new GKENode(PUB1, CLAIRE, CLAIRE_ROOT_FOLDER, formats);
        GKE_Listener listenerAlice = new GKE_Listener();
        GKE_Listener listenerBob = new GKE_Listener();
        GKE_Listener listenerClaire = new GKE_Listener();
        
        
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, listenerAlice);
        asapJavaApplicationBob.setASAPMessageReceivedListener(APP_FORMAT, listenerBob);
        asapJavaApplicationClaire.setASAPMessageReceivedListener(APP_FORMAT, listenerClaire);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                        create a tcp connection                                //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        // create connections for both sides
        TCPChannel aliceChannel = new TCPChannel(PORT7783, true, "a2b");
        TCPChannel bobChannel = new TCPChannel(PORT7783, false, "b2a");

        aliceChannel.start(); bobChannel.start();

        aliceChannel.waitForConnection(); bobChannel.waitForConnection();

        ASAPHandleConnectionThread aliceEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationAlice,
                aliceChannel.getInputStream(), aliceChannel.getOutputStream());

        aliceEngineThread.start();

        // let's start communication
        asapJavaApplicationBob.handleConnection(bobChannel.getInputStream(), bobChannel.getOutputStream());

        // wait until communication probably ends
        Thread.sleep(2000); System.out.flush(); System.err.flush();
        // close connections: note ASAPEngine does NOT close any connection!!
        aliceChannel.close(); bobChannel.close(); Thread.sleep(1000);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                            test results                                       //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        // received?
        Assert.assertTrue(listenerBob.hasReceivedMessage());
        ASAPMessages bobMessages = listenerBob.popASAPMessages();
        assert bobMessages.size() == 1;
        
        Iterator<byte[]> iter = bobMessages.getMessages();
        boolean iteratorCalled = false;
        while (iter.hasNext()) {
        	byte[] rawmsgBytes = iter.next();
        	String rawmsgString = new String(rawmsgBytes);
        	CharSequence charSeq = rawmsgString;
        	GKEMessage msg = new GKEMessage_Impl(charSeq);
        	System.out.println("msg=" + rawmsgString);;
        	assert msg.getContentAsString().equals(TESTMESSAGEString1);
        	iteratorCalled = true;
        }
        assert iteratorCalled;
        
        //Bob to claire 
        TCPChannel bob2claire = new TCPChannel(PORT7784, true, "b2c");
        TCPChannel claire2bob = new TCPChannel(PORT7784, false, "c2b");
        recipients.clear();
        recipients.add(CLAIRE);
        asapJavaApplicationBob.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE2.getContent());
        bob2claire.start(); claire2bob.start();
        bob2claire.waitForConnection(); claire2bob.waitForConnection();
        ASAPHandleConnectionThread bobEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationBob,
                bob2claire.getInputStream(), bob2claire.getOutputStream());

        bobEngineThread.start();
        asapJavaApplicationClaire.handleConnection(claire2bob.getInputStream(), claire2bob.getOutputStream());
        Thread.sleep(2000); System.out.flush(); System.err.flush();
        bob2claire.close(); claire2bob.close(); Thread.sleep(1000);
        Assert.assertTrue(listenerClaire.hasReceivedMessage());
        
        //Claire to Alice
        TCPChannel claire2alice = new TCPChannel(PORT7785, true, "c2a");
        TCPChannel alice2claire = new TCPChannel(PORT7785, false, "a2c");
        recipients.clear();
        recipients.add(ALICE);
        asapJavaApplicationClaire.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE3.getContent());
        claire2alice.start(); alice2claire.start();
        claire2alice.waitForConnection(); alice2claire.waitForConnection();
        ASAPHandleConnectionThread claireEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationClaire,
                claire2alice.getInputStream(), claire2alice.getOutputStream());

        claireEngineThread.start();
        asapJavaApplicationAlice.handleConnection(alice2claire.getInputStream(), alice2claire.getOutputStream());
        Thread.sleep(2000); System.out.flush(); System.err.flush();
        claire2alice.close(); alice2claire.close(); Thread.sleep(1000);
        Assert.assertTrue(listenerAlice.hasReceivedMessage());
        
   
    }
}
