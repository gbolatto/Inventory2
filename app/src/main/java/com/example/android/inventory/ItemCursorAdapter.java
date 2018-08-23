package com.example.android.inventory;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.db.ItemContract;

/**
 * Created by gbolatto on 8/19/2018.
 */
public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(
                R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView textViewItemName = view.findViewById(R.id.item_name);
        TextView textViewItemPrice = view.findViewById(R.id.item_price);
        TextView textViewItemQuantity = view.findViewById(R.id.item_quantity);

        Button subtractOne = view.findViewById(R.id.main_subtract_one);

        String itemId = cursor.getString(
                cursor.getColumnIndexOrThrow(ItemContract.ItemEntry._ID));

        String itemName = cursor.getString(
                cursor.getColumnIndexOrThrow(ItemContract.ItemEntry.COLUMN_ITEM_NAME));

        String itemPrice = cursor.getString(
                cursor.getColumnIndexOrThrow(ItemContract.ItemEntry.COLUMN_ITEM_PRICE));

        String itemQuantity = cursor.getString(
                cursor.getColumnIndexOrThrow(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY));

        textViewItemName.setText(itemName);
        textViewItemPrice.setText(itemPrice);
        textViewItemQuantity.setText(itemQuantity);

        final int id = Integer.parseInt(itemId);
        final int quantity = Integer.parseInt(itemQuantity);

        subtractOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int tempQuantity = quantity;
                if (tempQuantity < 1) {
                    Toast.makeText(context, R.string.quantity_below_zero, Toast.LENGTH_SHORT).show();
                } else {
                    tempQuantity -= 1;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, tempQuantity);
                    Uri updatedUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);
                    int row = context.getContentResolver().update(updatedUri, contentValues, null, null);
                }
            }
        });
    }
}