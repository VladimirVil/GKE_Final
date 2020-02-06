package basicTestsSimpleInterfaceVersion;

import net.sharksystem.asap.ASAPEngineFS;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.apps.*;
import net.sharksystem.asap.util.ASAPEngineThread;
import net.sharksystem.cmdline.TCPChannel;
import nodesV2.GKENode;
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
import java.util.Random;
import java.util.StringJoiner;

public class GKETestsV3 {
    public static final String ALICE = "Alice";
    public static final String BOB = "Bob";
    public static final String CLAIRE = "Claire";
    public static final String ERIC = "Eric";
    public static final int MIN_RANGE = 3;
    public static final int MAX_RANGE = 33;

    

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
    private static final int PORT7787 = 7787;
    private static final int PORT7788 = 7788;
    private static final int PORT7789 = 7789;
    private static final int PORT7790 = 7790;


    @Test
    public void usageTest2() throws IOException, ASAPException, InterruptedException {
        Random rand = new Random();   

        BigInteger aliceSecret = BigInteger.valueOf(rand.nextInt((MAX_RANGE - MIN_RANGE) + 1) + MIN_RANGE);
        BigInteger bobSecret = BigInteger.valueOf(rand.nextInt((MAX_RANGE - MIN_RANGE) + 1) + MIN_RANGE);
        BigInteger claireSecret = BigInteger.valueOf(rand.nextInt((MAX_RANGE - MIN_RANGE) + 1) + MIN_RANGE);
        BigInteger ericSecret = BigInteger.valueOf(rand.nextInt((MAX_RANGE - MIN_RANGE) + 1) + MIN_RANGE);
        
        System.out.println("Personal secrets of ALice, Bob, Claire, Eric : " + aliceSecret +  "," + bobSecret + "," + claireSecret + "," + ericSecret);

        //creating the first upflow message which is {A', 1} whereby A' is (generator^aliceSecret)mod(prime) and 1 
        //is a custom help element for further calculations 
        List tokens = SecurityUtil.getTokenList(Collections.EMPTY_LIST,aliceSecret);
        String aliceMessage=String.join(",", tokens); //
		GKEMessage TESTMESSAGE1 = new GKEMessage_Impl("Alice", aliceMessage, new Date());
        
        ASAPEngineFS.removeFolder(TESTS_ROOT_FOLDER);
        Collection<CharSequence> formats = new HashSet<>();
        formats.add(APP_FORMAT);

        Collection<CharSequence> recipients = new HashSet<>();
        recipients.add(BOB);
        
        GKENode asapJavaApplicationAlice =new GKENode(ALICE, ALICE_ROOT_FOLDER, formats);

        asapJavaApplicationAlice.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE1.getSerializedMessage().toString().getBytes());
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, new GKE_Listener());

        // create bob engine
        GKENode asapJavaApplicationBob =
                new GKENode(BOB, BOB_ROOT_FOLDER, formats);

        GKENode asapJavaApplicationClaire =
                new GKENode(CLAIRE, CLAIRE_ROOT_FOLDER, formats);
        
        GKENode asapJavaApplicationEric =
                new GKENode(ERIC, ERIC_ROOT_FOLDER, formats);
        GKE_Listener listenerAlice = new GKE_Listener();
        GKE_Listener listenerBob = new GKE_Listener();
        GKE_Listener listenerClaire = new GKE_Listener();
        GKE_Listener listenerEric = new GKE_Listener();

        
        
        asapJavaApplicationAlice.setASAPMessageReceivedListener(APP_FORMAT, listenerAlice);
        asapJavaApplicationBob.setASAPMessageReceivedListener(APP_FORMAT, listenerBob);
        asapJavaApplicationClaire.setASAPMessageReceivedListener(APP_FORMAT, listenerClaire);
        asapJavaApplicationEric.setASAPMessageReceivedListener(APP_FORMAT, listenerEric);

        // create connections for both sides
        TCPChannel alice2bob = new TCPChannel(PORT7783, true, "a2b");
        TCPChannel bob2alice = new TCPChannel(PORT7783, false, "b2a");
        alice2bob.start(); bob2alice.start();
        alice2bob.waitForConnection(); bob2alice.waitForConnection();
        ASAPHandleConnectionThread aliceEngineThread = new ASAPHandleConnectionThread(asapJavaApplicationAlice,
                alice2bob.getInputStream(), alice2bob.getOutputStream());
        aliceEngineThread.start();
        // let's start communication
        asapJavaApplicationBob.handleConnection(bob2alice.getInputStream(), bob2alice.getOutputStream());
        // wait until communication probably ends
        Thread.sleep(1600); System.out.flush(); System.err.flush();
        // close connections: note ASAPEngine does NOT close any connection!!
        alice2bob.close(); bob2alice.close(); Thread.sleep(800);
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
        	//System.out.println("<>msg=" + msg.getContentAsString());
        	tokensFromAlice=Arrays.asList(msg.getContentAsString().toString().split(","));
        	iteratorCalled = true;
        }
        assert iteratorCalled;
        List tokensFromBob = SecurityUtil.getTokenList(tokensFromAlice, bobSecret);
        
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
        Thread.sleep(1800); System.out.flush(); System.err.flush();
        bob2claire.close(); claire2bob.close(); Thread.sleep(800);
        
        Assert.assertTrue(listenerClaire.hasReceivedMessage());
        ASAPMessages claireMEssages = listenerClaire.popASAPMessages();
        Iterator<CharSequence> iterator = claireMEssages.getMessagesAsCharSequence();

        while(iterator.hasNext()) {
        	CharSequence msg = iterator.next();
        	GKEMessage gkemsg = new GKEMessage_Impl(msg);
        	//System.out.println("<<>>msg=" + gkemsg.getContentAsString());
        	tokensFromBob=Arrays.asList(gkemsg.getContentAsString().toString().split(","));
        }
        List tokensFromClaire = SecurityUtil.getTokenList(tokensFromBob, claireSecret);

        //Claire to Eric
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
        Thread.sleep(1600); System.out.flush(); System.err.flush();
        claire2eric.close(); eric2claire.close(); Thread.sleep(700);
        Assert.assertTrue(listenerEric.hasReceivedMessage());
        
        ASAPMessages ericMessages = listenerEric.popASAPMessages();
        Iterator<CharSequence> iteratorEric = ericMessages.getMessagesAsCharSequence();

        while(iteratorEric.hasNext()) {
        	CharSequence msg = iteratorEric.next();
        	GKEMessage gkemsg = new GKEMessage_Impl(msg);
        	tokensFromClaire=Arrays.asList(gkemsg.getContentAsString().toString().split(","));
        }
        List<String> tokensFromEric = SecurityUtil.getTokenList(tokensFromClaire, ericSecret);
        String secretEricFinal = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(0)));
        System.out.println("Shared secret that eric has is " + secretEricFinal);


        List<String> tokensFromEricOriginalCopy = tokensFromEric;
    //    String secretE = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(0)));
//        String secretA = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(1)), aliceSecret);
//        String secretB = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(2)), bobSecret);
//        String secretC = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(3)), claireSecret);
//        
//        System.out.println(String.format("**%s**%s**%s**%s**",secretA,secretB,secretC,secretEricFinal));
        //Downflow, one by one
        //Eric to Alice
        TCPChannel eric2alice = new TCPChannel(PORT7787, true, "ee2aa");
        TCPChannel alice2eric = new TCPChannel(PORT7787, false, "aa2ee");
        recipients.clear();
        recipients.add(ALICE);

        GKEMessage TESTMESSAGE4 = new GKEMessage_Impl("Eric", String.join(",", tokensFromEric.get(1)), new Date());

        asapJavaApplicationEric.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE4.getSerializedMessage().toString().getBytes());

        eric2alice.start(); alice2eric.start();
        eric2alice.waitForConnection(); alice2eric.waitForConnection();
        ASAPHandleConnectionThread ericEngineThreadDownflowAlice = new ASAPHandleConnectionThread(asapJavaApplicationEric,
                eric2alice.getInputStream(), eric2alice.getOutputStream());

        ericEngineThreadDownflowAlice.start();
        asapJavaApplicationAlice.handleConnection(alice2eric.getInputStream(), alice2eric.getOutputStream());
        Thread.sleep(1600); System.out.flush(); System.err.flush();
        eric2alice.close(); alice2eric.close(); Thread.sleep(700);
        Assert.assertTrue(listenerAlice.hasReceivedMessage());
        
        ASAPMessages aliceMessagesDownflow = listenerAlice.popASAPMessages();
        Iterator<CharSequence> iteratorAlice = aliceMessagesDownflow.getMessagesAsCharSequence();

        while(iteratorAlice.hasNext()) {
        	CharSequence msg = iteratorAlice.next();
        	GKEMessage gkemsg = new GKEMessage_Impl(msg);
        	tokensFromEric=Arrays.asList(gkemsg.getContentAsString().toString().split(","));
        }
        
        String secretAliceFinal = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(0)), aliceSecret);
        System.out.println("Shared secret that alice has is " + secretAliceFinal);
        
        //Eric to Bob
        TCPChannel eric2bob = new TCPChannel(PORT7788, true, "ee2bb");
        TCPChannel bob2eric = new TCPChannel(PORT7788, false, "bb2ee");
        recipients.clear();
        recipients.add(BOB);

        GKEMessage TESTMESSAGE5 = new GKEMessage_Impl("Eric", String.join(",", tokensFromEricOriginalCopy.get(2)), new Date());

        asapJavaApplicationEric.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE5.getSerializedMessage().toString().getBytes());

        eric2bob.start(); bob2eric.start();
        eric2bob.waitForConnection(); bob2eric.waitForConnection();
        ASAPHandleConnectionThread ericEngineThreadDownflowBob = new ASAPHandleConnectionThread(asapJavaApplicationEric,
                eric2bob.getInputStream(), eric2bob.getOutputStream());

        ericEngineThreadDownflowBob.start();
        asapJavaApplicationBob.handleConnection(bob2eric.getInputStream(), bob2eric.getOutputStream());
        Thread.sleep(1600); System.out.flush(); System.err.flush();
        eric2bob.close(); bob2eric.close(); Thread.sleep(700);
        Assert.assertTrue(listenerBob.hasReceivedMessage());
        
        ASAPMessages bobMessagesDownflow = listenerBob.popASAPMessages();
        Iterator<CharSequence> iteratorBobDownflow = bobMessagesDownflow.getMessagesAsCharSequence();

        while(iteratorBobDownflow.hasNext()) {
        	CharSequence msg = iteratorBobDownflow.next();
        	GKEMessage gkemsg = new GKEMessage_Impl(msg);
        	tokensFromEric=Arrays.asList(gkemsg.getContentAsString().toString().split(","));
        }
        
        String secretBobFinal = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(0)), bobSecret);
        System.out.println("Shared secret that bob has is " + secretBobFinal);

        
        //Eric to Claire
        TCPChannel eric2claireDownflow = new TCPChannel(PORT7789, true, "ee2cc");
        TCPChannel claire2ericDownflow = new TCPChannel(PORT7789, false, "cc2ee");
        recipients.clear();
        recipients.add(CLAIRE);

        GKEMessage TESTMESSAGE6 = new GKEMessage_Impl("Eric", String.join(",", tokensFromEricOriginalCopy.get(3)), new Date());

        asapJavaApplicationEric.sendASAPMessage(APP_FORMAT, "yourSchema://yourURI", recipients, TESTMESSAGE6.getSerializedMessage().toString().getBytes());

        eric2claireDownflow.start(); claire2ericDownflow.start();
        eric2claireDownflow.waitForConnection(); claire2ericDownflow.waitForConnection();
        ASAPHandleConnectionThread ericEngineThreadDownflowClaire = new ASAPHandleConnectionThread(asapJavaApplicationEric,
                eric2claireDownflow.getInputStream(), eric2claireDownflow.getOutputStream());

        ericEngineThreadDownflowClaire.start();
        asapJavaApplicationClaire.handleConnection(claire2ericDownflow.getInputStream(), claire2ericDownflow.getOutputStream());
        Thread.sleep(1600); System.out.flush(); System.err.flush();
        eric2claireDownflow.close(); claire2ericDownflow.close(); Thread.sleep(700);
        Assert.assertTrue(listenerClaire.hasReceivedMessage());
        
        ASAPMessages claireMessagesDownflow = listenerClaire.popASAPMessages();
        Iterator<CharSequence> iteratorClaireDownflow = claireMessagesDownflow.getMessagesAsCharSequence();

        while(iteratorClaireDownflow.hasNext()) {
        	CharSequence msg = iteratorClaireDownflow.next();
        	GKEMessage gkemsg = new GKEMessage_Impl(msg);
        	tokensFromEric=Arrays.asList(gkemsg.getContentAsString().toString().split(","));

        }
        String secretClaireFinal = SecurityUtil.getSharedSecret(new BigInteger(tokensFromEric.get(0)), claireSecret);
        System.out.println("Shared secret that claire has is " + secretClaireFinal);
        System.out.println("Testing shared secret is equal for all participants");
        Thread.sleep(250);
        Assert.assertTrue(secretAliceFinal.equals(secretBobFinal));
        Assert.assertTrue(secretBobFinal.equals(secretClaireFinal));
        Assert.assertTrue(secretClaireFinal.equals(secretEricFinal));
        Assert.assertTrue(secretEricFinal.equals(secretAliceFinal));
        System.out.println("Done");
        System.out.println("Testing shared secret is not equal to personal secret of any of the participants");
        Assert.assertFalse(secretAliceFinal.equals(aliceSecret.toString()));
        Assert.assertFalse(secretBobFinal.equals(bobSecret.toString()));
        Assert.assertFalse(secretClaireFinal.equals(claireSecret.toString()));
        Assert.assertFalse(secretEricFinal.equals(ericSecret.toString()));
        System.out.println("Shared secrets of ALice, Bob, Claire, Eric : " + secretAliceFinal +  "," + secretBobFinal + "," + secretClaireFinal + "," + secretEricFinal);
        System.out.println("End");

    }
}
