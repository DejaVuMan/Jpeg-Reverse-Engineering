package JpegHelpers;

import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;

public class Driver {
    private static String path = ""; // empty initial path to be changed on button press.
    public static void main(String[] args) {
    JFrame mainWindow = new JFrame("JPEG Encoder");

    JButton encodeButton = new JButton("Encode (BMP to JPEG)");
    JTextField filePathField = new JTextField();
    JButton setPath = new JButton("Set Path");
    JButton decodeButton = new JButton("Decode (JPEG to BMP)");
    JLabel pathLabel = new JLabel("File Path:");

    encodeButton.setBounds(40,90,200,30);
    filePathField.setBounds(100, 50, 140, 30);
    pathLabel.setBounds(40, 50, 100, 30);
    setPath.setBounds(250, 50, 100, 30);
    decodeButton.setBounds(40, 130, 200, 30);


    mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Terminate on close of window
    mainWindow.add(encodeButton);
    mainWindow.add(filePathField);
    mainWindow.add(setPath);
    mainWindow.add(decodeButton);
    mainWindow.add(pathLabel);

    mainWindow.setSize(800,400);
    mainWindow.setLayout(null);
    mainWindow.setVisible(true);

    setPath.addActionListener(e -> {
        path = filePathField.getText();
        System.out.println("Path set to: " + path);
    });
    //TODO: Move to separate class for better organization and code readability
    encodeButton.addActionListener(e -> {
        if(path.equals("")){
            System.out.println("Please set a path first!");
            return;
        } else if (!path.endsWith(".bmp")){
            System.out.println("Please set a valid BMP file path!");
            return;
        }
        try {
            Instant start = Instant.now();
            new JpegEncoder().Encode(path); // this is jpeg encoder
            Instant end = Instant.now();
            System.out.println("Total Time elapsed: " + java.time.Duration.between(start, end).toMillis() + "ms");

            JFrame doneWindow = new JFrame("Encoding Done");
            JLabel doneLabel = new JLabel("Encoding Done!");

            Container contentPane = doneWindow.getContentPane();
            Image image = ImageIO.read(new File(path.substring(0, path.length() - 4) + ".jpg"));
            ImageIcon icon = new ImageIcon(image);
            JLabel imageLabel = new JLabel(icon);
            Dimension size = imageLabel.getPreferredSize(); // get size of image we set to JLabel

            Image dctImage = ImageIO.read(new File("dct.png"));
            ImageIcon dctImageIcon = new ImageIcon(dctImage);
            JLabel dctLabel = new JLabel(dctImageIcon);
            Dimension dctSize = dctLabel.getPreferredSize(); // get size of image we set to JLabel

            dctLabel.setBounds(size.width, 0, dctSize.width, dctSize.height);
            imageLabel.setBounds(0, 0, size.width, size.height); // set bounds of JLabel to size of image
            doneLabel.setBounds(40, 50, 100, 30);
            contentPane.add(imageLabel);
            doneWindow.add(doneLabel);
            contentPane.add(dctLabel);
            doneWindow.setSize(imageLabel.getWidth()*2, imageLabel.getHeight());
            doneWindow.setLayout(null);
            doneWindow.setVisible(true);

            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

    decodeButton.addActionListener(e -> {
        if (path.equals("")) {
            System.out.println("Please set a path first!");
            return;
        } else if (!path.endsWith(".jpg")) {
            System.out.println("Please set a valid JPEG file path!");
            return;
        }
        try {
            Instant start = Instant.now();
            new JpegDecoder().decode(path);
            Instant end = Instant.now();
            System.out.println("Total Time elapsed: " + java.time.Duration.between(start, end).toMillis() + "ms");

            JFrame doneWindow = new JFrame("Decoding Done");
            JLabel doneLabel = new JLabel("Decoding Done!");

            Container contentPane = doneWindow.getContentPane();
            Image image = ImageIO.read(new File(path.substring(0, path.length() - 4) + ".bmp"));
            ImageIcon resultImage = new ImageIcon(image);
            JLabel imageLabel = new JLabel(resultImage);
            Dimension resultSize = imageLabel.getPreferredSize(); // get size of image we set to JLabel
            imageLabel.setBounds(0, 0, resultSize.width, resultSize.height); // set bounds of JLabel

            doneLabel.setBounds(40, 50, 100, 30);
            contentPane.add(imageLabel);
            doneWindow.add(doneLabel);
            doneWindow.setSize(imageLabel.getWidth(), imageLabel.getHeight());
            doneWindow.setLayout(null);
            doneWindow.setVisible(true);

            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }
}