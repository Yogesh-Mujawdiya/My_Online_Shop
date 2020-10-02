package com.my.online_shop.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.my.online_shop.Class.Product;
import com.my.online_shop.Controller.StoreData;
import com.my.online_shop.R;
import java.util.ArrayList;
import java.util.List;

public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.ProductViewHolder> implements Filterable {
    private List<Product> productsList;
    private List<Product> productsListFull;
    Context context;

    class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewPrice, textViewCategory;

        ProductViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.OrderProductName);
            textViewPrice = itemView.findViewById(R.id.OrderProductPrice);
            textViewCategory = itemView.findViewById(R.id.OrderProductCategory);
        }
    }

    public MyProductAdapter(Context context, List<Product> list) {
        this.productsList = list;
        productsListFull = new ArrayList<>(productsList);
        this.context = context;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_products_layout,
                parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final ProductViewHolder holder, int position) {
        final Product currentItem = productsList.get(position);
        holder.textViewName.setText(currentItem.getName());
        holder.textViewPrice.setText(String.format("%.2f ₹", currentItem.getPrice()));
        holder.textViewCategory.setText(currentItem.getCategory());
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

}