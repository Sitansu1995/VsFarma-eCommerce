package com.blucor.vsfarm.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blucor.vsfarm.R;
import com.blucor.vsfarm.activity.DrawerActivity;
import com.blucor.vsfarm.extra.Preferences;


public class OrderDetailsFragment extends Fragment {
    TextView orderId,tvOrderDate,tvProductName,tvOrderTotal;
    TextView tvName,tvAddress,tvMobile,tvSize;
    View v;
    Preferences preferences;
    String contact;
    int Total_price=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_order_detais, container, false);
        orderId=v.findViewById(R.id.tvOrderId);
        tvOrderDate=v.findViewById(R.id.tvOrderDate);
        tvProductName=v.findViewById(R.id.tvProductname);
        tvOrderTotal=v.findViewById(R.id.tvOrderTotal);
        tvName=v.findViewById(R.id.tvName);
        tvAddress=v.findViewById(R.id.tvAddress);
        tvMobile=v.findViewById(R.id.tvContact);
        tvSize=v.findViewById(R.id.tvSize);

        DrawerActivity.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                replaceFragmentWithAnimation(new OrderFragment());
            }
        });
        preferences=new Preferences(getActivity());

        tvName.setText(preferences.get("name"));
        tvAddress.setText(preferences.get("address"));
        tvMobile.setText(preferences.get("contact"));

        Bundle b = getArguments();
        String order_id = b.getString("order_id");
        String order_name=b.getString("product_name");
        String order_date=b.getString("order_date_month");
        String order_qnty=b.getString("product_quantity");
        String product_price=b.getString("product_price");

        tvSize.setText(order_qnty);
        orderId.setText("#"+order_id);
        tvOrderDate.setText(order_date);
        tvProductName.setText(order_name);

        //tvOrderTotal.setText(product_price);
        int price=Integer.parseInt(product_price);
        int qty=Integer.parseInt(order_qnty);
        Total_price = Total_price + (qty* price);
        tvOrderTotal.setText("\u20b9 " +Total_price);

        Log.e("contact",""+contact);
        return v;
    }
    public void replaceFragmentWithAnimation(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
}