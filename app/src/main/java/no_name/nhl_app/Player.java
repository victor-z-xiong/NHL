package no_name.nhl_app;

import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class Player extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        String profileUrl = "https://statsapi.web.nhl.com" + getIntent().getExtras().getString("PLAYER_URL");
        String playerId = getIntent().getExtras().getString("PLAYER_URL").substring(15);
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
        String careerStatsUrl = "https://statsapi.web.nhl.com/api/v1/people/"+playerId+"?expand=person.stats&stats=yearByYear";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, careerStatsUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                setPlayerCareerStats(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Something is wrong");
                error.printStackTrace();
            }
        });

        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest2);
        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void setPlayerCareerStats(JSONObject response){
        try{
            JSONArray statsYearByYear = response.getJSONArray("people").getJSONObject(0)
                    .getJSONArray("stats").getJSONObject(0).getJSONArray("splits");
            LinearLayout statsTable = (LinearLayout) findViewById(R.id.player_career_stats_table);
            makeCareerStatRowTitle(statsTable);
            for(int i = 0; i < statsYearByYear.length(); i++){
                makeCareerStatRow(statsYearByYear.getJSONObject(i), statsTable);
            }
        }catch (JSONException e){
            System.out.println("Something wrong");
        }
    }

    private void makeCareerStatRow(JSONObject statsForYear, LinearLayout statsTable){
        LinearLayout horizontalRow = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView year = new TextView(this);
        TextView team = new TextView(this);
        TextView gamesPlayed = new TextView(this);
        TextView goals = new TextView(this);
        TextView assists = new TextView(this);
        TextView points = new TextView(this);
        TextView pim = new TextView(this);
        TextView shots = new TextView(this);
        TextView hits = new TextView(this);
        TextView plusMinus = new TextView(this);

        try{
            year.setText(statsForYear.getString("season"));
            setTitleParams(year);

            team.setText(statsForYear.getJSONObject("team").getString("name"));
            setTitleParams(team);

            gamesPlayed.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("games")));
            setTitleParams(gamesPlayed);

            goals.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("goals")));
            setTitleParams(goals);

            assists.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("assists")));
            setTitleParams(assists);

            points.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("points")));
            setTitleParams(points);

            pim.setText(statsForYear.getJSONObject("stat").getString("penaltyMinutes"));
            setTitleParams(pim);

            shots.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("shots")));
            setTitleParams(shots);

            hits.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("hits")));
            setTitleParams(hits);
        } catch (JSONException e ){
            System.out.println("Something went wrong");
        }

        horizontalRow.addView(year);
        horizontalRow.addView(team);
        horizontalRow.addView(gamesPlayed);
        horizontalRow.addView(goals);
        horizontalRow.addView(assists);
        horizontalRow.addView(points);
        horizontalRow.addView(pim);
        horizontalRow.addView(shots);
        horizontalRow.addView(hits);
        horizontalRow.addView(plusMinus);

        row.addView(horizontalRow);
        statsTable.addView(row);
    }

    private void makeCareerStatRowTitle(LinearLayout statsTable){
        LinearLayout tableHead = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView year = new TextView(this);
        TextView team = new TextView(this);
        TextView gamesPlayed = new TextView(this);
        TextView goals = new TextView(this);
        TextView assists = new TextView(this);
        TextView points = new TextView(this);
        TextView pim = new TextView(this);
        TextView shots = new TextView(this);
        TextView hits = new TextView(this);
        TextView plusMinus = new TextView(this);

        year.setText("Year");
        setTitleParams(year);

        team.setText("team");
        setTitleParams(team);

        gamesPlayed.setText("GP");
        setTitleParams(gamesPlayed);

        goals.setText("G");
        setTitleParams(goals);

        assists.setText("A");
        setTitleParams(assists);

        points.setText("P");
        setTitleParams(points);

        pim.setText("PIM");
        setTitleParams(pim);

        shots.setText("Shots");
        setTitleParams(shots);

        hits.setText("Hits");
        setTitleParams(hits);

        plusMinus.setText("+/-");
        setTitleParams(plusMinus);

        tableHead.addView(year);
        tableHead.addView(team);
        tableHead.addView(gamesPlayed);
        tableHead.addView(goals);
        tableHead.addView(assists);
        tableHead.addView(points);
        tableHead.addView(pim);
        tableHead.addView(shots);
        tableHead.addView(hits);
        tableHead.addView(plusMinus);

        row.addView(tableHead);
        statsTable.addView(row);
    }

    private void setTitleParams(TextView headerTitle){
        headerTitle.setGravity(Gravity.CENTER);
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
            String playerName = player.getString("fullName");
            playerNameText.setText(playerName + " | #" + Integer.toString(player.getInt("primaryNumber")));
            playerNameText.setTextSize(playerName.length() > 17 ? 18 : 20);
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
