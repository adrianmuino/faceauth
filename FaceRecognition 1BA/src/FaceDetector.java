import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import java.io.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import org.openimaj.image.FImage;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;

public class FaceDetector extends JFrame {
    public java.awt.image.BufferedImage readImg(String fn, int x, int y) throws IOException {
        int width = x;
        int height = y;
        BufferedImage image;
        File f;

        //read image
        try {
            f = new File(fn); //image file path
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println("Exception caught: " + e);
            
            return null;
        }
        
        return image;
    }

    private static final long serialVersionUID = 1L;
    private static final HaarCascadeDetector DETECTOR = new HaarCascadeDetector();
    private BufferedImage img = null;
    private List< DetectedFace> faces = null;

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public FaceDetector(int width, int height, String name) throws IOException {
        int w = width;
        int h = height;
        
        img = readImg(name, w, h);
        
        ImagePanel panel = new ImagePanel(img);
        panel.setPreferredSize(new Dimension(w, h));
        
        add(panel);
        setTitle("Face Recognizer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public ArrayList<BufferedImage> detectFace() {
        ArrayList<BufferedImage> images = new ArrayList<>();
        
        JFrame fr = new JFrame("Discovered Faces");
        
        faces = DETECTOR.detectFaces(ImageUtilities.createFImage(img));
        
        if (faces == null) {
            System.out.println("No faces found in the captured image");
            return null;
        }

        Iterator<DetectedFace> dfi = faces.iterator();
        int faceCounter = 0;
        
        while (dfi.hasNext()) {
            DetectedFace face = dfi.next();
            FImage image1 = face.getFacePatch();
            BufferedImage buff = ImageUtilities.createBufferedImage(image1);
            BufferedImage resized = new BufferedImage(100, 100, buff.getType());
            Graphics2D g = resized.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(buff, 0, 0, 100, 100, 0, 0, buff.getWidth(), buff.getHeight(), null);
            g.dispose();
            
            images.add(resized);
            
            ImagePanel p = new ImagePanel(resized);
            fr.add(p);
            faceCounter++;
        }

        System.out.println("Discovered faces: " + faceCounter);
        
        fr.setLayout(new FlowLayout(0));
        fr.setSize(500, 120);
        fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        fr.setVisible(true);
        
        return images;
    }
}