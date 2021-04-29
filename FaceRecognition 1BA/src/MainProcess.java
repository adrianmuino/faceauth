/**
 * Author: Martin Garcia
 */

import com.hopding.jrpicam.RPiCamera;
import com.hopding.jrpicam.exceptions.FailedToRunRaspistillException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author Martin Garcia
 */
public class MainProcess {
    //path to authenticated users in the project
    static final File AUTHENTICATED_USERS = new File("/home/pi/NetBeansProjects/FaceRecognition 1BA/authenticated_users");
    static final String PASSWORD = "Team22";
    static boolean inProcess = false;
    static FileWriter log;
    
    @SuppressWarnings("Convert2Lambda")
    static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(final File AUTHENTICATED_USERS, final String name) {
            return name.endsWith(".jpeg");
        }
    };
    
    @SuppressWarnings({"SleepWhileInLoop", "Convert2Lambda"})
    public static void main(String[] args) {
        try {
            File logFile = new File("/home/pi/NetBeansProjects/FaceRecognition 1BA/log/log.txt");
            
            if (!logFile.exists()) {
                logFile.createNewFile();
            } 
            
            log = new FileWriter("/home/pi/NetBeansProjects/FaceRecognition 1BA/log/log.txt", true);
            
            log.append(new Date() + ". Process started...\n");
            System.out.println("Process started...");
            
            createMainFrame();
            
            // create gpio controller
            final GpioController gpio = GpioFactory.getInstance();

            // provision gpio pin # 18, 17, 27, 22 as input pins with internal pull down resistor enabled. The pin numbers are different in the library
            final GpioPinDigitalInput authenticationButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_UP);
            final GpioPinDigitalInput lockOutsideButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_UP);
            final GpioPinDigitalInput lockInsideButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_00, PinPullResistance.PULL_UP);
            final GpioPinDigitalInput unlockInsideButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_UP);

            // set shutdown state for input pins
            authenticationButton.setShutdownOptions(true);
            lockOutsideButton.setShutdownOptions(true);
            lockInsideButton.setShutdownOptions(true);
            unlockInsideButton.setShutdownOptions(true);
            
            // provision gpio pin # 23, 24 as output pins with low pinstates. The pin numbers are different in the library
            final GpioPinDigitalOutput greenLED = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_05, "Green LED", PinState.LOW);
            final GpioPinDigitalOutput redLED = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "Red LED", PinState.LOW);
            
            // create and register gpio pin listener
            authenticationButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    if (!inProcess) {
                        if (event.getState().isLow()) {
                            inProcess = true;
                            RPiCamera piCamera;
                            ArrayList<BufferedImage> images;

                            //Take picture
                            try {
                                log.append(new Date() + ". Starting authentication...\n");
                                System.out.println("Starting authentication");
                                
                                piCamera = new RPiCamera();
                                BufferedImage userCheck = piCamera.takeBufferedStill();
                                File tempUserCheckFile = new File("/home/pi/Pictures/userCheck.jpeg");
                                ImageIO.write(userCheck, "jpeg", tempUserCheckFile);
                                boolean authenticated = false;

                                if (tempUserCheckFile.canRead()) {
                                    FaceDetector fd = new FaceDetector(500, 500, "/home/pi/Pictures/userCheck.jpeg");
                                    images = fd.detectFace();
                                    
                                    log.append(new Date() + ". " + images.size() + " faces detected.\n");
                                    System.out.println(images.size() + " faces detected.");
                                    
                                    if (AUTHENTICATED_USERS.isDirectory()) {
                                        Main: for (BufferedImage buff : images) {
                                            for (final File f : AUTHENTICATED_USERS.listFiles(IMAGE_FILTER)) {
                                                BufferedImage user = ImageIO.read(f);
                                                double diffValue = imageComparison(buff, user);
                                                System.out.println("Difference value: " + diffValue);

                                                if (diffValue < 10) {
                                                    log.append(new Date() + ". User: " + f.getName().replace(".jpeg", "") + " authenticated!\n");
                                                    System.out.println("User: " + f.getName().replace(".jpeg", "") + " authenticated!");
                                                    
                                                    //Flash green light
                                                    flashLED(greenLED);
                                                    
                                                    //Open lock
                                                    try {
                                                        authenticated = true;
                                                        //lockEvent("unlock");
                                                    } catch (Exception/*IOException*/ e) {
                                                        log.append(new Date() + ". Exception when attempting to open lock! " + e + "\n");
                                                        System.out.println("Exception caught: " + e);
                                                    }

                                                    break Main;
                                                }
                                            }
                                            
                                            if (!authenticated) {
                                                //ask to add new user with joptionpane password
                                                if (JOptionPane.showConfirmDialog(null, "Add new user?", "Add User", 
                                                        JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                                    log.append(new Date() + ". Unauthenticated user detected! Program will try to add new user...\n");
                                                    System.out.println("Unauthenticated user detected! Program will try to add new user...");

                                                    String pwCheck = JOptionPane.showInputDialog(null, "Enter Password:", 
                                                            "Password Verification");

                                                    if (pwCheck.equals(PASSWORD)) {
                                                        log.append(new Date() + ". Password match! New user will be added.\n");
                                                        System.out.println("Password match! New user will be added.");

                                                        String newUser = JOptionPane.showInputDialog(null, "Enter Name:", 
                                                            "Add New User");

                                                        try {
                                                            File saveFile = new File(AUTHENTICATED_USERS + "/" + newUser + ".jpeg");
                                                            ImageIO.write(buff, "jpeg", saveFile);

                                                            authenticated = true;
                                                            log.append(new Date() + ". User " + newUser + " added!\n");
                                                            System.out.println("User " + newUser + " added!\n");
                                                            
                                                            authenticated = true;
                                                        }
                                                        catch (IOException e) {
                                                            log.append(new Date() + ". Exception when attempting to add new user! " + e + "\n");
                                                            System.out.println("Exception caught: " + e);
                                                        }
                                                    }
                                                    else {
                                                        log.append(new Date() + ". Incorrect password! No new user will be added.\n");
                                                        System.out.println("Incorrect password!");
                                                    }
                                                }
                                            }
                                        }

                                        if (!authenticated) {
                                            //Flash red light
                                            flashLED(redLED);
                                            
                                            log.append(new Date() + ". No user could be authenticated!\n");
                                            System.out.println("No user could be authenticated!");
                                        }
                                    }
                                    else {
                                        log.append(new Date() + ". The authenticated users folder is not a directory. Make sure the specified folder exists.\n");
                                        System.out.println("The authenticated users folder is not a directory. Make sure the specified folder exists.");
                                    }
                                } 
                                else {
                                    log.append(new Date() + ". Picture could not be saved!\n");
                                    System.out.println("Picture could not be saved!");
                                }
                            } 
                            catch (FailedToRunRaspistillException | IOException | InterruptedException ex) {
                                System.out.println("Exception caught: " + ex);
                            }
                            finally {
                                inProcess = false;
                            }
                        }
                    }
                }
            });
            
            lockOutsideButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    if (!inProcess) {
                        try {
                            log.append(new Date() + ". Locking door from outside\n");
                            System.out.println("Locking door from inside");
                            lockEvent("lock");
                        }
                        catch (IOException e) {
                            System.out.println("Exception caught: " + e);
                        }
                    }
                }
            });
            
            lockInsideButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    if (!inProcess) {
                        try {
                            log.append(new Date() + ". Locking door from inside\n");
                            System.out.println("Locking door from inside");
                            lockEvent("lock");
                        }
                        catch (IOException e) {
                            System.out.println("Exception caught: " + e);
                        }
                    }
                }
            });
            
            unlockInsideButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    if (!inProcess) {
                        try {
                            log.append(new Date() + ". Unocking door from inside\n");
                            System.out.println("Unocking door from inside");
                            lockEvent("unlock");
                        }
                        catch (IOException e) {
                            System.out.println("Exception caught: " + e);
                        }
                    }
                }
            });
            
            while (!inProcess) {
                Thread.sleep(500);
            }
        } 
        catch (IOException | InterruptedException e) {
            System.out.println("Exception caught: " + e);
        }
    }
    
    private static double imageComparison(BufferedImage toCompare, BufferedImage existing) {
        int width1 = toCompare.getWidth(); 
        int width2 = existing.getWidth(); 
        int height1 = toCompare.getHeight(); 
        int height2 = existing.getHeight(); 
  
        if ((width1 != width2) || (height1 != height2)) {
            System.out.println("Error: Images dimensions"+ 
                                             " mismatch");
            return 100;
        }
        else
        { 
            long difference = 0; 
            for (int y = 0; y < height1; y++) 
            { 
                for (int x = 0; x < width1; x++) 
                { 
                    int rgbA = toCompare.getRGB(x, y); 
                    int rgbB = existing.getRGB(x, y); 
                    int redA = (rgbA >> 16) & 0xff; 
                    int greenA = (rgbA >> 8) & 0xff; 
                    int blueA = (rgbA) & 0xff; 
                    int redB = (rgbB >> 16) & 0xff; 
                    int greenB = (rgbB >> 8) & 0xff; 
                    int blueB = (rgbB) & 0xff; 
                    difference += Math.abs(redA - redB); 
                    difference += Math.abs(greenA - greenB); 
                    difference += Math.abs(blueA - blueB); 
                } 
            } 
  
            // Total number of red pixels = width * height 
            // Total number of blue pixels = width * height 
            // Total number of green pixels = width * height 
            // So total number of pixels = width * height * 3 
            double total_pixels = width1 * height1 * 3; 
  
            // Normalizing the value of different pixels 
            // for accuracy(average pixels per color 
            // component) 
            double avg_different_pixels = difference / 
                                          total_pixels; 
  
            // There are 255 values of pixels in total 
            double percentage = (avg_different_pixels / 
                                            255) * 100; 
  
            return percentage;
        }
    }
    
    private static void createMainFrame() {
        try {
            JFrame mainFrame = new JFrame("FaceRecognition 1RA");
            mainFrame.setSize(500, 200);

            BufferedImage frameImage = ImageIO.read(new File("/home/pi/NetBeansProjects/FaceRecognition 1BA/images/frameImage.jpeg"));
            BufferedImage resizedFrameImage = new BufferedImage(500, 200, frameImage.getType());
            
            Graphics2D g = resizedFrameImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(frameImage, 0, 0, 500, 200, 0, 0, frameImage.getWidth(), frameImage.getHeight(), null);
            g.dispose();
            
            ImagePanel frameImagePanel = new ImagePanel(resizedFrameImage);
            mainFrame.add(frameImagePanel);
            
            mainFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                    try {
                        log.append(new Date() + ". End of process!\n");
                        System.out.println("End of process!");
                        
                        log.close();
                    }
                    catch (IOException e) {
                        System.out.println("Exception caught: " + e);
                    }
                }
            }); 
            
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            mainFrame.setVisible(true);
        }
        catch (IOException e) {
            System.out.println("Exception caught: " + e);
        }
    }
    
    private static void flashLED(GpioPinDigitalOutput aLED) {
        try {
            aLED.setState(PinState.HIGH);
            Thread.sleep(3000);
            aLED.setState(PinState.LOW);
        }
        catch (InterruptedException e) {
            System.out.println("Exception caught: " + e);
        }
    }
    
    private static void lockEvent(String event) {
        try {
            Runtime.getRuntime().exec("sudo python3 /home/pi/NetBeansProjects/"
                + "FaceRecognition 1BA/python/lock.py " + event);
        }
        catch (IOException e) {
            System.out.println("Exception caught: " + e);
        }
    }
}