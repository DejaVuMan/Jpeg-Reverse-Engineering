package JpegHelpers;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

class JpegDecoder { // JPG and JPEG are the same thing :)

    /*
    0xFFD8 - Start of Image
    0xFFE0 - App specific JFIF
    0xFFE1 - EXIF
    0xFFDB - Define Quant Tables
    0xFFC0 - Frame Start
    0xFFDD - Define Restart Interval
    0xFFC4 - Define Huffman Tables
    0xFFDA - Start Scan
    0xFFD9 - End of Image
     */

    private String fileName;
    private Map<Integer, HuffmanTable> huffmanTables; // <ht header, ht> DC Y, CbCr : 0, 1 AC Y, CbCr : 16, 17
    private Map<Integer, int[]> quantizationTables;

    private int precision;
    private int height;
    private int width;
    private int mcuWidth;
    private int mcuHeight;
    private int mcuHSF; // Horizontal Sample Factor
    private int mcuVSF; // Vertical Sample Factor
    private boolean color; // chroma components in JPEG
    private int mode; // 0 for Baseline

    public void decode(String image) throws IOException {
        int[] jpegImageData;
        try(DataInputStream dataInputStream = new DataInputStream(new FileInputStream(image))){
            List<Integer> dataList = new ArrayList<>();

            while(dataInputStream.available() > 0 ){
                int uByte = dataInputStream.readUnsignedByte();
                dataList.add(uByte);
            }
            jpegImageData = dataList.stream().mapToInt(Integer::intValue).toArray();
        }

        // Init Values
        quantizationTables = new HashMap<>();
        huffmanTables = new HashMap<>();
        fileName = image.substring(0, image.lastIndexOf('.'));
        mode = -1; // Uninitialized Mode

        System.out.println("Reading Jpeg File: " + fileName);

        // Decoding
        main: for (int i = 0; i < jpegImageData.length; i++) {
            if(jpegImageData[i] == 0xFF){
                int marker = jpegImageData[i] << 8 | jpegImageData[i + 1];
                switch(marker) {
                    case 0xFFE0 -> System.out.println("This file is in JFIF format");
                    case 0xFFE1 -> System.out.println("This file is in EXIF format");
                    case 0xFFC4 -> {
                        int length = jpegImageData[i + 2] << 8 | jpegImageData[i + 3];
                        decodeHuffmanTables(Arrays.copyOfRange(jpegImageData, i + 4, i + 2 + length));
                    }
                    // more cases here
                }
            }
        }
    }

    private void decodeHuffmanTables(int[] chunk){
        int cd = chunk[0];
        // ...
    }


}
