/*Working TCPClient.java error 1st char
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FanManager;

/**
 *
 * @author Reign
 */
import FanManager.model.Fan;
import FanManager.model.FanGroup;
import FanManager.model.FanPane;
import FanManager.view.FanManagerLayoutController;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class TCPClient implements Runnable {

    private DataOutputStream toPrototype;
    private DataInputStream fromPrototype;
//    private ObjectOutputStream toClient;
//    private ObjectInputStream fromClient;

    private ServerSocket server;
    private Socket client;
    private String consoleData; // logs data from prototype

    private double f1Speed = 0;
    private double f2Speed = 0;
    private double f3Speed = 0;

    private String FS1 = "";
    private String FS2 = "";
    private String FS3 = "";

    private double f1Freq = 0;
    private double f2Freq = 0;
    private double f3Freq = 0;

    private String FF1 = "";
    private String FF2 = "";
    private String FF3 = "";

    private FanManagerLayoutController mainApp;
    private FanGroup fanGroup;
    private ObservableList<Fan> fanList;
    private FanPane[] fanPanes;

    public TCPClient(FanManagerLayoutController mainApp) {
        this.mainApp = mainApp;
        fanList = this.mainApp.getFanList();
        fanGroup = this.mainApp.getFanGroup();
        fanPanes = this.mainApp.getFanPanes();
    }

    @Override
    public void run() {
        try {
            // Create a socket to connect to the server
            server = new ServerSocket(8001);
            System.out.println("Wifi started at " + new Date() + '\n');
            client = server.accept();
            System.out.println("client = Server accepted");

            // Create an output stream to send data to the server
//            toClient = new ObjectOutputStream(client.getOutputStream());
//            System.out.println("toClient: " + toClient + '\n');
//            toClient.flush();
//            System.out.println("toClient flush: " + toClient + '\n');
            // Create an input stream to receive data from the server
            fromPrototype = new DataInputStream(client.getInputStream());
            System.out.println("fromPrototype: " + fromPrototype + '\n');
//                System.out.println(fromClient.readObject());

            toPrototype = new DataOutputStream(client.getOutputStream());

            fanList.addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change change) {
                    System.out.println("Data changed");
                    sendData();
                }
            });

            // Receive server data
            Thread recieveData = new Thread(() -> recieveData());
            recieveData.setDaemon(true);
            recieveData.start();

//            String sentence;   String modifiedSentence;   
//            System.out.println("Inside aThis TCPClient RUN!");
//
//            BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));   
//            Socket clientSocket = new Socket("10.0.1.32", 8000);
//
//            ObjectOutputStream outToServer = new ObjectOutputStream(clientSocket.getOutputStream());   
//            ObjectInputStream inFromServer = new ObjectInputStream(clientSocket.getInputStream());   
//
//            outToServer.writeBytes("hi");   
//
//            sentence = (String) inFromServer.readObject(); 
//            System.out.println("FROM SERVER: " + sentence); 
//
//            clientSocket.close();  
        } catch (ConnectException ce) {
            System.err.println(ce);
        } catch (Exception ex) {
            System.out.println("Bad TCPClient Connection");
            System.err.println(ex);

        }
    }

    private void recieveData() {
        try {
            while (true) {
//                System.out.println("Waiting for object");
                // Read from network
//                char someInt = fromPrototype.readChar();
                char someInt = (char) fromPrototype.read();
                consoleData += someInt;
                if (someInt == '\r') {
                    //data += "\n";
                    System.out.println(consoleData);

                }
//                FanGroup temp = (FanGroup) fromClient.readObject();
//                System.setErr(new PrintStream(Console.getInstance(theObject)));

                //System.out.println("Recieved Char");
                //System.out.println(someInt);
//                // If new fanGroup sent over, then update each fan
//                if (!fanGroup.equals(someInt)) {
//                    fanGroup.update(someInt);
//                    
//                    // Update fan animations
//                    for (int i = 0; i < fanGroup.getFans().size(); i++) {
//                        if (!fanList.get(i).equals(fanGroup.getFans().get(i))) {
//                            fanPanes[i].setFan(fanGroup.getFans().get(i));
//                            fanPanes[i].updateGauge();
//                        }
//                    }
//                }
                Thread.sleep(10);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
//                client.close();
                fromPrototype.close();
//                toClient.close();
            } catch (SocketException ex) {
                System.err.println(ex);
                System.out.println("Fan Server Error: Finally Block");
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

    private void sendData() {
        try {

            System.out.println("sending data");
            fanGroup = mainApp.getFanGroup();

            f1Speed = (double) fanGroup.getFans().get(0).getSpeed();
            f2Speed = (double) fanGroup.getFans().get(1).getSpeed();
            f3Speed = (double) fanGroup.getFans().get(2).getSpeed();

            FS1 += round(f1Speed, 0);;
            FS2 += round(f2Speed, 0);;
            FS3 += round(f3Speed, 0);;
            
            System.out.println("FS1: " + FS1);
            System.out.println("FS2: " + FS2);
            System.out.println("FS3: " + FS3);

            f1Freq = (double) fanGroup.getFans().get(0).getFreq();
            f2Freq = (double) fanGroup.getFans().get(1).getFreq();
            f3Freq = (double) fanGroup.getFans().get(2).getFreq();

            FF1 += round(f1Freq, 0);;
            FF2 += round(f2Freq, 0);;
            FF3 += round(f3Freq, 0);;
            
            System.out.println("FF1: " + FF1);
            System.out.println("FF2: " + FF2);
            System.out.println("FF3: " + FF3);

//            System.out.println(fanGroup.getFans().get(0).getSpeed());

            // Write to network
//            toPrototype.writeUTF("<FS1><" + FS1 + ">,<FS2><" + FS2 + ">,<FS3><" + FS3 + ">,<FF1><" + FF1 + ">,<FF2><" + FF2 + ">,<FF3><" + FF3 + "-------->\n");
            toPrototype.writeBytes("H" + FS1 + "," + FS2 + "," + FS3 + "," + FF1 + "," + FF2 + "," + FF3 + "F");

            System.out.println("data sent");
            toPrototype.flush();
            f1Speed = 0;
            f2Speed = 0;
            f3Speed = 0;
            FS1 = "";
            FS2 = "";
            FS3 = "";
            f1Freq = 0;
            f2Freq = 0;
            f3Freq = 0;
            FF1 = "";
            FF2 = "";
            FF3 = "";
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static double round(double value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

}
