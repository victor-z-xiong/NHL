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
        ArrayList<Integer> scoringPlaysOne = new ArrayList<Integer>();
        ArrayList<Integer> scoringPlaysTwo = new ArrayList<Integer>();
        ArrayList<Integer> scoringPlaysThree = new ArrayList<Integer>();
        try {
            JSONArray goals = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("scoringPlays");
            int endOfFirst = 1 + response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(0).getInt("endIndex");
            int endOfSecond = 1 + response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(1).getInt("endIndex");
            triCodeAway = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("away").getString("triCode");
            triCodeHome = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("home").getString("triCode");
            for(int i = 0; i < goals.length(); i++){
                if(goals.getInt(i) <= endOfFirst){
                    scoringPlaysOne.add(goals.getInt(i));
                }else if(goals.getInt(i) <= endOfSecond){
                    scoringPlaysTwo.add(goals.getInt(i));
                }else{
                    scoringPlaysThree.add(goals.getInt(i));
                }
            }

            LinearLayout ll = (LinearLayout) findViewById(R.id.scoring_summary);
            TableRow FirstPeriod = new TableRow(this);
            TableRow SecondPeriod = new TableRow(this);
            TableRow ThirdPeriod = new TableRow(this);

            TextView firstText = new TextView(this);
            TextView secondText = new TextView(this);
            TextView thirdText = new TextView(this);

            firstText.setText("First Period");
            firstText.setTextSize(18);
            secondText.setText("Second Period");
            secondText.setTextSize(18);
            thirdText.setText("ThirdPeriod");
            thirdText.setTextSize(18);

            JSONArray allPlays = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("allPlays");
            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            tableRowParams.setMargins(20, 10,20, 0);
            makeScoringSummaryPeriod(scoringPlaysOne, ll, FirstPeriod, firstText, allPlays, tableRowParams);
            makeScoringSummaryPeriod(scoringPlaysTwo, ll, SecondPeriod, secondText, allPlays, tableRowParams);
            makeScoringSummaryPeriod(scoringPlaysThree, ll, ThirdPeriod, thirdText, allPlays, tableRowParams);
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
        ImageView awaySmall = findViewById(R.id.small_away_logo);
        ImageView homeSmall = findViewById(R.id.small_home_logo);
        TextView timeLeft = findViewById(R.id.time_remaining);
        TextView periodState = findViewById(R.id.period);
        try {
            JSONObject gameData = response.getJSONObject("gameData");
            JSONObject teams = gameData.getJSONObject("teams");
            setLogo(teams.getJSONObject("away").getString("name"), awayBig);
            setLogo(teams.getJSONObject("away").getString("name"), awaySmall);
            setLogo(teams.getJSONObject("home").getString("name"), homeBig);
            setLogo(teams.getJSONObject("home").getString("name"), homeSmall);
            timeLeft.setText(response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodOrdinal"));
            periodState.setText(response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodTimeRemaining"));
        } catch (JSONException e){
            System.out.println("Unexpected JSON exception");
        }

        addScoresToLineScore(response);
    }

    private void addScoresToLineScore(JSONObject response){
        try {
            JSONArray period = response.getJSONObject("liveData").getJSONObject("linescore").getJSONArray("periods");
            TextView awayOne = findViewById(R.id.away_one);
            TextView awayTwo = findViewById(R.id.away_two);
            TextView awayThree = findViewById(R.id.away_three);
            TextView awayFinal = findViewById(R.id.away_final);
            TextView homeOne = findViewById(R.id.home_one);
            TextView homeTwo = findViewById(R.id.home_two);
            TextView homeThree = findViewById(R.id.home_three);
            TextView homeFinal = findViewById(R.id.home_final);

            awayOne.setText(Integer.toString(period.getJSONObject(0).getJSONObject("away").getInt("goals")));
            awayTwo.setText(Integer.toString(period.getJSONObject(1).getJSONObject("away").getInt("goals")));
            awayThree.setText(Integer.toString(period.getJSONObject(2).getJSONObject("away").getInt("goals")));
            homeOne.setText(Integer.toString(period.getJSONObject(0).getJSONObject("home").getInt("goals")));
            homeTwo.setText(Integer.toString(period.getJSONObject(1).getJSONObject("home").getInt("goals")));
            homeThree.setText(Integer.toString(period.getJSONObject(2).getJSONObject("home").getInt("goals")));
            awayFinal.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                    .getJSONObject("away").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt("goals")));
            homeFinal.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                    .getJSONObject("home").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt("goals")));
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
