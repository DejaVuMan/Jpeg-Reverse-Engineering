package JpegHelpers;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

public class JpegEncoder {

    private Map<Integer, HuffmanTable> huffmanTables; // <ht header, ht> DC Y, CbCr : 0, 1 AC Y, CbCr : 16, 17
    private Map<Integer, int[]> quantizationTables;
    private int width;
    private int height;
    int Quality; // quality we want to encode JPEG into (0 to 100)


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

        // byte 15 tells us size of DIB header
        System.out.println("Header Size: " + byteArr[14]);

        // Go to Color Palette at 2Eh (4 byte size)
        // Go to Color Importance at 32h (4 bytes)

        // Start of Pixel Array to encode into JPEG

        // Create empty img obj we will write to

        // Write Header

        // do stuff and call on supporting funcs

        // End Marker
        //byte[] endOfImage = { (byte)0xFF, (byte)0xD9 };
    }

    //public void Compress()

    //public void WriteCompressedData(OutPutStream outputStream)

    //public void WriteHeaders(OutPutStream outputStream)
    //{
    // tempArr, SOI marker, WriteMarker(), JFIF Header Data,
    // Comment Header, DQT Header, SoF Header, DHT Header SoS Header
    //}

    //void WriteArray(byte[] data, OutPutStream outputStream)

    //void WriteMarker(byte[] data, OutPutStream outputStream)
}
