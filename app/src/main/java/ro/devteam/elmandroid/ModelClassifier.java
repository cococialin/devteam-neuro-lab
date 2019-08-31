package ro.devteam.elmandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ModelClassifier {

    public int id;
    public String name;
    public String timestamp;
    public Uri uri;

    public int batch_size;
    public int img_height;
    public int img_width;
    public int num_channel;
    public int num_classes;
    public int pixel_size;
    public int channel_bytes;
    public int img_rotation;
    public String img_flip;
    public int norm_min;
    public int norm_max;
    public int quantized;
    public int selected;

    private Database db;
    private String TAG = "-devteam-";
    private Context context;

    private Interpreter.Options options = new Interpreter.Options();
    private Interpreter mInterpreter;
    private ByteBuffer mImageData;
    private int[] mImagePixels = new int[img_height * img_width];
    private float[][] mResult = new float[1][num_classes];

    public ModelClassifier() {
    }

    public ModelClassifier(Context context) {
        this.context = context;
        db = new Database(context);

        ModelClassifier dbModel = db.getSelectedModel();
        if(dbModel.id > 0) {
            this.id = dbModel.id;
            this.name = dbModel.name;
            this.timestamp = dbModel.timestamp;
            this.uri = dbModel.uri;
            this.batch_size = dbModel.batch_size;
            this.img_height = dbModel.img_height;
            this.img_width = dbModel.img_width;
            this.num_channel = dbModel.num_channel;
            this.num_classes = dbModel.num_classes;
            this.pixel_size = dbModel.pixel_size;
            this.channel_bytes = dbModel.channel_bytes;
            this.img_rotation = dbModel.img_rotation;
            this.img_flip = dbModel.img_flip;
            this.norm_max = dbModel.norm_max;
            this.norm_min = dbModel.norm_min;
            this.quantized = dbModel.quantized;
            this.selected = dbModel.selected;

            this.load();
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean load() {
//        if(this.context == null) {
//            Log.d(TAG, "Error: Context not set for this model!");
//        }

        if(this.id > 0) {

            try {

                File file = new File(uri.getPath());

                mInterpreter = new Interpreter(file, options);
                mImageData = ByteBuffer.allocateDirect(channel_bytes * batch_size * img_height * img_width * num_channel);
                mImageData.order(ByteOrder.nativeOrder());

            } catch (Exception e) {
                Log.d(TAG, "Error: " + e.getMessage());
            }

            return true;
        } else Log.d(TAG, "Error: No model in the database!");

        return false;
    }

    public int classify(Bitmap bitmap) {

        convertBitmapToByteBuffer(bitmap);
        mInterpreter.run(mImageData, mResult);

        return argmax(mResult[0]);
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

        int pixel = 0;
        for (int i = 0; i < img_width; ++i) {
            for (int j = 0; j < img_height; ++j) {
                int value = mImagePixels[pixel++];

                mImageData.putFloat(normalizePixel(convertPixel(value), min, max));
            }
        }
    }

    private static float convertPixel(int color) {
        return (255 - (((color >> 16) & 0xFF) * 0.299f
                + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f)) / 255.0f;
    }

    private float normalizePixel(float pixel, int min, int max) {
        int newMin = -1;
        int newMax = 1;
        float norm = ((pixel-min)*((newMax-newMin)/(max-min))) + newMin;

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

    private static int argmax(float[] probs) {
        int maxIdx = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    private Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();

        matrix.postRotate(angle);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap flipXBitmap(Bitmap source) {
        Matrix matrix = new Matrix();
        float cx = source.getWidth() / 2;
        float cy = source.getHeight() / 2;

        matrix.postScale(-1, 1, cx, cy);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap flipYBitmap(Bitmap source) {
        Matrix matrix = new Matrix();
        float cx = source.getWidth() / 2;
        float cy = source.getHeight() / 2;

        matrix.postScale(1, -1, cx, cy);

        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }
}
