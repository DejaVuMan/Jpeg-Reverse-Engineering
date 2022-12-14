package JpegHelpers;

public class RootMeanSquareError {
    // TODO: Revamp RMSE Func to compare original values versus after encoding values
//    public double RootMeanSquareError(float[][] observed, int[] forecast){
//        // also do this for all 3 channels, avg those values and then do sqrt
//        // rmse can be summarized as sqrt(mean((forecast - observed)^2))
//        // observed is [8][8], forecast is [8*8] (so [64])
//        double addSubResults = 0.0;
//        for(int i = 0; i < 8; i++)
//        {
//            for(int j = 0; j < 8; j++)
//            {
//                //addSubResults += Math.pow((forecast[(8*i) + j] - observed[i][j]), 2);
//                addSubResults += Math.abs(forecast[(8*i) + j] - observed[i][j]);
//                // do this for whole image instead of on 8x8 blocks at a time
//            }
//        }
//        addSubResults = addSubResults / 64.0;
//        return addSubResults; // math.sqrt should be here
//    }
}
