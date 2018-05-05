package no_name.nhl_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Player extends AppCompatActivity {
    private static final String apiKey = "AIzaSyD2sRn3cJx3H5dWTtBKN5MqzeXuMBiWtRo";
    private static final String cx = "015362381441204153993:d55bah2cf0e";
    private String playerName = "bohorvat";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playerName = getIntent().getExtras().getString("PLAYER_NAME").replace(" ", "");

        String url = "https://www.googleapis.com/customsearch/v1?q="+playerName+"&cx="+cx+"&searchType=image&key="+apiKey;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray items = response.getJSONArray("items");
                    String imageLink = items.getJSONObject(0).getString("link");
                    Picasso.get().load(imageLink).into((ImageView) findViewById(R.id.profile_pic));
                    TextView name = findViewById(R.id.player_name_placeholder);
                    name.setText(playerName);
                }catch (JSONException e){
                    System.out.println("Something wrong");
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Something is wrong");
                error.printStackTrace();
            }
        });

        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest);
    }
}
