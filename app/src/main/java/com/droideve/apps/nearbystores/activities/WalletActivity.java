package com.droideve.apps.nearbystores.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.droideve.apps.nearbystores.R;
import com.droideve.apps.nearbystores.adapter.lists.TransactionWalletAdapter;
import com.droideve.apps.nearbystores.appconfig.Constances;
import com.droideve.apps.nearbystores.classes.WTransaction;
import com.droideve.apps.nearbystores.controllers.sessions.SessionsController;
import com.droideve.apps.nearbystores.customView.SimpleWebViewActivity;
import com.droideve.apps.nearbystores.network.api_request.ApiRequest;
import com.droideve.apps.nearbystores.network.api_request.ApiRequestListeners;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.api_parser.WTransactionParser;
import com.droideve.apps.nearbystores.utils.NSProgressDialog;
import com.droideve.apps.nearbystores.utils.NSToast;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

public class WalletActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.toolbar_title)
    TextView APP_TITLE_VIEW;
    @BindView(R.id.toolbar_subtitle)
    TextView APP_DESC_VIEW;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    @BindView(R.id.transactionsList)
    RecyclerView transactionsList;

    @BindView(R.id.myBalance)
    TextView myBalance;


    @BindView(R.id.topUpBtn)
    TextView topUpBtn;

    @BindView(R.id.sendMoney)
    Button sendMoneyBtn;

    @BindView(R.id.withdraw)
    Button withdrawBtn;


    private TransactionWalletAdapter adapter;
    private String sendMoneyUrl = null;
    private String withdrawUrl = null;
    private String topUpUrl = null;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        ButterKnife.bind(this);
        initToolbar();

        APP_TITLE_VIEW.setText(getResources().getString(R.string.myWallet));
        APP_TITLE_VIEW.setVisibility(View.VISIBLE);

        setup();

    }

    private void setup(){

        adapter = new TransactionWalletAdapter(this, new ArrayList<>());

        transactionsList.setHasFixedSize(false);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        transactionsList.setLayoutManager(mLayoutManager);
        transactionsList.setAdapter(adapter);

        transactionsList.setVisibility(View.VISIBLE);

        callApi();


        sendMoneyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(sendMoneyUrl==null){
                    NSToast.show(getString(R.string.unavaliable_feature));
                    return;
                }


                Intent intent = new Intent(WalletActivity.this, SimpleWebViewActivity.class);
                intent.putExtra("url", sendMoneyUrl);
                intent.putExtra("title", getString(R.string.sendMoney));
                mStartForResult.launch(intent);

            }
        });

        topUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(topUpUrl==null){
                    NSToast.show(getString(R.string.unavaliable_feature));
                    return;
                }

                Intent intent = new Intent(WalletActivity.this, SimpleWebViewActivity.class);
                intent.putExtra("url", topUpUrl);
                intent.putExtra("title", getString(R.string.top_up));
                mStartForResult.launch(intent);

            }
        });

        withdrawBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(withdrawUrl==null){
                    NSToast.show(getString(R.string.unavaliable_feature));
                    return;
                }

                Intent intent = new Intent(WalletActivity.this, SimpleWebViewActivity.class);
                intent.putExtra("url", withdrawUrl);
                intent.putExtra("title", getString(R.string.withdraw));
                mStartForResult.launch(intent);

            }
        });

    }



    private void parseOtherFields(WTransactionParser parser){
        try {
            myBalance.setText(parser.getBalance());
        } catch (JSONException e) {
            myBalance.setText("0.0");
        }

        try {
            sendMoneyUrl = parser.getField("sendMoneyUrl");
        } catch (JSONException e) {
            sendMoneyUrl = null;
        }

        try {
            withdrawUrl = parser.getField("withdrawUrl");
        } catch (JSONException e) {
            withdrawUrl = null;
        }

        try {
            topUpUrl = parser.getField("topUpUrl");
        } catch (JSONException e) {
            topUpUrl = null;
        }
    }

    private void callApi(){

        NSProgressDialog.newInstance(this).show(getString(R.string.loading));

        Map<String, String> params = new HashMap<String, String>();

        if(SessionsController.isLogged()){
            params.put("user_id", String.valueOf(SessionsController.getSession().getUser().getId()));
        }else
            return;

        ApiRequest.newPostInstance(Constances.API.API_DIGITAL_WALLET, new ApiRequestListeners() {
            @Override
            public void onSuccess(Parser parser) {

                if(NSProgressDialog.getInstance()!=null){
                    NSProgressDialog.getInstance().dismiss();
                }

                final WTransactionParser mWTransactionParser = new WTransactionParser(parser);

                parseOtherFields(mWTransactionParser);

                if (mWTransactionParser.getSuccess() == 1
                        && mWTransactionParser.getList().size() > 0) {

                    adapter.getData().clear();
                    adapter.notifyDataSetChanged();

                    RealmList<WTransaction> list = mWTransactionParser.getList();
                    for (int i = 0; i < list.size(); i++) {
                        adapter.addItem(list.get(i));
                    }

                }
            }

            @Override
            public void onFail(Map<String, String> errors) {

            }
        },params);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void initToolbar() {


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        APP_DESC_VIEW.setVisibility(View.GONE);
        APP_TITLE_VIEW.setText(R.string.About_us);
        APP_DESC_VIEW.setVisibility(View.GONE);

    }



    @Override
    public void onClick(View view) {




    }

    @Override
    protected void onRestart() {
        super.onRestart();

        callApi();
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();

                        NSToast.show("vvvvvvv");
                    }
                }
            });
}




