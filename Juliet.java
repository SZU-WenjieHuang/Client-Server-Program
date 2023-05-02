/*
 * Juliet.java
 *
 * Juliet class.  Implements the Juliet subsystem of the Romeo and Juliet ODE system.
 */



import javafx.util.Pair;

import java.lang.Thread;
import java.net.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class Juliet extends Thread {

    //ServerSocket is a class that represents a socket that listens for incoming connections from clients.
    private ServerSocket ownServerSocket = null; //Juliet's (server) socket

    //A ServerSocket can be configured to listen on a specific port
    private Socket serviceMailbox = null; //Juliet's (service) socket
    private double currentLove = 0;
    private double b = 0;
    static private int thePort = 0;
    static private String theIPAddress = null;
    private ServerSocket serverSocket =  null;

    //Class construtor
    public Juliet(double initialLove) {
        currentLove = initialLove;
        b = 0.01;
        thePort = 7779;
        theIPAddress = "127.0.0.1";
        try {
            int maxConnectionQueue = 3;
            serverSocket = new ServerSocket(thePort,maxConnectionQueue, InetAddress.getByName(theIPAddress));
            System.out.println("Juliet: Good pilgrim, you do wrong your hand too much, ...");
        } catch(Exception e) {
            System.out.println("Juliet: Failed to create own socket " + e);
        }
    }

    // Get acquaintance with lover;
    // Receives lover's socket information and share's own socket
    public static Pair<InetAddress,Integer> getAcquaintance() {
        System.out.println("Juliet: My bounty is as boundless as the sea,\n" +
                "       My love as deep; the more I give to thee,\n" +
                "       The more I have, for both are infinite.");

        try {
            InetAddress address = InetAddress.getByName(theIPAddress);
            Pair<InetAddress,Integer> pair = new Pair<>(address, thePort);
            return pair;
        } catch (UnknownHostException e) {
            // Handle the exception
            e.printStackTrace();
        }
        return null;
    }



    //Retrieves the lover's love
    public double receiveLoveLetter() {
        double tmp = 0;
        try {
            // its listen on a specific port
            serviceMailbox = serverSocket.accept(); // Wait for a connection with Romeo
            System.out.println("Juliet: Letter received");
            InputStream is = serviceMailbox.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buf = new char[1024];
            isr.read(buf, 0, 1024); // Read message into buffer
            String message = new String(buf); // Convert buffer to string

            // Remove last character if it is "J" or "R"
            for(int i = 0; i < 1024 ; i++){
                if(message.charAt(i) == 'R'){
                    message = message.substring(0, i);
                    break;
                }
            }
            tmp = Double.parseDouble(message.trim()); // Parse message into double

        } catch (IOException e) {
            System.out.println("Juliet: Failed to receive love letter " + e);
        }
        System.out.println("Juliet: Romeo, Romeo! Wherefore art thou Romeo? (<-" + tmp + ")");
        return tmp;
    }


    //Love (The ODE system)
    //Given the lover's love at time t, estimate the next love value for Romeo
    public double renovateLove(double partnerLove){
        System.out.println("Juliet: Come, gentle night, come, loving black-browed night,\n" +
                "       Give me my Romeo, and when I shall die,\n" +
                "       Take him and cut him out in little stars.");
        currentLove = currentLove+(-b*partnerLove);
        return currentLove;
    }


    //Communicate love back to playwriter
    public void declareLove() {
        try {
            // Create output stream to send message to client
            OutputStream outputStream = serviceMailbox.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);

            // Send current love value as message
            String message = currentLove + "J";
            outputWriter.write(message);
            outputWriter.flush(); //refresh

            System.out.println("Juliet: Good night, good night! Parting is such sweet sorrow,\n" +
                    "That I shall say good night till it be morrow." + "(->" + message + ")");

            // Close output stream
            outputWriter.close();
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Juliet: Failed to declare love: " + e);
        }
    }




    //Execution
    public void run () {
        try {
            while (!this.isInterrupted()) {
                //Retrieve lover's current love
                System.out.println("Juliet: Awaiting letter");
                double RomeoLove = this.receiveLoveLetter();

                //Estimate new love value
                this.renovateLove(RomeoLove);

                //Communicate back to lover, Romeo's love
                this.declareLove();
            }
        }catch (Exception e){
            System.out.println("Juliet: " + e);
        }
        if (this.isInterrupted()) {
            System.out.println("Juliet: I will kiss thy lips.\n" +
                    "Haply some poison yet doth hang on them\n" +
                    "To make me die with a restorative.");
        }

    }

}
