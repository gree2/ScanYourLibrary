package io.github.gree2.scanyourlibrary.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import io.github.gree2.scanyourlibrary.R;
import io.github.gree2.scanyourlibrary.controller.AppController;
import io.github.gree2.scanyourlibrary.model.Book;
import io.github.gree2.scanyourlibrary.util.Const;
import io.realm.Realm;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by hqlgree2 on 11/15/15.
 */
public class BookAdapter  extends RealmBaseAdapter<Book> implements ListAdapter {

    private String TAG = BookAdapter.class.getSimpleName();

    //private ViewHolder holder = new ViewHolder();

    private Context context;

    private RealmResults<Book> realmResults;

    private static class ViewHolder {
        TextView bookIsbn;
        TextView bookTitl;
        ImageView sync_status;
    }

    public BookAdapter(Context context, RealmResults<Book> realmResults, boolean automaticUpdate) {
        super(context, realmResults, automaticUpdate);
        this.context = context;
        this.realmResults = realmResults;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Book book = this.realmResults.get(position);
        final ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.book_item, parent, false);
            holder = new ViewHolder();
            holder.bookIsbn = (TextView) convertView.findViewById(R.id.book_isbn);
            holder.bookTitl = (TextView) convertView.findViewById(R.id.book_titl);
            holder.sync_status = (ImageView) convertView.findViewById(R.id.sync_status);
            final ImageView ivSync = holder.sync_status;

            holder.sync_status.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // is synced
                    if (book.getSync() != null && book.getSync()){
                        holder.sync_status.setImageResource(R.drawable.done);
                    } else {
                        DoSyncData(ivSync, book.getIsbn());
                    }
                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bookIsbn.setText(book.getIsbn());
        holder.bookTitl.setText(book.getTitl());
        if (book.getSync() != null && book.getSync()){
            holder.sync_status.setImageResource(R.drawable.done);
        } else{
            holder.sync_status.setImageResource(R.drawable.sync);
        }
        return convertView;
    }

    private void DoSyncData(ImageView ivSync, String isbn){

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
        final Context context = this.context;
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
                    book.setSubt(info.parent().select("a").first().text());
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
