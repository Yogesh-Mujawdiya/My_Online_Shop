package com.my.online_shop.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.my.online_shop.AddProductActivity;
import com.my.online_shop.Class.Product;
import com.my.online_shop.Controller.StoreData;
import com.my.online_shop.OrderActivity;
import com.my.online_shop.ProductActivity;
import com.my.online_shop.R;

import java.util.ArrayList;
import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> implements Filterable {
    private List<Product> productsList;
    private List<Product> productsListFull;
    StoreData controller;
    Context context;
    boolean IsAdmin = false;
    String CategoryName;

    class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, imageViewAddToCard;
        TextView textViewName, textViewPrice, textViewDelete;
        RelativeLayout Card;

        ProductViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.ProductIcon);
            textViewName = itemView.findViewById(R.id.Name);
            textViewPrice = itemView.findViewById(R.id.Price);
            textViewDelete = itemView.findViewById(R.id.DeleteProduct);
            Card = itemView.findViewById(R.id.ProductCard);
            imageViewAddToCard = itemView.findViewById(R.id.AddToCard);
        }
    }

    public ProductAdapter(Context context, List<Product> list, String category) {
        this.productsList = list;
        isAdmin();
        controller = new StoreData(context);
        CategoryName = category;
        productsListFull = new ArrayList<>(productsList);
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card,
                parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductViewHolder holder, int position) {
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

        if(IsAdmin) {
            holder.textViewDelete.setVisibility(View.VISIBLE);
            holder.textViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete " + currentItem.getName() + " Product")
                            .setMessage("Are you sure you want to delete this entry?")
                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase.getInstance().getReference("Category/" + CategoryName + "/" + currentItem.getName()).setValue("");
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/Product/");
                                    StorageReference photoRef = storageRef.child(currentItem.getName());
                                    photoRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // File deleted successfully
                                            Toast.makeText(context, "Successfully Deleted !!", Toast.LENGTH_SHORT).show();
                                            Log.d("Success", "onSuccess: deleted file");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Uh-oh, an error occurred!
                                            Log.d("Error", "onFailure: did not delete file");
                                        }
                                    });
                                }
                            })

                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setNegativeButton(android.R.string.no, null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                }
            });
        }


        holder.imageViewAddToCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controller.addToCard(currentItem);
                Toast.makeText(context, "Product Successfully Added in your Card", Toast.LENGTH_SHORT).show();
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


    public void isAdmin(){
        DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference("Admin");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (final DataSnapshot user : dataSnapshot.getChildren()) {
                        if(user.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                            IsAdmin = true;
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
}