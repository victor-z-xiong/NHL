package no_name.nhl_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BoxScore extends AppCompatActivity {

    String triCodeAway = "";
    String triCodeHome = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_score);

        Bundle extras = getIntent().getExtras();
        String url = extras.getString("BOX_SCORE_URL");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                makeBanner(response);
                makeMiddleLayer(response, true);
                makeMiddleLayer(response, false);
                makeScoringSummary(response);

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

    private void makeScoringSummary(JSONObject response){

        ArrayList<Integer> scoringPlays = new ArrayList<Integer>();
        try {
            JSONArray goals = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("scoringPlays");
            JSONArray periods = response.getJSONObject("liveData").getJSONObject("linescore").getJSONArray("periods");
            int endOfPeriod, startOfPeriod;
            LinearLayout ll = (LinearLayout) findViewById(R.id.scoring_summary);
            TableRow periodRow;
            TextView periodText;
            String periodString;
            JSONArray allPlays = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("allPlays");
            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            tableRowParams.setMargins(20, 10,20, 0);
            triCodeAway = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("away").getString("triCode");
            triCodeHome = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("home").getString("triCode");
            for(int j = 0; j < periods.length(); j++){
                endOfPeriod  = 1 + response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(j).getInt("endIndex");
                startOfPeriod = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(j).getInt("startIndex");
                scoringPlays.clear();
                for(int k = 0; k < goals.length(); k++){
                    if(goals.getInt(k) <= endOfPeriod && goals.getInt(k) >=startOfPeriod){
                        scoringPlays.add(goals.getInt(k));
                    }
                }

                periodRow = new TableRow(this);
                periodText = new TextView(this);
                periodString = periods.getJSONObject(j).getString("ordinalNum") + (j < 3 ? " period" : "");
                periodText.setText(periodString);
                periodText.setTextSize(18);

                makeScoringSummaryPeriod(scoringPlays, ll, periodRow, periodText, allPlays, tableRowParams);
            }

        } catch(JSONException e){
            System.out.println("Unexpected JSON exception");
        }
    }

    private void makeScoringSummaryPeriod(ArrayList<Integer> scoringPlays, LinearLayout scoringSummary, TableRow period, TextView periodText, JSONArray allPlays, TableLayout.LayoutParams tableRowParams){
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        period.addView(periodText);
        period.setLayoutParams(tableRowParams);
        ll.addView(period);
        TableRow row1, row2, spacerRow;
        TextView playTextLine1, playTextLine2, blankView;
        String playString1, playString2;
        JSONObject playObject;
        for(int i = 0; i < scoringPlays.size(); i++){
            try {
                row1 = new TableRow(this);
                row2 = new TableRow(this);
                playTextLine1 = new TextView(this);
                playTextLine2 = new TextView(this);
                playObject = allPlays.getJSONObject(scoringPlays.get(i));

                String triCode = playObject.getJSONObject("team").getString("triCode");
                String description = playObject.getJSONObject("result").getString("description");

                String timeOfGoal = playObject.getJSONObject("about").getString("periodTimeRemaining");
                String strength = playObject.getJSONObject("result").getJSONObject("strength").getString("code");
                String awayGoal = Integer.toString(playObject.getJSONObject("about").getJSONObject("goals").getInt("away"));
                String homeGoal = Integer.toString(playObject.getJSONObject("about").getJSONObject("goals").getInt("home"));

                playString1 = triCode + " " + description;
                playString2 = timeOfGoal + " " + strength + " " + triCodeAway + " " + awayGoal + " " + triCodeHome + " " + homeGoal;
                playTextLine1.setText(playString1);
                playTextLine2.setText(playString2);
                row1.addView(playTextLine1);
                row2.addView(playTextLine2);
                tableRowParams.setMargins(20, 10,20, 0);
                row1.setLayoutParams(tableRowParams);
                tableRowParams.setMargins(20, 0,20, 20);
                row2.setLayoutParams(tableRowParams);
                ll.addView(row1);
                ll.addView(row2);
            } catch(JSONException e){
                System.out.println("Unexpected JSON exception");
            }
        }

        spacerRow = new TableRow(this);
        blankView = new TextView(this);
        if(scoringPlays.size() == 0){
            playTextLine1 = new TextView(this);
            row1 = new TableRow(this);
            playTextLine1.setText("No Scoring This Period");
            row1.addView(playTextLine1);
            row1.setLayoutParams(tableRowParams);
            ll.addView(row1);
        }
        spacerRow.addView(blankView);
        ll.addView(spacerRow);

        scoringSummary.addView(ll);
    }

    private void makeBanner(JSONObject response){

        ImageView awayBig = findViewById(R.id.away_banner_logo);
        ImageView homeBig = findViewById(R.id.home_banner_logo);

        TextView timeLeft = findViewById(R.id.time_remaining);
        TextView periodState = findViewById(R.id.period);
        try {
            JSONObject gameData = response.getJSONObject("gameData");
            JSONObject teams = gameData.getJSONObject("teams");
            setLogo(teams.getJSONObject("away").getString("name"), awayBig);
            setLogo(teams.getJSONObject("home").getString("name"), homeBig);

            timeLeft.setText(response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodOrdinal"));
            periodState.setText(response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodTimeRemaining"));
        } catch (JSONException e){
            System.out.println("Unexpected JSON exception");
        }
    }

    private void makeMiddleLayer(JSONObject response, Boolean scoring){
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        LinearLayout head = findViewById(scoring ? R.id.Header_box_score : R.id.Header_shot_total);
        LinearLayout away = findViewById(scoring ? R.id.away_box_score : R.id.away_shot_total);
        LinearLayout home = findViewById(scoring ? R.id.home_box_score : R.id.home_shot_total);
        TextView headText = new TextView(this);
        headText.setText("Team");
        headText.setTextSize(18);
        headText.setLayoutParams(params);
        head.addView(headText);
        ImageView awaySmall = new ImageView(this);
        ImageView homeSmall = new ImageView(this);
        try {
            JSONObject gameData = response.getJSONObject("gameData");
            JSONObject teams = gameData.getJSONObject("teams");
            setLogo(teams.getJSONObject("away").getString("name"), awaySmall);
            setLogo(teams.getJSONObject("home").getString("name"), homeSmall);
            awaySmall.setLayoutParams(params);
            homeSmall.setLayoutParams(params);
            away.addView(awaySmall);
            home.addView(homeSmall);
        } catch (JSONException e){
            System.out.println("Unexpected JSON exception");
        }

        addScoresToLineScore(response, params, scoring);
    }



    private void addScoresToLineScore(JSONObject response, android.widget.LinearLayout.LayoutParams params, boolean scoring){
        try {
            JSONArray period = response.getJSONObject("liveData").getJSONObject("linescore").getJSONArray("periods");
            TextView away, home, title;
            LinearLayout titleLayout = findViewById(scoring ? R.id.Header_box_score : R.id.Header_shot_total);
            LinearLayout awayLayout = findViewById(scoring ? R.id.away_box_score : R.id.away_shot_total);
            LinearLayout homeLayout = findViewById(scoring ? R.id.home_box_score : R.id.home_shot_total);

            for(int i = 0; i <= period.length()+1; i++){
                title = new TextView(this);
                away = new TextView(this);
                home = new TextView(this);

                if(i == period.length()){
                    title.setText("final");
                    away.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                            .getJSONObject("away").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt(scoring ? "goals" : "shots")));
                    home.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                            .getJSONObject("home").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt(scoring ? "goals" : "shots")));
                }else {
                    title.setText(period.getJSONObject(i).getString("ordinalNum"));
                    away.setText(Integer.toString(period.getJSONObject(i).getJSONObject("away").getInt(scoring ? "goals" : "shotsOnGoal")));
                    home.setText(Integer.toString(period.getJSONObject(i).getJSONObject("home").getInt(scoring ? "goals" : "shotsOnGoal")));
                }

                title.setLayoutParams(params);
                away.setLayoutParams(params);
                home.setLayoutParams(params);

                titleLayout.addView(title);
                awayLayout.addView(away);
                homeLayout.addView(home);
            }

        } catch (JSONException e){
            System.out.println("Unexpected JSON exception");
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
