package ro.devteam.cnntest.customview;

import java.util.List;
import ro.devteam.cnntest.tflite2.Classifier.Recognition;

public interface ResultsView {
  public void setResults(final List<Recognition> results);
}
