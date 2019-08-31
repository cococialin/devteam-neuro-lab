package ro.devteam.elmandroid;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.ojalgo.matrix.PrimitiveMatrix;
import org.ojalgo.matrix.store.PrimitiveDenseStore;
import org.ojalgo.matrix.store.SparseStore;
import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

public class OldDatabase {

    private String[] problems = {"iris", "optd64", "satimg", "h6", "h7", "mnist", "mnist_sfert", "mnist_small", "usps"};
    private String problem;
    public String TAG = "-devteam-";
    private Context context;

    public double[][] train_samples;
    public double[][] train_labels;

    public double[][] test_samples;
    public double[][] test_labels;

    int n;
    int m;
    SparseStore<Double> A;
    CsvContainer csv;

    private static final String MODEL_NAME = "2conv.tflite";
    private final Interpreter.Options options = new Interpreter.Options();
    private final float[][] mResult = new float[1][10];

    public OldDatabase(String problem, Context context) {
        this.context = context;
        this.problem = problem;
    }

    public void loadTrainData() {
        if(Arrays.asList(this.problems).contains(problem)) {

            this.train_samples = this.readCsvSamples(problem + "_train_samples.csv");
//            this.train_labels = this.readCsvLabels(problem + "_train_labels.csv");

        } else {
            Toast.makeText(this.context, "Problema nu exista in baza de date!", Toast.LENGTH_LONG).show();
        }
    }

    public void loadTestData() {
        if(Arrays.asList(this.problems).contains(problem)) {

            this.test_samples = this.readCsvSamples(problem + "_test_samples.csv");
            this.test_labels = this.readCsvLabels(problem + "_test_labels.csv");

        } else {
            Toast.makeText(this.context, "Problema nu exista in baza de date!", Toast.LENGTH_LONG).show();
        }
    }

    public CsvContainer readExternalCSV(Uri uri) throws IOException {

        Bitmap bitmap = Bitmap.createBitmap(28, 28, Bitmap.Config.ARGB_8888);
        int[] array = new int[784];

        try {
            InputStream csvStream = context.getContentResolver().openInputStream(uri);
            Reader csvStreamReader = new InputStreamReader(csvStream);
            CsvReader csvReader = new CsvReader();

            csv = csvReader.read(csvStreamReader);
        } catch (Exception e) {
            Log.d(TAG, "Error: " + e.getMessage());
        }

        return csv;
    }


    private double[][] readCsvSamples(String path) {
        AssetManager assetManager = this.context.getAssets();

        try {
            InputStream csvStream = assetManager.open(path);
            Reader csvStreamReader = new InputStreamReader(csvStream);
            CsvReader csvReader = new CsvReader();

            csv = csvReader.read(csvStreamReader);

            n = csv.getRowCount();
            m = csv.getRow(0).getFieldCount();

            Log.d("ELM", n + " " + m);

            A = SparseStore.PRIMITIVE.make(n, m);
            PrimitiveMatrix p;
            PrimitiveDenseStore pr;

//            new Thread(new Runnable(){
//                public void run() {
//
//                    for(int i = 0; i < n; i++) {
//                        csv.getRow(i).toString().split(" ")).forEach(s->res.add(Integer.parseInt(s))
//                        for(int j = 0; j < m; j++) {
//                            A.set(i,j, Double.parseDouble(csv.getRow(i).getField(j)));
//                            Log.d("ELM", i + " " + j);
//                        }
//                    }
//                }
//            }).start();



            Log.d("ELM", csv.getRow(0).toString());


        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return new double[0][0];
    }

    private double[][] readCsvLabels(String path) {
        AssetManager assetManager = this.context.getAssets();

        try {
            InputStream csvStream = assetManager.open(path);
            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
//            CSVReader csvReader = new CSVReader(csvStreamReader);
//
//            return doubleMatrix(csvReader.readAll());

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        return new double[0][0];
    }

    private double[][] doubleMatrix(List<String[]> listArray) {

        double[][] dblArray = new double[listArray.size()][listArray.get(0).length];

        for(int i = 0; i < listArray.size(); i++) {
            for(int j = 0; j < listArray.get(i).length ; j++) {
                dblArray[i][j] = Double.parseDouble(listArray.get(i)[j]);
            }
        }

        return dblArray;
    }

}
