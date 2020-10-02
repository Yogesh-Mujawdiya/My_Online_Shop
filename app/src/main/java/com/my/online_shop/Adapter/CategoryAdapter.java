package com.my.online_shop.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.my.online_shop.Class.Category;
import com.my.online_shop.Controller.StoreData;
import com.my.online_shop.ProductActivity;
import com.my.online_shop.R;
import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> implements Filterable {
    private List<Category> categoriesList;
    private List<Category> categoriesListFull;
    Context context;
    StoreData controller;

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewName, textViewCount, textViewDelete;
        RelativeLayout Card;

        CategoryViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.CategoryIcon);
            textViewName = itemView.findViewById(R.id.Name);
            textViewCount = itemView.findViewById(R.id.Count);
            textViewDelete = itemView.findViewById(R.id.DeleteCategory);
            Card = itemView.findViewById(R.id.CategoryCard);
        }
    }

    public CategoryAdapter(Context context, List<Category> list) {
        this.categoriesList = list;
        controller = new StoreData(context);
        categoriesListFull = new ArrayList<>(categoriesList);
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card,
                parent, false);
        return new CategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CategoryViewHolder holder, int position) {
        final Category currentItem = categoriesList.get(position);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/Category/");

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
        holder.textViewCount.setText(Integer.toString(currentItem.getItemCount()));
        if(controller.isAdmin()) {
            holder.textViewDelete.setVisibility(View.VISIBLE);
            holder.textViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(context)
                            .setTitle("Delete " + currentItem.getName() + " Category")
                            .setMessage("It will also Delete All Product Inside it? ")
                            // Specifying a listener allows you to take an action before dismissing the dialog.
                            // The dialog is automatically dismissed when a dialog button is clicked.
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    FirebaseDatabase.getInstance().getReference("Category/" + currentItem.getName()).setValue(null);
                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReferenceFromUrl("gs://golu-online-shop.appspot.com/Category/");
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
        holder.Card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ProductActivity.class);
                intent.putExtra("Id", currentItem.getId());
                intent.putExtra("Name", currentItem.getName());
                view.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoriesList.size();
    }

    @Override
    public Filter getFilter() {
        return categoryFilter;
    }

    private Filter categoryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Category> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(categoriesListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Category item : categoriesListFull) {
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
            categoriesList.clear();
            categoriesList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}