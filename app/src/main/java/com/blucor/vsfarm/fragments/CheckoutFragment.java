package com.blucor.vsfarm.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blucor.vsfarm.Model.CartItem;
import com.blucor.vsfarm.R;
import com.blucor.vsfarm.activity.AdminDashActivity;
import com.blucor.vsfarm.activity.DrawerActivity;
import com.blucor.vsfarm.activity.Utils;
import com.blucor.vsfarm.extra.Preferences;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class CheckoutFragment extends Fragment {

    //view
    View v;

    //recyclerview
    RecyclerView recyclerview;

    private List<CartItem> cartList;
    private CartAdapter cartAdapter;
    public static final String cart_url = "http://vsfarma.blucorsys.in/fetchCartItem.php";
    public static final String cart_delete = "http://vsfarma.blucorsys.in/deleteCartItem.php";
    public static final String continue_order = "http://vsfarma.blucorsys.in/checkout_order.php";
    public static final String order_billling = "http://vsfarma.blucorsys.in/order_billing.php";



    int final_price= 0;
    String product_id;
    //Gridlayoutmanger
    GridLayoutManager mGridLayoutManager;
    int result;
    int Total_price=0;

    //Textview
    TextView tvProceed;
    TextView tvChange;
    TextView tvCartPrice;


    TextView Username,UserAddress;
    TextView checkoutPrice;
    TextView totalAmount;
    Dialog dialog;
    LinearLayout llcartItem;
    ImageView emptyCart;


    //Preference
    Preferences preferences;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v=inflater.inflate(R.layout.fragment_checkout, container, false);

        recyclerview=v.findViewById(R.id.recyclerview);
        cartList=new ArrayList<>();

        tvProceed=v.findViewById(R.id.tvProceed);
        tvChange=v.findViewById(R.id.tvChange);
        Username =v.findViewById(R.id.tvAddress);
        UserAddress=v.findViewById(R.id.tvPhoneNumber);
        checkoutPrice=v.findViewById(R.id.tvCheckoutprice);
        tvCartPrice=v.findViewById(R.id.tvCartprice);

        emptyCart = v.findViewById(R.id.gif);
        llcartItem = v.findViewById(R.id.llcartItem);

        totalAmount=v.findViewById(R.id.tvTotalAmount);


        preferences=new Preferences(getActivity());

        Username.setText(preferences.get("name"));
        UserAddress.setText(preferences.get("address"));


       //setadapter
    /*    mGridLayoutManager = new GridLayoutManager(getActivity(), 1);
        recyclerview.setLayoutManager(mGridLayoutManager);
        recyclerview.setAdapter(new CartAdapter());*/
        DrawerActivity.ivCart.setVisibility(View.GONE);
        DrawerActivity.tvCount.setVisibility(View.GONE);

        if (Utils.isNetworkConnectedMainThred(getActivity())) {
            cartFragmentItem();
            ProgressForCheckout();
            dialog.show();
        } else {
            Toasty.error(getActivity(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }

        //setvalue
        DrawerActivity.tvHeaderText.setText("Checkout");
        DrawerActivity.iv_menu.setImageResource(R.drawable.ic_back);
        //Onclick listener
        DrawerActivity.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               replaceFragmentWithAnimation(new CartFragment());
            }
        });

        tvProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utils.isNetworkConnectedMainThred(getActivity())) {
                     OrderConfirm();
                     BillingMail();
                     SuccessPopup();

                    dialog.show();
                } else {
                    Toasty.error(getActivity(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                }
                AdminDashActivity.count++;
            }
        });

       tvChange.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               replaceFragmentWithAnimation(new AddAddressFragment());
           }
       });

        return v;
    }

    private void BillingMail() {
        StringRequest request = new StringRequest(Request.Method.POST,order_billling, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Mail send",response);

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e("error_response", "" + error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("user_id",preferences.get("user_id"));
                return parameters;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(request);

    }

    private void ProgressForCheckout() {
        dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progress_for_cart);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(false);
    }

    private void  OrderConfirm() {
        StringRequest request = new StringRequest(Request.Method.POST,continue_order, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.cancel();
                Log.e("place order",response);
                try {
                    JSONObject jsonObject=new JSONObject(response);

                    if(jsonObject.getString("success").equalsIgnoreCase("order placed successfully"))
                    {
                        preferences.set("order_id",jsonObject.getString("order_id"));
                        preferences.set("order_date_month",jsonObject.getString("order_date_month"));
                        preferences.commit();
                    }
                    //replaceFragmentWithAnimation(new CheckoutFragment());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.cancel();
                Log.e("error_response", "" + error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("user_id",preferences.get("user_id"));
              /*  parameters.put("product_id",);
                parameters.put("product_name",);
                parameters.put("product_price",);
                parameters.put("product_quantity",);
                parameters.put("order_total",);*/
                Log.e("check","wwww"+parameters);
                return parameters;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(request);

    }

    private void cartFragmentItem() {
        StringRequest request = new StringRequest(Request.Method.POST,cart_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.cancel();
                try{
                    Log.e("checkout",response);
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        CartItem cart = new CartItem();
                        String cart_id=jsonObject.getString("cart_id");
                        String user_id=jsonObject.getString("user_id");
                        String product_id=jsonObject.getString("product_id");
                        String product_name=jsonObject.getString("product_name");
                        String product_price=jsonObject.getString("product_price");
                        String product_image=jsonObject.getString("product_image");
                        String product_qnty=jsonObject.getString("product_quantity");


                        String res= product_image.replace("//","");
                        //Log.e("responsee",""+res);
                        //category_image.replaceAll("\\/","//");
                        cart.setCart_id(cart_id);
                        cart.setUser_id(user_id);
                        cart.setProduct_id(product_id);
                        cart.setProduct_name(product_name);
                        cart.setProduct_image(product_image);
                        cart.setProduct_price(product_price);
                        cart.setProduct_quantity(product_qnty);
                        cartList.add(cart);

                        int price=Integer.parseInt(product_price);
                        int qty=Integer.parseInt(product_qnty);

                        //Total_price = Total_price + Integer.parseInt(product_price*product_qnty);
                        Total_price = Total_price + ( price* qty);
                        //final_price = final_price + Integer.parseInt(cartList.get(i).getProduct_price());
                        Log.e("price add",""+Total_price);
                        checkoutPrice.setText(String.valueOf( Total_price));
                        checkoutPrice.setText("Total Amount \u20b9" +Total_price);
                        Log.e("total price","price"+Total_price);

                    }
                   // totalAmount.setText("Pay \u20b9" +final_price);
                    totalAmount.setText("\u20b9 " +Total_price);

                    setAdapter();
                }
                catch (JSONException e) {
                    Log.d("JSONException", e.toString());
                }
               /* if (cartList.isEmpty()){
                    llcartItem.setVisibility(View.GONE);
                    emptyCart.setVisibility(View.VISIBLE);
                } else {
                    llcartItem.setVisibility(View.VISIBLE);
                    emptyCart.setVisibility(View.GONE);
                }*/
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.cancel();
                Log.e("error_response", "" + error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                parameters.put("user_id",preferences.get("user_id"));
                return parameters;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(request);
    }

    private void setAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerview.setLayoutManager(linearLayoutManager);
        cartAdapter = new CartAdapter(cartList, getActivity());
        Log.e("check adapter", "cart adapter" + cartAdapter);
        recyclerview.setAdapter(cartAdapter);
    }

    public void replaceFragmentWithAnimation(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }
    public void SuccessPopup() {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alert);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        //  dialog.show();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawableResource(R.color.black_trans);
        dialog.show();

        Thread timerThread = new Thread() {
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    dialog.dismiss();
                    preferences.set("count", 0);
                    preferences.commit();
                    replaceFragmentWithAnimation(new OrderConfirmationFragment());
                }
            }
        };

        timerThread.start();
    }

    //=============================Adapter====================================================//
    private class CartHolder extends RecyclerView.ViewHolder {
        TextView tvOldPrice;
        TextView cart_item_number;
        TextView tvSave;
        ImageButton cart_quant_minus;
        ImageButton cart_quant_add;
        TextView tvProductName,qntyPrice;
        TextView tvFinalprice;
        EditText productQuantity;
        ImageButton checkquantity;
        TextView cart_item_delete;
        ImageView cart_item_image;
        TextView tvDelCharge;
        TextView tvSize;


        public CartHolder(View itemView) {
            super(itemView);
            tvOldPrice = itemView.findViewById(R.id.tvOldPrice);
            tvFinalprice = itemView.findViewById(R.id.price);
            cart_item_number = itemView.findViewById(R.id.cart_item_number);
            cart_quant_minus = itemView.findViewById(R.id.cart_quant_minus);
            cart_quant_add = itemView.findViewById(R.id.cart_quant_add);
            tvSave = itemView.findViewById(R.id.tvSave);
            productQuantity = itemView.findViewById(R.id.quantity);
            tvDelCharge = itemView.findViewById(R.id.tvDelCharge);
            tvSize = itemView.findViewById(R.id.tvSize);
            checkquantity=itemView.findViewById(R.id.check);

            qntyPrice = itemView.findViewById(R.id.totalPrice);
            cart_item_image = itemView.findViewById(R.id.cart_item_image);
            cart_item_delete = itemView.findViewById(R.id.cart_item_delete);
            tvProductName = itemView.findViewById(R.id.tvcartProductName);
        }
    }

    private class CartAdapter extends RecyclerView.Adapter<CartHolder> {
        private List<CartItem> mModel;
        private Context mContext;

        public CartAdapter(List<CartItem> mModel, Context mContext) {
            this.mModel = mModel;
            this.mContext = mContext;
        }
        public CartHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CartHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_items, parent, false));
        }
        @Override
        public void onBindViewHolder(@NonNull final CartHolder holder, final int position) {

            holder.tvProductName.setText(mModel.get(position).getProduct_name());
            Glide.with(mContext)
                    .load(mModel.get(position).getProduct_image())
                    .into(holder.cart_item_image);
            holder.tvFinalprice.setText(mModel.get(position).getProduct_price());
            holder.productQuantity.setText(mModel.get(position).getProduct_quantity());
            holder.productQuantity.setFocusable(false);
            holder.cart_item_delete.setVisibility(View.GONE);

            String productQnty= String.valueOf(holder.productQuantity.getText());

            holder.checkquantity.setVisibility(View.GONE);
            int productprice;
            int qnty;
            productprice = Integer.parseInt(mModel.get(position).getProduct_price());
            qnty = Integer.parseInt(mModel.get(position).getProduct_quantity());
            result=productprice*qnty;
            Log.e("result",""+result);
            holder.qntyPrice.setText("\u20b9" +result);
            holder.cart_quant_add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int count = Integer.parseInt(holder.cart_item_number.getText().toString());
                    count=count+1;
                    holder.cart_item_number.setText(String.valueOf(count));
                    Vibrator vibe = (Vibrator)getActivity(). getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(100);
                    holder.cart_item_number.setText(String.valueOf(count));
                }
            });
            holder.cart_quant_minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int count = Integer.parseInt(holder.cart_item_number.getText().toString());

                    if (count > 1) {
                        count=count-1;

                        holder.cart_item_number.setText(String.valueOf(count));
                    }
                    else {

                    }
                }
            });
            holder.cart_item_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utils.isNetworkConnectedMainThred(getActivity())) {
                        deleteCartItem();
                        //ProgressForChe();
                        dialog.show();
                    } else {
                        Toasty.error(getActivity(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
                    }
                    product_id=cartList.get(position).getProduct_id();
                    mModel.remove(position);
                    notifyDataSetChanged();

                }
            });
        }
        public int getItemCount() {
            return mModel.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }

    private void deleteCartItem() {
        StringRequest request = new StringRequest(Request.Method.POST,cart_delete, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                dialog.cancel();
                Toasty.success(getActivity(), "Item Deleted Successfully", Toast.LENGTH_SHORT).show();

                //Toast.makeText(getActivity(),"Item Deleted Successfully",Toast.LENGTH_SHORT).show();
                replaceFragmentWithAnimation(new CheckoutFragment());
                Log.e("delete",response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                dialog.cancel();
                Log.e("error_response", "" + error);
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("product_id",product_id);
                Log.e("delete",""+params);
                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(request);
    }

}