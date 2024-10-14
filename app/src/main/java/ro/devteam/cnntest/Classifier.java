package ro.devteam.cnntest;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class Classifier {

    private static final String LOG_TAG = Classifier.class.getSimpleName();

    private static final String MODEL_NAME = "2conv.tflite";

    private static final int BATCH_SIZE = 1;
    public static final int IMG_HEIGHT = 28;
    public static final int IMG_WIDTH = 28;
    private static final int NUM_CHANNEL = 1;
    private static final int NUM_CLASSES = 10;

    public String TAG = "devteam";

    private final Interpreter.Options options = new Interpreter.Options();
    private final Interpreter mInterpreter;
    private final ByteBuffer mImageData;
    private final int[] mImagePixels = new int[IMG_HEIGHT * IMG_WIDTH];
    private final float[][] mResult = new float[1][NUM_CLASSES];

    public Classifier(Activity activity) throws IOException {
        mInterpreter = new Interpreter(loadModelFile(activity), options);
        mImageData = ByteBuffer.allocateDirect(
                4 * BATCH_SIZE * IMG_HEIGHT * IMG_WIDTH * NUM_CHANNEL);
        mImageData.order(ByteOrder.nativeOrder());
    }

    public Result classify(Bitmap bitmap) {
        convertBitmapToByteBuffer(bitmap);
        long startTime = System.nanoTime();
        mInterpreter.run(mImageData, mResult);
        long endTime =  System.nanoTime();
        long timeCost = (endTime - startTime);
        Log.d("-"+TAG+"-", "classify(): result = " + Arrays.toString(mResult[0])
                + ", timeCost = " + timeCost);
        return new Result(mResult[0], timeCost);
    }

    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (mImageData == null) {
            return;
        }
        mImageData.rewind();

        bitmap.getPixels(mImagePixels, 0, bitmap.getWidth(), 0, 0,
                bitmap.getWidth(), bitmap.getHeight());

        int min = this.min(mImagePixels);
        int max = this.max(mImagePixels);
        Log.d("-"+TAG+"-", "Image size: " + mImagePixels.length + " Min: " + min + " | Max: " + max);

        int pixel = 0;
        for (int i = 0; i < IMG_WIDTH; ++i) {
            for (int j = 0; j < IMG_HEIGHT; ++j) {
                int value = mImagePixels[pixel++];
                Log.d("-"+TAG+"-", "Float value: " + convertPixel(value) + " | Normalized value: " + normalizePixel(convertPixel(value), min, max));

                mImageData.putFloat(normalizePixel(convertPixel(value), min, max));
            }
        }
//        Log.d("-"+TAG+"-", "Pixels processed: " + pixel);
    }

    private static float convertPixel(int color) {
        return (255 - (((color >> 16) & 0xFF) * 0.299f
                + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f)) / 255.0f;

    }

    private float normalizePixel(float pixel, int min, int max) {
        int newMin = -1;
        int newMax = 1;

        float norm = ((pixel-min)*(newMax-newMin)/(max-min))+newMin;

        norm = (float)Math.round(norm * 100000f) / 100000f;

        return norm;
//        return (float) (norm > 0 ? newMax : newMin);
    }

    private int min(int[] imgPixels){
        int min = (int) convertPixel(imgPixels[0]);
        for (int p: imgPixels) {
            p = (int) convertPixel(p);
            if(min > p)
                min = p;
        }
        return min;
    }

    private int max(int[] imgPixels){
        int max = (int) convertPixel(imgPixels[0]);
        for (int p: imgPixels) {
            p = (int) convertPixel(p);
            if(max < p)
                max = p;
        }
        return max;
    }
}
