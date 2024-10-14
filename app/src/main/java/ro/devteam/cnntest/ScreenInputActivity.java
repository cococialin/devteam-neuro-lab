package ro.devteam.cnntest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.nex3z.fingerpaintview.FingerPaintView;

import java.io.IOException;

import butterknife.ButterKnife;

public class ScreenInputActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Timings timing;
    private static final String LOG_TAG = ro.devteam.cnntest.MainActivity.class.getSimpleName();

    FingerPaintView mFpvPaint;
    TextView mTvPrediction;
    TextView mTvProbability;
    TextView mTvTimeCost;
    ImageView img;
    Button btn_detect;
    Button btn_clear;

    private Classifier mClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        timing = new Timings("ELM");
        timing.addWork("Load User Interface");

        mFpvPaint = (FingerPaintView) findViewById(R.id.fpv_paint);
        mTvPrediction = (TextView) findViewById(R.id.tv_prediction);
        mTvProbability = (TextView) findViewById(R.id.tv_probability);
        mTvTimeCost = (TextView) findViewById(R.id.tv_timecost);
        img = (ImageView) findViewById(R.id.img);
        btn_detect = (Button) findViewById(R.id.btn_detect);
        btn_clear = (Button) findViewById(R.id.btn_clear);

        setContentView(R.layout.activity_screen_input);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        ButterKnife.bind(this);
        init();

        btn_detect.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mClassifier == null) {
                    Log.e(LOG_TAG, "onDetectClick(): Classifier is not initialized");
                    return;
                } else if (mFpvPaint.getEmpty()) {
                    Toast.makeText(getBaseContext(), "Please write a digit!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Bitmap image = mFpvPaint.exportToBitmap(
                        Classifier.IMG_WIDTH, Classifier.IMG_HEIGHT);

                image = rotateBitmap(image, 90);
                image = flipXBitmap(image);
                img.setImageBitmap(image);
                Result result = mClassifier.classify(image);
                renderResult(result);
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mFpvPaint.clear();
                mTvPrediction.setText(R.string.empty);
                mTvProbability.setText(R.string.empty);
                mTvTimeCost.setText(R.string.empty);
            }
        });
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

    private void init() {
        try {
            mClassifier = new Classifier(this);
        } catch (IOException e) {
            Toast.makeText(this, R.string.failed_to_create_classifier, Toast.LENGTH_LONG).show();
            Log.e(LOG_TAG, "init(): Failed to create Classifier", e);
        }
    }

    private void renderResult(Result result) {
        mTvPrediction.setText(String.valueOf(result.getNumber()));
        mTvProbability.setText(String.valueOf(result.getProbability()));
        mTvTimeCost.setText(result.getTimeCost() + "ms");
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
            Intent intent = new Intent(this, ro.devteam.cnntest.MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_import) {

        } else if (id == R.id.nav_screen_input) {

        } else if (id == R.id.nav_select_model) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
