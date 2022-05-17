package JpegHelpers;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

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

    private int mode; // 0 for Baseline
    private int restartInterval;
    private int precision; // bit precision
    private int width;
    private int height;
    private int mcuWidth;
    private int mcuHeight;
    private int mcuHSF; // horizontal sample factor
    private int mcuVSF; // vertical sample factor
    private boolean color; // chroma components exist in jpeg

    void decode(String image) throws IOException {
        int[] jpegImageData;
        try (DataInputStream dataInputStream = new DataInputStream(new FileInputStream(image))) {
            List<Integer> dataList = new ArrayList<>();

            while (dataInputStream.available() > 0) {
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
        main:
        for (int i = 0; i < jpegImageData.length; i++) {
            System.out.println("Reached Element " + i + ". Value: " + jpegImageData[i]); // gets stuck on Value 255 After JPEG Sampling Factor ID? Element 1281
            if (jpegImageData[i] == 0xff) { // 0xff == 255
                int marker = jpegImageData[i] << 8 | jpegImageData[i + 1];
                switch (marker) { // marker value is 65476 AKA 0xFFC4
                    case 0xffe0 -> System.out.println("This file is in JFIF format");
                    case 0xffe1 -> System.out.println("This file is in EXIF format");
                    case 0xffc4 -> { // dht - 1281 enters here
                        int length = jpegImageData[i + 2] << 8 | jpegImageData[i + 3];
                        decodeHuffmanTables(Arrays.copyOfRange(jpegImageData, i + 4, i + 2 + length)); // put statement causes hang
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
                        if (mode == -1) mode = 0;
                    }
                    case 0xffc2 -> { // Start of Frame (Progressive)
                        if (mode == -1) mode = 1;
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

    private void decodeHuffmanTables(int[] chunk) { // 00, 01, 10, 11 - 0, 1, 16, 17 - Y DC, CbCr DC, Y AC, CbCr AC
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
        for (int i = 0; i < length.length; i++) {
            int l = length[i];
            int[] symbolsOfLengthI = new int[l];
            for (int j = 0; j < l; j++) {
                symbolsOfLengthI[j] = symbols[si];
                si++;
            }
            lookupTable.put(i + 1, symbolsOfLengthI);
        }
        huffmanTables.put(cd, new HuffmanTable(lookupTable));

        int[] newChunk = Arrays.copyOfRange(chunk, to, chunk.length);
        if (newChunk.length > 0) decodeHuffmanTables(newChunk);
    }

    private void decodeQuantizationTables(int[] chunk) {
        int d = chunk[0];
        int[] table = Arrays.copyOfRange(chunk, 1, 65);
        quantizationTables.put(d, table);
        int[] newChunk = Arrays.copyOfRange(chunk, 65, chunk.length);
        if (newChunk.length > 0) decodeQuantizationTables(newChunk);
    }

    private void decodeStartOfFrame(int[] chunk) {
        precision = chunk[0];

        height = chunk[1] << 8 | chunk[2];
        width = chunk[3] << 8 | chunk[4];
        int noc = chunk[5]; // 1 grey-scale, 3 colour
        color = noc == 3;

        // component sample factor stored relatively, so y component sample factor contains information about how
        // large mcu is.
        for (int i = 0; i < noc; i++) {
            int id = chunk[6 + (i * 3)]; // 1 = Y, 2 = Cb, 3 = Cr, 4 = I, 5 = Q
            int factor = chunk[7 + (i * 3)];
            if (id == 1) { // y component, check sample factor to determine mcu size
                mcuHSF = (factor >> 4); // first nibble (horizontal sample factor)
                mcuVSF = (factor & 0x0f); // second nibble (vertical sample factor)
                mcuWidth = 8 * mcuHSF;
                mcuHeight = 8 * mcuVSF;
                System.out.println("JPEG Sampling Factor -> " + mcuHSF + "x" + mcuVSF + (mcuHSF == 1 && mcuVSF == 1 ? " (No Subsampling)" : " (Chroma Subsampling)"));
            }
            // int table = chunk[8+(i*3)];
        }
    }

    private void decodeStartOfScan(/*int[] chunk, */int[] imgData) {
        if (mode != 0) {
            System.err.println("This decoder only supports baseline JPEG images.");
            return;
        }

        System.out.println("Decoding Scan Image Data...");

        List<Integer> imgDataList = new ArrayList<>(imgData.length);
        for (int b : imgData) imgDataList.add(b);

        // check for and remove stuffing byte and restart markers
        for (int i = 0; i < imgDataList.size(); i++) {
            if (imgDataList.get(i).equals(0xff)) {
                int nByte = imgDataList.get(i + 1);
                if (nByte == 0x00) // stuffing byte
                    imgDataList.remove(i + 1);
                if (nByte >= 0xd0 && nByte <= 0xd7) { // remove restart marker
                    imgDataList.remove(i); // remove 0xff
                    imgDataList.remove(i); // remove 0xdn
                }
            }
        }

        // convert back to int[]
        imgData = new int[imgDataList.size()];
        for (int i = 0; i < imgDataList.size(); i++) imgData[i] = imgDataList.get(i);

        // list of converted matrices to write to file
        List<int[][]> convertedMCUs = new ArrayList<>();

        // start decoding
        int restartCount = restartInterval; // for restart markers, interval obtained from DRI marker
        BitStream stream = new BitStream(imgData);
        int[] oldDCCoes = new int[]{0, 0, 0}; // Y, Cb, Cr

        // matrices
        List<int[][]> yMatrices;
        int[][] yMatrix;
        int[][] cbMatrix = null;
        int[][] crMatrix = null;

        outer:
        for (int i = 0; i < (int) Math.ceil(height / (float) mcuHeight); i++) { // cast to float to avoid rounding errors
            for (int j = 0; j < (int) Math.ceil(width / (float) mcuWidth); j++) {

                // mcu
                yMatrices = new ArrayList<>(); // 2x2 - y0 y1 y2 y3 | 2x1 - y0 y1 | 1x1 y0

                // loop to obtain all luminance (y) matrices, which is greater than 1 if there is chroma subsampling
                for (int k = 0; k < mcuVSF; k++) {
                    for (int l = 0; l < mcuHSF; l++) {
                        yMatrix = createMatrix(stream, 0, oldDCCoes, 0);
                        if (yMatrix == null) // end of bit stream
                            break outer;
                        else
                            yMatrices.add(yMatrix);
                    }
                }

                if (color) {
                    cbMatrix = createMatrix(stream, 1, oldDCCoes, 1);
                    crMatrix = createMatrix(stream, 1, oldDCCoes, 2);
                    if (cbMatrix == null || crMatrix == null) break outer; // end of bit stream
                }

                convertedMCUs.add(convertMCU(yMatrices,
                        cbMatrix,
                        crMatrix));

                if (restartInterval != 0) { // dri marker exists in image
                    if (--restartCount == 0) {
                        restartCount = restartInterval; // reset counter to interval

                        // reset DC coefficients
                        oldDCCoes[0] = 0;
                        oldDCCoes[1] = 0;
                        oldDCCoes[2] = 0;

                        stream.restart(); // set bit stream to start again on byte boundary
                    }
                }
            }
        }
        createDecodedBitMap(convertedMCUs);
    }

    private int[][] convertMCU(List<int[][]> yMatrices, int[][] cbMatrix, int[][] crMatrix) {
        // int values representing pixel colour or just luminance (greyscale image) in the sRGB ColorModel 0xAARRGGBB
        int[][] convertedMCU = new int[mcuHeight][mcuWidth];

        for (int r = 0; r < convertedMCU.length; r++) {
            for (int c = 0; c < convertedMCU[r].length; c++) {

                // luminance
                int yMatrixIndex = ((r / 8) * (mcuHSF)) + (c / 8);
                int[][] yMatrix = yMatrices.get(yMatrixIndex);
                int y = yMatrix[r % 8][c % 8];

                float[] channels; // rgb or just luminance for greyscale
                if (color) {
                    // chrominance
                    int cb = cbMatrix[r / mcuVSF][c / mcuHSF];
                    int cr = crMatrix[r / mcuVSF][c / mcuHSF];

                    channels = new float[]{
                            ((y + (1.402f * cr))), // red
                            ((y - (0.344f * cb) - (0.714f * cr))), // green
                            ((y + (1.772f * cb))) // blue
                    };
                } else {
                    channels = new float[]{y};
                }

                for (int chan = 0; chan < channels.length; chan++) {
                    channels[chan] += 128; // shift block

                    // clamp block
                    if (channels[chan] > 255) channels[chan] = 255;
                    if (channels[chan] < 0) channels[chan] = 0;
                }

                convertedMCU[r][c] = 0xff << 24 | (int) channels[0] << 16 | (int) channels[color ? 1 : 0] << 8 | (int) channels[color ? 2 : 0]; // 0xAARRGGBB
            }
        }
        return convertedMCU;
    }

    private void createDecodedBitMap(List<int[][]> rgbMCUs) {
        // prepare BufferedImage for writing blocks to
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // set buffered image pixel values for every matrix
        int blockCount = 0;
        for(int i = 0; i < (int)Math.ceil(height / (float)mcuHeight); i++) {
            for (int j = 0; j < (int)Math.ceil(width / (float)mcuWidth); j++) {
                for (int y = 0; y < mcuHeight; y++) { // mcu block
                    for (int x = 0; x < mcuWidth; x++) {
                        try {
                            img.setRGB((j * mcuWidth) + x, (i * mcuHeight) + y, rgbMCUs.get(blockCount)[y][x]);
                        } catch (ArrayIndexOutOfBoundsException ignored) {
                        } // extra part of partial mcu
                    }
                }
                blockCount++;
            }
        }

        // write bmp file
        try {
            ImageIO.write(img, "bmp", new File(fileName+".bmp"));
            System.out.println("Successful Write to File");
        } catch (IOException e) {
            System.err.println("Error Writing to BMP File. " + e.getLocalizedMessage());
        }
    }

    private int decodeComponent(int bits, int code) { // decodes to find signed value from bits
        float c = (float)Math.pow(2, code-1);
        return (int) (bits>=c?bits:bits-(c*2-1));
    }

    // key used for dc and ac huffman table and quantization table
    private int[][] createMatrix(BitStream stream, int key, int[] oldDCCodes, int oldDCCoIndex) {
        DCT3 inverseDCT = new DCT3(precision);

        int code = huffmanTables.get(key).getCode(stream);
        if(code == -1) return null; // end of bit stream
        int bits = stream.getNextBits(code);
        oldDCCodes[oldDCCoIndex] += decodeComponent(bits, code);
        // oldDCCo[oldDCCoIndex] is now new dc coefficient

        // set new dc value to old dc value multiplied by the first value in quantization table
        inverseDCT.setComponent(
                0,
                oldDCCodes[oldDCCoIndex] * quantizationTables.get(key)[0]);

        int index = 1;
        while(index < 64) {
            code = huffmanTables.get(key+16).getCode(stream);
            if(code == 0) {
                break; // end of block
            } else if(code == -1) {
                return null; // end of bit stream
            }

            // read first nibble of each code to find number of leading zeros
            int nib;
            if((nib = code >> 4) > 0) {
                index += nib;
                code &= 0x0f; // chop off preceding nibble
            }

            bits = stream.getNextBits(code);

            if(index < 64) { // if haven't reached end of mcu
                int acCo = decodeComponent(bits, code); // ac coefficient
                inverseDCT.setComponent(
                        index,
                        acCo * quantizationTables.get(key)[index]);
                index++;
            }
        }

        inverseDCT.zigzagRearrange();
        return inverseDCT.dct3();

    }
}