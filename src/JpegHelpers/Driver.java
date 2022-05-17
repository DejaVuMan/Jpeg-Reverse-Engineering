package JpegHelpers;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Driver {

    public static void main(String[] args){
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

        String filePath = "test.JPG";

        try{
            new JpegDecoder().decode(filePath); // args[0]
        } catch(FileNotFoundException e){
            System.err.println("File not found! Make sure you're using a valid file.");
        } catch(IOException e){
            System.err.println("An IO Exception occurred: " + e.getLocalizedMessage());
        }
    }
}