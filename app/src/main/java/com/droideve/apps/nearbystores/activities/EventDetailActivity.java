package com.droideve.apps.nearbystores.activities;

import static com.droideve.apps.nearbystores.appconfig.AppConfig.APP_DEBUG;
import static com.droideve.apps.nearbystores.controllers.sessions.SessionsController.getSession;
import static com.droideve.apps.nearbystores.controllers.sessions.SessionsController.isLogged;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.animation.ImageLoaderAnimation;
import com.droideve.apps.nearbystores.appconfig.AppConfig;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.booking.controllers.CartController;
import com.droideve.apps.nearbystores.booking.modals.Cart;
import com.droideve.apps.nearbystores.booking.views.activities.BookingCheckoutActivity;
import com.droideve.apps.nearbystores.classes.Event;
import com.droideve.apps.nearbystores.classes.Store;
import com.droideve.apps.nearbystores.classes.User;
import com.droideve.apps.nearbystores.controllers.events.EventController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.controllers.stores.StoreController;
import com.droideve.apps.nearbystores.customView.StoreCardCustomView;
import com.droideve.apps.nearbystores.load_manager.ViewManager;
import com.droideve.apps.nearbystores.location.GPStracker;
import com.droideve.apps.nearbystores.location.GoogleDirection;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.VolleySingleton;
import com.droideve.apps.nearbystores.network.api_request.SimpleRequest;
import com.droideve.apps.nearbystores.parser.api_parser.EventParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.DateUtils;
import com.droideve.apps.nearbystores.utils.MessageDialog;
import com.droideve.apps.nearbystores.utils.NSLog;
import com.droideve.apps.nearbystores.utils.NSToast;
import com.droideve.apps.nearbystores.utils.TextUtils;
import com.droideve.apps.nearbystores.utils.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;


public class EventDetailActivity extends GlobalActivity implements ViewManager.CustomView, OnMapReadyCallback, View.OnClickListener {

    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.scroll_view)
    NestedScrollView scrollView;

    @BindView(R.id.customStoreCV)
    StoreCardCustomView customStoreCV;
    @BindView(R.id.description_content)
    TextView description;
    @BindView(R.id.address)
    TextView address;
    @BindView(R.id.event_date_begin)
    TextView event_date_begin;
    @BindView(R.id.event_begin_date_end)
    TextView event_begin_date_end;
    @BindView(R.id.day_calendar)
    TextView event_day;
    @BindView(R.id.month_calendar)
    TextView event_month;

    @BindView(R.id.header_title)
    TextView header_title;

    @BindView(R.id.progressMapLL)
    LinearLayout progressMapLL;


    @BindView(R.id.addToWishlist)
    MaterialRippleLayout addToWishlist;
    @BindView(R.id.removeFromWishlist)
    MaterialRippleLayout removeFromWishlist;
    @BindView(R.id.phoneBtn)
    MaterialRippleLayout phoneBtn;
    @BindView(R.id.webBtn)
    MaterialRippleLayout webBtn;


    @BindView(R.id.addToWishlist0)
    Button addToWishlist0;
    @BindView(R.id.removeFromWishlist0)
    Button removeFromWishlist0;
    @BindView(R.id.phoneBtn0)
    Button phoneBtn0;
    @BindView(R.id.webBtn0)
    Button webBtn0;




    @BindView(R.id.maps_container)
    LinearLayout mapcontainer;
    @BindView(R.id.mapping)
    View mapping;

    @BindView(R.id.adsLayout)
    LinearLayout adsLayout;
    @BindView(R.id.adView)
    AdView mAdView;

    @BindView(R.id.bookYourTicketBtnContainer)
    LinearLayout bookYourTicketBtnContainer;

    @BindView(R.id.bookYourTicketBtn)
    Button bookYourTicketBtn;


    private Context context;
    private ViewManager mViewManager;
    private GoogleMap mMap;
    private GoogleDirection gd;
    private Event eventData;
    private BottomSheetDialog mBottomSheetDialog;


    private LatLng customerPosition, myPosition;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(null);
        setContentView(R.layout.activity_detail_event);
        ButterKnife.bind(this);

        //contents of menu have changed, and menu should be redrawn.
        invalidateOptionsMenu();

        //setup toolbar and header
        setupViews();

        //setup view manager showError loading content
        setupViewManager();

        //populating data
        listingEventData();

    }


    protected void listingEventData() {

        int eid = 0;

        try {
            eid = getIntent().getExtras().getInt("id");
            //get it from external url (deep linking)
            if (eid == 0) {

                Intent appLinkIntent = getIntent();
                String appLinkAction = appLinkIntent.getAction();
                Uri appLinkData = appLinkIntent.getData();

                if (appLinkAction.equals(Intent.ACTION_VIEW)) {

                    if (AppConfig.APP_DEBUG)
                        Toast.makeText(getApplicationContext(), appLinkData.toString(), Toast.LENGTH_LONG).show();

                    eid = Utils.dp_get_id_from_url(appLinkData.toString(), Constances.ModulesConfig.EVENT_MODULE);

                    if (AppConfig.APP_DEBUG)
                        Toast.makeText(getApplicationContext(), "The ID: " + eid + " " + appLinkAction, Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        //get it from external url (deep linking)


        eventData = EventController.getEvent(eid);

        //GET DATA FROM API IF NETWORK IS AVAILABLE
        if (ServiceHandler.isNetworkAvailable(this)) {
            syncEvent(eid);
        } else {
            //IF NOT GET THE ITEM FROM THE DATABASE
            if (eventData != null) {
                putDataIntoViews();
            }
        }


    }

    public void syncEvent(final int evnt_id) {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        mViewManager.showLoading();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_USER_GET_EVENTS, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (APP_DEBUG) {
                        NSLog.e("responseStoresString", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);

                    //NSLog.e("response",response);

                    final EventParser mEventParser = new EventParser(jsonObject);
                    RealmList<Event> list = mEventParser.getEvents();

                    if (list.size() > 0) {
                        EventController.insertEvents(list);
                        eventData = list.get(0);
                        putDataIntoViews();

                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                    mViewManager.showError();

                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }
                mViewManager.showError();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                //
                params.put("limit", "1");
                params.put("event_id", String.valueOf(evnt_id));


                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    private void setupViews() {

        //setup toolbar
        setupToolbar(toolbar);
        getAppBarSubtitle().setVisibility(View.GONE);


        //setup header views
        setupHeader();

    }


    private void initParams() {
        //Set current context
        context = this;

    }

    private void setupViewManager() {

        //setup view manager
        mViewManager = new ViewManager(this);
        mViewManager.setLoadingView(findViewById(R.id.loading));
        mViewManager.setContentView(findViewById(R.id.content));
        mViewManager.setErrorView(findViewById(R.id.error));
        mViewManager.setEmptyView(findViewById(R.id.empty));

        mViewManager.setListener(new ViewManager.CallViewListener() {
            @Override
            public void onContentShown() {
                scrollView.setNestedScrollingEnabled(true);
            }

            @Override
            public void onErrorShown() {

            }

            @Override
            public void onEmptyShown() {
                scrollView.setNestedScrollingEnabled(false);
            }

            @Override
            public void onLoadingShown() {

            }
        });

        mViewManager.showLoading();

    }

    private void setupScrollView() {


    }

    private void setupHeader() {




    }


    @Override
    protected void onResume() {

        if (mAdView != null)
            mAdView.resume();

        super.onResume();
    }

    @Override
    protected void onPause() {

        if (mAdView != null)
            mAdView.pause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {

        if (mAdView != null)
            mAdView.destroy();

        super.onDestroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_menu, menu);

        menu.findItem(R.id.share_post).setVisible(true);


        /////////////////////////////
        menu.findItem(R.id.share_post).setVisible(true);
        Drawable send_location = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon2.cmd_share_variant)
                .color(ResourcesCompat.getColor(getResources(), R.color.defaultColor, null))
                .sizeDp(22);
        menu.findItem(R.id.share_post).setIcon(send_location);
        /////////////////////////////

        menu.findItem(R.id.report_icon).setVisible(true);


        return true;
    }


    @Override
    public void customErrorView(View v) {

    }

    @Override
    public void customLoadingView(View v) {

    }

    @Override
    public void customEmptyView(View v) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (android.R.id.home == item.getItemId()) {
            if (!MainActivity.isOpend()) {
                startActivity(new Intent(this, MainActivity.class));
            }
            finish();
        } else if (item.getItemId() == R.id.share_post) {

            double lat = eventData.getLat();
            double lng = eventData.getLng();
            String mapLink = null;

            try {
                mapLink = "https://maps.google.com/?q=" + URLEncoder.encode(eventData.getAddress(), "UTF-8") + "&ll=" + String.format("%f,%f", lat, lng);
            } catch (UnsupportedEncodingException e) {
                mapLink = "https://maps.google.com/?ll=" + String.format("%f,%f", lat, lng);
                e.printStackTrace();
            }


            @SuppressLint({"StringFormatInvalid", "LocalSuppress", "StringFormatMatches"}) String shared_text =
                    String.format(getString(R.string.shared_text),
                            eventData.getName(),
                            getString(R.string.app_name),
                            eventData.getLink()
                    );

            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, shared_text);
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        } else if (item.getItemId() == R.id.report_icon) {

            if (!SessionsController.isLogged()) {
                startActivity(new Intent(EventDetailActivity.this, LoginV2Activity.class));
                
            } else {

                Intent intent = new Intent(EventDetailActivity.this, ReportIssueActivity.class);
                intent.putExtra("id", eventData.getId());
                intent.putExtra("name", eventData.getName());
                intent.putExtra("link", eventData.getLink());
                if (eventData.getStore_id() > 0) {
                    intent.putExtra("owner_id", StoreController.getStore(eventData.getStore_id()).getUser_id());
                }
                intent.putExtra("module", "event");
                startActivity(intent);
            }


        }
        return super.onOptionsItemSelected(item);
    }


    private void putDataIntoViews() {

        getAppBarTitle().setText((!eventData.getName().equals("") && eventData.getName().length() > 20) ? (eventData.getName().substring(0, 20) + "...") : eventData.getName());

        header_title.setText(eventData.getName());

        //description.setText(eventData.getAddress());
        description.setText(
                Html.fromHtml(/*HtmlEscape.unescapeHtml(*/eventData.getDescription()/*)*/)
        );

        new TextUtils.decodeHtml(description).execute(eventData.getDescription());


        event_date_begin.setText(DateUtils.getDateByTimeZone(eventData.getDateB(), "dd MMM yyyy"));
        event_begin_date_end.setText(DateUtils.getDateByTimeZone(eventData.getDateE(), "dd MMM yyyy"));


        Drawable icon_address_map_marker = new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon2.cmd_map_marker)
                .color(ResourcesCompat.getColor(getResources(), R.color.defaultColorText, null))
                .sizeDp(18);
        if (eventData.getStore_id() > 0 && !eventData.getStore_name().equals("") && !eventData.getStore_name().toLowerCase()
                .equals("null")) {
            address.setText(eventData.getStore_name());
        } else {
            address.setText(eventData.getAddress());
        }
        address.setCompoundDrawables(icon_address_map_marker, null, null, null);
        address.setCompoundDrawablePadding(20);


        setupQuickButtons();


        if (APP_DEBUG) {
            NSLog.e("eventStore", eventData.getStore_id() + " - " + eventData.getStore_name());
        }

        if (eventData.getStore_id() > 0 && !eventData.getStore_name().equals("") && !eventData.getStore_name().toLowerCase()
                .equals("null")) {
            customStoreCV.loadData(eventData.getStore_id(), false, new StoreCardCustomView.StoreListener() {
                @Override
                public void onLoaded(Store object) {
                    customStoreCV.setVisibility(View.VISIBLE);
                }
            });

        } else {
            customStoreCV.setVisibility(View.GONE);
        }

        event_day.setText(DateUtils.getDateByTimeZone(eventData.getDateB(), "dd"));
        event_month.setText(DateUtils.getDateByTimeZone(eventData.getDateB(), "MMM"));


        initMapping();

        mViewManager.showContent();

    }

    private void setupQuickButtons() {

        if (eventData.getTel().length() > 0 && !eventData.getTel().equals("")) {
            phoneBtn.setEnabled(true);
            phoneBtn0.setOnClickListener(this);
        } else {
            phoneBtn.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.gray_field, null));
            phoneBtn.setEnabled(false);
        }

        if (eventData.getListImages().size() > 0) {

            Glide.with(getBaseContext())
                    .asBitmap()
                    .load(eventData.getListImages().get(0).getUrl500_500())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap,
                                                    Transition<? super Bitmap> transition) {
                            int w =  bitmap.getWidth();
                            int h = bitmap.getHeight();

                            if(h < w ){
                                h = w;
                            }

                            float ratio = (float) w/h;
                            image.setImageBitmap(bitmap);

                            //setup scroll with header
                            setupScrollNHeaderCustomized(
                                    scrollView,
                                    (LinearLayout) findViewById(R.id.header_detail),
                                    ratio,
                                    null
                            );

                        }
                    });

        } else {

            Glide.with(getBaseContext())
                    .load(R.drawable.def_logo)
                    .centerCrop().placeholder(R.drawable.def_logo)
                    .into(image);
        }


        if (eventData.getWebSite().length() > 0 && !eventData.getWebSite().equalsIgnoreCase("null")) {
            webBtn.setVisibility(View.VISIBLE);

            try {
                webBtn0.setOnClickListener(this);
            } catch (Exception e) {
                if (APP_DEBUG) {
                    NSLog.e("WebView", e.getMessage());
                }
            }

        } else {
            webBtn.setVisibility(View.GONE);
        }

        initializeBtn();


    }


    private void attachMap() {

        try {

            SupportMapFragment mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.mapping);
            if (mSupportMapFragment == null) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                mSupportMapFragment = SupportMapFragment.newInstance();
                mSupportMapFragment.setRetainInstance(true);
                fragmentTransaction.replace(R.id.mapping, mSupportMapFragment).commit();
            }
            if (mSupportMapFragment != null) {
                mSupportMapFragment.getMapAsync(this);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        putMap();

        progressMapLL.setVisibility(View.GONE);

    }


    private void initMapping() {

        if (eventData.getLat() != null && eventData.getLng() != null) {

            double TraderLat = eventData.getLat();
            double TraderLng = eventData.getLng();

            customerPosition = new LatLng(TraderLat, TraderLng);
            customerPosition = new LatLng(TraderLat, TraderLng);
            //INITIALIZE MY LOCATION
            GPStracker trackMe = new GPStracker(this);

            myPosition = new LatLng(trackMe.getLatitude(), trackMe.getLongitude());

            attachMap();

        } else {

            if (AppConfig.APP_DEBUG) {
                Toast.makeText(this, "Debug mode: Couldn't get position maps", Toast.LENGTH_LONG).show();
            }

            mapcontainer.setVisibility(View.GONE);
        }

    }

    private void putMap() {

        if (mMap != null) {

            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(customerPosition, 16));
            //trader location

            Bitmap b = ((BitmapDrawable) Utils.changeDrawableIconMap(
                    this, R.drawable.ic_marker)).getBitmap();
            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(b);


            mMap.addMarker(new MarkerOptions().position(customerPosition)
                    .title(eventData.getName())
                    .anchor(0.0f, 1.0f)
                    .icon(icon)
                    .snippet(eventData.getAddress())).showInfoWindow();

            mMap.addMarker(new MarkerOptions().position(myPosition)
                    .title(eventData.getName())
                    .anchor(0.0f, 1.0f)
                    .draggable(true)
                    //.icon(icon)
                    .snippet(eventData.getAddress())).showInfoWindow();

            if (ServiceHandler.isNetworkAvailable(this)) {
                try {
                    gd = new GoogleDirection(this);
                    //My Location
                    gd.setOnDirectionResponseListener(new GoogleDirection.OnDirectionResponseListener() {

                        @Override
                        public void onResponse(String status, Document doc, GoogleDirection gd) {
                            mMap.addPolyline(gd.getPolyline(doc, 8,
                                    ResourcesCompat.getColor(getResources(), R.color.colorAccent, null)));
                            //mMap.setMyLocationEnabled(true);
                        }
                    });
                    gd.setLogging(true);
                    gd.request(myPosition, customerPosition, GoogleDirection.MODE_DRIVING);

                } catch (Exception e) {
                    if (APP_DEBUG)
                        e.printStackTrace();
                }

            }


        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.phoneBtn0) {
            try {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + eventData.getTel().trim()));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getApplicationContext(), getString(R.string.store_call_error) + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else if (v.getId() == R.id.webBtn0) {
//            new AwesomeWebView.Builder(EventDetailActivity.this)
//                    .statusBarColorRes(R.color.colorAccent)
//                    .theme(R.style.FinestWebViewAppTheme)
//                    .show(eventData.getWebSite());
        } else if (v.getId() == R.id.addToWishlist0) {
            // Click action
            if (eventData != null) {
                if (isLogged()) {

                    User currentUser = SessionsController.getSession().getUser();
                    saveEventToBookmarks(EventDetailActivity.this, currentUser.getId(), eventData.getId());

                } else {
                    startActivity(new Intent(EventDetailActivity.this, LoginV2Activity.class));
                    
                }
            }
        } else if (v.getId() == R.id.removeFromWishlist0) {
            // Click action
            if (eventData != null) {
                if (isLogged()) {
                    User currentUser = SessionsController.getSession().getUser();
                    if (eventData.getSaved() > 0) {
                        removeEventToBookmarks(EventDetailActivity.this, currentUser.getId(), eventData.getId());
                    }
                }

            }

        }
    }


    private void initializeBtn() {

        //Setup Like buttons
        if (SessionsController.isLogged()) {
            if (eventData.getSaved() > 0) {
                addToWishlist.setVisibility(View.GONE);
                removeFromWishlist.setVisibility(View.VISIBLE);
            } else {
                addToWishlist.setVisibility(View.VISIBLE);
                removeFromWishlist.setVisibility(View.GONE);
            }
        } else {
            addToWishlist.setVisibility(View.VISIBLE);
            removeFromWishlist.setVisibility(View.GONE);
        }

        addToWishlist0.setOnClickListener(this);
        removeFromWishlist0.setOnClickListener(this);

        //Setup Participation button
        if(eventData.getPrice()>0){
            bookYourTicketBtnContainer.setVisibility(View.VISIBLE);
            bookYourTicketBtn.setText(String.format(getString(R.string.buy_ticket),eventData.getPriceFormatted()));
        }else{
            bookYourTicketBtnContainer.setVisibility(View.VISIBLE);
            bookYourTicketBtn.setText(getString(R.string.participate));
        }

        if(eventData.getPrice()>=0){
            bookYourTicketBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBuyTicketBottomSheetDialog();
                }
            });

            bookYourTicketBtnContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showBuyTicketBottomSheetDialog();
                }
            });
        }

    }


    public void saveEventToBookmarks(final Context context, final int user_id, final int int_id) {

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_BOOKMARK_EVENT_SAVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (AppConfig.APP_DEBUG) {
                    NSLog.e("response", response);
                }

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getInt(Tags.SUCCESS) == 1) {

                        eventData = EventController.doSave(eventData.getId(), 1);
                        if (eventData != null) {
                            initializeBtn();
                        }

                        showBuyTicketBottomSheetDialog(jsonObject.getInt(Tags.RESULT));
                    } else {
                        Toast.makeText(getApplicationContext(), "Something went wrong , please try later ", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", String.valueOf(user_id));
                params.put("event_id", String.valueOf(int_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }

    public void removeEventToBookmarks(final Context context, final int user_id, final int int_id) {

        RequestQueue queue = VolleySingleton.getInstance(context).getRequestQueue();
        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_BOOKMARK_EVENT_REMOVE, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (AppConfig.APP_DEBUG) {
                    NSLog.e("response", response);
                }

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getInt(Tags.SUCCESS) == 1) {

                        eventData = EventController.doSave(eventData.getId(), 0);
                        if (eventData != null) {
                            initializeBtn();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), "Something went wrong , please try later ", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }


            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("user_id", String.valueOf(user_id));
                params.put("event_id", String.valueOf(int_id));

                return params;
            }

        };

        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);

    }


    private void showBuyTicketBottomSheetDialog(final int bookmark_id) {

        if(!AppConfig.NOTIFICATION_AGREEMENT)
            return;

        if (mBottomSheetDialog == null) return;


        final View view = getLayoutInflater().inflate(R.layout.notifyme_sheet, null);
        ((TextView) view.findViewById(R.id.name)).setText(getString(R.string.remind_me));
        ((TextView) view.findViewById(R.id.address)).setText(getString(R.string.remind_me_msg));
        (view.findViewById(R.id.bt_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
            }
        });

        ((TextView) view.findViewById(R.id.name)).setText(getString(R.string.remind_me));
        (view.findViewById(R.id.bt_details)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notificationAgreement(bookmark_id, getSession().getUser().getId(), 1);
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }


    public void notificationAgreement(final int bookmark_id, final int user_id, final int notificationStatus) {

        RequestQueue queue = VolleySingleton.getInstance(this).getRequestQueue();

        //mViewManager.showLoading();

        SimpleRequest request = new SimpleRequest(Request.Method.POST,
                Constances.API.API_NOTIFICATIONS_AGREEMENT, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    if (AppConfig.APP_DEBUG) {
                        NSLog.e("notificationAgreement", response);
                    }

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getInt(Tags.SUCCESS) == 1) {
                        Toast.makeText(getApplicationContext(), "Notification agreement granted for this business ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Something went wrong , please try later ", Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    //send a rapport to support
                    e.printStackTrace();

                    mViewManager.showError();

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (AppConfig.APP_DEBUG) {
                    NSLog.e("ERROR", error.toString());
                }
                mViewManager.showError();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("bookmark_id", String.valueOf(bookmark_id));
                params.put("user_id", String.valueOf(user_id));
                params.put("agreement", String.valueOf(notificationStatus)); //todo : set the agreement according to the store status

                return params;
            }

        };


        request.setRetryPolicy(new DefaultRetryPolicy(SimpleRequest.TIME_OUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);


    }


    private void setupAdmob() {


        if (AppConfig.SHOW_ADS) {

            mAdView.setVisibility(View.VISIBLE);
            AdRequest adRequest = new AdRequest.Builder().build();

            mAdView.loadAd(adRequest);
            mAdView.setAdListener(new AdListener() {
                @Override
                public void onAdClicked() {
                    // Code to be executed when the user clicks on an ad.
                }

                @Override
                public void onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }

                @Override
                public void onAdFailedToLoad(LoadAdError adError) {
                    // Code to be executed when an ad request fails.
                    mAdView.setVisibility(View.GONE);
                    adsLayout.setVisibility(View.GONE);
                }

                @Override
                public void onAdImpression() {
                    // Code to be executed when an impression is recorded
                    // for an ad.
                }

                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();
                    mAdView.setVisibility(View.VISIBLE);
                    adsLayout.setVisibility(View.VISIBLE);

                }

                @Override
                public void onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }
            });

            mAdView.loadAd(adRequest);


        } else
            adsLayout.setVisibility(View.GONE);

    }


    double original_price;
    double custom_price;
    int custom_qte;

    private void showBuyTicketBottomSheetDialog() {

        if (eventData == null)
            return;

        if(eventData.getCf_id()==0){
            MessageDialog.showMessage(this,Map.of("err",getString(R.string.this_event_not_avaliable_for_checkout)));
            return;
        }

        BottomSheetDialog mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(R.layout.bottom_sheet_event_qty);
        mBottomSheetDialog.setCancelable(false);

        mBottomSheetDialog.show();

        //set default  values
        original_price = eventData.getPrice();
        custom_price = eventData.getPrice();
        custom_qte = 1;

        //set product_id image
        if (eventData.getListImages().size() > 0) {
            Glide.with(AppController.getInstance())
                    .load(eventData.getListImages().get(0).getUrl200_200())
                    .centerCrop()
                    .placeholder(ImageLoaderAnimation.glideLoader(this))
                    .centerCrop().into(((ImageView) mBottomSheetDialog.findViewById(R.id.image)));

            //set product_id name
            ((TextView)mBottomSheetDialog.findViewById(R.id.name)).setText(eventData.getName());

            if(original_price>0){
                ((TextView) mBottomSheetDialog.findViewById(R.id.price)).setText(String.valueOf(custom_price));
            }else{
                ((TextView) mBottomSheetDialog.findViewById(R.id.price)).setText(getString(R.string.event_free));
            }
            //action qte buttons
            (mBottomSheetDialog.findViewById(R.id.btn_less_qte)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (custom_qte <= 1)
                        return;
                    custom_qte--;
                    if (original_price < custom_price) {
                        custom_price = custom_price - original_price;
                    }
                    //set custom quantity
                    if(original_price>0){
                        ((TextView) mBottomSheetDialog.findViewById(R.id.price)).setText(String.valueOf(custom_price));
                    }else{
                        ((TextView) mBottomSheetDialog.findViewById(R.id.price)).setText(getString(R.string.event_free));
                    }

                    ((TextView) mBottomSheetDialog.findViewById(R.id.quantity)).setText(String.valueOf(custom_qte));
                }
            });

            (mBottomSheetDialog.findViewById(R.id.btn_more_qte)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    custom_qte++;
                    custom_price = custom_price + original_price;
                    //set custom quantity

                    if(original_price>0){
                        ((TextView) mBottomSheetDialog.findViewById(R.id.price)).setText(String.valueOf(custom_price));
                    }else{
                        ((TextView) mBottomSheetDialog.findViewById(R.id.price)).setText(getString(R.string.event_free));
                    }

                    ((TextView) mBottomSheetDialog.findViewById(R.id.quantity)).setText(String.valueOf(custom_qte));
                }
            });
        }


        (mBottomSheetDialog.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close the window
                mBottomSheetDialog.dismiss();
            }
        });


        (mBottomSheetDialog.findViewById(R.id.goCheckout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close the window
                mBottomSheetDialog.dismiss();
                launchCheckout();
            }
        });

    }


    public void launchCheckout(){

        //fill cart detail
        Cart mcart = new Cart();
        mcart.setModule_id(eventData.getId());
        mcart.setModule(Constances.ModulesConfig.EVENT_MODULE);
        mcart.setAmount(eventData.getPrice());
        mcart.setQte(custom_qte);
        mcart.setSelectedEvent(eventData);

        if (SessionsController.isLogged())
            mcart.setUser_id(SessionsController.getSession().getUser().getId());

        //delete all from carts
        CartController.removeAll();
        //save cart in the database
        CartController.addServiceToCart(mcart);

        //redirect to cart activity
        Intent intent = new Intent(new Intent(this, BookingCheckoutActivity.class));
        intent.putExtra("module_id", eventData.getId());
        intent.putExtra("module", Constances.ModulesConfig.EVENT_MODULE);

        startActivity(intent);
        finish();

    }


}
