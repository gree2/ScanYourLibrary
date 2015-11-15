package io.github.gree2.scanyourlibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.camera.CameraManager;

import io.github.gree2.scanyourlibrary.adapter.BookAdapter;
import io.github.gree2.scanyourlibrary.model.Book;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ListView lvBooks;

    //private RealmResults books;

    private BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        lvBooks =(ListView) findViewById(R.id.book_list);

        final Realm realm = Realm.getInstance(this);

        // after sync refresh ui
        realm.addChangeListener(new RealmChangeListener() {
            @Override
            public void onChange() {
                //books = realm.where(Book.class).findAll();
                bookAdapter.notifyDataSetChanged();
            }
        });

        RealmResults books = realm.where(Book.class).findAll();
        books.sort("isbn");

        // setting the nav drawer list adapter
        bookAdapter = new BookAdapter(getApplicationContext(), books, true);
        lvBooks.setAdapter(bookAdapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);
    }

    public void onClick(View v) {
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //retrieve result of scanning - instantiate ZXing object
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        //check we have a valid result
        if (scanningResult != null) {
            String isbn = scanningResult.getContents();
            String format = scanningResult.getFormatName();
            if (isbn != null && format != null && "EAN_13".equals(format)) {
                saveScanInfo(isbn, format);
            }
        } else {
            //invalid scan data or scan canceled
            Toast toast = Toast.makeText(getApplicationContext(),
                    "No book scan data received!", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void saveScanInfo(String isbn, String format){
        final Realm realm = Realm.getInstance(this);
        realm.beginTransaction();
        Book book = realm.createObject(Book.class);
        book.setIsbn(isbn);
        book.setFmtn(format);
        realm.copyToRealmOrUpdate(book);
        realm.commitTransaction();
        Toast.makeText(getApplicationContext(),
                "book info saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}
