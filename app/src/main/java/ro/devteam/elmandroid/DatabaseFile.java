package ro.devteam.elmandroid;

import android.content.Context;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;

public class DatabaseFile {

    public int id;
    public String name;
    public String timestamp;
    public Uri uri;

    public int selected;
    public String type;

    public Context context;

    public DatabaseFile() {

    }

    public DatabaseFile(Context context) {

    }

    public CsvContainer load(Uri uri) {

        CsvContainer csv = null;
        try {

        InputStream csvStream = context.getContentResolver().openInputStream(uri);
        Reader csvStreamReader = new InputStreamReader(csvStream);
        CsvReader csvReader = new CsvReader();

           csv = csvReader.read(csvStreamReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return csv;
    }

}
