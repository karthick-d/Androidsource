package com.droideve.apps.nearbystores.parser.api_parser;


import com.droideve.apps.nearbystores.classes.Coupon;
import com.droideve.apps.nearbystores.classes.Images;
import com.droideve.apps.nearbystores.classes.Ticket;
import com.droideve.apps.nearbystores.parser.Parser;
import com.droideve.apps.nearbystores.parser.tags.Tags;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.RealmList;


public class TicketParser extends Parser {

    public TicketParser(JSONObject json) {
        super(json);
    }

    public TicketParser(Parser parser) {
        this.json = parser.json;
    }

    public RealmList<Ticket> getTickets() {

        RealmList<Ticket> list = new RealmList<Ticket>();

        try {

            JSONObject json_array = json.getJSONObject(Tags.RESULT);

            for (int i = 0; i < json_array.length(); i++) {
                try {

                    JSONObject json = json_array.getJSONObject(i + "");
                    Ticket ticket = new Ticket();
                    ticket.setId(json.getInt("id"));
                    ticket.setBooking_id(json.getInt("booking_id"));
                    ticket.setAttachementUrl(json.getString("attachementUrl"));
                    ticket.setStatus(json.getInt("status"));
                    ticket.setFull_name(json.getString("full_name"));
                    ticket.setPhone(json.getString("phone"));
                    ticket.setUser_id(json.getInt("user_id"));
                    ticket.setEmail(json.getString("email"));
                    ticket.setEventName(json.getString("event_name"));

                    String jsonValues = "";
                    try {
                        if (!json.isNull("images")) {
                            jsonValues = json.getJSONObject("images").toString();
                            JSONObject jsonObject = new JSONObject(jsonValues);
                            ImagesParser imgp = new ImagesParser(jsonObject);
                            ticket.setListImages(imgp.getImagesList());
                        } else {
                            ticket.setListImages(new RealmList<Images>());
                        }
                    } catch (JSONException jex) {
                        ticket.setListImages(new RealmList<Images>());
                    }

                    list.add(ticket);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return list;
    }


    public Coupon getCoupon() {

        try {


            JSONArray json_array = json.getJSONArray(Tags.RESULT);


            try {
                JSONObject json_coupon = json_array.getJSONObject(0);
                Coupon coupon = new Coupon();
                coupon.setId(json_coupon.getInt("id"));
                coupon.setLabel(json_coupon.getString("label"));
                coupon.setStatus(json_coupon.getInt("status"));
                
                return coupon;
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


        return null;
    }


}
