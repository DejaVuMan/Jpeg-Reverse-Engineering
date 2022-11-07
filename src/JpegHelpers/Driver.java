package JpegHelpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;

public class Driver {

    public static void main(String[] args) {
//        if(args.length != 1){
//            System.out.println("Usage: java jpegdecode <filename>");
//            return;
//        }

//        String fileExtension = args[0].substring(args[0].lastIndexOf('.')).toLowerCase();
//
//        if(!fileExtension.equals(".jpg") || !fileExtension.equals(".jpeg")){
//            System.out.println("File must be a .jpeg/.jpg file!");
//            return;
//        }

        String filePath = "bmp_400.bmp"; // "asianchildmoment.jpg"; //bmp_400.bmp
        String filePath2 = "SPACE.JPG";
        // 2048x1536 -> 256 x 192 blocks -> Color Transform -> Apply DCT ->
        // Quantization (tables) -> Serialization (zig-zag)-> Vectoring(dpcm) -> Encoding (huffman)
        //Small_pict_test.JPG returns SMALLER resolution?
        //SPACE.JPG returns same image resolution
        //test.jpg returns same image resolution
        //jpeg444.jpg returns same image resolution
        //balloon.jpeg returns SMALLER image resolution
        //asianchildmoment.jpg returns same image resolution
        System.setProperty("org.graphstream.ui", "swing");
        try {
//            Instant start = Instant.now();
//            new JpegEncoder().Encode(filePath); // this is jpeg encoder
//            Instant end = Instant.now();
//            System.out.println("Total Time elapsed: " + java.time.Duration.between(start, end).toMillis() + "ms");
            new JpegDecoder().decode(filePath2); // this is jpeg decoder
        } catch(FileNotFoundException e) {
            System.err.println("File not found! Make sure you're using a valid file.");
        } catch(IOException e) {
            System.err.println("An IO Exception occurred: " + e.getLocalizedMessage());
        }
    }
}