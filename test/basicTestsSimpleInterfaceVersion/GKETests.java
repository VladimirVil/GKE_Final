package basicTestsSimpleInterfaceVersion;

import net.sharksystem.asap.ASAPEngineFS;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.*;
import net.sharksystem.asap.util.ASAPEngineThread;
import net.sharksystem.cmdline.TCPChannel;
import nodesV2.GKEMessage;
import nodesV2.GKEMessage_Impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

public class GKETests {
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    public static final String CLAIRE = "Claire";

    private static final CharSequence APP_FORMAT = "TEST_FORMAT";

    @Test
    public void usageTest() throws IOException, ASAPException, InterruptedException {
        final byte[] TESTMESSAGE1 = "Hallo  Bob Alice here WHatever Bye Bye ".getBytes();
        final byte[] TESTMESSAGE2 = "Hallo  Claire Bob here Bye ".getBytes();
        final byte[] TESTMESSAGE3 = "Hallo   Alice Claire here Bye Bye ".getBytes();
        
        final String TESTS_ROOT_FOLDER = "tests/";
        final String ALICE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Alice";
        final String BOB_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Bob";
        final String CLAIRE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Claire";
        
        final int PORT7777 = 7777;
        final int PORT7778 = 7778;
        final int PORT7779 = 7779;
    	
    	ASAPEngineFS.removeFolder(TESTS_ROOT_FOLDER);
        Collection<CharSequence> formats = new HashSet<>();
        formats.add(APP_FORMAT);

        ASAPJavaApplication asapJavaApplicationAlice =
                ASAPJavaApplicationFS.createASAPJavaApplication(ALICE, ALICE_ROOT_FOLDER, formats);

        Collection<CharSequence> recipients = new HashSet<>();
        recipients.add(BOB);

        asapJavaApplicationAlice.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE1);
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, new ListenerExample());

        // create bob engine
        ASAPJavaApplication asapJavaApplicationBob =
                ASAPJavaApplicationFS.createASAPJavaApplication(BOB, BOB_ROOT_FOLDER, formats);

        ASAPJavaApplication asapJavaApplicationClaire =
                ASAPJavaApplicationFS.createASAPJavaApplication(CLAIRE, CLAIRE_ROOT_FOLDER, formats);
        ListenerExample listenerAlice = new ListenerExample();
        ListenerExample listenerBob = new ListenerExample();
        ListenerExample listenerClaire = new ListenerExample();
        
        
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, listenerAlice);
        asapJavaApplicationBob.setASAPMessageReceivedListener(APP_FORMAT, listenerBob);
        asapJavaApplicationClaire.setASAPMessageReceivedListener(APP_FORMAT, listenerClaire);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                        create a tcp connection                                //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        // create connections for both sides
        TCPChannel aliceChannel = new TCPChannel(PORT7777, true, "a2b");
        TCPChannel bobChannel = new TCPChannel(PORT7777, false, "b2a");

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
        
        //Bob to claire 
        TCPChannel bob2claire = new TCPChannel(PORT7778, true, "b2c");
        TCPChannel claire2bob = new TCPChannel(PORT7778, false, "c2b");
        recipients.clear();
        recipients.add(CLAIRE);
        asapJavaApplicationBob.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE2);
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
        TCPChannel claire2alice = new TCPChannel(PORT7779, true, "c2a");
        TCPChannel alice2claire = new TCPChannel(PORT7779, false, "a2c");
        recipients.clear();
        recipients.add(ALICE);
        asapJavaApplicationClaire.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE3);
        claire2alice.start(); alice2claire.start();
        claire2alice.waitForConnection(); alice2claire.waitForConnection();
        ASAPHandleConnectionThread claireEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationClaire,
                claire2alice.getInputStream(), claire2alice.getOutputStream());

        claireEngineThread.start();
        asapJavaApplicationAlice.handleConnection(alice2claire.getInputStream(), alice2claire.getOutputStream());
        Thread.sleep(2000); System.out.flush(); System.err.flush();
        claire2alice.close(); alice2claire.close(); Thread.sleep(1000);
        Assert.assertTrue(listenerAlice.hasReceivedMessage());
   
        aliceChannel.close(); bobChannel.close();
        bob2claire.close(); claire2bob.close(); 
        claire2alice.close(); alice2claire.close();
    }
    
    @Test
    public void testMessageImplFor2Users() throws IOException, ASAPException, InterruptedException {
    	final String testMessageString1 = "Hallo Bob, Alice here äöüß";
    	final String testMessageString2 = "Hallo CLaire, Bob here äöüß";
    	final String testMessageString3 = "Hallo Alice, Claire here äöüß";
    	
        final String TESTS_ROOT_FOLDER = "tests_message_impl_for_2_users/";
        final String ALICE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Alice";
        final String BOB_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Bob";
        final String CLAIRE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Claire";
  
        final int PORT7780 = 7780;
        final int PORT7781 = 7781;
        final int PORT7782 = 7782;
        
        
        final GKEMessage testMessage1 = new GKEMessage_Impl(testMessageString1);
        final GKEMessage testMessage2 = new GKEMessage_Impl(testMessageString2);
        final GKEMessage testMessage3 = new GKEMessage_Impl(testMessageString3);
        
        
        final byte[] TESTMESSAGE_PAYLOAD1 = testMessage1.getContent();
        final byte[] TESTMESSAGE_PAYLOAD2 = testMessage2.getContent();
        final byte[] TESTMESSAGE_PAYLOAD3 = testMessage3.getContent();
       
        
        ASAPEngineFS.removeFolder(TESTS_ROOT_FOLDER);
        Collection<CharSequence> formats = new HashSet<>();
        formats.add(APP_FORMAT);

        ASAPJavaApplication asapJavaApplicationAlice =
                ASAPJavaApplicationFS.createASAPJavaApplication(ALICE, ALICE_ROOT_FOLDER, formats);

        Collection<CharSequence> recipients = new HashSet<>();
        recipients.add(BOB);

        asapJavaApplicationAlice.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE_PAYLOAD1);
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, new ListenerExample());

        // create bob engine
        ASAPJavaApplication asapJavaApplicationBob =
                ASAPJavaApplicationFS.createASAPJavaApplication(BOB, BOB_ROOT_FOLDER, formats);

        ASAPJavaApplication asapJavaApplicationClaire =
                ASAPJavaApplicationFS.createASAPJavaApplication(CLAIRE, CLAIRE_ROOT_FOLDER, formats);
        ListenerExample listenerAlice = new ListenerExample();
        ListenerExample listenerBob = new ListenerExample();
        ListenerExample listenerClaire = new ListenerExample();
        
        
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, listenerAlice);
        asapJavaApplicationBob.setASAPMessageReceivedListener(APP_FORMAT, listenerBob);
        asapJavaApplicationClaire.setASAPMessageReceivedListener(APP_FORMAT, listenerClaire);

        ///////////////////////////////////////////////////////////////////////////////////////////////////
        //                                        create a tcp connection                                //
        ///////////////////////////////////////////////////////////////////////////////////////////////////

        // create connections for both sides
        TCPChannel aliceChannel = new TCPChannel(PORT7780, true, "a2b");
        TCPChannel bobChannel = new TCPChannel(PORT7780, false, "b2a");

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

        
        
        //Bob to claire 
        TCPChannel bob2claire = new TCPChannel(PORT7781, true, "b2c");
        TCPChannel claire2bob = new TCPChannel(PORT7781, false, "c2b");
        recipients.clear();
        recipients.add(CLAIRE);
        asapJavaApplicationBob.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE_PAYLOAD2);
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
        TCPChannel claire2alice = new TCPChannel(PORT7782, true, "c2a");
        TCPChannel alice2claire = new TCPChannel(PORT7782, false, "a2c");
        recipients.clear();
        recipients.add(ALICE);
        asapJavaApplicationClaire.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE_PAYLOAD3);
        claire2alice.start(); alice2claire.start();
        claire2alice.waitForConnection(); alice2claire.waitForConnection();
        ASAPHandleConnectionThread claireEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationClaire,
                claire2alice.getInputStream(), claire2alice.getOutputStream());

        claireEngineThread.start();
        asapJavaApplicationAlice.handleConnection(alice2claire.getInputStream(), alice2claire.getOutputStream());
        Thread.sleep(2000); System.out.flush(); System.err.flush();
        claire2alice.close(); alice2claire.close(); Thread.sleep(1000);
        Assert.assertTrue(listenerAlice.hasReceivedMessage());
   
        aliceChannel.close(); bobChannel.close();
        bob2claire.close(); claire2bob.close(); 
        claire2alice.close(); alice2claire.close();
    }

    private class ListenerExample implements ASAPMessageReceivedListener {

        private boolean hasReceivedMessage = false;

        @Override
        public void asapMessagesReceived(ASAPMessages messages) {
            try {
                System.out.println("#message == " + messages.size());
                this.hasReceivedMessage = true;
            } catch (IOException e) {
                // do something with it.
            }
        }

        public boolean hasReceivedMessage() {
            return this.hasReceivedMessage;
        }
    }
}
