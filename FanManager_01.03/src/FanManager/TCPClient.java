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
import FanManager.view.ConsoleController;
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
    private final String consoleData = ""; // logs data from prototype

    private int f1Speed = 0;
    private int f2Speed = 0;
    private int f3Speed = 0;

    private String FS1 = "";
    private String FS2 = "";
    private String FS3 = "";

    private int f1Freq = 18000;
    private int f2Freq = 18000;
    private int f3Freq = 18000;

    private String FF1 = "";
    private String FF2 = "";
    private String FF3 = "";

    private double f1Temp = 0;
    private double f2Temp = 0;
    private double f3Temp = 0;

    private String FP1 = "";
    private String FP2 = "";
    private String FP3 = "";

    private boolean f1Power = false;
    private boolean f2Power = false;
    private boolean f3Power = false;

    private int f1PowerToInt = 0;
    private int f2PowerToInt = 0;
    private int f3PowerToInt = 0;

    private String FT1 = "";
    private String FT2 = "";
    private String FT3 = "";

    private double mainTemp = 0;
    private double mainHumid = 0;
    private double mainBarometer = 0;

    private String mainTempIn = "";
    private String mainHumidIn = "";
    private String mainBarometerIn = "";

    private final FanManagerLayoutController mainApp;
    private FanGroup fanGroup;
    private final ObservableList<Fan> fanList;
    private final FanPane[] fanPanes;

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
            ConsoleController.addOutput("Wifi started at " + new Date() + '\n');
            client = server.accept();
            System.out.println("client = Server accepted");
            ConsoleController.addOutput("client = Server accepted");

            fromPrototype = new DataInputStream(client.getInputStream());
            System.out.println("fromPrototype: " + fromPrototype + '\n');
            ConsoleController.addOutput("fromPrototype: " + fromPrototype + '\n');

            toPrototype = new DataOutputStream(client.getOutputStream());

            fanList.addListener(new ListChangeListener() {
                @Override
                public void onChanged(ListChangeListener.Change change) {
                    System.out.println("Data changed");
                    ConsoleController.addOutput("Data changed");
                    sendData();
                }
            });

            // Receive server data
            Thread recieveData = new Thread(() -> recieveData());
            recieveData.setDaemon(true);
            recieveData.start();

            // Send client data
            Thread sendData = new Thread(() -> sendData());
            sendData.setDaemon(true);
            sendData.start();

        } catch (ConnectException ce) {
            System.err.println(ce);
        } catch (Exception ex) {
            System.out.println("Bad TCPClient Connection");
            ConsoleController.addOutput("Bad TCPClient Connection");
            System.err.println(ex);

        }
    }

    private void recieveData() {
        try {
            while (true) {
                // Read from network
                String someLine = fromPrototype.readLine();
                String[] parts = someLine.split(",");

                mainTempIn = parts[0]; // Temp
                mainTemp = Double.parseDouble(mainTempIn);
//                System.out.println("Temp: " + mainTemp);

                mainHumidIn = parts[1]; // Humidity
                mainHumid = Double.parseDouble(mainHumidIn);
//                System.out.println("Humidity: " + mainHumid);

                mainBarometerIn = parts[2]; // Barometer
                mainBarometer = Double.parseDouble(mainBarometerIn);
//                System.out.println("Barometer: " + mainBarometer);

//                FT1 = parts[3]; // Fan1 Temp
//                f1Temp = Double.parseDouble(FT1);
////                System.out.println("Fan1 Temp: " + f1Temp);
//
//                FT2 = parts[4]; // Fan2 Temp
//                f2Temp = Double.parseDouble(FT2);
////                System.out.println("Fan2 Temp: " + f2Temp);
//
//                FT3 = parts[5]; // Fan3 Temp
//                f3Temp = Double.parseDouble(FT3);
////                System.out.println("Fan3 Temp: " + f2Temp);

                fanGroup = mainApp.getFanGroup();

                fanGroup.setTemperature(mainTemp);
                fanGroup.setHumidity(mainHumid);
                fanGroup.setBarometricPressure(mainBarometer);

                mainApp.updateTempList(mainTemp, mainHumid, mainBarometer);
                f1Temp = 0;
                f2Temp = 0;
                f3Temp = 0;
                FT1 = "";
                FT2 = "";
                FT3 = "";
//                consoleData += someLine;

                Thread.sleep(10);
            }
        } catch (IOException | NumberFormatException | InterruptedException ex) {
        } finally {
            try {
                fromPrototype.close();
            } catch (SocketException ex) {
                System.err.println(ex);
                System.out.println("Fan Server Error: Finally Block");
                ConsoleController.addOutput("Fan Server Error: Finally Block");
            } catch (IOException ex) {
                System.err.println(ex);
            }

        }
    }

    private void sendData() {
        try {

            System.out.println("sending data");
            ConsoleController.addOutput("sending data");
//            fanGroup = mainApp.getFanGroup();

            f1Speed = (int) (double) fanGroup.getFans().get(0).getSpeed();
            f2Speed = (int) (double) fanGroup.getFans().get(1).getSpeed();
            f3Speed = (int) (double) fanGroup.getFans().get(2).getSpeed();

//            System.out.println("f1Speed: " + f1Speed);
//            System.out.println("f2Speed: " + f2Speed);
//            System.out.println("f3Speed: " + f3Speed);

            f1Freq = (int) (double) fanGroup.getFans().get(0).getFreq();
            f2Freq = (int) (double) fanGroup.getFans().get(1).getFreq();
            f3Freq = (int) (double) fanGroup.getFans().get(2).getFreq();

//            System.out.println("f1Freq: " + f1Freq);
//            System.out.println("f2Freq: " + f2Freq);
//            System.out.println("f3Freq: " + f3Freq);

            f1Power = (boolean) fanGroup.getFans().get(0).getPower();
            f2Power = (boolean) fanGroup.getFans().get(1).getPower();
            f3Power = (boolean) fanGroup.getFans().get(2).getPower();
 
//            System.out.println("f1Power: " + f1Power);
//            System.out.println("f2Power: " + f2Power);
//            System.out.println("f3Power: " + f3Power);


//            FS1 += round(f1Speed, 0);
//            FS2 += round(f2Speed, 0);
//            FS3 += round(f3Speed, 0);
            FS1 = String.valueOf(f1Speed);
            FS2 = String.valueOf(f2Speed);
            FS3 = String.valueOf(f3Speed);

            System.out.println("FS1: " + FS1);
            System.out.println("FS2: " + FS2);
            System.out.println("FS3: " + FS3);


//            FF1 += round(f1Freq, 0); // TODO something wrong here, sending value of 50 ???
//            FF2 += round(f2Freq, 0);
//            FF3 += round(f3Freq, 0);

            FF1 = String.valueOf (f1Freq);
            FF2 = String.valueOf (f2Freq);
            FF3 = String.valueOf (f3Freq);
            
//            FF1 = "60";
//            FF2 = "60";
//            FF3 = "60";

            System.out.println("FF1: " + FF1);
            System.out.println("FF2: " + FF2);
            System.out.println("FF3: " + FF3);
            
            
            if (f1Power == false)
                f1PowerToInt = 1;
            if (f1Power == true)
                f1PowerToInt = 0;
            if (f2Power == false)
                f2PowerToInt = 1;
            if (f2Power == true)
                f2PowerToInt = 0;
            if (f3Power == false)
                f3PowerToInt = 1;
            if (f3Power == true)
                f3PowerToInt = 0;
            
            
            FP1 = String.valueOf (f1PowerToInt);
            FP2 = String.valueOf (f2PowerToInt);
            FP3 = String.valueOf (f3PowerToInt);

            System.out.println("FP1: " + FP1);
            System.out.println("FP2: " + FP2);
            System.out.println("FP3: " + FP3);
            
            
//            System.out.println(fanGroup.getFans().get(0).getSpeed());
            // Write to network
//            toPrototype.writeUTF("H" + FS1 + "," + FS2 + "," + FS3 + "," + FF1 + "," + FF2 + "," + FF3 + "\n");
            toPrototype.writeUTF("<" + FS1 + "," + FS2 + "," + FS3 + "," + FF1 + "," + FF2 + "," + FF3 + "," + FP1 + "," + FP2 + "," + FP3 + ">" + "\n");
//            System.out.println("data sent");
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
//            System.out.println("OUT FS1: " + FS1);
//            System.out.println("OUT FS2: " + FS2);
//            System.out.println("OUT FS3: " + FS3);

        } catch (Exception ex) {

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
