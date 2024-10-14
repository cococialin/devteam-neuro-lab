package ro.devteam.cnntest;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import ro.devteam.cnntest.tflite2.Classifier;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private static final int PICKFILE_RESULT_CODE = 1;
    private static final int PICKFILE2_RESULT_CODE = 2;
    private static final int PICKFILE3_RESULT_CODE = 3;
    private static final String TAG = "-devteam-";

    private Timings timing;

    private TextView log;

    public int batch_size = 1;
    public int img_height = 96;
    public int img_width = 96;
    public int num_channel = 3;
    public int num_classes = 7;
    public int pixel_size = 1;
    public int channel_bytes = 4;
    public int img_rotation = 0;
    public String img_flip = "--";
    //    public int norm_min = -1;
//    public int norm_max = 1;
    public int norm_min = -1;
    public int norm_max = 1;
    public int quantized = 0;
    public int selected = 1;

    private Handler handler;
    private GpuDelegate gpuDelegate = null;

    private TextView threadsTextView;
    private ImageView plusImageView, minusImageView;
    private Spinner deviceSpinner, flipSpinner;

    public Classifier.Device device = Classifier.Device.CPU;

    private int numThreads = -1;

    private static final String MODEL_NAME = "2conv.tflite";
    private Interpreter.Options options = new Interpreter.Options();
    private Interpreter mInterpreter;
    private ByteBuffer mImageData;
    private int[] mImagePixels = new int[img_height * img_width];
    private float[][] mResult = new float[1][num_classes];

    int[] array = new int[700000];

    Uri modelUri;

    ArrayList<String> labels, samples;
    ArrayList<Integer> labels2;

    String tempResult;

    int n;
    int positiv = 0;
    long timeCost2;
    float acc;
    Bitmap tempBitmap;
    Bitmap bitmap;
    Bitmap bitmap2;
    Bitmap bitmap3;
    int progress = 0;

    private static final float IMAGE_MEAN = 0f;
    private static final float IMAGE_STD = 255f;
//    private static final float IMAGE_MEAN = 127.5f;
//    private static final float IMAGE_STD = 127.5f;

    protected ImageView img;
    protected ImageView img2;
    protected ImageView img3;
    protected TextView textSample;
    protected EditText rotate_img;
    protected EditText classes;
    protected EditText test_folder;
    protected EditText text_img_width;
    protected EditText text_img_height;
    protected Button model;
    protected Button start;

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int PERMISSION_ALL = 1;
            String[] PERMISSIONS = {
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };

            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        timing = new Timings("ELM");
        timing.addWork("Load User Interface");

        img = (ImageView) findViewById(R.id.img);
        img2 = (ImageView) findViewById(R.id.img2);
        img3 = (ImageView) findViewById(R.id.img3);
        textSample = (TextView) findViewById(R.id.samples);
        rotate_img = (EditText) findViewById(R.id.rotate_img);
        classes = (EditText) findViewById(R.id.classes);
        test_folder = (EditText) findViewById(R.id.test_folder);
        text_img_width = (EditText) findViewById(R.id.img_width);
        text_img_height = (EditText) findViewById(R.id.img_height);
        model = (Button) findViewById(R.id.model);
        start = (Button) findViewById(R.id.start);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        log = (TextView) findViewById(R.id.log);
        log.append("\nLoad User Interface: "+ timing.get("Load User Interface") + "ms");

        try {
//

        } catch (Exception e) {
            log.append("\nError DL4J: "+ e.getMessage());
        }

        threadsTextView = findViewById(R.id.threads);
        plusImageView = findViewById(R.id.plus);
        minusImageView = findViewById(R.id.minus);
        deviceSpinner = findViewById(R.id.device_spinner);
        flipSpinner = findViewById(R.id.flip_img);

        deviceSpinner.setOnItemSelectedListener(this);
        flipSpinner.setOnItemSelectedListener(this);

        plusImageView.setOnClickListener(this);
        minusImageView.setOnClickListener(this);

        device = Classifier.Device.valueOf(deviceSpinner.getSelectedItem().toString());
        numThreads = Integer.parseInt(threadsTextView.getText().toString().trim());

        rotate_img.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!rotate_img.getText().toString().isEmpty()) {
                    img_rotation = Integer.parseInt(rotate_img.getText().toString());
                    log.append("\nRotated images: " + img_rotation + " degrees\n");
                }
            }
        });

        classes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!classes.getText().toString().isEmpty()) {
                    num_classes = Integer.parseInt(classes.getText().toString());
                    mResult = new float[1][num_classes];
                    log.append("\nClasses: " + num_classes + "\n");
                }
            }
        });

        model.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileintent = new Intent(Intent.ACTION_GET_CONTENT);
                fileintent.setType("*/*");
                try {
                    startActivityForResult(fileintent, PICKFILE3_RESULT_CODE);
                } catch (ActivityNotFoundException e) {
                    Log.e("tag", "No activity can handle picking a file. Showing alternatives.");
                }
            }
        });

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Thread thread = new Thread() {
                    @Override
                    public void run() {

                        try {

                            img_width = Integer.parseInt(text_img_width.getText().toString());
                            img_height = Integer.parseInt(text_img_height.getText().toString());

                            options = new Interpreter.Options();
                            switch (device) {
                                case NNAPI:
                                    options.setUseNNAPI(true);
                                    break;
                                case GPU:
                                    GpuDelegate gpuDelegate = new GpuDelegate();
                                    options.addDelegate(gpuDelegate);
                                    break;
                                case CPU:
                                    break;
                            }
                            options.setNumThreads(numThreads);
                            mInterpreter = new Interpreter(loadModel(modelUri), options);
                            mImageData = ByteBuffer.allocateDirect(channel_bytes * batch_size * img_height * img_width * num_channel);
                            mImageData.order(ByteOrder.nativeOrder());

                            String basePath = test_folder.getText().toString();
                            File files = new File(  basePath);

                            labels = new ArrayList<>();
                            samples = new ArrayList<>();
                            labels2 = new ArrayList<>();
                            if(files.exists()) {
                                for (File inFile : files.listFiles()) {
                                    if (inFile.isDirectory()) {
                                        labels.add(inFile.getName());
                                    }
                                }
                            } else {
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                log.append( "\nNo directory found at test folder path.");
                                            }
                                        });
                            }


                            Collections.sort(labels);

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            log.append( "\nLabels found: " + String.join(", ", labels));
                                        }
                                    });



//                            for(String label : labels) {
                            for(int g = 0; g < labels.size(); g++) {
                                files = new File(basePath + "/" + labels.get(g));
                                if(files.exists()) {
                                    for (File inFile : files.listFiles()) {
                                        if (inFile.getName().toLowerCase().endsWith("jpg") ||
                                                inFile.getName().toLowerCase().endsWith("png") ||
                                                inFile.getName().toLowerCase().endsWith("gif") ||
                                                inFile.getName().toLowerCase().endsWith("jpeg")) {
                                            samples.add(basePath + "/" + labels.get(g) + "/" + inFile.getName());
                                            labels2.add(g);
                                        }
                                    }
                                } else {
                                    runOnUiThread(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    log.append( "\nNo directory labels found at test folder path.");
                                                }
                                            });
                                }
                            }


                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            log.append( "\nTest images found: " + samples.size()  + "\n");
                                        }
                                    });




                            n = samples.size();
                            positiv = 0;
                            progress = 0;

                            long startTime2 = SystemClock.uptimeMillis();
                            for (int i = 0; i < n; i++) {


                                long startTime = SystemClock.uptimeMillis();

                                if(img_rotation > 0 || !img_flip.contentEquals("--")) {
                                    mImageData.rewind();

                                    tempBitmap = BitmapFactory.decodeFile(samples.get(i));

                                    if(img_rotation > 0)
                                        tempBitmap = rotateBitmap(tempBitmap, img_rotation);
                                    if(img_flip.contentEquals("X"))
                                        tempBitmap = flipXBitmap(tempBitmap);
                                    if(img_flip.contentEquals("Y"))
                                        tempBitmap = flipYBitmap(tempBitmap);

                                    tempBitmap.getPixels(array, 0, tempBitmap.getWidth(), 0, 0,
                                            tempBitmap.getWidth(), tempBitmap.getHeight());

                                    int min = min(array);
                                    int max = max(array);

                                    int pixel = 0;
                                    for (int k = 0; k < img_width; ++k) {
                                        for (int j = 0; j < img_height; ++j) {
                                            final int val = array[pixel++];
                                            mImageData.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                            mImageData.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                            mImageData.putFloat(((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                        }
                                    }


//                                    for (int j = 0; j < array.length; j++) {
//                                        int value = array[j];
//
//                                        mImageData.putFloat(normalizePixel(convertPixel(value), min, max, 0 , 1));
//                                    }



                                } else {
                                    mImageData.rewind();

                                    tempBitmap = BitmapFactory.decodeFile(samples.get(i));

                                    tempBitmap.getPixels(array, 0, tempBitmap.getWidth(), 0, 0,
                                            tempBitmap.getWidth(), tempBitmap.getHeight());

                                    int min = min(array);
                                    int max = max(array);

                                    int pixel = 0;
                                    for (int k = 0; k < img_width; ++k) {
                                        for (int j = 0; j < img_height; ++j) {
                                            final int val = array[pixel++];
                                            mImageData.putFloat((((val >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                            mImageData.putFloat((((val >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                            mImageData.putFloat(((val & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
                                        }
                                    }

//                                    for (int j = 0; j < array.length; j++) {
//                                        int value = array[j];
//
//                                        mImageData.putFloat(normalizePixel(convertPixel(value), min, max, 0 , 1));
//                                    }



                                }

                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                img.setImageBitmap(tempBitmap);

                                            }
                                        });

                                mInterpreter.run(mImageData, mResult);
                                long endTime = SystemClock.uptimeMillis();
                                if(argmax(mResult[0]) == labels2.get(i)) {
                                    positiv++;

                                    tempResult = "\n" + samples.get(i);
                                    tempResult += "\nResult: ";
                                    for (int d = 0; d < mResult[0].length; d++) {
                                        tempResult +=  mResult[0][d] + ",";
                                    }
                                    tempResult += " Label: " + labels2.get(i) + " - correct";
//                                    runOnUiThread(
//                                            new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    log.append(tempResult );
//                                                }
//                                            });

                                } else {
                                    tempResult = "\n" + samples.get(i);
                                    tempResult += "\nResult: ";
                                    for (int d = 0; d < mResult[0].length; d++) {
                                        tempResult +=  mResult[0][d] + ",";
                                    }
                                    tempResult += " Label: " + labels2.get(i);
//                                    runOnUiThread(
//                                            new Runnable() {
//                                                @Override
//                                                public void run() {
//                                                    log.append(tempResult );
//                                                }
//                                            });
                                }



                                progress++;
                                runOnUiThread(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                textSample.setText("Images processed: " + (progress * 100f / n) + "%");
                                            }
                                        });



                                long timeCost = endTime - startTime;


                            }
                            long endTime2 = SystemClock.uptimeMillis();
                            timeCost2 = endTime2 - startTime2;
                            acc = positiv * 100f / n;

                            close();

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (device) {
                                                case NNAPI:
                                                    log.append("\nClassification done on NNAPI");
                                                    break;
                                                case GPU:
                                                    log.append("\nClassification done on GPU");
                                                    break;
                                                case CPU:
                                                    log.append("\nClassification done on CPU with " + numThreads + " threads");
                                                    break;
                                            }

                                            log.append("\nClassification end for " + n +" samples \n time:"+ timeCost2 / 1000f + "s and acc.: " + acc + "%");
                                            log.append("\nMatched samples with labels: " + positiv +"/"+n +"\n");

                                        }
                                    });

                        } catch (Exception e) {
                            e.printStackTrace();

                            runOnUiThread(
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            textSample.setText("Error: " + e.getMessage());
                                        }
                                    });
                        }
                        return;
                    }
                };
                thread.start();

            }
        });

    }


    private void classify() {


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Fix no activity available
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {

//                    String FilePath = data.getData().getPath();
//                    OldDatabase db = new OldDatabase("mnist", this);
//
//                    try {
//                        samples = db.readExternalCSV(data.getData());
//                        log.append("\nLoaded samples");
//
//                        Thread thread = new Thread() {
//                            @Override
//                            public void run() {
//
////                                        n = samples.getRow(0).getFieldCount();
//                                        n = 3;
//                                        for (int i = 0; i < n; i++) {
//
//                                            int p = 0;
//                                            for (CsvRow row : samples.getRows()) {
//                                                array[p++] = Math.round(Float.parseFloat(row.getField(i)));
//                                            }
//
//                                            int min = min(array);
//                                            int max = max(array);
//
//                                            for (int j = 0; j < array.length; j++) {
//                                                array[j] = Color.rgb(Math.round(normalizePixel(array[j], min, max)),0,0);
//                                            }
//
//                                            if(i == 0) {
//
//                                                bitmap = Bitmap.createBitmap(img_width, img_height, Bitmap.Config.ARGB_8888);
//                                                bitmap.setPixels(array, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//                                                bitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, false);
//
//                                            } else if(i == 1) {
//                                                bitmap2 = Bitmap.createBitmap(img_width, img_height, Bitmap.Config.ARGB_8888);
//                                                bitmap2.setPixels(array, 0, bitmap2.getWidth(), 0, 0, bitmap2.getWidth(), bitmap2.getHeight());
//                                                bitmap2 = Bitmap.createScaledBitmap(bitmap2, 200, 200, false);
//                                            } else if(i == 2) {
//                                                bitmap3 = Bitmap.createBitmap(img_width, img_height, Bitmap.Config.ARGB_8888);
//                                                bitmap3.setPixels(array, 0, bitmap3.getWidth(), 0, 0, bitmap3.getWidth(), bitmap3.getHeight());
//                                                bitmap3 = Bitmap.createScaledBitmap(bitmap3, 200, 200, false);
//                                            }
//
//
//                                            runOnUiThread(
//                                                    new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            if(img.getDrawable() == null)
//                                                                img.setImageBitmap(bitmap);
//                                                            else if(img2.getDrawable() == null)
//                                                                img2.setImageBitmap(bitmap2);
//                                                            else if(img3.getDrawable() == null)
//                                                                img3.setImageBitmap(bitmap3);
//
//                                                        }
//                                                    });
//                                        }
//
//
//                                return;
//                            }
//                        };
//                        thread.start();
//
//
//                    } catch (Exception e) {
//                        e.getStackTrace();
//                    }
//
                }
                break;

            case PICKFILE2_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    String FilePath = data.getData().getPath();
//                    OldDatabase db = new OldDatabase("mnist", this);

//                    try {
//                        labels = db.readExternalCSV(data.getData());
//                        log.append("\nLoaded labels");
//                    } catch (Exception e) {
//                        e.getStackTrace();
//                    }

                }
                break;

            case PICKFILE3_RESULT_CODE:
                if (resultCode == RESULT_OK) {

                    try {
                        long startTime2 = SystemClock.uptimeMillis();
                        modelUri = data.getData();
                        Cursor returnCursor = getContentResolver().query(modelUri, null, null, null, null);
                        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();
                        String filename = returnCursor.getString(nameIndex);
                        long endTime2 = SystemClock.uptimeMillis();
                        long timeCost2 = endTime2 - startTime2;
                        log.append("\nLoaded classifier model: "+ filename);
                    } catch (Exception e) {
                        e.getStackTrace();
                    }

                }
                break;
        }
    }

    private float normalizePixel(float pixel, int min, int max) {
        int newMin = norm_min;
        int newMax = norm_max;
        float norm = ((pixel-min)*((newMax-newMin)/(max-min)))+newMin;

        norm = (float)Math.round(norm * 100000f) / 100000f;

        return norm;
    }


    private float normalizePixel(float pixel, int min, int max, int newMin, int newMax) {

        float norm = ((pixel-min)*((newMax-newMin)/(max-min)))+newMin;

        norm = (float)Math.round(norm * 100000f) / 100000f;

        return norm;
    }

    private int min(int[] imgPixels){
        int min = imgPixels[0];
        for (int p: imgPixels) {
            if(min > p)
                min = p;
        }
        return min;
    }

    private int max(int[] imgPixels){
        int max = imgPixels[0];
        for (int p: imgPixels) {
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

    private MappedByteBuffer loadModel(Uri uri) throws IOException {
        ParcelFileDescriptor fileDescriptor = this.getApplicationContext().getContentResolver().openFileDescriptor(uri, "r");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = 0;
        long declaredLength = fileDescriptor.getStatSize();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public ByteBuffer readBytes(InputStream inputStream) {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        try {

            // we need to know how may bytes were read to write them to the byteBuffer
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (Exception e) {
            e.getStackTrace();
        }

        byte[] byteArray = byteBuffer.toByteArray();

        Log.d(TAG, "Byte Array: " + byteArray.length );

        // and then we can return your byte array.
        return ByteBuffer.wrap(byteArray);
    }

    private MappedByteBuffer loadModelFile() throws IOException {
        AssetFileDescriptor fileDescriptor = this.getAssets().openFd(MODEL_NAME);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            Intent intent = new Intent(this, ClassifierActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_db_input) {


        } else if (id == R.id.nav_import) {

        } else if (id == R.id.nav_screen_input) {
            Intent intent = new Intent(this, ScreenInputActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_select_model) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted1");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked1");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted1");
            return true;
        }
    }

    public  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted2");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked2");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted2");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 2:
                Log.d(TAG, "External storage2");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission

                }else{

                }
                break;

            case 3:
                Log.d(TAG, "External storage1");
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
                    //resume tasks needing this permission

                }else{

                }
                break;
        }
    }

    protected synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }

    protected Classifier.Device getDevice() {
        return device;
    }

    private void setDevice(Classifier.Device device) {
        this.device = device;
//        if (this.device != device) {
//            runInBackground(() -> recreateClassifier(model, device, numThreads));
//        }

        boolean threadsEnabled = false;

        switch (device) {
            case NNAPI:
                log.append("\nModel will run on NNAPI\n");
                break;
            case GPU:
                log.append("\nModel will run on GPU\n");
                threadsEnabled = true;
                break;
            case CPU:
                log.append("\nModel will run on CPU\n");
                threadsEnabled = true;
                break;

        }


        plusImageView.setEnabled(threadsEnabled);
        minusImageView.setEnabled(threadsEnabled);
        threadsTextView.setText(threadsEnabled ? String.valueOf(numThreads) : "N/A");

    }

    protected int getNumThreads() {
        return numThreads;
    }

    private void setNumThreads(int numThreads) {
        this.numThreads = numThreads;

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.plus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads >= 9) return;
            setNumThreads(++numThreads);
            threadsTextView.setText(String.valueOf(numThreads));
        } else if (v.getId() == R.id.minus) {
            String threads = threadsTextView.getText().toString().trim();
            int numThreads = Integer.parseInt(threads);
            if (numThreads == 1) {
                return;
            }
            setNumThreads(--numThreads);
            threadsTextView.setText(String.valueOf(numThreads));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent == deviceSpinner) {
            setDevice(Classifier.Device.valueOf(parent.getItemAtPosition(pos).toString()));
        } else if(parent == flipSpinner) {
            img_flip = parent.getItemAtPosition(pos).toString();
            log.append("\nFliped images: " + img_flip + "\n");

//             if (img_flip.contentEquals("X")) {
//                 bitmap = flipXBitmap(bitmap);
//                 bitmap2 = flipXBitmap(bitmap2);
//                 bitmap3 = flipXBitmap(bitmap3);
//             } else if (img_flip.contentEquals("Y")) {
//                 bitmap = flipYBitmap(bitmap);
//                 bitmap2 = flipYBitmap(bitmap2);
//                 bitmap3 = flipYBitmap(bitmap3);
//             }
//
//             if(img.getDrawable() != null) {
//                 img.setImageBitmap(bitmap);
//             }
//             if(img2.getDrawable() != null){
//                 img2.setImageBitmap(bitmap2);
//             }
//             if(img3.getDrawable() != null){
//                 img3.setImageBitmap(bitmap3);
//             }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing.
    }

    public void close() {
        if (mInterpreter != null) {
            mInterpreter.close();
            mInterpreter = null;
        }
        if (gpuDelegate != null) {
            gpuDelegate.close();
            gpuDelegate = null;
        }
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

    private static float convertPixel(int color) {
        return (255 - (((color >> 16) & 0xFF) * 0.299f
                + ((color >> 8) & 0xFF) * 0.587f
                + (color & 0xFF) * 0.114f)) / 255.0f;

    }

}
