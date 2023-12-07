package com.omkokate.attendance2023;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class OutputActivity extends AppCompatActivity {
    RecyclerView recyclerViewpa;
    List<PA> PAList;
    PAAdapter PAAdapter;
    RecyclerView recyclerView;
    List<Attendance> attendance;
    Adapter adapter;
    TextView name_tv,roll_tv,date_tv,div_text;

    ShimmerFrameLayout shimmerFrameLayout,shimmerFrameLayout2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_output);
        Window window = this.getWindow();
        window.setStatusBarColor(this.getResources ().getColor(R.color.purple_700));


        shimmerFrameLayout=findViewById(R.id.shimmerFrameLayout);
        shimmerFrameLayout.startShimmer();

        String year = getIntent().getStringExtra("year");
        String division = getIntent().getStringExtra("division");
        String rollNo = getIntent().getStringExtra("rollNo");
        int roll= Integer.parseInt(rollNo)-1;
        String date = getIntent().getStringExtra("date");
        Log.d("myapp",date);

        div_text=findViewById(R.id.div_text);
        div_text.setText("DIVISION  : "+division);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("urls")
                .document("all")
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String fieldValue = String.valueOf(documentSnapshot.get("url"));
                            String url=fieldValue;
                            url=url+"?func=WRITE&a1="+year+"%20"+division+"&a2="+rollNo;
                            Log.d("myapp",""+url);

                            LinearLayout linearLayout = findViewById(R.id.linear_shimmer);
                            for (int i = 0; i < 5; i++) {
                                View view = getLayoutInflater().inflate(R.layout.attendance_shimmer, linearLayout, false);
                                linearLayout.addView(view);
                            }
                            extractAttendance(roll,date,url,year);

                        } else {
                            // Handle error
                            Log.d("myapp","error line 102");
                            Intent i4=new Intent(OutputActivity.this,ErrorActivity.class);
                            startActivity(i4);
                        }
                    }
                });

        recyclerView = findViewById(R.id.attendanceList);
        recyclerViewpa = findViewById(R.id.paList);
        attendance = new ArrayList<>();
        PAList = new ArrayList<>();
    }

    private void extractAttendance(int roll, String date, String url,String year) {

        name_tv=findViewById(R.id.name_tv);
        roll_tv=findViewById(R.id.roll_tv);
        date_tv=findViewById(R.id.tv_date);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonArrayRequest jsonArrayRequest=new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                Log.d("myapp", "" + response);

                JSONObject atte = null;
                for (int i = 0; i < response.length(); i++) {
                    atte = null;
                    try {
                        JSONObject sub = response.getJSONObject(i);
                        String subname = sub.getString("subname");
                        String avg = sub.getString("avg");
                        String avgth = sub.getString("avgth");
                        String avgpr = sub.getString("avgpr");
                        String avg3d = sub.getString("avg3d");
                        String avg7d = sub.getString("avg7d");
                        String avgmonth = sub.getString("avgmonth");
                        atte = sub.getJSONObject("att");

                        String nametv = sub.getString("name");
                        name_tv.setText(nametv);
                        int roll_real = roll + 1;
                        String Roll = String.valueOf(roll_real);
                        roll_tv.setText("ROLL NO :  " + Roll);

                        if (atte.has(date)) {
                            date_tv.setText("Attendance on :  " + date);
                        }

//                        if (!(atte.has(date))) {
//                            RelativeLayout layout = (RelativeLayout) findViewById(R.id.rel_pa);
////                            layout.setVisibility(View.GONE);
//                        }

                        PA eve = new PA();

                        if (atte.has(date)) {
                            JSONObject atte_date = atte.getJSONObject(date);
                            Iterator<String> keys = atte_date.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONArray pa = atte_date.getJSONArray(key);

                                if(pa.length()>0) {

                                    String PA = pa.getString(1);
                                    String priority = pa.getString(0);


                                    String s_name = subname;
//                                    s_name = s_name.substring(3);

                                    if (PA.equals("P") || PA.equals("A")) {
                                        eve.setPriority(priority);
                                        eve.setSubject(s_name);
                                        eve.setTime(key);
                                        eve.setPresenty(PA);
                                    }
                                    Log.d("myapp", subname + " " + key + " " + PA);
                                }


                            }
                            PAList.add(eve);

                        }

                        Attendance att = new Attendance();

                        att.setSubname(subname);

                        att.setAvg(valueS(avg));
                        att.setAvgPB(valueI(avg));

                        att.setAvgth(valueS(avgth));
                        att.setAvgthPB(valueI(avgth));

                        att.setAvgpr(valueS(avgpr));
                        att.setAvgprPB(valueI(avgpr));

                        att.setAvg3d(valueS(avg3d));
                        att.setAvg3dPB(valueI(avg3d));

                        att.setAvg7d(valueS(avg7d));
                        att.setAvg7dPB(valueI(avg7d));

                        att.setAvgmonth(valueS(avgmonth));
                        att.setAvgmonthPB(valueI(avgmonth));

                        attendance.add(att);

                        Log.d("myapp","pa list is "+PAList);
                        Log.d("myapp","attendance list is "+attendance);

//                        if(PAList.isEmpty()){
//                            RelativeLayout layout = (RelativeLayout) findViewById(R.id.rel_pa);
//                            layout.setVisibility(View.GONE);
//                        }


                        Log.d("myapp", "" + sub);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("myapp", "error"+e.getMessage());
//                        Intent i2 = new Intent(OutputActivity.this, ErrorActivity.class);
//                        startActivity(i2);
                    }

                    Collections.sort(attendance, new Comparator<Attendance>() {
                        @Override
                        public int compare(Attendance t1, Attendance t2) {
                            return t1.getSubname().compareToIgnoreCase(t2.getSubname());
                        }
                    });

                    Collections.sort(PAList, new Comparator<PA>() {
                        @Override
                        public int compare(PA t1, PA t2) {
                            return t1.getPriority().compareToIgnoreCase(t2.getPriority());
                        }
                    });

                    recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    adapter = new Adapter(getApplicationContext(), attendance);
                    recyclerView.setAdapter(adapter);

//                    if (atte.has(date)) {
                        recyclerViewpa.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        PAAdapter = new PAAdapter(getApplicationContext(), PAList);
                        recyclerViewpa.setAdapter(PAAdapter);
//                    }

                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("myapp","error fetching json");


            }
        });

        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsonArrayRequest.setRetryPolicy(retryPolicy);

        queue.add(jsonArrayRequest);

        shimmerFrameLayout.stopShimmer();

    }


    public String valueS(String v1) throws JSONException {
        String value1 = v1;
        String valuefin;
        if (value1.equals("#DIV/0!")||value1.equals("")) {
            valuefin = "0%";
        } else {
            float value2 = (Float.parseFloat(value1));
            float value3 = value2 * 100;
            int value4 = Math.round(value3);
            valuefin = value4 + "%";
        }
        return valuefin;
    }


    public int valueI(String v1) throws JSONException {
        String value1 = v1;
        int valuefin;
        if (value1.equals("#DIV/0!")||value1.equals("")) {
            valuefin = 0;
        } else {
            float value2 = (Float.parseFloat(value1));
            float value3 = value2 * 100;
            valuefin = Math.round(value3);
        }
        return valuefin;
    }

}