package JpegHelpers;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class JpegEncoder {

//    private Map<Integer, HuffmanTable> huffmanTables; // <ht header, ht> DC Y, CbCr : 0, 1 AC Y, CbCr : 16, 17
//    private Map<Integer, int[]> quantizationTables;
    private int width;
    private int height;
    int quality = 100; // quality we want to encode JPEG into (0 to 100)
    int dataStartPoint;
    BufferedOutputStream outputStream;
    DCT dct = new DCT();

    public static int[] jpegNaturalOrder = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5,
            12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36,
            29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62,
            63, }; // Inline with JPEGQ class standard which uses natural order instead of zig-zag

    public int GetQuality(){
        return quality;
    }

    void Encode(String image) throws IOException{
        BufferedImage imageBuff = ImageIO.read(new File(image));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(imageBuff, "bmp", baos);
        byte[] byteArr = baos.toByteArray();
        System.out.println("Raw BMP data written to byte array");
        // First 14 bytes are bitmap file header
        // Following is DIB header, EBMs, Color Table, Gap1
        // Byte 70+ appears to be real img data

        System.out.println("BMP File Encoding: " + (char)byteArr[0] + (char)byteArr[1]);
        StringBuilder sb = new StringBuilder();
        sb.append((char)byteArr[0]);
        sb.append((char)byteArr[1]);

        if(!sb.toString().equals("BM")){
            System.out.println("Not a valid BMP file!");
            return;
        }

        // Bytes 3 to 6 tell us file size in little endian format
        // If in hex editor we see: 36 0C 00 00, this is really 00 00 0C 36 -> 3126 -> 3126 Bytes -> 3.05KB

        System.out.println("File Size in Bytes: " + HexCalculator(5, 4, byteArr));

        dataStartPoint = HexCalculator(13,4,byteArr);
        System.out.println("Bitmap Data starting byte: " + dataStartPoint);

        // byte 15-18 tells us size of DIB header
        System.out.println("DIB Header Size: " + HexCalculator(17,4,byteArr));
        width = HexCalculator(21,4,byteArr); // Left to Right
        System.out.println("Image Width: " + width); // Bottom to Top
        height = HexCalculator(25,4,byteArr);
        System.out.println("Image Height: " + height);
        System.out.println("Color Plane count: " + HexCalculator(27,2,byteArr));
        System.out.println("Bits per pixel: " + HexCalculator(29,2,byteArr));
        System.out.println("Raw Bitmap data size in bytes with padding: " + HexCalculator(37,4,byteArr));

        // Color Palette at 2Eh (4 bytes)
        System.out.println("Colors in palette: " + HexCalculator(49,4,byteArr));
        //Color Importance at 32h (4 bytes)
        System.out.println("Important colors: " + HexCalculator(53,4,byteArr));

        System.out.println("Based on a width of " + width + ", there should be " + width%4 + " bytes of padding");

        byteArr = null;

        System.out.println("Preparing YCbCr Array to write to...");
        byte[][] YCbCr = new byte[height][width*3]; // First pixel would be values YCbCr[0,1,2], etc.

        boolean firstEntered = false;
        System.out.println("Converting RGB data to YCbCr...");
        for(int i = 0; i < height; i++){
            int channelCounter = 0;
            for(int j = 0; j < width; j++){
                int colorData = imageBuff.getRGB(j, i);
                //System.out.println("Red Color of pixel " + j + ", " + i + " is: " + ((colorData & 0x00ff0000) >> 16));
                RGBToYCbCrConverter(YCbCr,channelCounter,i, colorData); // parse through image data top to bottom
                channelCounter+=3;
            }
        }
        System.out.println("Completed RGB to YCbCr conversion.");

        System.out.println("Creating Buffered output stream...");
        outputStream = new BufferedOutputStream(new FileOutputStream("test.jpg"));

        System.out.println("Preparing DCT...");
        dct.setQuality(quality);

        WriteHeaders(outputStream);

        // Write Header etc

        // do stuff and call on supporting funcs

        // End Marker
        //byte[] endOfImage = { (byte)0xFF, (byte)0xD9 };
    }

    public void WriteHeaders(BufferedOutputStream output)
    {
        int i, j, index, offset, length;
        int[] tempArray;
        //Start of Image Marker
        byte[] startOfImage = { (byte)0xFF, (byte)0xD8 };
        WriteMarker(startOfImage, output);

        //JFIF Header
        byte[] jfifHeader = { (byte)0xFF, (byte)0xE0, (byte)0x00, (byte)0x10, (byte)0x4A, (byte)0x46,
                (byte)0x49, (byte)0x46, (byte)0x00, (byte)0x01, (byte)0x01, (byte)0x01,
                (byte)0x00, (byte)0x60, (byte)0x00, (byte)0x60, (byte)0x00, (byte)0x00 };
        WriteMarker(jfifHeader, output);

        //Comment Header
        String comment = "JPEG Encoding Test 2022";
        byte[] commentHeader = new byte[comment.length() + 4];
        commentHeader[0] = (byte)0xFF;
        commentHeader[1] = (byte)0xFE;
        commentHeader[2] = (byte)((comment.length() >> 8) & 0xFF); // accommodate very long comments
        commentHeader[3] = (byte)(comment.length() & 0xFF);
        System.arraycopy(comment.getBytes(), 0, commentHeader, 4, comment.length());
        WriteArray(commentHeader, output);

        //DQT Header
        byte[] dqtHeader = new byte[134];
        dqtHeader[0] = (byte)0xFF;
        dqtHeader[1] = (byte)0xDB;
        dqtHeader[2] = (byte)0x00;
        dqtHeader[3] = (byte)0x84;
        offset = 4; // account for first 4 bytes written in dqtheader array
        for(i = 0; i < 2; i++)
        {
            dqtHeader[offset++] = (byte) i; //((0 << 4) + i)
            tempArray = (int[]) dct.quantizationValues[i];
            for(j = 0; j < 64; j++)
            {
                dqtHeader[offset++] = (byte)(jpegNaturalOrder[j]); //implement array with nat. jpeg quantization order for 8x8 grid
            }
        }
        WriteArray(dqtHeader, output);

        //Start of Frame Header
        byte[] sofHeader = { (byte)0xFF, (byte)0xC0, (byte)0x00, (byte)17, (byte)8, // <- precision of img
                (byte)((height >> 8) & 0xFF), (byte)(height & 0xFF),
                (byte)((width >> 8) & 0xFF), (byte)(width & 0xFF),
                (byte)3, (byte)1, (byte)1 << 4 + 1, (byte)1};
        //last 3 are Composition ID, H and V sampling factors, QT #
        WriteArray(sofHeader, output);

        //DHT Header

        //Start of Scan Header
        //output.close();
    }

    void WriteMarker(byte[] marker, BufferedOutputStream output) {
        try{
            output.write(marker,0,2);
        } catch(IOException e){
            System.out.println("Error writing marker to output stream!");
        }
    }

    void WriteArray(byte[] array, BufferedOutputStream output) {
        try{
            int length = ((array[2] & 0xFF) << 8) + (array[3] & 0xFF) + 2;
            output.write(array,0,length);
        } catch(IOException e){
            System.out.println("Error writing array to output stream!");
        }
    }

    public int HexCalculator(int start, int length, byte[] toParse)
    {
        StringBuilder buffer = new StringBuilder(); // Hold our computation of hex
        for(int i = start; i > start-length; i--){ // For Little Endian - LE is actually just a manga, "Right to Left"
            buffer.append(Character.forDigit((toParse[i] >> 4) & 0xF, 16));
            buffer.append(Character.forDigit((toParse[i] & 0xF), 16));
        }
        return(Integer.parseInt(buffer.toString(), 16));
    }

    public void RGBToYCbCrConverter(byte[][] yCbCrData, int x, int yCord, int colorData)
    { // use BufferedImage getRGB here
        // color space conversion begin from 0 to 255 -> -128 to 128
        int y = 16 + ((int) (65.738*((colorData & 0xff0000) >> 16) + 129.057*((colorData & 0xff00) >> 8) + 25.064*(colorData & 0xff))) >> 8;
        int cb = 128 + ((int) (-37.945*((colorData & 0xff0000) >> 16) - 74.494*((colorData & 0xff00) >> 8) + 112.439*(colorData & 0xff))) >> 8;
        int cr = 128 + ((int) (112.439*((colorData & 0xff0000) >> 16) - 94.154*((colorData & 0xff00) >> 8) - 18.285*(colorData & 0xff))) >> 8;

        if(y > 255) {
            yCbCrData[yCord][x] = (byte)255; // row column order for arrays in java
        } else if(y < 0) {
            yCbCrData[yCord][x] = (byte)0;
        } else {
            yCbCrData[yCord][x] = (byte)y;
        }

        if(cb > 255) {
            yCbCrData[yCord][1 + x] = (byte)255;
        } else if(cb < 0) {
            yCbCrData[yCord][1 + x] = (byte)0;
        } else {
            yCbCrData[yCord][1 + x] = (byte)cb;
        }

        if(cr > 255) {
            yCbCrData[yCord][2 + x] = (byte)255;
        } else if(cr < 0) {
            yCbCrData[yCord][2 + x]= (byte)0;
        } else {
            yCbCrData[yCord][2 + x] = (byte)cr;
        }
        System.out.println("parsed coordinates: " + x + " " + yCord);
    }

    //public void Compress()

    //public void WriteCompressedData(OutPutStream outputStream)

    //public void WriteHeaders(OutPutStream outputStream)
    //{
    // tempArr, SOI marker, WriteMarker(), JFIF Header Data,
    // Comment Header, DQT Header, SoF Header, DHT Header SoS Header
    //}
}
