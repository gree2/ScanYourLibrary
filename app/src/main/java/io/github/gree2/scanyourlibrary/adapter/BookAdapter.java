package io.github.gree2.scanyourlibrary.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.github.gree2.scanyourlibrary.R;
import io.github.gree2.scanyourlibrary.model.Book;
import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;

/**
 * Created by hqlgree2 on 11/15/15.
 */
public class BookAdapter  extends RealmBaseAdapter<Book> implements ListAdapter {

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
            holder.bookTitl = (TextView) convertView.findViewById(R.id.book_title);
            holder.sync_status = (ImageView) convertView.findViewById(R.id.sync_status);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.bookIsbn.setText(book.getIsbn());
        holder.bookTitl.setText(book.getTitl());
        return convertView;
    }
}
