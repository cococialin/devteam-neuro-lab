package ro.devteam.cnntest;

import android.content.Context;
import android.net.Uri;

import de.siegmar.fastcsv.reader.CsvRecord;

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

    public CsvRecord load(Uri uri) {

        CsvRecord csv = null;

//        InputStream csvStream = context.getContentResolver().openInputStream(uri);
//        Reader csvStreamReader = new InputStreamReader(csvStream);
//        CsvReader csvReader = new CsvReader();
//
//           csv = csvReader.read(csvStreamReader);

        return csv;
    }

}
