/*
 * Romeo.java
 *
 * Romeo class.  Implements the Romeo subsystem of the Romeo and Juliet ODE system.
 */


import java.lang.Thread;
import java.net.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

import javafx.util.Pair;

public class Romeo extends Thread {

    private ServerSocket ownServerSocket = null; //Romeo's (server) socket
    private Socket serviceMailbox = null; //Romeo's (service) socket


    private double currentLove = 0;
    private double a = 0; //The ODE constant

    static private int thePort = 0;
    static private String theIPAddress = null;
    private ServerSocket serverSocket =  null;

    //Class construtor
    public Romeo(double initialLove) {
        currentLove = initialLove;
        a = 0.02;
        thePort = 7778;
        theIPAddress = "127.0.0.1";
        try {
            int maxConnectionQueue = 3;
            serverSocket = new ServerSocket(thePort,maxConnectionQueue, InetAddress.getByName(theIPAddress));
            //System.out.println("Server at " + theIPAddress + " is listening on port : " + this.thePort);
            System.out.println("Romeo: What lady is that, which doth enrich the hand\n" +
                    "       Of yonder knight?");
        } catch(Exception e) {
            System.out.println("Romeo: Failed to create own socket " + e);
        }
   }

    //Get acquaintance with lover;
    public static Pair<InetAddress,Integer> getAcquaintance() {
        System.out.println("Romeo: Did my heart love till now? forswear it, sight! For I ne'er saw true beauty till this night.");
        try {
            InetAddress address = InetAddress.getByName(theIPAddress);
            Pair<InetAddress,Integer> pair = new Pair<>(address, thePort);
            //System.out.println(pair);
            return pair;
        } catch (UnknownHostException e) {
            // Handle the exception
            e.printStackTrace();
        }
        return null;
    }


    //Retrieves the lover's love
    public double receiveLoveLetter()
    {
        double tmp = 0;
        try {
            serviceMailbox = serverSocket.accept(); // Wait for a connection
            System.out.println("Romeo: Letter received");
            InputStream is = serviceMailbox.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            char[] buf = new char[1024];
            isr.read(buf, 0, 1024); // Read message into buffer
            String message = new String(buf); // Convert buffer to string
            // Remove last character if it is "J" or "R"
            for(int i = 0; i < 1024 ; i++){
                if(message.charAt(i) == 'J'){
                    message = message.substring(0, i);
                    break;
                }
            }
            tmp = Double.parseDouble(message.trim()); // Parse message into double
        } catch (IOException e) {
            System.out.println("Romeo: Failed to receive love letter " + e);
        }
        System.out.println("Romeo: O sweet Juliet... (<-" + tmp + ")");
        return tmp;
    }


    //Love (The ODE system)
    //Given the lover's love at time t, estimate the next love value for Romeo
    public double renovateLove(double partnerLove){
        System.out.println("Romeo: But soft, what light through yonder window breaks?\n" +
                "       It is the east, and Juliet is the sun.");
        currentLove = currentLove+(a*partnerLove);
        //System.out.println(currentLove);
        return currentLove;
    }


    //Communicate love back to playwriter
    public void declareLove(){
        try {
            // Create output stream to send message to client
            OutputStream outputStream = serviceMailbox.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);

            // Send current love value as message
            String message = currentLove + "R";
            outputWriter.write(message);
            outputWriter.flush();

            System.out.println("Romeo: I would I were thy bird " + "(->" + message + ")");

            // Close output stream
            outputWriter.close();
            outputStream.close();
        } catch (IOException e) {
            System.out.println("Romeo: Failed to declare love: " + e);
        }
    }



    //Execution
    public void run () {
        System.out.println("Romeo: Entering service iteration.");
        try {
            while (!this.isInterrupted()) {
                //Retrieve lover's current love
                System.out.println("Romeo: Awaiting letter");
                double JulietLove = this.receiveLoveLetter();
                //System.out.println(JulietLove);

                //Estimate new love value
                this.renovateLove(JulietLove);

                //Communicate love back to playwriter
                this.declareLove();
            }
        }catch (Exception e){
            System.out.println("Romeo: " + e);
        }
        System.out.println("Romeo: Exiting service iteration.");
        if (this.isInterrupted()) {
            System.out.println("Romeo: Here's to my love. O true apothecary,\n" +
                    "Thy drugs are quick. Thus with a kiss I die." );
        }
    }

}
