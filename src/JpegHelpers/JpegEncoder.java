package JpegHelpers;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class JpegEncoder {

    HuffmanTableEncode huf;
    private int width;
    private int height;
    int quality = 100; // quality we want to encode JPEG into (0 to 100)
    int dataStartPoint;
    BufferedOutputStream outputStream;
    DCT dct = new DCT();

    public static int[] jpegNaturalOrder = { 0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5,
            12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36,
            29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62,
            63, }; // Inline with JPEGQ class standard which uses natural order

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
            int byteDataOffset = 0;
            for(int j = 0; j < width; j++){
                int colorData = imageBuff.getRGB(j, i);
                //System.out.println("Red Color of pixel " + j + ", " + i + " is: " + ((colorData & 0x00ff0000) >> 16));
                RGBToYCbCrConverter(YCbCr,byteDataOffset,i, colorData); // parse through image data top to bottom
                byteDataOffset+=3;
            }
        }
        System.out.println("Completed RGB to YCbCr conversion.");

        System.out.println("Creating Buffered output stream...");
        outputStream = new BufferedOutputStream(new FileOutputStream("test.jpg"));

        System.out.println("Preparing DCT...");
        dct.setQuality(quality);
        System.out.println("Initializing Huffman Table...");
        huf = new HuffmanTableEncode(width, height);

        WriteHeaders(outputStream);
        // Write Compressed data method
        // End Marker
        byte[] endOfImage = { (byte)0xFF, (byte)0xD9 };
        outputStream.write(endOfImage);
        try{
            outputStream.flush();
        } catch (IOException e){
            System.out.println("Error flushing output stream");
            System.out.println(e.getMessage());
        }
    }

    public void WriteHeaders(BufferedOutputStream output) {
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

        //DQT Header - Quantization Table
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
                dqtHeader[offset++] = (byte) tempArray[jpegNaturalOrder[j]];
                //implement array with nat. jpeg quantization order for 8x8 grid
            }
        }
        WriteArray(dqtHeader, output);

        //Start of Frame Header
        byte[] sofHeader = { (byte)0xFF, (byte)0xC0, (byte)0x00, (byte)11, (byte)8, // <- precision of img
                (byte)((height >> 8) & 0xFF), (byte)(height & 0xFF),
                (byte)((width >> 8) & 0xFF), (byte)(width & 0xFF),
                (byte)3, (byte)1, (byte)1 << 4 + 1, (byte)1 };
        //last 3 are Composition ID, H and V sampling factors, QT #
        WriteArray(sofHeader, output);

        //DHT Header - Huffman Table (Inspired by James R. Weeks and BioElectroMech's work)
        byte[] dht0;
        byte[] dht1;
        byte[] dht2;
        byte[] dht3;
        int bytes, temp, oldIndex, intermediateIndex;
        index = 4; // account for first 4 bytes written in DQT Header array
        oldIndex = 4;
        dht0 = new byte[17];
        dht3 = new byte[4];
        dht3[0] = (byte)0xFF;
        dht3[1] = (byte)0xC4;
        for(i = 0; i < 4; i++)
        {
            bytes = 0;
            dht0[index++ - oldIndex] = (byte) ((int[]) huf.bits.elementAt(i))[0];
            for(j = 1; j < 17; j++)
            {
                temp = ((int[]) huf.bits.elementAt(i))[j];
                dht0[index++ - oldIndex] = (byte) temp;
                bytes += temp;
            }

            intermediateIndex = index;
            dht1 = new byte[bytes];
            for(j = 0; j < bytes; j++)
            {
                dht1[index++ - intermediateIndex] = (byte) ((int[]) huf.val.elementAt(i))[j];
            }

            dht2 = new byte[index];
            java.lang.System.arraycopy(dht3, 0, dht2, 0, oldIndex);
            java.lang.System.arraycopy(dht0, 0, dht2, oldIndex, 17);
            java.lang.System.arraycopy(dht1, 0, dht2, oldIndex + 17, bytes);
            dht3 = dht2;
            oldIndex = index;
        }
        dht3[2] = (byte)(((index - 2) >> 8) & 0xFF);
        dht3[3] = (byte)((index - 2) & 0xFF);
        WriteArray(dht3, output);

        //Start of Scan Header
        byte[] sos = new byte[14];
        sos[0] = (byte)0xFF;
        sos[1] = (byte)0xDA;
        sos[2] = (byte)0x00;
        sos[3] = (byte)12;
        sos[4] = (byte)3; // number of components in img

        sos[5] = (byte)1; // ID of component
        sos[6] = 0; // dc, ac ids -> 0,1,1
        sos[7] = (byte)2; // ID of component
        sos[8] = (byte)((1 << 4) + 1);
        sos[9] = (byte)3; // ID of component
        sos[10] = (byte)((1 << 4) + 1);

        sos[11] = (byte)0; // Ss
        sos[12] = (byte)63; // Se
        //ah, al, 0, 0
        sos[13] = (byte)(0);
        WriteArray(sos, output);
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

    public void WriteCompressedData(BufferedOutputStream output, byte[][] yCbCrData)
    {
        int i, j, k, l, m, n;

        int comp, xPos, yPos, xBlockOffset, yBlockOffset;

        byte[][] yChannel = new byte[height][width];
        byte[][] cbChannel = new byte[height][width];
        byte[][] crChannel = new byte[height][width];

        for(i = 0; i < height; i++){ // put into separate channels for easier calculation during write time
            int threeChannelPosition = 0;
            for(j = 0; j < width; j++){
                yChannel[i][j] = yCbCrData[i][threeChannelPosition++]; // pos 0...3...6
                cbChannel[i][j] = yCbCrData[i][threeChannelPosition++]; // pos 1...4...7
                crChannel[i][j] = yCbCrData[i][threeChannelPosition++]; // pos 2...5...8
            }
        }

        float[][] dctArray0 = new float[8][8];
        double[][] dctArray1 = new double[8][8];
        int[] dctArray3 = new int[8*8];

        // start at upper left of image and work our way down in 8x8 chunks

        int[] lastDCValue = new int[3]; // number of components
        int minBlockWidth, minBlockHeight;
        minBlockWidth = ((width % 8 != 0) ? (int)(Math.floor(width / 8.0) + 1) * 8 : width);
        minBlockHeight = ((height % 8 != 0) ? (int)(Math.floor(height / 8.0) + 1) * 8 : height);

//        for(comp = 0; comp < 3; comp++)
//        {
//            minBlockWidth = Math.min(minBlockWidth, compInfo[comp].widthInBlocks * 8);
//        }

    }

}
