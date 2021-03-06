package com.blucor.vsfarm.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.blucor.vsfarm.R;
import com.blucor.vsfarm.extra.Preferences;
import com.blucor.vsfarm.fragments.AboutUsFragment;
import com.blucor.vsfarm.fragments.CartFragment;
import com.blucor.vsfarm.fragments.DashboardFragment;
import com.blucor.vsfarm.fragments.OrderFragment;
import com.blucor.vsfarm.fragments.ShareFragment;
import com.google.android.material.navigation.NavigationView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.FileUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;


public class DrawerActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    RelativeLayout rr_header;

    public static ImageView iv_menu, ivHome;

    DrawerLayout drawer;
    Dialog dialog;

    public static LinearLayout llmain;
    public static RelativeLayout rlsearchview;

    public static TextView tvCount;

    public static TextView tvHeaderText;

    MenuItem my_account;

    public static TextView marquee;

    public static NavigationView navigationView;

    String status;

    Preferences preferences;

    public static ImageView ivCart;

    //other
    public static int backPressed = 0;

    RelativeLayout rlHome;
    RelativeLayout rlSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        marquee=findViewById(R.id.marquee);
        marquee.setSelected(true);


        Intialize();

        //Setvalue
        tvHeaderText.setText("Home");

        iv_menu.setOnClickListener(this);

        ivCart.setOnClickListener(this);

       // setvalue
        tvCount.setText(String.valueOf(preferences.getInt("count")));

//        tvCount = findViewById(R.id.tvCount);
//        ivHome = findViewById(R.id.ivHome);
//        View headerview = navigationView.getHeaderView(0);
//        rlHome = headerview.findViewById(R.id.rlHome);
//        rlHome.setOnClickListener(this);
//        pref = new Preferences(this);


        replaceFragmentWithAnimation(new DashboardFragment());

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        if (navigationView != null) {
            Menu menu = navigationView.getMenu();
            if (preferences.get("login").equalsIgnoreCase("yes")) {

                menu.findItem(R.id.logout).setTitle("Login");
            } else {
                menu.findItem(R.id.logout).setTitle("Logout");
            }
            navigationView.setNavigationItemSelectedListener(this);
        }

    }

    public void replaceFragmentWithAnimation(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left);
        transaction.replace(R.id.main_fragment_container, fragment);
        transaction.commit();
    }


    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.ivCart:
               // Progress();
                replaceFragmentWithAnimation(new CartFragment());
                break;
            case R.id.iv_menu:
                drawer.openDrawer(Gravity.LEFT);
                break;
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.my_orders) {
            replaceFragmentWithAnimation( new OrderFragment());
        }

        else if (id == R.id.my_cart) {
            replaceFragmentWithAnimation(new CartFragment());
        }

        else if (id == R.id.info) {
            replaceFragmentWithAnimation(new AboutUsFragment());
        }
        else if (id == R.id.share) {
            replaceFragmentWithAnimation(new ShareFragment());
        }
        else if (id == R.id.logout) {
            logout();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void Intialize()
    {
        //Linearlayout
        llmain = findViewById(R.id.llmain);

        //textview
        tvHeaderText = findViewById(R.id.tvHeaderText);
        tvCount = findViewById(R.id.tvCount);

        //imageview
        iv_menu = findViewById(R.id.iv_menu);
        ivCart = findViewById(R.id.ivCart);

        rlSearch = findViewById(R.id.rlSearch);
        rlSearch.setOnClickListener(this);



        rlsearchview = findViewById(R.id.rlsearchview);
        rlsearchview.setVisibility(View.GONE);
        marquee=findViewById(R.id.marquee);

        //navigationview
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        //pref
        preferences=new Preferences(this) ;
    }
    public void logout() {
        //Dialog
        final Dialog dialog = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.alertyesno);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
        window.setAttributes(wlp);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.show();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);


        //findId
        TextView tvYes = (TextView) dialog.findViewById(R.id.tvOk);
        TextView tvCancel = (TextView) dialog.findViewById(R.id.tvcancel);
        TextView tvReason = (TextView) dialog.findViewById(R.id.textView22);
        TextView tvAlertMsg = (TextView) dialog.findViewById(R.id.tvAlertMsg);

        //set value
        tvAlertMsg.setText("Confirmation Alert..!!!");
        tvReason.setText("Are you sure you want to logout?");
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();

        //set listener
        tvYes.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DrawerActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
//                pref.set(AppSettings.CustomerID, "");
//                pref.commit();
                preferences.set("user_id","");
                preferences.commit();
                finishAffinity();
                dialog.dismiss();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }
    @Override
    public void onBackPressed() {
        backPressed = backPressed + 1;
        if (backPressed == 1) {
            Toast.makeText(DrawerActivity.this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            new CountDownTimer(5000, 1000) { // adjust the milli seconds here
                public void onTick(long millisUntilFinished) {
                }
                public void onFinish() { backPressed = 0;
                }
            }.start();
        }
        if (backPressed == 2) {
            backPressed = 0;
            finishAffinity();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }


}




