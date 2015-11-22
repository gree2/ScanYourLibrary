package io.github.gree2.scanyourlibrary;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import io.github.gree2.scanyourlibrary.controller.AppController;
import io.github.gree2.scanyourlibrary.model.Book;
import io.github.gree2.scanyourlibrary.util.Const;
import io.realm.Realm;

public class BookDetailActivity extends AppCompatActivity {

    private String TAG = BookDetailActivity.class.getSimpleName();


    private ViewHolder holder = new ViewHolder();

    private String isbn;

    private Boolean isSynced;


    static class ViewHolder {
        public TextView tv_book_title;
        public TextView tv_book_author;
        public TextView tv_book_publisher;
        public TextView tv_book_subtitle;
        public TextView tv_book_translator;
        public TextView tv_book_year;
        public TextView tv_book_page;
        public TextView tv_book_desc;
        public TextView tv_book_price;
        public TextView tv_book_layout;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        CollapsingToolbarLayout toolBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        toolBarLayout.setTitle(getTitle());

        // find
        findView();

        // get extra
        this.isbn = getIntent().getStringExtra("isbn");
        this.isSynced = getIntent().getBooleanExtra("isSynced", false);

        if (isSynced){
            // bind
            bindView();
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        if (isSynced) {
            fab.setImageResource(R.drawable.done);
        } else {
            fab.setImageResource(R.drawable.sync);
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isSynced){
                    DoSyncData(fab, isbn);
                }
            }
        });
    }

    private void findView(){
        holder.tv_book_title = (TextView)findViewById(R.id.tv_book_title);
        holder.tv_book_author = (TextView)findViewById(R.id.tv_book_author);
        holder.tv_book_publisher = (TextView)findViewById(R.id.tv_book_publisher);
        holder.tv_book_subtitle = (TextView)findViewById(R.id.tv_book_subtitle);
        holder.tv_book_translator = (TextView)findViewById(R.id.tv_book_translator);
        holder.tv_book_year = (TextView)findViewById(R.id.tv_book_year);
        holder.tv_book_page = (TextView)findViewById(R.id.tv_book_page);
        holder.tv_book_desc = (TextView)findViewById(R.id.tv_book_desc);
        holder.tv_book_price = (TextView)findViewById(R.id.tv_book_price);
        holder.tv_book_layout = (TextView)findViewById(R.id.tv_book_layout);
    }

    private void bindView(){
        Realm realm = Realm.getInstance(this);
        Book book = realm.where(Book.class).equalTo("isbn", this.isbn).findFirst();
        holder.tv_book_title.setText(book.getTitl());
        holder.tv_book_author.setText(book.getAuth());
        holder.tv_book_publisher.setText(book.getPubl());
        holder.tv_book_subtitle.setText(book.getSubt());
        holder.tv_book_translator.setText(book.getTran());
        holder.tv_book_year.setText(book.getYear());
        holder.tv_book_page.setText(book.getPage());
        holder.tv_book_desc.setText(book.getDesc());
        holder.tv_book_price.setText(book.getPric());
        holder.tv_book_layout.setText(book.getLayo());
    }

    private void DoSyncData(FloatingActionButton ivSync, String isbn){

        // rotate relative to self
        RotateAnimation anim = new RotateAnimation(360.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        // Setup anim with desired properties
        anim.setInterpolator(new LinearInterpolator());
        // Repeat animation indefinitely
        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(700); //Put desired duration per anim cycle here, in milliseconds
        // Start animation
        final ImageView myIvSync = ivSync;
        myIvSync.startAnimation(anim);
        final Context context = this;
        final String bookIsbn = isbn;
        String url = String.format(Const.URL_API, bookIsbn);
        // request data
        final StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        new Thread(new BookIsbnApiThread(response, myIvSync, context, bookIsbn)).start();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d(TAG, "Error: " + error.getMessage());
                        myIvSync.setAnimation(null);
                        myIvSync.setImageResource(R.drawable.sync);
                    }
                }) {
        };
        Log.d(TAG, request.getUrl());
        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(request);
    }

    private class BookIsbnApiThread implements Runnable{

        private String html;
        private ImageView sync_status;
        private Context context;
        private String isbn;

        public BookIsbnApiThread(String html, ImageView sync_status, Context context, String isbn){
            this.html = html;
            this.sync_status = sync_status;
            this.context = context;
            this.isbn = isbn;
        }

        @Override
        public void run() {
            Realm realm = Realm.getInstance(context);
            Document doc = Jsoup.parse(html);
            // get bus stops
            Element divRoot = doc.select("div[id=info]").first();
            Book book = new Book();
            book.setIsbn(this.isbn);
            // get title
            String title =  doc.select("a[class=nbg]").first().attr("title");
            book.setTitl(title);
            for (Element info: divRoot.select("span[class=pl]")){
                if (info.text() == null){
                    continue;
                }
                if(info.text().contains("作者")){
                    book.setAuth(info.parent().select("a").first().text());
                } else if(info.text().contains("出版社")){
                    book.setPubl(((Element)info.nextSibling().nextSibling()).text());
                } else if(info.text().contains("副标题")){
                    book.setSubt(((Element) info.nextSibling().nextSibling()).text());
                } else if(info.text().contains("译者")){
                    book.setTran(info.parent().select("a").first().text());
                } else if(info.text().contains("出版年")){
                    book.setYear(((Element) info.nextSibling().nextSibling()).text());
                } else if(info.text().contains("页数")){
                    book.setPage(((Element) info.nextSibling().nextSibling()).text());
                } else if(info.text().contains("丛书")){
                    book.setDesc(((Element) info.nextSibling().nextSibling()).text());
                } else if(info.text().contains("定价")){
                    book.setPric(((Element) info.nextSibling().nextSibling()).text());
                } else if(info.text().contains("装帧")){
                    book.setLayo(((Element) info.nextSibling().nextSibling()).text());
                }
            }
            // synced
            book.setSync(true);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(book);
            realm.commitTransaction();
            // stop animation
            sync_status.post(new Runnable() {
                @Override
                public void run() {
                    sync_status.setAnimation(null);
                    sync_status.setImageResource(R.drawable.done);
                }
            });
        }
    }

}
