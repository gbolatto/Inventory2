package com.example.android.inventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventory.db.ItemContract;

import org.w3c.dom.Text;

/**
 * Created by gbolatto on 8/19/2018.
 */
public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;

    private EditText mItemNameEditText;
    private EditText mItemPriceEditText;
    private TextView mItemQuantity;
    private EditText mItemSupplierNameEditText;
    private EditText mItemSupplierPhoneEditText;

    private int totalQuantity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle(R.string.add_item);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.edit_item);
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        mItemNameEditText = findViewById(R.id.edit_text_name);
        mItemPriceEditText = findViewById(R.id.edit_text_price);
        mItemQuantity = findViewById(R.id.quantity);
        mItemSupplierNameEditText = findViewById(R.id.edit_text_supplier_name);
        mItemSupplierPhoneEditText = findViewById(R.id.edit_text_supplier_phone);

        Button mAddButton = findViewById(R.id.add_button);
        Button mSubtractButton = findViewById(R.id.subtract_button);
        Button mPhoneButton = findViewById(R.id.phone_button);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                totalQuantity += 1;
                mItemQuantity.setText(String.valueOf(totalQuantity));
            }
        });

        mSubtractButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (totalQuantity < 1) {
                    Toast.makeText(DetailsActivity.this, R.string.quantity_below_zero, Toast.LENGTH_SHORT).show();
                } else {
                    totalQuantity -= 1;
                    mItemQuantity.setText(String.valueOf(totalQuantity));
                }
            }
        });

        mPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = mItemSupplierPhoneEditText.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // hide "delete item" from the options menu when on add item screen
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete_item);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save_item:
                showSaveDialog();
                return true;
            case R.id.action_delete_item:
                showDeleteDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem(){
        String name = mItemNameEditText.getText().toString().trim();
        String priceString = mItemPriceEditText.getText().toString().trim();
        String quantityString = mItemQuantity.getText().toString().trim();
        String supplierName = mItemSupplierNameEditText.getText().toString().trim();
        String supplierPhone = mItemSupplierPhoneEditText.getText().toString().trim();

        int price = 0;
        int quantity = 0;
        if (!TextUtils.isEmpty(priceString) && !TextUtils.isEmpty(quantityString)) {
            price = Integer.parseInt(priceString);
            quantity = Integer.parseInt(quantityString);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, name);
        contentValues.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, price);
        contentValues.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
        contentValues.put(ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, supplierName);
        contentValues.put(ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, supplierPhone);

        // if it's a new item, insert to the db. otherwise, update the current item
        if (mCurrentItemUri == null ) {
            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, contentValues);

            if (newUri == null) {
                Toast.makeText(this, R.string.error_saving_item, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.item_saved, Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsSaved = getContentResolver().update(mCurrentItemUri, contentValues, null, null);

            if (rowsSaved == 0) {
                Toast.makeText(this, R.string.error_updating_item, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.item_updated, Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

    private void deleteItem() {
        int rowsDeleted = 0;
        if (mCurrentItemUri != null) {
            rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);
        }

        if (rowsDeleted > 0) {
            Toast.makeText(this, R.string.item_deleted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.error_deleting_item, Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void showSaveDialog() {

        // making sure all text input fields are filled before moving to the dialog
        String name = mItemNameEditText.getText().toString().trim();
        String priceString = mItemPriceEditText.getText().toString().trim();
        String supplierName = mItemSupplierNameEditText.getText().toString().trim();
        String supplierPhone = mItemSupplierPhoneEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.save_required_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, R.string.save_required_price, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supplierName)) {
            Toast.makeText(this, R.string.save_required_supplier_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(supplierPhone)) {
            Toast.makeText(this, R.string.save_required_supplier_phone, Toast.LENGTH_SHORT).show();
            return;
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_save);
        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                saveItem();
            }
        });
        builder.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.confirm_delete);
        builder.setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE};

        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            String name = cursor.getString(
                    cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME));
            int price = cursor.getInt(
                    cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE));
            int quantity = cursor.getInt(
                    cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY));
            String supplierName = cursor.getString(
                    cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER_NAME));
            String supplierPhone = cursor.getString(
                    cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE));

            // set the running quantity for the add/sub buttons for existing items
            totalQuantity = quantity;

            mItemNameEditText.setText(name);
            mItemPriceEditText.setText(Integer.toString(price));
            mItemQuantity.setText(Integer.toString(quantity));
            mItemSupplierNameEditText.setText(supplierName);
            mItemSupplierPhoneEditText.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemNameEditText.setText("");
        mItemPriceEditText.setText("");
        mItemQuantity.setText("");
        mItemSupplierNameEditText.setText("");
        mItemSupplierPhoneEditText.setText("");
    }
}
