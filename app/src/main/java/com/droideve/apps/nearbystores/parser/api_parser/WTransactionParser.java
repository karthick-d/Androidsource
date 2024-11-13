package com.droideve.apps.nearbystores.parser.api_parser;


import com.droideve.apps.nearbystores.classes.WTransaction;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class WTransactionParser extends Parser {

    public WTransactionParser(JSONObject json) {
        super(json);
    }
    public WTransactionParser(Parser parser) {
        this.json = parser.json;
    }


    public String getBalance() throws JSONException {
        return  json.getString("balance");
    }

    public String getField(String key) throws JSONException {
        return  json.getString(key);
    }

    public RealmList<WTransaction> getList() {

        RealmList<WTransaction> list = new RealmList<WTransaction>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {

                try {

                    JSONObject json = json_array.getJSONObject(i + "");
                    WTransaction mWTransaction = new WTransaction();
                    mWTransaction.setId(json.getInt("id"));
                    mWTransaction.setNo(json.getString("no"));
                    mWTransaction.setCurrency(json.getString("currency"));
                    mWTransaction.setOperation(json.getString("operation"));
                    mWTransaction.setUser_id(json.getInt("user_id"));
                    mWTransaction.setDate(json.getString("created_at"));
                    mWTransaction.setAmount(json.getString("amount_v"));
                    mWTransaction.setNote(json.getString("note"));

                    list.add(mWTransaction);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return list;
    }


}
