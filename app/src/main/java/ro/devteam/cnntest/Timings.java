package ro.devteam.cnntest;

import android.util.Log;

import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class Timings {

    public NavigableMap<String, Long> works;
    private String tag;

    public Timings(String tag) {
        this.tag = tag;
        works = new TreeMap<String, Long>();
    }

    public void addWork(String tag) {
        works.put(tag, System.nanoTime());
    }

    public long get(String tag) {
        NavigableMap<String, Long> temp = works.descendingMap();
        Map.Entry<String, Long> nextWork = temp.higherEntry(tag);
        if(nextWork == null) {
            return (System.nanoTime() - temp.get(tag)) / 1000000;
        }

        return (nextWork.getValue() - temp.get(tag)) / 1000000;
    }

    public void dumpToLog() {
        works.put("End", System.nanoTime());
        NavigableMap<String, Long> temp = works.descendingMap();

        for (Map.Entry<String, Long> work : temp.entrySet()) {
            Map.Entry<String, Long> nextWork = temp.higherEntry(work.getKey());
            if(nextWork != null) {
                Log.d(tag + " " + work.getKey(), (nextWork.getValue() - work.getValue()) / 1000000 + "ms");
            }
        }
    }

}
