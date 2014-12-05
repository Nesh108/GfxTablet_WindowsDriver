import java.net.*;
import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Robot;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


public class MainUDPServer {

	static Robot mouse_handler;
	static final String[] resolutions = { "1024x768", "1080x800", "1280x800", "1366x768", 
		"1920x1080", "1920x1200", "2048x1536", "2560x1600" };

	static int RESOLUTION_X;
	static int RESOLUTION_Y;
	static int SCREEN_RESOLUTION_X;
	static int SCREEN_RESOLUTION_Y;
	static int MAX_VALUE = 32667;
	
	public static void main(String[] args) throws IOException, InterruptedException, AWTException {

		 String selectedResolution = (String) JOptionPane.showInputDialog(null, 
			        "Select the resolution of your tablet:",
			        "Resolution",
			        JOptionPane.QUESTION_MESSAGE, 
			        null, 
			        resolutions, 
			        resolutions[0]);

		if(selectedResolution == null)
			System.exit(1);
		else{
			
			RESOLUTION_X = Integer.parseInt(selectedResolution.split("x")[0]);
			RESOLUTION_Y = Integer.parseInt(selectedResolution.split("x")[1]);
	
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			SCREEN_RESOLUTION_X = (int) screenSize.getWidth();
			SCREEN_RESOLUTION_Y = (int) screenSize.getHeight();

		}
		 
		final DatagramSocket serverSocket = new DatagramSocket(40118);
		mouse_handler = new Robot();
		
		if (SystemTray.isSupported()) {
            // Yes My System Support System Tray
            
            // Create SystemTray and TrayIcon (TrayIcon : It is icon that
            // display in SystemTray)
            final SystemTray systemTray = SystemTray.getSystemTray();
            final TrayIcon trayIcon = new TrayIcon(getImage("icon.png"),
                    "GfxTablet Driver for Windows is running");
            trayIcon.setImageAutoSize(true);// Autosize icon base on space
                                            // available on
                                            // tray
 
            MouseAdapter mouseAdapter = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getButton() == 1){
                    // This will display small popup message from System Tray
                    trayIcon.displayMessage("GfxTablet Driver for Windows",
                            "GfxTablet Driver for Windows is running. \nRight click to stop it.",
                            TrayIcon.MessageType.INFO);
                    }
                    else if(e.getButton() == 3){
                    	
                    	serverSocket.close();
                    	mouse_handler.mouseRelease(InputEvent.BUTTON1_MASK);
                    	System.exit(0);
                    }
                }
            };
 
            trayIcon.addMouseListener(mouseAdapter);
 
            try {
                systemTray.add(trayIcon);
            } catch (Exception e) {
                e.printStackTrace();
            }
 
        }

		
		byte[] receiveData = new byte[1024];
		while(true){
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			System.out.println("Waiting for packet");
			serverSocket.receive(receivePacket);
			System.out.println("Got stuff");
			String sentence = bytesToHex(receivePacket.getData());
			System.out.println("RECEIVED[]: " + sentence.substring(24, 32));
			System.out.println("RECEIVED: " + getCoord(sentence));
			
			processData(getCoord(sentence), Integer.parseInt(sentence.substring(23, 24)));
		}
		
	}
	
	// 23 : motion/button {0,1}
	//
	
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static String getCoord(String Hex_String){
		String hex_coor = Hex_String.substring(24, 32);
		String x = convertHexToString(hex_coor.substring(0, 4));
		String y = convertHexToString(hex_coor.substring(4, 8));
		return x + "," + y;
		
	}
	
	public static String convertHexToString(String hex){
		 
		  StringBuilder val = new StringBuilder();
	 
		  //49204c6f7665204a617661 split into two characters 49, 20, 4c...
		  for( int i=0; i<hex.length()-1; i+=4 ){
		      //grab the hex in pairs
		      String output = hex.substring(i, (i + 4));
		      //convert hex to decimal
		      int decimal = Integer.parseInt(output, 16);
	 
		      val.append(decimal);
		  }
		  
		  return val.toString();
	  }
	

	
	public static void processData(String coord, int button) throws InterruptedException{
		int x_ = Integer.parseInt(coord.split(",")[0]);
		int y_ = Integer.parseInt(coord.split(",")[1]);
		
		int x1 = (RESOLUTION_X * x_) / MAX_VALUE;
		int y1 = (RESOLUTION_Y * y_) / MAX_VALUE;
		
		int x = (SCREEN_RESOLUTION_X * x1) / RESOLUTION_X;
		int y = (SCREEN_RESOLUTION_Y * y1) / RESOLUTION_Y;
		
		if(button == 1)
			mouse_handler.mouseRelease(InputEvent.BUTTON1_MASK);


		mouse_handler.mouseMove(x, y);
		mouse_handler.mousePress(InputEvent.BUTTON1_MASK);

		if(button == 1)
			mouse_handler.mouseRelease(InputEvent.BUTTON1_MASK);

	}
	
	public static Image getImage(String path) {
		
		URL iconURL = MainUDPServer.class.getResource(path);
		// iconURL is null when not found
		ImageIcon icon = new ImageIcon(iconURL);
		
        return icon.getImage();
    }
 
	
	
	
}
