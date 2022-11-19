package JpegHelpers;

import javax.swing.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;

public class Driver {
    private static String path = ""; // empty initial path to be changed on button press.
    public static void main(String[] args) {
//        String filePath = "bmp_1080.bmp";
//        String filePath2 = "bmp_400_jwork.jpg";
//        System.setProperty("org.graphstream.ui", "swing");
//        try {
//            Instant start = Instant.now();
//            new JpegEncoder().Encode(filePath); // this is jpeg encoder
//            Instant end = Instant.now();
//            System.out.println("Total Time elapsed: " + java.time.Duration.between(start, end).toMillis() + "ms");
//            //new JpegDecoder().decode(filePath2); // this is jpeg decoder
//        } catch(FileNotFoundException e) {
//            System.err.println("File not found! Make sure you're using a valid file.");
//        } catch(IOException e) {
//            System.err.println("An IO Exception occurred: " + e.getLocalizedMessage());
//        }
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


    mainWindow.add(encodeButton);
    mainWindow.add(filePathField);
    mainWindow.add(setPath);
    mainWindow.add(decodeButton);
    mainWindow.add(pathLabel);

    mainWindow.setSize(400,400);
    mainWindow.setLayout(null);
    mainWindow.setVisible(true);

    setPath.addActionListener(e -> {
        path = filePathField.getText();
        System.out.println("Path set to: " + path);
    });

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
            doneLabel.setBounds(40, 50, 100, 30);
            doneWindow.add(doneLabel);
            doneWindow.setSize(200, 200);
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
            doneLabel.setBounds(40, 50, 100, 30);
            doneWindow.add(doneLabel);
            doneWindow.setSize(200, 200);
            doneWindow.setLayout(null);
            doneWindow.setVisible(true);

            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });
    }

    // 2048x1536 -> 256 x 192 blocks -> Color Transform -> Apply DCT ->
    // Quantization (tables) -> Serialization (zig-zag)-> Vectoring(dpcm) -> Encoding (huffman)
    //Small_pict_test.JPG returns SMALLER resolution?
    //SPACE.JPG returns same image resolution
    //test.jpg returns same image resolution
    //jpeg444.jpg returns same image resolution
    //balloon.jpeg returns SMALLER image resolution
    //asianchildmoment.jpg returns same image resolution

}