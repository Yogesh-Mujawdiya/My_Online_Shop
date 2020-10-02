package com.my.online_shop.Adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.my.online_shop.Class.Product;
import com.my.online_shop.Controller.StoreData;
import com.my.online_shop.R;
import java.util.ArrayList;
import java.util.List;


public class PlaceOrderAdapter extends RecyclerView.Adapter<PlaceOrderAdapter.PlaceOrderViewHolder> implements Filterable {
    private List<Product> productsList;
    private List<Product> productsListFull;
    Context context;
    StoreData controller;

    class PlaceOrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName, textViewPrice, textViewAdd, textViewMinus;
        EditText editTextQuantity;
        Button removeProduct;
        RelativeLayout Card;

        PlaceOrderViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.SelectedProductIcon);
            textViewName = itemView.findViewById(R.id.SelectedProductName);
            textViewPrice = itemView.findViewById(R.id.SelectedProductPrice);
            textViewAdd = itemView.findViewById(R.id.SelectedProductAddQuantity);
            textViewMinus = itemView.findViewById(R.id.SelectedProductMinusQuantity);
            editTextQuantity = itemView.findViewById(R.id.SelectedProductQuantity);
            removeProduct = itemView.findViewById(R.id.SelectedProductRemoveBtn);
            Card = itemView.findViewById(R.id.ProductCard);
        }
    }

    public PlaceOrderAdapter(Context context, List<Product> list) {
        this.productsList = list;
        controller = new StoreData(context);
        productsListFull = new ArrayList<>(productsList);
        this.context = context;
    }

    @NonNull
    @Override
    public PlaceOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_product_layout,
                parent, false);
        return new PlaceOrderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaceOrderViewHolder holder, final int position) {
        final Product currentItem = productsList.get(position);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/Product/");

        storageRef.child(currentItem.getId()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(context).load(uri).into(holder.imageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
            }
        });
        holder.textViewName.setText(currentItem.getName());
        holder.textViewPrice.setText(String.format("%.2f ₹", currentItem.getPrice()));
        holder.editTextQuantity.setText(Integer.toString(currentItem.getQuantity()));

        holder.textViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.editTextQuantity.setText(Integer.toString(currentItem.getQuantity()+1));
            }
        });

        holder.textViewMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(currentItem.getQuantity()>0)
                    holder.editTextQuantity.setText(Integer.toString(currentItem.getQuantity()-1));
                else
                    Toast.makeText(context, "Quantity Should Not be Negative", Toast.LENGTH_SHORT).show();

            }
        });

        holder.removeProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productsList.remove(position);
                notifyItemRemoved(position);
                controller.updateCardProduct(productsList);
                notifyItemRangeChanged(position, productsList.size());
            }
        });

        holder.editTextQuantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                currentItem.setQuantity(Integer.parseInt(holder.editTextQuantity.getText().toString()));
                controller.updateCardProduct(productsList);
            }
        });
    }



    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Product> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productsListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Product item : productsListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productsList.clear();
            productsList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };


    public double getPrice() {
        double Price = 0;
        for (Product item : productsList)
            if (item.getQuantity()>0)
                Price+=item.getPrice()*item.getQuantity();
        return Price;
    }


}