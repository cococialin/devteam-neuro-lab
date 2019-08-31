package ro.devteam.elmandroid;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "devteam_db";

    private static final String TABLE_MODELS = "models";
    private static final String TABLE_DBS = "databases";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_URI = "uri";

    private static final String COLUMN_BATCH_SIZE = "batch_size";
    private static final String COLUMN_IMG_HEIGHT = "img_height";
    private static final String COLUMN_IMG_WIDTH = "img_width";
    private static final String COLUMN_NUM_CHANNEL = "num_channel";
    private static final String COLUMN_NUM_CLASSES = "num_classes";
    private static final String COLUMN_PIXEL_SIZE = "pixel_size";
    private static final String COLUMN_CHANNEL_BYTES = "channel_bytes";
    private static final String COLUMN_IMG_ROTATION = "img_rotation";
    private static final String COLUMN_IMG_FLIP = "img_flip";
    private static final String COLUMN_NORM_MIN = "norm_min";
    private static final String COLUMN_NORM_MAX = "norm_max";
    private static final String COLUMN_QUANTIZED = "quantized";
    private static final String COLUMN_SELECTED = "selected";

    private static final String COLUMN_TYPE = "type";

    private Context context;

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
    public int img_rotation;
    public int channel_bytes;
    public String img_flip;
    public int selected;

    public String type;

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create models table
        db.execSQL(
                "CREATE TABLE " + TABLE_MODELS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + COLUMN_NAME + " TEXT,"
                        + COLUMN_URI + " TEXT,"
                        + COLUMN_BATCH_SIZE + " INT,"
                        + COLUMN_IMG_HEIGHT + " INT,"
                        + COLUMN_IMG_WIDTH + " INT,"
                        + COLUMN_NUM_CHANNEL + " INT,"
                        + COLUMN_NUM_CLASSES + " INT,"
                        + COLUMN_PIXEL_SIZE + " INT,"
                        + COLUMN_CHANNEL_BYTES + " INT,"
                        + COLUMN_IMG_ROTATION + " INT,"
                        + COLUMN_IMG_FLIP + " TEXT,"
                        + COLUMN_NORM_MIN + " INT,"
                        + COLUMN_NORM_MAX + " INT,"
                        + COLUMN_QUANTIZED + " INT,"
                        + COLUMN_SELECTED + " INT"
                + ")"
        );

        db.execSQL(
                "CREATE TABLE " + TABLE_DBS + "("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                        + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + COLUMN_NAME + " TEXT,"
                        + COLUMN_URI + " TEXT,"
                        + COLUMN_TYPE + " TEXT,"
                        + COLUMN_SELECTED + " INT"
                        + ")"
        );
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MODELS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DBS);

        // Create tables again
        onCreate(db);
    }

    public long insertModel(ContentValues values) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        // insert row
        long id = db.insert(TABLE_MODELS, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public long insertDb(ContentValues values) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        // insert row
        long id = db.insert(TABLE_DBS, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        return id;
    }

    public ModelClassifier getModel(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MODELS,
                new String[]{
                        COLUMN_ID,
                        COLUMN_NAME,
                        COLUMN_TIMESTAMP,
                        COLUMN_URI,
                        COLUMN_BATCH_SIZE,
                        COLUMN_IMG_HEIGHT,
                        COLUMN_IMG_WIDTH,
                        COLUMN_NUM_CHANNEL,
                        COLUMN_NUM_CLASSES,
                        COLUMN_PIXEL_SIZE,
                        COLUMN_CHANNEL_BYTES,
                        COLUMN_IMG_ROTATION,
                        COLUMN_IMG_FLIP,
                        COLUMN_NORM_MIN,
                        COLUMN_NORM_MAX,
                        COLUMN_QUANTIZED,
                        COLUMN_SELECTED
                },
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        else
            return new ModelClassifier();

        // prepare note object
        ModelClassifier model = new ModelClassifier();
        model.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        model.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        model.timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
        model.uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COLUMN_URI)));
        model.batch_size = cursor.getInt(cursor.getColumnIndex(COLUMN_BATCH_SIZE));
        model.img_height = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_HEIGHT));
        model.img_width = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_WIDTH));
        model.num_channel = cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_CHANNEL));
        model.num_classes = cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_CLASSES));
        model.pixel_size = cursor.getInt(cursor.getColumnIndex(COLUMN_PIXEL_SIZE));
        model.channel_bytes = cursor.getInt(cursor.getColumnIndex(COLUMN_CHANNEL_BYTES));
        model.img_rotation = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_ROTATION));
        model.img_flip = cursor.getString(cursor.getColumnIndex(COLUMN_IMG_FLIP));
        model.norm_min = cursor.getInt(cursor.getColumnIndex(COLUMN_NORM_MIN));
        model.norm_max = cursor.getInt(cursor.getColumnIndex(COLUMN_NORM_MAX));
        model.quantized = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTIZED));
        model.selected = cursor.getInt(cursor.getColumnIndex(COLUMN_SELECTED));

        // close the db connection
        cursor.close();

        return model;
    }

    public ModelClassifier getSelectedModel() {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_MODELS,
                new String[]{
                        COLUMN_ID,
                        COLUMN_NAME,
                        COLUMN_TIMESTAMP,
                        COLUMN_URI,
                        COLUMN_BATCH_SIZE,
                        COLUMN_IMG_HEIGHT,
                        COLUMN_IMG_WIDTH,
                        COLUMN_NUM_CHANNEL,
                        COLUMN_NUM_CLASSES,
                        COLUMN_PIXEL_SIZE,
                        COLUMN_CHANNEL_BYTES,
                        COLUMN_IMG_ROTATION,
                        COLUMN_IMG_FLIP,
                        COLUMN_NORM_MIN,
                        COLUMN_NORM_MAX,
                        COLUMN_QUANTIZED,
                        COLUMN_SELECTED
                },
                COLUMN_SELECTED + "=?",
                new String[]{String.valueOf(1)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        else
            return new ModelClassifier();

        // prepare note object
        ModelClassifier model = new ModelClassifier();
        model.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        model.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        model.timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
        model.uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COLUMN_URI)));
        model.batch_size = cursor.getInt(cursor.getColumnIndex(COLUMN_BATCH_SIZE));
        model.img_height = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_HEIGHT));
        model.img_width = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_WIDTH));
        model.num_channel = cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_CHANNEL));
        model.num_classes = cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_CLASSES));
        model.pixel_size = cursor.getInt(cursor.getColumnIndex(COLUMN_PIXEL_SIZE));
        model.channel_bytes = cursor.getInt(cursor.getColumnIndex(COLUMN_CHANNEL_BYTES));
        model.img_rotation = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_ROTATION));
        model.img_flip = cursor.getString(cursor.getColumnIndex(COLUMN_IMG_FLIP));
        model.norm_min = cursor.getInt(cursor.getColumnIndex(COLUMN_NORM_MIN));
        model.norm_max = cursor.getInt(cursor.getColumnIndex(COLUMN_NORM_MAX));
        model.quantized = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTIZED));
        model.selected = cursor.getInt(cursor.getColumnIndex(COLUMN_SELECTED));

        // close the db connection
        cursor.close();

        return model;
    }

    public DatabaseFile getDbFile(long id) {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DBS,
                new String[]{
                        COLUMN_ID,
                        COLUMN_NAME,
                        COLUMN_TIMESTAMP,
                        COLUMN_URI,
                        COLUMN_TYPE,
                        COLUMN_SELECTED
                },
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        else
            return new DatabaseFile();

        // prepare note object
        DatabaseFile dbFile = new DatabaseFile();
        dbFile.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        dbFile.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        dbFile.timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
        dbFile.uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COLUMN_URI)));
        dbFile.type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
        dbFile.selected = cursor.getInt(cursor.getColumnIndex(COLUMN_SELECTED));

        // close the db connection
        cursor.close();

        return dbFile;
    }

    public DatabaseFile getSelectedDbFile() {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DBS,
                new String[]{
                        COLUMN_ID,
                        COLUMN_NAME,
                        COLUMN_TIMESTAMP,
                        COLUMN_URI,
                        COLUMN_TYPE,
                        COLUMN_SELECTED
                },
                COLUMN_SELECTED + "=?",
                new String[]{String.valueOf(1)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();
        else
            return new DatabaseFile();

        // prepare note object
        DatabaseFile dbFile = new DatabaseFile();
        dbFile.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
        dbFile.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
        dbFile.timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
        dbFile.uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COLUMN_URI)));
        dbFile.type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
        dbFile.selected = cursor.getInt(cursor.getColumnIndex(COLUMN_SELECTED));

        // close the db connection
        cursor.close();

        return dbFile;
    }

    public List<ModelClassifier> getAllModels() {
        List<ModelClassifier> models = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_MODELS + " ORDER BY " +
                COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                ModelClassifier model = new ModelClassifier();
                model.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                model.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                model.timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                model.uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COLUMN_URI)));
                model.batch_size = cursor.getInt(cursor.getColumnIndex(COLUMN_BATCH_SIZE));
                model.img_height = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_HEIGHT));
                model.img_width = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_WIDTH));
                model.num_channel = cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_CHANNEL));
                model.num_classes = cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_CLASSES));
                model.pixel_size = cursor.getInt(cursor.getColumnIndex(COLUMN_PIXEL_SIZE));
                model.channel_bytes = cursor.getInt(cursor.getColumnIndex(COLUMN_CHANNEL_BYTES));
                model.img_rotation = cursor.getInt(cursor.getColumnIndex(COLUMN_IMG_ROTATION));
                model.img_flip = cursor.getString(cursor.getColumnIndex(COLUMN_IMG_FLIP));
                model.norm_min = cursor.getInt(cursor.getColumnIndex(COLUMN_NORM_MIN));
                model.norm_max = cursor.getInt(cursor.getColumnIndex(COLUMN_NORM_MAX));
                model.quantized = cursor.getInt(cursor.getColumnIndex(COLUMN_QUANTIZED));
                model.selected = cursor.getInt(cursor.getColumnIndex(COLUMN_SELECTED));

                models.add(model);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return models;
    }

    public List<DatabaseFile> getAllDbFiles() {
        List<DatabaseFile> dbFiles = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DBS + " ORDER BY " +
                COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                DatabaseFile dbFile = new DatabaseFile();
                dbFile.id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                dbFile.name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                dbFile.timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP));
                dbFile.uri = Uri.parse(cursor.getString(cursor.getColumnIndex(COLUMN_URI)));
                dbFile.type = cursor.getString(cursor.getColumnIndex(COLUMN_TYPE));
                dbFile.selected = cursor.getInt(cursor.getColumnIndex(COLUMN_SELECTED));

                dbFiles.add(dbFile);
            } while (cursor.moveToNext());
        }

        // close db connection
        db.close();

        // return notes list
        return dbFiles;
    }

    public int getModelsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_MODELS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }
    public int getDbFilesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_DBS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }

    public int updateModel(ModelClassifier model) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, model.name);
        values.put(COLUMN_URI, model.uri.toString());
        values.put(COLUMN_BATCH_SIZE, model.batch_size);
        values.put(COLUMN_IMG_HEIGHT, model.img_height);
        values.put(COLUMN_IMG_WIDTH, model.img_width);
        values.put(COLUMN_NUM_CHANNEL, model.num_channel);
        values.put(COLUMN_NUM_CLASSES, model.num_classes);
        values.put(COLUMN_PIXEL_SIZE, model.pixel_size);
        values.put(COLUMN_CHANNEL_BYTES, model.channel_bytes);
        values.put(COLUMN_IMG_ROTATION, model.img_rotation);
        values.put(COLUMN_IMG_FLIP, model.img_flip);
        values.put(COLUMN_NORM_MIN, model.norm_min);
        values.put(COLUMN_NORM_MAX, model.norm_max);
        values.put(COLUMN_QUANTIZED, model.quantized);

        // updating row
        return db.update(TABLE_MODELS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(model.id)});
    }

    public int updateDbFile(DatabaseFile dbFile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, dbFile.name);
        values.put(COLUMN_URI, dbFile.uri.toString());
        values.put(COLUMN_TYPE, dbFile.type);

        // updating row
        return db.update(TABLE_DBS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(dbFile.id)});
    }

    public int updateSelectedModel(ModelClassifier model) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SELECTED, 0);

        db.update(TABLE_MODELS, values, COLUMN_SELECTED + " = ?",
                new String[]{String.valueOf(1)});

        ContentValues values2 = new ContentValues();
        values.put(COLUMN_SELECTED, 1);

        // updating row
        return db.update(TABLE_MODELS, values2, COLUMN_ID + " = ?",
                new String[]{String.valueOf(model.id)});
    }

    public int updateSelectedDbFile(DatabaseFile dbFile) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SELECTED, 0);

        db.update(TABLE_DBS, values, COLUMN_SELECTED + " = ?",
                new String[]{String.valueOf(1)});

        ContentValues values2 = new ContentValues();
        values.put(COLUMN_SELECTED, 1);

        // updating row
        return db.update(TABLE_DBS, values2, COLUMN_ID + " = ?",
                new String[]{String.valueOf(dbFile.id)});
    }

    public void deleteModel(ModelClassifier model) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_MODELS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(model.id)});
        db.close();
    }

    public void deleteDbFile(DatabaseFile dbFile) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DBS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(dbFile.id)});
        db.close();
    }
}
