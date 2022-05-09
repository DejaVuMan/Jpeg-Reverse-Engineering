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
                    case 0xffdb -> { // Quantization Table
                        int length = jpegImageData[i + 2] << 8 | jpegImageData[i + 3];
                        decodeQuantizationTables(Arrays.copyOfRange(jpegImageData, i + 4, i + 2 + length));
                    }
                    case 0xffdd -> { // Define Restart Interval
                        int length = jpegImageData[i + 2] << 8 | jpegImageData[i + 3];
                        int[] arr = Arrays.copyOfRange(jpegImageData, i + 4, i + 2 + length);
                        restartInterval = Arrays.stream(arr).sum();
                    }
                    case 0xffc0 -> { // Start of Frame (Baseline)
                        int length = jpegImageData[i + 2] << 8 | jpegImageData[i + 3];
                        decodeStartOfFrame(Arrays.copyOfRange(jpegImageData, i + 4, i + 2 + length));
                        if(mode == -1) mode = 0;
                    }
                    case 0xffc2 -> { // Start of Frame (Progressive)
                        if(mode == -1) mode = 1;
                    }
                    case 0xffda -> { // Start of Scan
                        int length = jpegImageData[i + 2] << 8 | jpegImageData[i + 3];
                        decodeStartOfScan(
                                /*Arrays.copyOfRange(jpegImgData, i + 4, i + 2 + length),*/
                                Arrays.copyOfRange(jpegImageData, i + 2 + length, jpegImageData.length - 2));
                        // last 2 two bytes are 0xffd9 - EOI
                        break main; // all done!
                    }
                }
            }
        }
    }

    private void decodeHuffmanTables(int[] chunk){ // 00, 01, 10, 11 - 0, 1, 16, 17 - Y DC, CbCr DC, Y AC, CbCr AC
        int cd = chunk[0];

        /*
        A Chunk of data contains various bytes.
        The first byte, or "nibble", is the class
        The second nibble is the destination
        a 0 class means we want DC, a 1 class means we want AC
         */

        int[] length = Arrays.copyOfRange(chunk, 1, 17);
        int to = 17 + Arrays.stream(length).sum();
        int[] symbols = Arrays.copyOfRange(chunk, 17, to);

        HashMap<Integer, int[]> lookupTable = new HashMap<>(); // code lengths and symbols
        int si = 0;
        for(int i = 0; i < length.length; i++){
            int l = length[i];
            int[] symbolsOfLengthI = new int[l];
            for(int j = 0; j < l; j++){
                symbolsOfLengthI[j] = symbols[si];
                si++;
            }
            lookupTable.put(i + 1, symbolsOfLengthI);
        }
        huffmanTables.put(cd, new HuffmanTable(lookupTable));

        int[] newChunk = Arrays.copyOfRange(chunk, to, chunk.length);
        if(newChunk.length > 0) decodeHuffmanTables(newChunk);
    }

    private void decodeQuantizationTables(int[] chunk){
        int d = chunk[0];
        int[] table = Arrays.copyOfRange(chunk, 1, 65);
        quantizationTables.put(d, table);
        int[] newChunk = Arrays.copyOfRange(chunk, 65, chunk.length);
        if(newChunk.length > 0) decodeQuantizationTables(newChunk);
    }

}
