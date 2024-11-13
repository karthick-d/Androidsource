package com.droideve.apps.nearbystores.activities;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nirhart.parallaxscroll.views.ParallaxScrollView;

public class GlobalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppController.changeLanguage(this);

    }


    private Toolbar myAppBar = null;

    public Toolbar getAppBar() {
        return myAppBar;
    }

    public TextView getAppBarTitle() {
        return myAppBar.findViewById(R.id.toolbar_title);
    }

    public TextView getAppBarSubtitle() {
        return myAppBar.findViewById(R.id.toolbar_subtitle);
    }

    public void setupToolbar(Toolbar appbar) {

        setSupportActionBar(appbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        myAppBar = appbar;

    }


    public TextView setAppBarTitle(String title) {

        if (myAppBar != null) {
            TextView toolbar_title = myAppBar.findViewById(R.id.toolbar_title);
            toolbar_title.setText(title);
            return toolbar_title;
        } else {

            new Exception("Please call setup toolbar before");
        }

        return null;
    }

    public TextView setAppBarSubTitle(String title) {

        if (myAppBar != null) {
            TextView toolbar_subtitle = myAppBar.findViewById(R.id.toolbar_subtitle);
            toolbar_subtitle.setText(title);
            return toolbar_subtitle;
        } else {
            new Exception("Please call setuptoolbar before");
        }

        return null;
    }

    protected void changeViewSize(LinearLayout view, int s) {

        //define height header size
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int height = metrics.heightPixels;

        height = height / s;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height);
        view.setLayoutParams(lp);


    }


    protected void setupFloatingActionButton(){

        FloatingActionButton floatingActionButton = new FloatingActionButton(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(32, 32, 32, 32);
        floatingActionButton.setLayoutParams(layoutParams);
        floatingActionButton.setImageResource(android.R.drawable.ic_dialog_email);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // We are showing only toast message. However, you can do anything you need.
                NSToast.show("clicked");
            }
        });

    }


    protected void setupScrollNHeader(NestedScrollView scrollView, LinearLayout header, int s) {
        setupScrollNHeader(scrollView, header, s);
    }


    protected void setupScrollNHeaderCustomized(NestedScrollView scrollView, final LinearLayout header, float ratio, final View viewWithAlpha) {

        //define height header size
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int screen_width = metrics.widthPixels;
        float new_height = screen_width / ratio;


        //set minimum
        if(new_height<700){
            new_height = 700;
        }

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) new_height);
        header.setLayoutParams(lp);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            final float finalHeight = new_height;
            final float finalHeight1 = new_height;

            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    if (scrollY < (finalHeight1 / 3)) {
                        getAppBar().setBackground(getDrawable(R.drawable.gradient_bg_top_to_bottom_50));
                        getAppBarTitle().setVisibility(View.GONE);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (finalHeight - (scrollY / 2)));
                        header.setLayoutParams(lp);
                    } else {
                        getAppBar().setBackgroundColor(getColor(R.color.colorPrimary));
                        getAppBarTitle().setTextColor(getColor(R.color.white));
                        getAppBarTitle().setVisibility(View.VISIBLE);
                    }

                    if(scrollListener!=null){
                        scrollListener.onScroll(scrollY,finalHeight1);
                    }


                    if(viewWithAlpha != null){
                        if (scrollY < (finalHeight1 / 2)) {
                            if (viewWithAlpha != null) {
                                viewWithAlpha.animate().alpha(1.0f).setDuration(200);
                            }
                        } else {
                            if (viewWithAlpha != null) {
                                //viewWithAlpha.setVisibility(View.GONE);
                                viewWithAlpha.animate().alpha(0.0f).setDuration(200);
                            }
                        }
                    }
                }
            });
        }
    }

    protected void setupScrollNHeader(ParallaxScrollView scrollView, LinearLayout header, int s, final View viewWithAlpha) {

        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int height = metrics.heightPixels;

        height = height / s;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        header.setLayoutParams(lp);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int finalHeight = height;
            final int finalHeight1 = height;
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    NSLog.e("onScrollChange", "scrollX=" + scrollX + ";scrollY=" + scrollY + " height_header=" + finalHeight);

                    if (scrollY < (finalHeight1 / 3)) {

                        getAppBar().setBackground(getDrawable(R.drawable.gradient_bg_top_to_bottom_70));
                        getAppBarTitle().setVisibility(View.GONE);

                    } else {

                        getAppBar().setBackgroundColor(getColor(R.color.colorPrimary));
                        getAppBarTitle().setVisibility(View.VISIBLE);

                    }


                    if(viewWithAlpha != null){
                        if (scrollY < (finalHeight1 / 2)) {
                            if (viewWithAlpha != null) {
                                viewWithAlpha.animate().alpha(1.0f).setDuration(200);
                            }
                        } else {
                            if (viewWithAlpha != null) {
                                //viewWithAlpha.setVisibility(View.GONE);
                                viewWithAlpha.animate().alpha(0.0f).setDuration(200);
                            }
                        }
                    }


                }
            });
        }
    }

    protected void setupScrollNHeader(NestedScrollView scrollView, LinearLayout header, int s, final View viewWithAlpha) {

        //define height header size
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();
        int height = metrics.heightPixels;

        height = height / s;

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        header.setLayoutParams(lp);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            final int finalHeight = height;
            final int finalHeight1 = height;
            scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {

                    NSLog.e("onScrollChange", "scrollX=" + scrollX + ";scrollY=" + scrollY + " height_header=" + finalHeight);

                    if (scrollY < (finalHeight1 / 3)) {

                        getAppBar().setBackground(getDrawable(R.drawable.gradient_bg_top_to_bottom_70));
                        getAppBarTitle().setVisibility(View.GONE);

                    } else {

                        getAppBar().setBackgroundColor(getColor(R.color.colorPrimary));
                        getAppBarTitle().setVisibility(View.VISIBLE);

                    }


                    if(viewWithAlpha != null){
                        if (scrollY < (finalHeight1 / 2)) {
                            if (viewWithAlpha != null) {
                                viewWithAlpha.animate().alpha(1.0f).setDuration(200);
                            }
                        } else {
                            if (viewWithAlpha != null) {
                                //viewWithAlpha.setVisibility(View.GONE);
                                viewWithAlpha.animate().alpha(0.0f).setDuration(200);
                            }
                        }
                    }


                }
            });
        }
    }

    public interface CustomScrollListener{
        void onScroll(int scrollY,float height);
    }
    public CustomScrollListener scrollListener;

}
