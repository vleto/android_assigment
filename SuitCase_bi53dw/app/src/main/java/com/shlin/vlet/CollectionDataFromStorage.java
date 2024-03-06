package com.shlin.vlet;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;


import java.util.ArrayList;

public class CollectionDataFromStorage extends ArrayAdapter<ProductModel> {

    public CollectionDataFromStorage(Context context, ArrayList<ProductModel> productModel) {
        super(context, 0, productModel);
    }

    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater()
                    .inflate(R.layout.activity_product, parent, false);
        }

        TextView productName = convertView.findViewById(R.id.tvProductName);
        ImageView shareBtn = convertView.findViewById(R.id.ivShare);
        TextView productDescription = convertView.findViewById(R.id.tvProductDescription);
        TextView productPrice = convertView.findViewById(R.id.tvProductPrice);
        TextView productPurchased = convertView.findViewById(R.id.tvPurchased);
        ImageView ivProductImage = convertView.findViewById(R.id.ivProductImage);

        ProductModel productModel = getItem(position);
        productName.setText(productModel.getName());
        productDescription.setText(productModel.getDescription());
        productPurchased.setText(productModel.getPurchased());
        productPrice.setText("£" + productModel.getPrice());
        Glide.with(getContext())
                .load(productModel.getFilePath())
                .placeholder(R.mipmap.ic_launcher)
                .into(ivProductImage);

        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareBody = productName.getText().toString()
                        + "\n"
                        + productPrice.getText().toString()
                        + "\n"
                        + productDescription.getText().toString();

                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, productModel.getName());
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);

                getContext().startActivity(Intent.createChooser(sharingIntent, getContext().getResources().getString(R.string.app_name)));
            }
        });

        Log.e("TAG", "getView: " + productModel.getFilePath());
        return convertView;
    }
}
