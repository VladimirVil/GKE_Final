package basicTestsSimpleInterfaceVersion;

import net.sharksystem.asap.ASAPEngineFS;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.*;
import net.sharksystem.asap.util.ASAPEngineThread;
import net.sharksystem.cmdline.TCPChannel;
import nodesV2.GKENode;
import nodesV2.GKENode_Impl;
import nodesV2.GKE_Listener;
import nodesV2.SecurityUtil;
import nodesV2.GKEMessage;
import nodesV2.GKEMessage_Impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringJoiner;

public class GKETestsV3 {
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    public static final String CLAIRE = "Claire";
    public static final String ERIC = "Eric";

    public static final String TESTS_ROOT_FOLDER = "tests2/";
    public static final String ALICE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Alice";
    public static final String BOB_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Bob";
    public static final String CLAIRE_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Claire";
    public static final String ERIC_ROOT_FOLDER = TESTS_ROOT_FOLDER + "Eric";


    private static final CharSequence APP_FORMAT = "TEST_FORMAT";



    private static final int PORT7783 = 7783;
    private static final int PORT7784 = 7784;
    private static final int PORT7785 = 7785;
    private static final int PORT7786 = 7786;

    private static final BigInteger PUB1 = new BigInteger("11111");



    @Test
    public void usageTest2() throws IOException, ASAPException, InterruptedException {
        final String TESTMESSAGEString1 = "Hallo  Bob Alice here WHatever Bye Bye ";
        final String TESTMESSAGEString2 = "Hallo  Claire Bob here Bye ";
        final String TESTMESSAGEString3 = "Hallo   Alice Claire here Bye Bye ";
        int aRandom=7;
        List tokens = SecurityUtil.getTokenList(Collections.EMPTY_LIST,aRandom);
        String aliceMessage=String.join(",", tokens); //
		GKEMessage TESTMESSAGE1 = new GKEMessage_Impl("Alice", aliceMessage, new Date());
        
        
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
        
        GKENode asapJavaApplicationEric =
                new GKENode(PUB1, ERIC, ERIC_ROOT_FOLDER, formats);
        GKE_Listener listenerAlice = new GKE_Listener();
        GKE_Listener listenerBob = new GKE_Listener();
        GKE_Listener listenerClaire = new GKE_Listener();
        GKE_Listener listenerEric = new GKE_Listener();

        
        
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, listenerAlice);
        asapJavaApplicationBob.setASAPMessageReceivedListener(APP_FORMAT, listenerBob);
        asapJavaApplicationClaire.setASAPMessageReceivedListener(APP_FORMAT, listenerClaire);
        asapJavaApplicationEric.setASAPMessageReceivedListener(APP_FORMAT, listenerEric);


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
        List<String> tokensFromAlice = Collections.EMPTY_LIST;
        while (iter.hasNext()) {
        	byte[] rawmsgBytes = iter.next();
        	String rawmsgString = new String(rawmsgBytes);
        	CharSequence charSeq = rawmsgString;
        	GKEMessage msg = new GKEMessage_Impl(charSeq);
        	System.out.println("<>msg=" + msg.getContentAsString());
        	tokensFromAlice=Arrays.asList(msg.getContentAsString().toString().split(","));
        	iteratorCalled = true;
        }
        assert iteratorCalled;
        int bRandom=6;
        List tokensFromBob = SecurityUtil.getTokenList(tokensFromAlice, bRandom);
        
        //Bob to claire 
        TCPChannel bob2claire = new TCPChannel(PORT7784, true, "b2c");
        TCPChannel claire2bob = new TCPChannel(PORT7784, false, "c2b");
        recipients.clear();
        recipients.add(CLAIRE);
        GKEMessage TESTMESSAGE2 = new GKEMessage_Impl("Bob", String.join(",", tokensFromBob), new Date());

        asapJavaApplicationBob.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE2.getSerializedMessage().toString().getBytes());
        bob2claire.start(); claire2bob.start();
        bob2claire.waitForConnection(); claire2bob.waitForConnection();
        ASAPHandleConnectionThread bobEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationBob,
                bob2claire.getInputStream(), bob2claire.getOutputStream());

        bobEngineThread.start();
        asapJavaApplicationClaire.handleConnection(claire2bob.getInputStream(), claire2bob.getOutputStream());
        Thread.sleep(2000); System.out.flush(); System.err.flush();
        bob2claire.close(); claire2bob.close(); Thread.sleep(1000);
        
        Assert.assertTrue(listenerClaire.hasReceivedMessage());
        ASAPMessages claireMEssages = listenerClaire.popASAPMessages();
        Iterator<CharSequence> iterator = claireMEssages.getMessagesAsCharSequence();

        while(iterator.hasNext()) {
        	CharSequence msg = iterator.next();
        	System.out.println("<<>>received=" + msg);
        	GKEMessage gkemsg = new GKEMessage_Impl(msg);
        	System.out.println("<<>>msg=" + gkemsg.getContentAsString());
        	tokensFromBob=Arrays.asList(gkemsg.getContentAsString().toString().split(","));
        }
        
        int cRandom=5;
        List tokensFromClaire = SecurityUtil.getTokenList(tokensFromBob, cRandom);

        
        //Claire to Alice
        TCPChannel claire2eric = new TCPChannel(PORT7786, true, "c2e");
        TCPChannel eric2claire = new TCPChannel(PORT7786, false, "e2c");
        recipients.clear();
        recipients.add(ERIC);

        GKEMessage TESTMESSAGE3 = new GKEMessage_Impl("Claire", String.join(",", tokensFromClaire), new Date());

        asapJavaApplicationClaire.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE3.getSerializedMessage().toString().getBytes());

        claire2eric.start(); eric2claire.start();
        claire2eric.waitForConnection(); eric2claire.waitForConnection();
        ASAPHandleConnectionThread claireEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationClaire,
                claire2eric.getInputStream(), claire2eric.getOutputStream());

        claireEngineThread.start();
        asapJavaApplicationEric.handleConnection(eric2claire.getInputStream(), eric2claire.getOutputStream());
        Thread.sleep(2000); System.out.flush(); System.err.flush();
        claire2eric.close(); eric2claire.close(); Thread.sleep(1000);
        Assert.assertTrue(listenerEric.hasReceivedMessage());
        
        ASAPMessages ericMessages = listenerEric.popASAPMessages();
        Iterator<CharSequence> iteratorEric = ericMessages.getMessagesAsCharSequence();

        while(iteratorEric.hasNext()) {
        	CharSequence msg = iteratorEric.next();
        	System.out.println("<<>>received=" + msg);
        	GKEMessage gkemsg = new GKEMessage_Impl(msg);
        	System.out.println("<<>>msg=" + gkemsg.getContentAsString());
        	tokensFromClaire=Arrays.asList(gkemsg.getContentAsString().toString().split(","));
        }
        int eRandom = 4;
        List<String> tokensForEric = SecurityUtil.getTokenList(tokensFromClaire, eRandom);
        
        String secretE = SecurityUtil.getSharedSecret(new BigInteger(tokensForEric.get(0)));
        String secretA = SecurityUtil.getSharedSecret(new BigInteger(tokensForEric.get(1)), aRandom);
        String secretB = SecurityUtil.getSharedSecret(new BigInteger(tokensForEric.get(2)), bRandom);
        String secretC = SecurityUtil.getSharedSecret(new BigInteger(tokensForEric.get(3)), cRandom);
        
        System.out.println(String.format("**%s**%s**%s**%s**",secretA,secretB,secretC,secretE));
    }
}
