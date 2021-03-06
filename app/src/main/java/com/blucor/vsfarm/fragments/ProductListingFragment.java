package com.blucor.vsfarm.fragments;


import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blucor.vsfarm.Model.CartItem;
import com.blucor.vsfarm.Model.CategoryProducts;
import com.blucor.vsfarm.R;
import com.blucor.vsfarm.activity.DrawerActivity;
import com.blucor.vsfarm.activity.Utils;
import com.blucor.vsfarm.extra.AppSettings;
import com.blucor.vsfarm.extra.Preferences;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;


public class ProductListingFragment extends Fragment {

    //view
    View view;

    //recyclerview
    RecyclerView recyclerView;
    String  product_id;
    String  user_id;
    String  product_name;
    String  product_image;
    String  product_price;
    String product_qnty;
    EditText prodcutQnty;

//    LinearLayout llAddtocart;
//    RelativeLayout rlAddtocart;


    Dialog dialog;


    //gridlayoutmanager
    GridLayoutManager mGridLayoutManager;

    //Textview
    TextView tvHeaderText;
    EditText searchView;
    TextView noProduct;

    ProductAdapter mAdapter;
    //public static final String cart = "http://vsfarma.blucorsys.in/cartItem.php";
    public static final String cart = "http://vsfarma.blucorsys.in/update_qty.php";
    public static final String url = "http://vsfarma.blucorsys.in/row.php";


    private List<CategoryProducts> productList;
    private  List<CartItem> cartItem;


    //Arraylist
    ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();

    //preferences
    Preferences preferences;


    String value;
    public static JSONObject jsonObject;

    LinearLayout layout_empty;
    Button bAddNew;
    private Collection<? extends CategoryProducts> values;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.productfragment, container, false);

        recyclerView = view.findViewById(R.id.productRecyclerView);

        searchView=view.findViewById(R.id.EdSearchView);
        noProduct=view.findViewById(R.id.no_product);
      //  prodcutQnty=view.findViewById(R.id.etQuantity);


        Log.d("data",""+value);

        productList=new ArrayList<>();
        cartItem=new ArrayList<>();

        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                 mAdapter.getFilter().filter(s);

                // TODO Auto-generated method stub
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {

                // filter your list from your input
                filter(s.toString());
                //you can use runnable postDelayed like 500 ms to delay search text
            }
        });

        if (Utils.isNetworkConnectedMainThred(getActivity())) {
            jsonProducts();
            ProductProgressBar();
            dialog.show();
        } else {

            Toasty.error(getActivity(), "No Internet Connection!", Toast.LENGTH_SHORT).show();
        }



//        tvHeaderText = view.findViewById(R.id.tvHeaderText);
        preferences=new Preferences(getActivity());

        //page
        AppSettings.fromPage="2";

        DrawerActivity.tvHeaderText.setText("Products");
        DrawerActivity.marquee.setVisibility(View.GONE);
        DrawerActivity.iv_menu.setImageResource(R.drawable.ic_back);
        DrawerActivity.rlsearchview.setVisibility(View.GONE);

        DrawerActivity.iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DrawerActivity.class);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });

        layout_empty = view.findViewById(R.id.layout_empty);
        bAddNew = view.findViewById(R.id.bAddNew);
        bAddNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), DrawerActivity.class);
                i.putExtra("page", "home");
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
            }
        });
        //Back
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        Intent intent = new Intent(getActivity(), DrawerActivity.class);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.slide_left, R.anim.slide_right);

                        return true;
                    }
                }
                return false;
            }
        });
        return view;
    }

    private void ProductProgressBar() {
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

    private void filter(String toString) {
        List<CategoryProducts> temp = new ArrayList();
        for(CategoryProducts d: productList){
            if(d.getProduct_name().toLowerCase().contains(toString.toLowerCase())){
                temp.add(d);
            }else{
                //noProduct.setVisibility(View.VISIBLE);
                //Toast.makeText(getActivity(),"No Product Found",Toast.LENGTH_SHORT).show();
            }
        }
        //update recyclerview
        mAdapter.updateList(temp);
    }

    private void jsonProducts() {
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                dialog.cancel();
                Log.e("response",response);

                try{
                    JSONArray jsonArray = new JSONArray(response);
                    for (int i = 0; i < jsonArray.length(); i++) {

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        CategoryProducts product = new CategoryProducts();
                        String category_id=jsonObject.getString("category_id");
                        String product_id=jsonObject.getString("product_id");
                        String product_name=jsonObject.getString("product_name");
                        String product_price=jsonObject.getString("product_price");
                        String product_image="http://vsfarma.blucorsys.in/productimages/"+jsonObject.getString("product_image");

                        //String product_desc=jsonObject.getString("product_desc");

                       /* String res= product_image.replace("//","");
                        Log.e("responsee",""+res);*/
                        //category_image.replaceAll("\\/","//");
                        product.setCategory_id(category_id);
                        product.setProduct_id(product_id);
                        product.setProduct_name(product_name);
                        product.setProduct_image(product_image);
                        product.setProduct_price(product_price);
                        //product.setProduct_desc(product_desc);

                        productList.add(product);
                    }

                    setAdapter();
                }
                catch (JSONException e) {
                    Log.d("JSONException", e.toString());
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
                parameters.put("category_id",getArguments().getString("category_id"));
                return parameters;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(request);
    }

    private class Holder extends RecyclerView.ViewHolder {

        TextView tvFinalprice;
        TextView tvProductName;
        TextView tvDesc;
        TextView tvQty;
        TextView cart_item_number;
        ImageView ivProductimage;

        ImageButton cart_quant_minus;
        ImageButton cart_quant_add;

        RelativeLayout rlAddtocart;
        RelativeLayout rlOutofstock;

        LinearLayout llAddtocart;

        CardView cardView;

        public Holder(View itemView) {
            super(itemView);

            ivProductimage=itemView.findViewById(R.id.ivProductimage);
            tvQty=itemView.findViewById(R.id.tvQty);
            cart_item_number=itemView.findViewById(R.id.cart_item_number);
            tvProductName=itemView.findViewById(R.id.tvProductName);
            tvFinalprice=itemView.findViewById(R.id.tvFinalprice);
            tvDesc=itemView.findViewById(R.id.desc);
            rlAddtocart=itemView.findViewById(R.id.rlAddtocart);
            rlOutofstock=itemView.findViewById(R.id.rlOutofstock);
            cardView=itemView.findViewById(R.id.cardView);
            llAddtocart=itemView.findViewById(R.id.llAddtocart);
            cart_quant_minus=itemView.findViewById(R.id.cart_quant_minus);
            cart_quant_add=itemView.findViewById(R.id.cart_quant_add);

        }
    }
    private void setAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        mAdapter = new ProductAdapter(productList,getActivity());
        recyclerView.setAdapter(mAdapter);
    }
    private class ProductAdapter extends RecyclerView.Adapter<Holder> implements Filterable {

        private List<CategoryProducts> mModel;
        private Context mContext;

        public ProductAdapter(List<CategoryProducts>mModel, Context mContext) {
            this.mModel=mModel;
            this.mContext=mContext;

        }
        public void updateList(List<CategoryProducts> list){
            mModel = list;
            notifyDataSetChanged();
        }

        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {

            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.products_list_items, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull final Holder holder, final int position) {

            holder.tvProductName.setText(mModel.get(position).getProduct_name());
            Glide.with(mContext)
                    .load(mModel.get(position).getProduct_image())
                    .into(holder.ivProductimage);
            //holder.tvDesc.setText(mModel.get(position).getProduct_desc());
            holder.tvFinalprice.setText(mModel.get(position).getProduct_price());
            holder.tvDesc.setText(mModel.get(position).getProduct_id());


            holder.rlAddtocart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    productQuantityPopUp(holder.llAddtocart,holder.rlAddtocart);
                    Log.e("","idddd"+product_id);

                    product_id=productList.get(position).getProduct_id();
                    product_name=productList.get(position).getProduct_name();
                    product_image=productList.get(position).getProduct_image();
                    product_price=productList.get(position).getProduct_price();

                  /*  int counter=preferences.getInt("count");
                    int totalcount=counter+1;
                    preferences.set("count",totalcount);
                    preferences.commit();

                    ShakeAnimation(DrawerActivity.tvCount);
                    DrawerActivity.tvCount.setText(""+totalcount);*/

//                    holder.rlAddtocart.setVisibility(View.GONE);
//                    holder.llAddtocart.setVisibility(View.VISIBLE);

                /*    Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(100);*/
                }
            });

            holder.cart_quant_add.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int count = Integer.parseInt(holder.cart_item_number.getText().toString());


                    holder.cart_item_number.setText(String.valueOf(count));

                    Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(100);

                    count=count+1;

                    holder.cart_item_number.setText(String.valueOf(count));

                    int counter=preferences.getInt("count");
                    int totalcount=counter+1;
                    preferences.set("count",totalcount);
                    preferences.commit();

                    ShakeAnimation(DrawerActivity.tvCount);

                    DrawerActivity.tvCount.setText(""+totalcount);

                }
            });

            holder.cart_quant_minus.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    int count = Integer.parseInt(holder.cart_item_number.getText().toString());

                    if (count > 1) {
                        Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        vibe.vibrate(100);
                        count=count-1;

                        int counter=preferences.getInt("count");
                        int totalcount=counter-1;
                        preferences.set("count",totalcount);
                        preferences.commit();
                        ShakeAnimation(DrawerActivity.tvCount);
                        DrawerActivity.tvCount.setText(""+totalcount);
                        holder.cart_item_number.setText(String.valueOf(count));

                    } else {
                        int counter=preferences.getInt("count");
                        int totalcount=counter-1;
                        preferences.set("count",totalcount);
                        preferences.commit();
                        ShakeAnimation(DrawerActivity.tvCount);
                        DrawerActivity.tvCount.setText(""+totalcount);
                        holder.rlAddtocart.setVisibility(View.VISIBLE);
                        holder.llAddtocart.setVisibility(View.GONE);
                    }
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

        @Override
        public Filter getFilter() {
            return filter;
        }
        Filter filter=new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                ArrayList<CategoryProducts> filteredList=new ArrayList<>();
                if (charSequence.toString().isEmpty()){
                    filteredList.addAll(productList);
                }else {
                    for (CategoryProducts product:productList){
                        if (product.getProduct_name().toLowerCase().contains(charSequence.toString().toLowerCase())){
                            filteredList.add(product);
                        }
                    }
                }
                FilterResults filterResults=new FilterResults();
                filterResults.values=filteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mModel.clear();
               mModel.addAll((Collection<? extends CategoryProducts>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

    private void productQuantityPopUp(final LinearLayout llLayout,final RelativeLayout rlLayout) {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.quanty_log);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        final ImageView ivClose=dialog.findViewById(R.id.ivClose);
        final EditText etQuantity=dialog.findViewById(R.id.etQuantity);
        TextView tvOk=dialog.findViewById(R.id.btnAddCart);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                 //llAddtocart.setVisibility(View.VISIBLE);
            }
        });

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                        if(etQuantity.getText().toString().trim().isEmpty())
                        {
                            etQuantity.setError("Please enter quantity");
                            etQuantity.requestFocus();
                        }
                        else
                        {
                            if (Utils.isNetworkConnectedMainThred(getActivity())) {


                                llLayout.setVisibility(View.VISIBLE);
                                rlLayout.setVisibility(View.GONE);

                                AddtoCart(etQuantity.getText().toString());
                                //rlAddtocart.setVisibility(View.GONE);
                                // rlAddtocart.setVisibility(View.GONE);
                                // holder.llAddtocart.setVisibility(View.VISIBLE);
                                int counter=preferences.getInt("count");
                                int totalcount=counter+1;
                                preferences.set("count",totalcount);
                                preferences.commit();
                                ShakeAnimation(DrawerActivity.tvCount);
                                DrawerActivity.tvCount.setText(""+totalcount);
                                Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                vibe.vibrate(100);

                                //ProductQntyUpdate();
                            } else {
                                Toasty.error(getActivity(), "No Internet Connection!", Toast.LENGTH_LONG).show();
                            }
                            dialog.cancel();
                        }

            }
        });
    }


    private void AddtoCart(final String qty) {

        StringRequest rqst = new StringRequest(Request.Method.POST, cart, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                //dialog.cancel();
                Log.e("response11111",response);
                Toasty.success(getActivity(), "Item Added", Toast.LENGTH_SHORT).show();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //dialog.cancel();
                Log.e("error_response", "" + error);
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> param = new HashMap<String, String>();
                param.put("product_id",product_id);
                param.put("user_id",preferences.get("user_id"));
                param.put("product_name",product_name);
                param.put("product_image",product_image);
                param.put("product_price",product_price);
                param.put("product_quantity",qty);
                Log.d("msg","parameters"+param);
                return param;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(getActivity());
        requestQueue.add(rqst);
    }
    public void replaceFragmentWithAnimation(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }

    double roundTwoDecimals(double d) {
        DecimalFormat twoDForm = new DecimalFormat("#");
        return Double.valueOf(twoDForm.format(d));
    }

    public void ProductDescriptionPopup() {
        final Dialog dialog = new Dialog(getActivity(), android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alertdialog);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

        ImageView ivClose=dialog.findViewById(R.id.ivClose);
        TextView tvOk=dialog.findViewById(R.id.tvOk);

        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        tvOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

    }

    public void ShakeAnimation(View view)
    {
        final Animation animShake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
        view.startAnimation(animShake);
    }

}
