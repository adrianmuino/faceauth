import com.hopding.jrpicam.RPiCamera;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinDirection;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.trigger.GpioCallbackTrigger;
import com.pi4j.io.gpio.trigger.GpioPulseStateTrigger;
import com.pi4j.io.gpio.trigger.GpioSetStateTrigger;
import com.pi4j.io.gpio.trigger.GpioSyncStateTrigger;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.io.gpio.event.PinEventType;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

/**
 *
 * @author Martin Garcia
 */
public class MainProcess {

    public static void main(String[] args) {
        RPiCamera piCamera = null;
        ArrayList<BufferedImage> images = new ArrayList<>();
        
        //Take picture
        try {
            piCamera = new RPiCamera();
            BufferedImage buffImage = piCamera.takeBufferedStill();
            File saveFile = new File("/home/pi/Pictures/FirstImage.jpeg");
            ImageIO.write(buffImage, "jpeg", saveFile);

            if (saveFile.canRead()) {
                FaceDetector fd = new FaceDetector(100, 100, "/home/pi/Pictures/FirstImage.jpeg");
                images = fd.detectFace();
                
                BufferedImage martin = ImageIO.read(new File("/home/pi/Pictures/image.jpeg"));
                
                for (BufferedImage buff : images) {
                    System.out.println("Difference value: " + imageComparison(buff, martin));
                }
                
            } else {
                System.out.println("Picture could not be saved");
            }

        } catch (Exception ex) {
            System.out.println("Exception caught" + ex);
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
}