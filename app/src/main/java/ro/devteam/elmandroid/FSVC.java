package ro.devteam.elmandroid;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.util.TimingLogger;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FSVC {

//    private Context context;
//    private String[] problems = {"iris", "optd64", "satimg", "h6", "h7", "mnist", "mnist_sfert", "mnist_small", "usps"};
//
//    public MatOfFloat trainLabels;
//    public MatOfFloat trainLabelsOrig;
//    public MatOfFloat trainSamples;
//    public MatOfFloat testLabels;
//    public MatOfFloat testLabelsOrig;
//    public MatOfFloat testSamples;
//
//    public int nOut;       //nr. iesiri
//    public int epTrLength; //lungime epoca de antrenare
//    public int epTsLength; //lungime epoca de test
//    public int classNr;
//
//    public MatOfFloat centers;
//    public MatOfFloat weights;
//    public MatOfFloat bestWeights;
//
//    public double prag, raza, eta;
//    public String tip_r, dist_type;
//    public int epoci;
//
//    public FSVC(Context context, String problem) {
//        this.context = context;
//
//        if(Arrays.asList(this.problems).contains(problem)) {
//            try {
//
//                Timewatch timer = new Timewatch();
//
//                // Incarcare baze de date
//                this.trainLabels = this.readCsv(problem + "_train_labels.csv", "labels");
//                this.trainLabelsOrig = this.readCsv(problem + "_train_labels.csv", "");
//                this.trainSamples = this.readCsv(problem + "_train_samples.csv", "samples");
//                this.testLabels = this.readCsv(problem + "_test_labels.csv", "labels");
//                this.testLabelsOrig = this.readCsv(problem + "_test_labels.csv", "");
//                this.testSamples = this.readCsv(problem + "_test_samples.csv", "samples");
//
//                // Initializare parametrii
//                this.nOut = (int) this.trainLabels.rows();
//                this.epTrLength = (int) this.trainSamples.cols();
//                this.epTsLength = (int) this.testSamples.cols();
//                this.classNr = (int) this.getMax(this.trainLabelsOrig);
//
//                this.centers = new MatOfFloat();
//                this.weights = new MatOfFloat(Mat.zeros(1, this.nOut, CV_32FC1));
//                this.bestWeights = new MatOfFloat();
//
//                this.prag = 1.2;
//                this.raza = 0.23;
//                this.eta = 0;
//                this.tip_r = "rbf_gus";
//                this.dist_type = "eucl";
//                this.epoci = 2;
//
//                this.calcRbfUnits();
//
//                timer.stop();
//
//            } catch (Exception e) {
//                Toast.makeText(this.context, e.getStackTrace()[0].getLineNumber()+": "+e.getMessage(), Toast.LENGTH_LONG).show();
//                e.printStackTrace();
//            }
//        } else {
//            Toast.makeText(this.context, "Problema incorecta!", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    public void calcRbfUnits() {
//        try {
//            MatrixClass util = new MatrixClass();
//
//            MatOfFloat in_rbf = new MatOfFloat();
//            MatOfFloat Y = new MatOfFloat();
//            RBF_Unit rbf_unit;
//
//            TimingLogger timings = new TimingLogger("FSVC", "Timpi de executie");
//            Timewatch timer = new Timewatch();
////          Determinare centrii
//
////            for(int j = 1; j <= this.classNr; j++) {
//            for (int i = 0; i < this.epTrLength; i++) {
////                    if(j == this.trainLabelsOrig.get(0,i)[0]) {
//
//                Core.transpose(this.trainSamples.col(i), in_rbf);
//                rbf_unit = new RBF_Unit(this.tip_r, this.centers, in_rbf, this.raza, this.dist_type);
//
//                if (rbf_unit.activity < this.prag) {
//                    //Adauga centru nou si initializeaza cu 1 ponderea din clasa X
//                    this.centers.push_back(in_rbf);
//                    this.weights.push_back(Mat.zeros(1, this.nOut, CV_32FC1));
//                    this.weights.put(this.weights.rows() - 1, (int) this.trainLabelsOrig.get(0, i)[0] - 1, 1);
//
//                    if (this.eta > 0) {
//                        List<Mat> mat_list = new ArrayList<>();
//                        mat_list.add(rbf_unit.exit_rbf);
//                        mat_list.add(Mat.ones(1, 1, CV_32FC1));
//                        Core.hconcat(mat_list, rbf_unit.exit_rbf);
//                    }
//                }
//
//                if (this.eta > 0) {
//                    // Antrenare LMS daca rata de antrenare este mai mare ca 0
//                    Core.transpose(util.mult(rbf_unit.exit_rbf, this.weights), Y);
//                    MatOfFloat dW = new MatOfFloat();
//                    Core.subtract(this.trainLabels.col(i), Y, dW);
//                    dW = util.multScalar(dW, this.eta);
//                    dW = util.mult(dW, rbf_unit.exit_rbf);
//                    Core.transpose(dW, dW);
//                    Core.add(this.weights, dW, this.weights);
//                }
//
////                    }
//            }
////            }
//            timings.addSplit("Determinare centrii");
//
//            Log.d("FSVC", " ");
//            Log.d("FSVC", "-------------------------------------------------");
//            Log.d("FSVC", "Finalizarea determinarii numarului de centri dupa " + timer.getDuration() + " secunde");
//            Log.d("FSVC", "Prag: "+this.prag);
//            Log.d("FSVC", "Raza neuron RBF: "+this.raza);
//            Log.d("FSVC", "Rata de antrenare LMS: "+this.eta);
//            Log.d("FSVC", "Tipul RBF: "+this.tip_r);
//            Log.d("FSVC", "Tipul distantei: "+this.dist_type);
//            Log.d("FSVC", "Numarul de neuroni: "+this.centers.rows());
//            Log.d("FSVC", "-------------------------------------------------");
//            Log.d("FSVC", " ");
//
//            float acc_best = 0;
//            int ep_best = 0;
//
//            MatOfFloat conf_best = new MatOfFloat();
//            Mat.zeros(this.nOut, this.nOut, CV_32FC1).copyTo(conf_best);
//
//            Mat.zeros(this.nOut, this.epTrLength, CV_32FC1).copyTo(Y);
//            MatOfFloat Ys = new MatOfFloat();
//            Mat.zeros(this.nOut, this.epTsLength, CV_32FC1).copyTo(Ys);
//            MatOfFloat y = new MatOfFloat();
//            Ys.copyTo(y);
//
//            MatOfFloat Ptr = new MatOfFloat();
//            MatOfFloat p = new MatOfFloat();
//
//            MatOfFloat Wbest = new MatOfFloat();
//            MatOfFloat W = new MatOfFloat();
//
//            timer = new Timewatch();
//
//            if(this.eta > 0) {
//                // Antrenarea ponderilor daca rata de antrenare e mai mare ca 0
//                for (int ep = 0; ep < this.epoci; ep++) {
//                    for (int i = 0; i < this.epTrLength; i++) {
//                        Core.transpose(this.trainSamples.col(i), in_rbf);
//                        rbf_unit = new RBF_Unit(this.tip_r, this.centers, in_rbf, this.raza, this.dist_type);
//
//                        MatOfFloat weightTranspose = new MatOfFloat();
//                        Core.transpose(this.weights, weightTranspose);
//                        Core.transpose(rbf_unit.exit_rbf, rbf_unit.exit_rbf);
//
//                        MatOfFloat temp = util.mult(weightTranspose, rbf_unit.exit_rbf);
//
//                        for (int k = 0; k < temp.rows(); k++) {
//                            Y.put(k, i, temp.get(k, 0));
//                        }
//
//                        for (int j = 0; j < this.nOut; j++) {
//                            MatOfFloat dW = util.multScalar(rbf_unit.exit_rbf, this.trainLabels.get(j, i)[0] - Y.get(j, i)[0]);
//                            dW = util.multScalar(dW, this.eta);
//                            Core.add(dW, this.weights.col(j), dW);
//
//                            for (int k = 0; k < dW.rows(); k++) {
//                                this.weights.put(k, j, dW.get(k, 0));
//                            }
//                        }
//                    }
//                    this.weights.copyTo(W);
//                }
//            }
//            timings.addSplit("Finalizare antrenare");
//
//            Log.d("FSVC", " ");
//            Log.d("FSVC", "-------------------------------------------------");
//            Log.d("FSVC", "Finalizarea ciclului de antrenare dupa " + timer.getDuration() + " secunde");
//            Log.d("FSVC", "-------------------------------------------------");
//            Log.d("FSVC", " ");
//
//
//            MatOfFloat conf = new MatOfFloat();
//            Mat.zeros(this.nOut, this.nOut, CV_32FC1).copyTo(conf);
//            MatOfFloat tmp = new MatOfFloat();
//            Core.subtract(Y, new Scalar(Core.norm(this.trainLabels)/Math.sqrt(this.epTrLength)), tmp);
////                List<Mat> mat_list = new ArrayList<>();
////                mat_list.add(Ptr);
////                mat_list.add(tmp);
////                Core.hconcat(mat_list, Ptr);
//
//            float on_target = 0, total_targets = 0;
//
//            timer = new Timewatch();
//
//            // Testare + matricea de confuzie
//            for(int i = 0; i < this.epTsLength; i++) {
//                Core.transpose(this.testSamples.col(i), in_rbf);
//                rbf_unit = new RBF_Unit(this.tip_r, this.centers, in_rbf, this.raza, this.dist_type);
//
//                MatOfFloat weightTranspose = new MatOfFloat();
//                Core.transpose(this.weights, weightTranspose);
//                Core.transpose(rbf_unit.exit_rbf, rbf_unit.exit_rbf);
//
//                MatOfFloat temp = util.mult(weightTranspose, rbf_unit.exit_rbf);
//
//                for(int k = 0; k < temp.rows(); k++) {
//                    y.put(k,i,temp.get(k,0));
//                }
//
//                byte[] ix = this.getMaxPosition(y.col(i));
//                int i_pred = ix[0];
//                int i_actual = (int) this.testLabelsOrig.get(0, i)[0]-1;
//                conf.put(i_actual, i_pred, 1);
//                if(i_actual == i_pred)
//                    on_target++;
//                total_targets++;
//            }
//            timings.addSplit("Finalizare testare");
//
//            float acc = on_target/total_targets*100;
//            MatOfFloat temp = new MatOfFloat(Mat.zeros(1,1,CV_32FC1));
//            temp.put(0,0,acc);
////                mat_list = new ArrayList<>();
////                mat_list.add(p);
////                mat_list.add(temp);
////                Core.hconcat(mat_list, p);
//
//            if(acc > acc_best) {
//                acc_best = acc;
//                Wbest = W;
////                    ep_best = ep;
//                conf_best = conf;
//            }
//
//            Log.d("FSVC", " ");
//            Log.d("FSVC", "-------------------------------------------------");
//            Log.d("FSVC", "Finalizarea ciclului de testare dupa " + timer.getDuration() + " secunde");
//            Log.d("FSVC", "Best accuracy: "+acc_best + "% after " + (ep_best+1) + " epoca");
//            timings.dumpToLog();
//            Log.d("FSVC", "-------------------------------------------------");
//            Log.d("FSVC", " ");
//
//        } catch (Exception e) {
//            Toast.makeText(this.context, e.getStackTrace()[0].getLineNumber()+": "+e.getMessage(), Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//            Log.d("FSVC Error", e.getStackTrace()[0].getLineNumber()+": "+e.getMessage());
//        }
//    }
//
//    private List<Integer> find(PrimitiveMatrix x, String op, double val) {
//        List<Integer> found = new ArrayList<>();
//
//        for(int i = 0; i < x.count(); i++) {
//            if(op == "==" && x.get(i) == val)
//                found.add(i);
//            else if(op == ">" && x.get(i) > val)
//                found.add(i);
//            else if(op == "<" && x.get(i) < val)
//                found.add(i);
//            else if(op == "<=" && x.get(i) <= val)
//                found.add(i);
//            else if(op == ">=" && x.get(i) >= val)
//                found.add(i);
//        }
//
//        return found;
//    }
//
//    private MatOfFloat readCsv(String path, String type) {
//        MatOfFloat resultMatrix = new MatOfFloat();
//
//        AssetManager assetManager = this.context.getAssets();
//
//        try {
//            InputStream csvStream = assetManager.open(path);
//            InputStreamReader csvStreamReader = new InputStreamReader(csvStream);
//            CSVReader csvReader = new CSVReader(csvStreamReader);
//            String[] line;
//
//            while ((line = csvReader.readNext()) != null) {
//                resultMatrix.push_back(this.processLine(line));
//            }
//
//            if(type == "labels")
//                resultMatrix = this.processLabels(resultMatrix);
//
//        } catch (IOException e) {
//            Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }
//
//        return resultMatrix;
//    }
//
//    private MatOfFloat processLine(String[] strings) {
//        MatOfFloat m = new MatOfFloat(Mat.zeros(1,strings.length, CV_32FC1));
//        int i=0;
//        try {
//            for (String str : strings) {
//                m.put(0, i, Float.parseFloat(str.trim()));//Exception in this line
//                i++;
//            }
//
//        } catch (NumberFormatException e) {
//            Toast.makeText(this.context, e.getMessage(), Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }
//
//        return m;
//    }
//
//    private MatOfFloat processLabels(MatOfFloat m) {
//        double max = this.getMax(m);
//        MatOfFloat resultMatrix = new MatOfFloat();
//        Mat.zeros((int) max, m.cols(), CV_32FC1).copyTo(resultMatrix);
//
//        for (int i = 0; i < resultMatrix.cols(); i++)
//            for (int j = 0; j < resultMatrix.rows(); j++)
//                if(j == m.get(0,i)[0]-1)
//                    resultMatrix.put(j, i, 1);
//                else
//                    resultMatrix.put(j, i, -1);
//
//        return resultMatrix;
//    }
//
//    private double getMax(MatOfFloat m) {
//        double max = 0;
//        if(!m.empty()) {
//            max = m.get(0,0)[0];
//            for (int i = 0; i < m.rows(); i++)
//                for (int j = 0; j < m.cols(); j++)
//                    if(max < m.get(i,j)[0])
//                        max = m.get(i,j)[0];
//        }
//        return max;
//    }
//
//    private byte[] getMaxPosition(Mat m) {
//        double max = 0;
//        byte[] max_position = new byte[2];
//        if(!m.empty()) {
//            max = m.get(0,0)[0];
//            max_position[0] = 0;
//            max_position[1] = 0;
//            for (int i = 0; i < m.rows(); i++)
//                for (int j = 0; j < m.cols(); j++)
//                    if(max < m.get(i,j)[0]) {
//                        max = m.get(i, j)[0];
//                        max_position[0] = (byte) i;
//                        max_position[1] = (byte) j;
//                    }
//        }
//        return max_position;
//    }
//
//    private PrimitiveMatrix getSign(PrimitiveMatrix m) {
//
//        PrimitiveMatrix result = PrimitiveMatrix.FACTORY.makeZero(m.countRows(), m.countColumns());
//        if(!m.isEmpty()) {
//            for (int i = 0; i < m.countRows(); i++)
//                for (int j = 0; j < m.countColumns(); j++) {
//                    if(m.get(i,j) > 0)
//                        result = result.add(i, j, 1);
//                    else if(m.get(i,j) == 0)
//                        result = result.add(i, j, 0);
//                    else if(m.get(i,j) < 0)
//                        result = result.add(i, j, -1);
//                }
//
//        }
//        return result;
//    }
//
//    private PrimitiveMatrix getAbs(PrimitiveMatrix m) {
//
//        if(!m.isEmpty()) {
//            for (int i = 0; i < m.countRows(); i++)
//                for (int j = 0; j < m.countColumns(); j++)
//                    if(m.get(i,j) < 0)
//                        m = m.add(i, j, -2 * m.get(i,j));
//        }
//        return m;
//    }
//
//    private PrimitiveMatrix getSums(PrimitiveMatrix m) {
//
//        PrimitiveMatrix sums = PrimitiveMatrix.FACTORY.makeZero(1, m.countColumns());
//        double sum = 0;
//        if(!m.isEmpty()) {
//            for (int i = 0; i < m.countColumns(); i++) {
//                for (int j = 0; j < m.countRows(); j++) {
//                    sum += m.get(j,i);
//                }
//                sums.add(0, i, sum);
//                sum = 0;
//            }
//
//        }
//        return sums;
//    }


}