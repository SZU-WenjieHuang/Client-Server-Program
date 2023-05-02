/*
 * PlayWriter.java
 *
 * PLayWriter class.
 * Creates the lovers, and writes the two lover's story (to an output text file).
 */


import java.net.ConnectException;
import java.net.Socket;
import java.net.InetAddress;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import javafx.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;



public class PlayWriter {

    private Romeo  myRomeo  = null;
    private InetAddress RomeoAddress = null;
    private int RomeoPort = 0;
    private Socket RomeoMailbox = null;

    private Juliet myJuliet = null;
    private InetAddress JulietAddress = null;
    private int JulietPort = 0;
    private Socket JulietMailbox = null;

    double[][] theNovel = null;
    int novelLength = 0;

    public PlayWriter()
    {
        novelLength = 500; //Number of verses
        theNovel = new double[novelLength][2];
        theNovel[0][0] = 0;
        theNovel[0][1] = 1;
    }

    //Create the lovers
    public void createCharacters() {
        //Create the lovers
        System.out.println("PlayWriter: Romeo enters the stage.");

        Romeo myRomeo = new Romeo(0.0);
        Thread romeoThread = new Thread(myRomeo);
        romeoThread.start();

        System.out.println("PlayWriter: Juliet enters the stage.");

        Juliet myJuliet = new Juliet(1.0);
        Thread julietThread = new Thread(myJuliet);
        julietThread.start();
    }

    //Meet the lovers and start letter communication
    public void charactersMakeAcquaintances() {

        Pair<InetAddress, Integer> romeoPair = Romeo.getAcquaintance();
        RomeoAddress = romeoPair.getKey();
        RomeoPort = romeoPair.getValue();
        System.out.println("PlayWriter: I've made acquaintance with Romeo");

        Pair<InetAddress, Integer> julietPair = Juliet.getAcquaintance();
        JulietAddress = julietPair.getKey();
        JulietPort = julietPair.getValue();
        System.out.println("PlayWriter: I've made acquaintance with Juliet");
    }


    //Request next verse: Send letters to lovers communicating the partner's love in previous verse
    public void requestVerseFromRomeo(int verse) {
        System.out.println("PlayWriter: Requesting verse " + verse + " from Romeo. -> (" + theNovel[verse-1][1] + ")");

        try {
            // Create a service request message containing Juliet's love and add a termination character
            String message = Double.toString(theNovel[verse-1][1]) + "J";

            // Connect to Romeo server and get the output and input streams
            RomeoMailbox = new Socket(RomeoAddress, RomeoPort);

            //output Stream
            PrintWriter out = new PrintWriter(RomeoMailbox.getOutputStream(), true);

            // Send the service request message to the Romeo server
            out.println(message);

        } catch (ConnectException e) {
            System.out.println("PlayWriter: Failed to connect to Romeo server: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("PlayWriter: Error occurred while requesting verse from Romeo: " + e.getMessage());
        }
    }



    //Request next verse: Send letters to lovers communicating the partner's love in previous verse
    public void requestVerseFromJuliet(int verse) {
        System.out.println("PlayWriter: Requesting verse " + verse + " from Juliet. -> (" + theNovel[verse-1][0] + ")");

        try {
            // Create a service request message containing Juliet's love and add a termination character
            String message = Double.toString(theNovel[verse-1][0]) + "R";

            // Connect to Romeo server and get the output and input streams
            JulietMailbox = new Socket(JulietAddress, JulietPort);

            //output Stream
            PrintWriter out = new PrintWriter(JulietMailbox.getOutputStream(), true);

            // Send the service request message to the Juliet Server
            out.println(message);

        } catch (ConnectException e) {
            System.out.println("PlayWriter: Failed to connect to Juliet server: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("PlayWriter: Error occurred while requesting verse from Juliet: " + e.getMessage());
        }
    }

    //Receive letter from Romeo with renovated love for current verse
    public void receiveLetterFromRomeo(int verse) {
        System.out.println("PlayWriter: Receiving letter from Romeo for verse " + verse + ".");

        try {
            //input Stream
            BufferedReader in = new BufferedReader(new InputStreamReader(RomeoMailbox.getInputStream()));

            // Read the response message from the Romeo server
            String response = in.readLine();

            // Check if the response message is valid
            if (response != null) {
                // Extract Juliet's love from the response message
                double RomeoLove = Double.parseDouble(response.substring(0, response.length()-1));

                // Update the novel array with Juliet's love
                theNovel[verse][0] = RomeoLove;
            } else {
                System.out.println("PlayWriter: Failed to receive verse " + verse + " from Romeo.");
            }

            System.out.println("PlayWriter: Romeo's verse " + verse + " -> " + theNovel[verse][0]);

            // Close the connection with the RomeoMailbox
            RomeoMailbox.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Receive letter from Juliet with renovated love fro current verse
    public void receiveLetterFromJuliet(int verse) {
        try {
            //input Stream
            BufferedReader in = new BufferedReader(new InputStreamReader(JulietMailbox.getInputStream()));

            // Read the response message from the Romeo server
            String response = in.readLine();

            // Check if the response message is valid
            if (response != null) {
                // Extract Juliet's love from the response message
                double JulietLove = Double.parseDouble(response.substring(0, response.length()-1));

                // Update the novel array with Juliet's love
                theNovel[verse][1] = JulietLove;
            } else {
                System.out.println("PlayWriter: Failed to receive verse " + verse + " from Juliet.");
            }

            System.out.println("PlayWriter: Juliet's verse " + verse + " -> " + theNovel[verse][1]);

            // Close the connection with the RomeoMailbox
            RomeoMailbox.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Let the story unfold
    public void storyClimax() {
        for (int verse = 1; verse < novelLength; verse++) {
            //Write verse
            System.out.println("PlayWriter: Writing verse " + verse + ".");

            // Request service from Romeo server
            requestVerseFromRomeo(verse);
            // Receive outcome of service from Romeo server
            receiveLetterFromRomeo(verse);
            // Request service from Juliet server
            requestVerseFromJuliet(verse);
            // Receive outcome of service from Juliet server
            receiveLetterFromJuliet(verse);

            System.out.println("PlayWriter: Verse " + verse + " finished.");
        }
    }

    //Character's death
    public void charactersDeath() {
        try {
            // Close connection with Romeo server
            RomeoMailbox.close();

            // Close connection with Juliet server
            JulietMailbox.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
			
    }


    //A novel consists of introduction, conflict, climax and denouement
    public void writeNovel() {
        System.out.println("PlayWriter: The Most Excellent and Lamentable Tragedy of Romeo and Juliet.");
        System.out.println("PlayWriter: A play in IV acts.");
        //Introduction,
        System.out.println("PlayWriter: Act I. Introduction.");
        this.createCharacters();
        //Conflict
        System.out.println("PlayWriter: Act II. Conflict.");
        this.charactersMakeAcquaintances();
        //Climax
        System.out.println("PlayWriter: Act III. Climax.");
        this.storyClimax();
        //Denouement
        System.out.println("PlayWriter: Act IV. Denouement.");
        this.charactersDeath();

    }


    //Dump novel to file
    public void dumpNovel() {
        FileWriter Fw = null;
        try {
            Fw = new FileWriter("RomeoAndJuliet.csv");
        } catch (IOException e) {
            System.out.println("PlayWriter: Unable to open novel file. " + e);
        }

        System.out.println("PlayWriter: Dumping novel. ");
        StringBuilder sb = new StringBuilder();
        for (int act = 0; act < novelLength; act++) {
            String tmp = theNovel[act][0] + ", " + theNovel[act][1] + "\n";
            sb.append(tmp);
            //System.out.print("PlayWriter [" + act + "]: " + tmp);
        }

        try {
            BufferedWriter br = new BufferedWriter(Fw);
            br.write(sb.toString());
            br.close();
        } catch (Exception e) {
            System.out.println("PlayWriter: Unable to dump novel. " + e);
        }
    }

    public static void main (String[] args) {
        PlayWriter Shakespeare = new PlayWriter();
        Shakespeare.writeNovel();
        Shakespeare.dumpNovel();
        System.exit(0);
    }


}
