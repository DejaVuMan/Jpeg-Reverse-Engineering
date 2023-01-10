package JpegHelpers;

import java.util.Arrays;

public class DCT {
    int blockSize = 8; // Generally, Discreet Cosine Transforms are done in blocks of 8x8 pixels
    int quality = 100; // default quality value - 1 = Highest Quality??????
    public Object[] quantizationValues = new Object[2]; // hold quantization tables for luminance and chrominance
    public Object[] divisorValues = new Object[2]; // hold divisors for luminance and chrominance
    // Theoretically we could change Object[] to Number[], but then we'd also need to type cast everything to Number

    // quantumLuminance and divisorLuminance are the default quantization table for luminance
    public int[] quantumLuminance= new int[blockSize * blockSize];
    public double[] divisorLuminance = new double[blockSize * blockSize];

    // quantumChrominance and divisorChrominance are the default quantization table for chrominance
    public int[] quantumChrominance= new int[blockSize * blockSize];
    public double[] divisorChrominance = new double[blockSize * blockSize];

    public void setQuality(int num) {
        quality = num;
    }

    // Construct new DCT object - initialize our cosine transform table used for computing DCT, as well as ZigZag table
    // Much of this is the same from DCT3, but because I forgot to do documentation when I was creating it I am
    // not sure how it works now lol

    public DCT() {
        Initialize();
    }

    private void Initialize() {
        double[] scaleFactor = {
                1.0, 1.387039845, 1.306562965, 1.175875602, 1.0, 0.785694958, 0.541196100, 0.275899379
        };
        int i;
        int j;
        int index;
        int temp;

        if (quality < 1) { // quality cannot be less than 1
            quality = 1;
        }
        if (quality > 100) {
            quality = 100;
        }

        // Luminance table - redefine so we dont have to enter each index value by hand
        quantumLuminance = new int[]{
                16, 11, 10, 16, 24, 40, 51, 61,
                12, 12, 14, 19, 26, 58, 60, 55,
                14, 13, 16, 24, 40, 57, 69, 56,
                14, 17, 22, 29, 51, 87, 80, 62,
                18, 22, 37, 56, 68, 109, 103, 77,
                24, 35, 55, 64, 81, 104, 113, 92,
                49, 64, 78, 87, 103, 121, 120, 101,
                72, 92, 95, 98, 112, 100, 103, 99
        };

        // jpeg divisors method - This is the AAN method
        for (j = 0; j < 64; j++) {
            temp = (quantumLuminance[j] * quality + 50) / 100;
            if (temp <= 0)
                temp = 1;
            if (temp > 255)
                temp = 255;
            quantumLuminance[j] = temp;
        }
        index = 0;
        for (i = 0; i < blockSize; i++) {
            for (j = 0; j < blockSize; j++) {
                divisorLuminance[index] = (1.0 / (quantumLuminance[index] * scaleFactor[i] * scaleFactor[j] * 8.0));
                index++;
            }
        }

        //Chrominance table
        quantumChrominance = new int[]{
                17, 18, 24, 47, 99, 99, 99, 99,
                18, 21, 26, 66, 99, 99, 99, 99,
                24, 26, 56, 99, 99, 99, 99, 99,
                47, 66, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99,
                99, 99, 99, 99, 99, 99, 99, 99
        };

        for (j = 0; j < 64; j++) {
            temp = (quantumChrominance[j] * quality + 50) / 100;
            if (temp <= 0)
                temp = 1;
            if (temp > 255)
                temp = 255;
            quantumChrominance[j] = temp;
        }
        index = 0;
        for (i = 0; i < blockSize; i++) {
            for (j = 0; j < blockSize; j++) {
                divisorChrominance[index] = 1.0 / (quantumChrominance[index] * scaleFactor[i] * scaleFactor[j] * 8.0);
                index++;
            }
        }

        // objects which hold the relevant arrays
        quantizationValues[0] = quantumLuminance;
        quantizationValues[1] = quantumChrominance;
        divisorValues[0] = divisorLuminance;
        // remove this for final version
        System.out.println(Arrays.deepToString(divisorValues));
        divisorValues[1] = divisorChrominance;
    }

    public double[][] ForwardDCT(float[][] input)
    {
        double[][] output = new double[blockSize][blockSize];
        double temp0, temp1, temp2, temp3, temp4, temp5, temp6, temp7, temp10, temp11, temp12, temp13;
        double z1, z2, z3, z4, z5, z11, z13; // if these were byte, we would lose a ton of precision
        int i, j;

        // subtract 128 from input values to get close to smaller values across the board for compression purposes
        for (i = 0; i < blockSize; i++)
        {
            for (j = 0; j < blockSize; j++)
            {
                output[i][j] = (input[i][j] - 128);
            }
        }

        for(i = 0; i < blockSize; i++)
        {
            // explicit typecasting here -> on operations, everything is promoted to at least an int before computation
            // See JLS 5.6.2 - Binary Numeric Promotion -> Typecasting back to byte is easiest way to avoid comp errors
            temp0 = (output[i][0] + output[i][7]);
            temp7 = (output[i][0] - output[i][7]);
            temp1 = (output[i][1] + output[i][6]);
            temp6 = (output[i][1] - output[i][6]);
            temp2 = (output[i][2] + output[i][5]);
            temp5 = (output[i][2] - output[i][5]);
            temp3 = (output[i][3] + output[i][4]);
            temp4 = (output[i][3] - output[i][4]);

            temp10 = (temp0 + temp3);
            temp13 = (temp0 - temp3);
            temp11 = (temp1 + temp2);
            temp12 = (temp1 - temp2);

            output[i][0] = (temp10 + temp11);
            output[i][4] = (temp10 - temp11);

            z1 = ((temp12 + temp13) * 0.707106781);
            output[i][2] = temp13 + z1;
            output[i][6] = temp13 - z1;

            temp10 = (temp4 + temp5);
            temp11 = (temp5 + temp6);
            temp12 = (temp6 + temp7);

            z5 = (temp10 - temp12) * 0.382683433;
            z2 = 0.541196100 * temp10 + z5;
            z4 = 1.306562965 * temp12 + z5;
            z3 = temp11 * 0.707106781;

            z11 = temp7 + z3;
            z13 = temp7 - z3;

            output[i][5] = z13 + z2;
            output[i][3] = z13 - z2;
            output[i][1] = z11 + z4;
            output[i][7] = z11 - z4;
        }

        for(i = 0; i < blockSize; i++)
        {
            temp0 = (output[0][i] + output[7][i]);
            temp7 = (output[0][i] - output[7][i]);
            temp1 = (output[1][i] + output[6][i]);
            temp6 = (output[1][i] - output[6][i]);
            temp2 = (output[2][i] + output[5][i]);
            temp5 = (output[2][i] - output[5][i]);
            temp3 = (output[3][i] + output[4][i]);
            temp4 = (output[3][i] - output[4][i]);

            temp10 = (temp0 + temp3);
            temp13 = (temp0 - temp3);
            temp11 = (temp1 + temp2);
            temp12 = (temp1 - temp2);

            output[0][i] = (temp10 + temp11);
            output[4][i] = (temp10 - temp11);

            z1 = (temp12 + temp13) * 0.707106781;
            output[2][i] = temp13 + z1;
            output[6][i] = temp13 - z1;

            temp10 = (temp4 + temp5);
            temp11 = (temp5 + temp6);
            temp12 = (temp6 + temp7);

            z5 = (temp10 - temp12) * 0.382683433;
            z2 = 0.541196100 * temp10 + z5;
            z4 = 1.306562965 * temp12 + z5;
            z3 = temp11 * 0.707106781;

            z11 = temp7 + z3;
            z13 = temp7 - z3;

            output[5][i] = z13 + z2;
            output[3][i] = z13 - z2;
            output[1][i] = z11 + z4;
            output[7][i] = z11 - z4;
        }
        return output; // TODO: Check if this can be optimized and simplified
    }

    public int[] QuantizeBlock(double[][] inputData, int code)
    {
        int[] output = new int[blockSize * blockSize];
        int i, j, index;
        index = 0;
        for (i = 0; i < blockSize; i++)
        {
            for (j = 0; j < blockSize; j++)
            {
                output[index] = (int)(Math.round(inputData[i][j] * (((double[]) (divisorValues[code]))[index])));
                index++;
            }
        }
        return output;
    }
}
