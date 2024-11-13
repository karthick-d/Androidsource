package com.droideve.apps.nearbystores.fragments;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.droideve.apps.nearbystores.AppController;
import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.activities.EventDetailActivity;
import com.droideve.apps.nearbystores.activities.OfferDetailActivity;
import com.droideve.apps.nearbystores.adapter.lists.TicketsAdapter;
import com.droideve.apps.nearbystores.adapter.lists.TicketsAdapter;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.Ticket;
import com.droideve.apps.nearbystores.classes.Ticket;
import com.droideve.apps.nearbystores.controllers.ErrorsController;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.load_manager.ViewManager;
import com.droideve.apps.nearbystores.network.ServiceHandler;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.TicketParser;
import com.droideve.apps.nearbystores.parser.api_parser.TicketParser;
import com.droideve.apps.nearbystores.parser.tags.Tags;
import com.droideve.apps.nearbystores.utils.NSToast;

import java.util.ArrayList;
import java.util.Map;

import io.realm.RealmList;

/**
 * A simple {@link Fragment} subclass.
 */
public class TicketsFragment extends Fragment implements ViewManager.CustomView, SwipeRefreshLayout.OnRefreshListener, TicketsAdapter.ClickListener {

    private RecyclerView recyclerView;
    private TicketsAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ViewManager mViewManager;
    private Context context;
    //for scrolling params
    int pastVisiblesItems, visibleItemCount, totalItemCount;
    LinearLayoutManager mLayoutManager;
    private boolean loading = true;
    //pager
    private int COUNT = 0;
    private int REQUEST_PAGE = 1;


    // newInstance constructor for creating fragment with arguments
    public static TicketsFragment newInstance(int page, String title) {
        TicketsFragment fragmentFirst = new TicketsFragment();

        Bundle args = new Bundle();
        args.putInt("id", page);
        args.putString("title", title);
        fragmentFirst.setArguments(args);

        return fragmentFirst;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.v2_fragment_my_coupons, container, false);
        context = rootView.getContext();
        initSwipeRefresh(rootView);


        //init view manager
        initViewManager(rootView);

        //setup views
        initComponent(rootView);

        //call api
        loadTicketsApi(REQUEST_PAGE);

        return rootView;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();


    }

    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume

            //sync data from server
            loadTicketsApi(REQUEST_PAGE);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
    }


    private void initSwipeRefresh(View view) {
        swipeRefreshLayout = view.findViewById(R.id.refresh);

        swipeRefreshLayout.setOnRefreshListener(this);


        swipeRefreshLayout.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary,
                R.color.colorPrimary
        );

    }


    private void initViewManager(View view) {
        mViewManager = new ViewManager(getContext());
        mViewManager.setLoadingView(view.findViewById(R.id.loading));
        mViewManager.setContentView(view.findViewById(R.id.container));
        mViewManager.setErrorView(view.findViewById(R.id.error));
        mViewManager.setEmptyView(view.findViewById(R.id.empty));
        mViewManager.setCustumizeView(this);
    }


    private void initComponent(View view) {

        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView = view.findViewById(R.id.list);
        mAdapter = new TicketsAdapter(getContext(), new ArrayList());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutDirection(AppController.isRTL() ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        //scroll to import data
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);


        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                visibleItemCount = mLayoutManager.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = false;
                        if (ServiceHandler.isNetworkAvailable(getContext())) {
                            if (COUNT > mAdapter.getItemCount())
                                loadTicketsApi(REQUEST_PAGE);
                        } else {
                            Toast.makeText(getContext(), "Network not available ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });


        //refreshing mode off
        swipeRefreshLayout.setRefreshing(false);

        mAdapter.setClickListener(this);
    }

    public void loadTicketsApi(final int page) {

        swipeRefreshLayout.setRefreshing(true);

        if(page == 1){
            mAdapter.removeAll();
        }

        ApiRequest.newPostInstance(Constances.API.API_EVENT_TICKETS, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                parse_tickets(parser, page);
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }

        }, Map.of(
                "user_id", (SessionsController.getLocalDatabase.isLogged()?String.valueOf(SessionsController.getLocalDatabase.getUserId()):String.valueOf(-1)),
                "limit", "30",
                "page", String.valueOf(page)
        ));

    }

    private void parse_tickets(Parser parser, int page){

        final TicketParser mTicketParser = new TicketParser(parser);
        COUNT = 0;
        COUNT = mTicketParser.getIntArg(Tags.COUNT);

        //check server permission and display the errors
        if (mTicketParser.getSuccess() == -1) {
            ErrorsController.serverPermissionError(getActivity());
        }

        RealmList<Ticket> list = mTicketParser.getTickets();
        if (page == 1) mAdapter.removeAll();

        mAdapter.addAll(list);

        loading = true;

        if (COUNT > mAdapter.getItemCount())
            REQUEST_PAGE++;
        if (COUNT == 0 || mAdapter.getItemCount() == 0) {
            mViewManager.showEmpty();
        } else {
            mViewManager.showContent();
        }


        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onRefresh() {

        if (ServiceHandler.isNetworkAvailable(getActivity())) {
            REQUEST_PAGE = 1;
            loadTicketsApi(REQUEST_PAGE);
        } else {
            Toast.makeText(getActivity(), "Network not available ", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void customErrorView(View v) {
        Button btn = v.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTicketsApi(REQUEST_PAGE);
            }
        });
    }

    @Override
    public void customLoadingView(View v) {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void customEmptyView(View v) {

        Button btn = (Button) v.findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                REQUEST_PAGE = 1;
                loadTicketsApi(REQUEST_PAGE);
            }
        });
    }

    @Override
    public void onItemClick(View view, int pos) {
        Ticket obj = mAdapter.getItems().get(pos);
        if (mAdapter.getItems() != null) {
            handleClick(obj);
        }
    }

    private void handleClick(Ticket obj) {
        if (obj != null) {
            Intent intentOffer = new Intent(getContext(), EventDetailActivity.class);
            intentOffer.putExtra("id", obj.getEvent_id());
            getContext().startActivity(intentOffer);
        }
    }

    @Override
    public void onMoreButtonClick(View view, int position) {

        Ticket obj = mAdapter.getItems().get(position);

        PopupMenu popupMenu = new PopupMenu(getContext(), view);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (R.id.download == item.getItemId()) {

                    if(!obj.getAttachementUrl().equals("")){
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(obj.getAttachementUrl()));
                        startActivity(browserIntent);
                    }else{
                        NSToast.show(getString(R.string.no_attachment_to_download));
                    }

                }else if (R.id.go_event == item.getItemId()) {
                    handleClick(obj);
                }else if (R.id.share == item.getItemId()) {
                    if(!obj.getAttachementUrl().equals("")){
                        String c = String.format(getString(R.string.here_is_ticket),
                                obj.getEventName(),
                                obj.getAttachementUrl()
                        );
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, c);
                        sendIntent.setType("text/plain");
                        startActivity(sendIntent);
                    }else{
                        NSToast.show(getString(R.string.no_attachment_to_share));
                    }
                }
                return true;
            }
        });
        popupMenu.inflate(R.menu.menu_ticket_more);
        popupMenu.show();

    }


    public void removeTicket(final int user_id, final int coupon_id) {

        ApiRequest.newPostInstance(Constances.API.API_EVENT_TICKETS, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {
                if(parser.getSuccess() == 1){
                    REQUEST_PAGE = 1;
                    loadTicketsApi(REQUEST_PAGE);
                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        },  Map.of(
                "user_id", String.valueOf(user_id),
                "id", String.valueOf(coupon_id)
        ));



    }

}
