package no_name.nhl_app;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        String profileUrl = "https://statsapi.web.nhl.com" + getIntent().getExtras().getString("PLAYER_URL");

        JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.GET, profileUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setProfilePicture(response);
                setPlayerProfile(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Something is wrong");
                error.printStackTrace();
            }
        });

        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest2);
    }

    private void setProfilePicture(JSONObject response){
        try{
            int id = response.getJSONArray("people").getJSONObject(0).getInt("id");
            String imageLink = "https://nhl.bamcontent.com/images/headshots/current/168x168/" + Integer.toString(id) + ".jpg";
            Picasso.get().load(imageLink).into((ImageView) findViewById(R.id.profile_pic));

        }catch (JSONException e){
            System.out.println("Something wrong");
        }
    }

    private void setPlayerProfile(JSONObject response){
        try {
            JSONObject player = response.getJSONArray("people").getJSONObject(0);
            TextView playerNameText = findViewById(R.id.player_name);
            TextView playerTeamText = findViewById(R.id.player_card_team_name);
            ImageView playerTeamLogo = findViewById(R.id.player_card_team_logo);
            TextView playerPositionText = findViewById(R.id.player_pos);
            TextView playerBirthDate = findViewById(R.id.birthdate);
            TextView playerBirthPlace = findViewById(R.id.birthplace);
            TextView playerShootsCatches = findViewById(R.id.shootscatches);

            android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

            playerNameText.setText(player.getString("fullName")+ " | #" + Integer.toString(player.getInt("primaryNumber")));
            playerNameText.setTextSize(20);
            playerNameText.setGravity(Gravity.CENTER);
            playerNameText.setTypeface(null, Typeface.BOLD);

            String teamName = player.getJSONObject("currentTeam").getString("name");
            setLogo(teamName, playerTeamLogo);

            playerTeamText.setText(teamName);
            if(teamName.equals("Columbus Blue Jackets")){
                playerTeamText.setTextSize(18);
            }else {
                playerTeamText.setTextSize(20);
            }
            playerTeamText.setGravity(Gravity.CENTER);

            playerPositionText.setText(player.getJSONObject("primaryPosition").getString("abbreviation")
                                        + " | " + Integer.toString(player.getInt("weight")) + " lb"
                                        + " | " + player.getString("height")
                                        + " | Age: " + Integer.toString(player.getInt("currentAge")));
            playerPositionText.setTextSize(20);
            playerPositionText.setGravity(Gravity.CENTER);
            playerPositionText.setLayoutParams(params);

            playerBirthDate.setText("Born: " + player.getString("birthDate"));
            playerBirthDate.setTextSize(20);

            playerBirthPlace.setText("Birthplace: " + player.getString("birthCity") + ", " + player.getString("birthCountry"));
            playerBirthPlace.setTextSize(20);

            playerShootsCatches.setText("Shoots: " + player.getString("shootsCatches"));
            playerShootsCatches.setTextSize(20);

        } catch (JSONException e){
            System.out.println("Something went wrong");
        }
    }

    private void setLogo(String team, ImageView teamLogo){
        String teamLogoFileName = team.toLowerCase();
        teamLogoFileName = teamLogoFileName.replace(" ", "_");
        teamLogoFileName = teamLogoFileName.replace("é", "e");
        teamLogoFileName = teamLogoFileName.replace(".", "");
        int drawableID = getResources().getIdentifier(teamLogoFileName, "drawable", getPackageName());
        teamLogo.setImageResource(drawableID);
        teamLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}
