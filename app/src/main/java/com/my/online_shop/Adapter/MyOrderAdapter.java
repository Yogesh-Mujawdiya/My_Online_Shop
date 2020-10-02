package com.my.online_shop.Adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.online_shop.Class.Order;
import com.my.online_shop.Controller.StoreData;
import com.my.online_shop.R;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.PlaceOrderViewHolder> implements Filterable {
    private List<Order> ordersList;
    private List<Order> ordersListFull;
    Context context;
    StoreData controller;

    class PlaceOrderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewStatus, textViewPrice, textViewName, textViewMobile,
                textViewAddress, textViewPayment, textViewTime;
        RecyclerView recyclerViewProducts;
        RelativeLayout relativeLayout;


        PlaceOrderViewHolder(View itemView) {
            super(itemView);
            textViewStatus = itemView.findViewById(R.id.OrderProductStatus);
            textViewPrice = itemView.findViewById(R.id.OrderProductPrice);
            textViewPayment = itemView.findViewById(R.id.OrderProductPayment);
            textViewName = itemView.findViewById(R.id.OrderByName);
            textViewMobile = itemView.findViewById(R.id.OrderByMobileNo);
            textViewAddress = itemView.findViewById(R.id.OrderByAddress);
            textViewTime = itemView.findViewById(R.id.OrderProductTime);
            relativeLayout = itemView.findViewById(R.id.OrderCard);
            recyclerViewProducts = itemView.findViewById(R.id.OrderProducts);
        }
    }

    public MyOrderAdapter(Context context, List<Order> list) {
        this.ordersList = list;
        controller = new StoreData(context);
        ordersListFull = new ArrayList<>(ordersList);
        this.context = context;
    }

    @NonNull
    @Override
    public PlaceOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout,
                parent, false);
        return new PlaceOrderViewHolder(v);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull final PlaceOrderViewHolder holder, final int position) {
        final Order currentItem = ordersList.get(position);
        Log.d("MY", currentItem.getProductList().get(0).getName());
        holder.textViewStatus.setText(currentItem.getDeliver());
        holder.textViewPrice.setText(String.format("%.2f ₹", currentItem.getAmount()));
        Timestamp timestamp = new Timestamp(currentItem.getTime());
        holder.textViewTime.setText(timestamp.toString());
        holder.textViewPayment.setText(currentItem.getPayment());
        holder.textViewName.setText(currentItem.getName());
        holder.textViewMobile.setText(currentItem.getMobile());
        holder.textViewAddress.setText(currentItem.getAddress());

        MyProductAdapter adapter = new MyProductAdapter(context,currentItem.getProductList());
        holder.recyclerViewProducts.setAdapter(adapter);
        holder.recyclerViewProducts.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        holder.recyclerViewProducts.setLayoutManager(layoutManager);
        holder.recyclerViewProducts.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerViewProducts.setAdapter(adapter);
        if (controller.isAdmin()){
            final DatabaseReference mDatabase  = FirebaseDatabase.getInstance().getReference("Order/"+currentItem.getId());
                holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                            final Dialog dialog = new Dialog(context);
                            dialog.setContentView(R.layout.dialog_change_status);
                            dialog.setTitle("Change Delivery Status");
                            dialog.findViewById(R.id.StatusDone).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    currentItem.setDeliver("Delivered");
                                    mDatabase.child("deliver").setValue("Delivered");
                                    mDatabase.child("payment").setValue("Done");
                                    currentItem.setPayment("Done");
                                    holder.textViewPayment.setText(currentItem.getPayment());
                                    holder.textViewStatus.setText(currentItem.getDeliver());
                                    dialog.dismiss();
                                }
                            });
                            dialog.findViewById(R.id.StatusCancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    currentItem.setDeliver("Canceled");
                                    mDatabase.child("deliver").setValue("Canceled");
                                    holder.textViewPayment.setText(currentItem.getPayment());
                                    holder.textViewStatus.setText(currentItem.getDeliver());
                                    dialog.dismiss();
                                }
                            });
                            dialog.show();
                    }
                });
        }

    }

    @Override
    public int getItemCount() {
        return ordersList.size();
    }

    @Override
    public Filter getFilter() {
        return productFilter;
    }

    private Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Order> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(ordersListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Order item : ordersListFull) {
                    if (item.getDeliver().toLowerCase().contains(filterPattern)) {
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
            ordersList.clear();
            ordersList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

}