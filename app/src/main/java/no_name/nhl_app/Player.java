package no_name.nhl_app;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
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

    int pixelWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pixelWidth = size.x;

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

        String careerStatsTotalUrl = "https://statsapi.web.nhl.com/api/v1/people/"+playerId+"/stats/?stats=careerRegularSeason";
        JsonObjectRequest jsonObjectRequest3 = new JsonObjectRequest(Request.Method.GET, careerStatsTotalUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                setPlayerCareerStatsTotal(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Something is wrong");
                error.printStackTrace();
            }
        });
        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest2);
        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest3);
        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void setPlayerCareerStatsTotal(JSONObject response){
        try{
            JSONArray statsYearByYear = response.getJSONArray("stats").getJSONObject(0).getJSONArray("splits");

            LinearLayout statsTable = (LinearLayout) findViewById(R.id.last_row);

            makeCareerStatRow(statsYearByYear.getJSONObject(0), statsTable, true, 1);

        }catch (JSONException e){
            System.out.println("Something wrong");
        }
    }

    private void setPlayerCareerStats(JSONObject response){
        try{
            JSONArray statsYearByYear = response.getJSONArray("people").getJSONObject(0)
                    .getJSONArray("stats").getJSONObject(0).getJSONArray("splits");
            LinearLayout statsTable = (LinearLayout) findViewById(R.id.player_career_stats_table);
            makeCareerStatRowTitle(statsTable);
            for(int i = 0; i < statsYearByYear.length(); i++){

                makeCareerStatRow(statsYearByYear.getJSONObject(i), statsTable, false, i);

            }
        }catch (JSONException e){
            System.out.println("Something wrong");
        }
    }

    private void makeCareerStatRow(JSONObject statsForYear, LinearLayout statsTable, boolean total, int rowNum){
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
            String season = total ? "" : statsForYear.getString("season").substring(0,4) + "-" + statsForYear.getString("season").substring(4);
            year.setText(season);
            setTitleParams(year, yearSpacing(), false);

            team.setText(total ? "NHL Totals" : statsForYear.getJSONObject("team").getString("name"));
            setTitleParams(team, teamNameSpacing(), false);
            team.setGravity(Gravity.LEFT);

            gamesPlayed.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("games")));
            setTitleParams(gamesPlayed, mainColumnSpacing(), false);

            goals.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("goals")));
            setTitleParams(goals, mainColumnSpacing(), false);

            assists.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("assists")));
            setTitleParams(assists, mainColumnSpacing(), false);

            points.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("points")));
            setTitleParams(points, mainColumnSpacing(), false);

            pim.setText(statsForYear.getJSONObject("stat").getString("penaltyMinutes"));
            setTitleParams(pim, mainColumnSpacing(), false);

            shots.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("shots")));
            setTitleParams(shots, mainColumnSpacing(), false);

            hits.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("hits")));
            setTitleParams(hits, mainColumnSpacing(), false);

            plusMinus.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("plusMinus")));
            setTitleParams(plusMinus, mainColumnSpacing(), false);
        } catch (JSONException e ){
            System.out.println("Something went wrong");
        }

        if(rowNum % 2 == 0){
            year.setTextColor(Color.WHITE);
            team.setTextColor(Color.WHITE);
            gamesPlayed.setTextColor(Color.WHITE);
            goals.setTextColor(Color.WHITE);
            assists.setTextColor(Color.WHITE);
            points.setTextColor(Color.WHITE);
            pim.setTextColor(Color.WHITE);
            shots.setTextColor(Color.WHITE);
            hits.setTextColor(Color.WHITE);
            plusMinus.setTextColor(Color.WHITE);
            row.setBackgroundColor(Color.GRAY);
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
        setTitleParams(year, yearSpacing(), true);

        team.setText("Team");
        setTitleParams(team, teamNameSpacing(), true);
        team.setGravity(Gravity.LEFT);

        gamesPlayed.setText("GP");
        setTitleParams(gamesPlayed, mainColumnSpacing(), true);

        goals.setText("G");
        setTitleParams(goals, mainColumnSpacing(), true);

        assists.setText("A");
        setTitleParams(assists, mainColumnSpacing(), true);

        points.setText("P");
        setTitleParams(points, mainColumnSpacing(), true);

        pim.setText("PIM");
        setTitleParams(pim, mainColumnSpacing(), true);

        shots.setText("Shots");
        setTitleParams(shots, mainColumnSpacing(), true);

        hits.setText("Hits");
        setTitleParams(hits, mainColumnSpacing(), true);

        plusMinus.setText("+/-");
        setTitleParams(plusMinus, mainColumnSpacing(), true);

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

    private int mainColumnSpacing(){

        if(pixelWidth >= 1440){
            return 150;
        }else if (pixelWidth >= 1080){
            return 112;
        }else if (pixelWidth >= 720) {
            return 75;
        }

        return 75;
    }

    private int teamNameSpacing(){

        if(pixelWidth >= 1440){
            return 400;
        }else if (pixelWidth >= 1080){
            return 300;
        }else if (pixelWidth >= 720) {
            return 200;
        }

        return 200;
    }

    private int yearSpacing(){

        if(pixelWidth >= 1440){
            return 300;
        }else if (pixelWidth >= 1080){
            return 225;
        }else if (pixelWidth >= 720) {
            return 150;
        }

        return 150;
    }

    private void setTitleParams(TextView headerTitle, int width, boolean bold){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ActionBar.LayoutParams.WRAP_CONTENT);
        headerTitle.setGravity(Gravity.CENTER);
        headerTitle.setLayoutParams(params);
        if(bold){
            headerTitle.setTypeface(null, Typeface.BOLD);
        }
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
            if(teamName.length() > 18){
                playerTeamText.setTextSize(18);
            }else {
                playerTeamText.setTextSize(20);
            }
            playerTeamText.setGravity(Gravity.CENTER);

            playerPositionText.setText(player.getJSONObject("primaryPosition").getString("abbreviation")
                                        + " | " + Integer.toString(player.getInt("weight")) + " lb"
                                        + " | " + player.getString("height")
                                        + " | Age: " + Integer.toString(player.getInt("currentAge")));
            playerPositionText.setTextSize(pixelWidth >= 1080 ? 20 : 17);
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
        teamLogoFileName = teamLogoFileName.replace("(", "");
        teamLogoFileName = teamLogoFileName.replace(")", "");
        int drawableID = getResources().getIdentifier(teamLogoFileName, "drawable", getPackageName());
        teamLogo.setImageResource(drawableID);
        teamLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }
}
