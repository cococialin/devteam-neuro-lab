package ro.devteam.elmandroid.customview;

import java.util.List;
import ro.devteam.elmandroid.tflite.Classifier.Recognition;

public interface ResultsView {
  public void setResults(final List<Recognition> results);
}
