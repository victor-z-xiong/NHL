package no_name.nhl_app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import java.util.HashMap;

public class BoxScore extends AppCompatActivity {

    String triCodeAway = "";
    String triCodeHome = "";
    WebView replayWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_score);

        Bundle extras = getIntent().getExtras();
        final String url = extras.getString("BOX_SCORE_URL");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                makeBanner(response);
                makeMiddleLayer(response, true);
                makeMiddleLayer(response, false);
                makeScoringSummary(response, url);

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

    private void makeScoringSummary(JSONObject response, String url){

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
            TableLayout.LayoutParams tableRowParams2 = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            triCodeAway = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("away").getString("triCode");
            triCodeHome = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("home").getString("triCode");
            //String gameDate = response.getJSONObject("gameData").getJSONObject("dateTime").getString("dateTime").substring(0,10);
            for(int j = 0; j < periods.length(); j++){
                endOfPeriod  = 1 + response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(j).getInt("endIndex");
                startOfPeriod = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(j).getInt("startIndex");
                scoringPlays.clear();
                for(int k = 0; k < goals.length(); k++){
                    if(goals.getInt(k) <= endOfPeriod && goals.getInt(k) >=startOfPeriod){
                        scoringPlays.add(goals.getInt(k));
                        goalToEventID.put(goals.getInt(k), allPlays.getJSONObject(goals.getInt(k)).getJSONObject("about").getInt("eventId"));
                    }
                }

                periodRow = new TableRow(this);
                periodText = new TextView(this);
                periodString = periods.getJSONObject(j).getString("ordinalNum") + (j < 3 ? " period" : "");
                periodText.setText(periodString);
                periodText.setTextSize(18);
                periodText.setPadding(0,0,0,20);

                makeScoringSummaryPeriod(scoringPlays, ll, periodRow, periodText, allPlays, tableRowParams, tableRowParams2, url);
            }
            if(periods.length() == 0){
                TextView noGameYet = new TextView(this);
                noGameYet.setText("Game has yet to start!");
                noGameYet.setGravity(Gravity.CENTER);
                noGameYet.setPadding(0,30,0,0);
                ll.addView(noGameYet);
            }

        } catch(JSONException e){
            System.out.println("Unexpected JSON exception");
        }
    }
    HashMap<Integer, Integer> goalToEventID = new HashMap<Integer, Integer>();
    HashMap<Integer, String> idToPlayerURL = new HashMap<Integer, String>();
    HashMap<Integer, String> idToPlayerName = new HashMap<Integer, String>();
    HashMap<Integer, String> idToReplayURL = new HashMap<Integer, String>();
    int idMaker = 0;

    private void makeScoringSummaryPeriod(ArrayList<Integer> scoringPlays, LinearLayout scoringSummary,
                                          TableRow period, TextView periodText, JSONArray allPlays,
                                          TableLayout.LayoutParams tableRowParams, TableLayout.LayoutParams tableRowParams2,
                                          String url){
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        period.addView(periodText);
        period.setLayoutParams(tableRowParams);
        ll.addView(period);
        LinearLayout row1LinearLayout, rowMidLinearLayout, row2LinearLayout;
        TableRow row1, row2, spacerRow, midRow;
        TextView playTextLine1, goalScorerText, afterGoalScocerText, assistOneText, afterAssistOneText,
                assistTwoText, afterAssistTwoText, playTextLine2, blankView, assistTitleText, unassistedText;
        String playString1, playString2, goalScorer, assistOne, assistTwo, goalScorerUrl, assistOneUrl, assistTwoUrl;
        JSONObject playObject;
        ImageView replayButton;
        for(int i = 0; i < scoringPlays.size(); i++){
            try {
                idMaker++;
                row1 = new TableRow(this);
                row2 = new TableRow(this);
                midRow = new TableRow(this);
                row1LinearLayout = new LinearLayout(this);
                rowMidLinearLayout = new LinearLayout(this);
                row2LinearLayout = new LinearLayout(this);
                playTextLine1 = new TextView(this);
                goalScorerText = new TextView(this);
                afterGoalScocerText = new TextView(this);
                assistOneText = new TextView(this);
                afterAssistOneText = new TextView(this);
                assistTwoText = new TextView(this);
                afterAssistTwoText = new TextView(this);
                playTextLine2 = new TextView(this);
                assistTitleText = new TextView(this);
                unassistedText = new TextView(this);
                replayButton = new ImageView(this);
                replayButton.setImageResource(R.drawable.replay_button);
                playObject = allPlays.getJSONObject(scoringPlays.get(i));
                boolean unassisted = false;

                String triCode = playObject.getJSONObject("team").getString("triCode");
                String description = playObject.getJSONObject("result").getString("description");
                String typeOfGoal = typeOfGoal(description);
                String afterAssistOne = afterAssistOne(description);
                String afterAssistTwo = afterAssistTwo(description);
                switch(playObject.getJSONArray("players").length()){
                    case 4:
                        goalScorer = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("fullName");
                        assistOne = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("fullName");
                        assistTwo = playObject.getJSONArray("players").getJSONObject(2).getJSONObject("player").getString("fullName");
                        goalScorerUrl = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("link");
                        assistOneUrl = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("link");
                        assistTwoUrl = playObject.getJSONArray("players").getJSONObject(2).getJSONObject("player").getString("link");
                        break;
                    case 3:
                        goalScorer = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("fullName");
                        assistOne = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("fullName");
                        assistTwo = "";
                        goalScorerUrl = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("link");
                        assistOneUrl = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("link");
                        assistTwoUrl = "";
                        break;
                    default:
                        goalScorer = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("fullName");
                        assistOne = "";
                        assistTwo = "";
                        goalScorerUrl = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("link");
                        assistOneUrl = "";
                        assistTwoUrl = "";
                        unassisted = true;
                        break;
                }

                String timeOfGoal = playObject.getJSONObject("about").getString("periodTimeRemaining");
                String strength = playObject.getJSONObject("result").getJSONObject("strength").getString("code");
                String awayGoal = Integer.toString(playObject.getJSONObject("about").getJSONObject("goals").getInt("away"));
                String homeGoal = Integer.toString(playObject.getJSONObject("about").getJSONObject("goals").getInt("home"));

                playString1 = triCode + " ";
                playString2 = timeOfGoal + " " + strength + " " + triCodeAway + " " + awayGoal + " " + triCodeHome + " " + homeGoal;

                playTextLine1.setText(playString1);
                goalScorerText.setText(goalScorer);
                goalScorerText.setId(17*idMaker+34);
                idToPlayerURL.put(17*idMaker+34, goalScorerUrl);
                idToPlayerName.put(17*idMaker+34, goalScorer);
                goalScorerText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchPlayerPage(view.getId());
                    }
                });
                afterGoalScocerText.setText(typeOfGoal);
                assistOneText.setText(assistOne);
                assistOneText.setId(17*idMaker+35);
                idToPlayerURL.put(17*idMaker+35, assistOneUrl);
                idToPlayerName.put(17*idMaker+35, assistOne);
                assistOneText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchPlayerPage(view.getId());
                    }
                });
                afterAssistOneText.setText(afterAssistOne);
                assistTwoText.setText(assistTwo);
                assistTwoText.setId(17*idMaker+36);
                idToPlayerURL.put(17*idMaker+36, assistTwoUrl);
                idToPlayerName.put(17*idMaker+36, assistTwo);
                assistTwoText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchPlayerPage(view.getId());
                    }
                });
                afterAssistTwoText.setText(afterAssistTwo);
                playTextLine2.setText(playString2);
                assistTitleText.setText("Assists: ");
                unassistedText.setText("unassisted");

                replayButton.setId(17*idMaker+37);
                putReplayURLInHashMap(17*idMaker+37, url, scoringPlays.get(i));
                replayButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        launchReplay(view.getId());
                    }
                });
                replayButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

                row1LinearLayout.addView(playTextLine1);
                row1LinearLayout.addView(goalScorerText);
                row1LinearLayout.addView(afterGoalScocerText);
                rowMidLinearLayout.addView(assistTitleText);
                if(!unassisted) {
                    rowMidLinearLayout.addView(assistOneText);
                }else{
                    rowMidLinearLayout.addView(unassistedText);
                }
                rowMidLinearLayout.addView(afterAssistOneText);
                rowMidLinearLayout.addView(assistTwoText);
                rowMidLinearLayout.addView(afterAssistTwoText);

                row2LinearLayout.addView(playTextLine2);
                row2LinearLayout.addView(replayButton);

                row1.addView(row1LinearLayout);
                row2.addView(row2LinearLayout);
                midRow.addView(rowMidLinearLayout);
                tableRowParams.setMargins(20, 0,20, 0);
                row1.setLayoutParams(tableRowParams);
                midRow.setLayoutParams(tableRowParams);
                tableRowParams2.setMargins(20, 0,20, 30);
                row2.setLayoutParams(tableRowParams2);

                ll.addView(row1);
                ll.addView(midRow);
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

    private void launchReplay(int replayID){

        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(idToReplayURL.get(replayID))));
        Intent intent = new Intent(getApplicationContext(), replayPlayer.class);
        Bundle extras = new Bundle();
        extras.putString("REPLAY_URL", idToReplayURL.get(replayID));
        intent.putExtras(extras);
        startActivity(intent);

    }

    private void putReplayURLInHashMap(int replayIdImageView, String url, int goalId){

        String urlContent = url.substring(0, 52) + "content";
        final int goalID = goalId;
        final int replayIDImageView = replayIdImageView;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlContent, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

               goToGameContentURL(response, goalID, replayIDImageView);

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

    private void goToGameContentURL(JSONObject response, int gameId, int replayId){
        try{
            JSONArray items = response.getJSONObject("highlights").getJSONObject("gameCenter").getJSONArray("items");
            int eventId = goalToEventID.get(gameId);
            JSONArray keywords;
            for(int i = 0; i < items.length(); i++){
                keywords = items.getJSONObject(i).getJSONArray("keywords");
                for(int j = 0; j < keywords.length(); j++){
                    String type = keywords.getJSONObject(j).getString("type");
                    if(type.equals("statsEventId")){
                        if(Integer.parseInt(keywords.getJSONObject(j).getString("value")) == eventId){
                            String replayURL = items.getJSONObject(i).getJSONArray("playbacks").getJSONObject(9).getString("url");
                            idToReplayURL.put(replayId, replayURL);
                        }
                    }
                }
            }
        } catch (JSONException e){
            System.out.println("Something is wrong");
        }
    }


    private void makeBanner(JSONObject response){
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        ImageView awayBig = findViewById(R.id.away_banner_logo);
        ImageView homeBig = findViewById(R.id.home_banner_logo);

        TextView timeLeft = findViewById(R.id.time_remaining);
        TextView periodState = findViewById(R.id.period);
        TextView awayScore = findViewById(R.id.away_score);
        TextView homeScore = findViewById(R.id.home_score);
        try {
            JSONObject gameData = response.getJSONObject("gameData");
            JSONObject teams = gameData.getJSONObject("teams");
            setLogo(teams.getJSONObject("away").getString("name"), awayBig);
            setLogo(teams.getJSONObject("home").getString("name"), homeBig);

            timeLeft.setText(response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodOrdinal"));
            timeLeft.setLayoutParams(params);
            periodState.setText(response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodTimeRemaining"));
            awayScore.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                    .getJSONObject("away").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt("goals")));
            awayScore.setTextSize(18);
            awayScore.setLayoutParams(params);
            awayScore.setGravity(Gravity.CENTER);
            awayScore.setTypeface(null, Typeface.BOLD);
            homeScore.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                    .getJSONObject("home").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt("goals")));
            homeScore.setTextSize(18);
            homeScore.setLayoutParams(params);
            homeScore.setGravity(Gravity.CENTER);
            homeScore.setTypeface(null, Typeface.BOLD);

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
        String gameState = getAbstractGameState(response);
        addScoresToLineScore(response, params, scoring, gameState);
    }



    private void addScoresToLineScore(JSONObject response, android.widget.LinearLayout.LayoutParams params, boolean scoring, String gameState){
        try {
            JSONArray period = response.getJSONObject("liveData").getJSONObject("linescore").getJSONArray("periods");
            TextView away = new TextView(this);
            TextView home = new TextView (this);
            TextView title = new TextView(this);
            LinearLayout titleLayout = findViewById(scoring ? R.id.Header_box_score : R.id.Header_shot_total);
            LinearLayout awayLayout = findViewById(scoring ? R.id.away_box_score : R.id.away_shot_total);
            LinearLayout homeLayout = findViewById(scoring ? R.id.home_box_score : R.id.home_shot_total);

            if(gameState.equals("Final")){
                addScoresFinal(response, period, title, away, home, titleLayout, awayLayout, homeLayout, params, scoring);
            }else if(gameState.equals("Live")){
                addScoresLive(response, period, title, away, home, titleLayout, awayLayout, homeLayout, params, scoring);
            }else{
                addScoresPreview(response, period, title, away, home, titleLayout, awayLayout, homeLayout, params, scoring);
            }

        } catch (JSONException e){
            System.out.println("Unexpected JSON exception");
        }
    }

    private void addScoresPreview(JSONObject response, JSONArray period, TextView title, TextView away, TextView home, LinearLayout titleLayout, LinearLayout awayLayout,
                                  LinearLayout homeLayout, android.widget.LinearLayout.LayoutParams params, boolean scoring){

        for(int j = 0; j < 4; j++){
            title = new TextView(this);
            away = new TextView(this);
            home = new TextView(this);

            title.setText(setOrdinalNumber(j));
            away.setText("---");
            home.setText("---");


            title.setLayoutParams(params);
            away.setLayoutParams(params);
            home.setLayoutParams(params);

            titleLayout.addView(title);
            awayLayout.addView(away);
            homeLayout.addView(home);
        }
    }

    private void addScoresLive(JSONObject response, JSONArray period, TextView title, TextView away, TextView home, LinearLayout titleLayout, LinearLayout awayLayout,
                               LinearLayout homeLayout, android.widget.LinearLayout.LayoutParams params, boolean scoring){
        try {
            int i;
            for(i = 0; i < period.length(); i++){
                title = new TextView(this);
                away = new TextView(this);
                home = new TextView(this);

                title.setText(period.getJSONObject(i).getString("ordinalNum"));
                away.setText(Integer.toString(period.getJSONObject(i).getJSONObject("away").getInt(scoring ? "goals" : "shotsOnGoal")));
                home.setText(Integer.toString(period.getJSONObject(i).getJSONObject("home").getInt(scoring ? "goals" : "shotsOnGoal")));


                title.setLayoutParams(params);
                away.setLayoutParams(params);
                home.setLayoutParams(params);

                titleLayout.addView(title);
                awayLayout.addView(away);
                homeLayout.addView(home);
            }

            if(i < 4){
                for(int j = i; j < 4; j++){
                    title = new TextView(this);
                    away = new TextView(this);
                    home = new TextView(this);

                    title.setText(setOrdinalNumber(j));
                    away.setText("---");
                    home.setText("---");


                    title.setLayoutParams(params);
                    away.setLayoutParams(params);
                    home.setLayoutParams(params);

                    titleLayout.addView(title);
                    awayLayout.addView(away);
                    homeLayout.addView(home);
                }
            }

        } catch (JSONException e){
            System.out.println("Unexpected JSON exception");
        }
    }

    private void addScoresFinal(JSONObject response, JSONArray period, TextView title, TextView away, TextView home, LinearLayout titleLayout, LinearLayout awayLayout,
                                LinearLayout homeLayout, android.widget.LinearLayout.LayoutParams params, boolean scoring){
        try {
            for(int i = 0; i < period.length()+1; i++){
                title = new TextView(this);
                away = new TextView(this);
                home = new TextView(this);

                if(i == period.length()){
                    title.setText("Final");
                    away.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                            .getJSONObject("away").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt(scoring ? "goals" : "shots")));
                    home.setText(Integer.toString(response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                            .getJSONObject("home").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt(scoring ? "goals" : "shots")));
                }else{
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

    private String getAbstractGameState(JSONObject response){
        String gameState = "";
        try{
            gameState = response.getJSONObject("gameData").getJSONObject("status").getString("abstractGameState");
        } catch (JSONException e){
            System.out.println("Unexpected JSON Exception");
        }
        return gameState;
    }

    //only gives periods 1st, 2nd, 3rd and final
    private String setOrdinalNumber(int j){

        String ordinalNum = "";
        switch(j){
            case 0:
                ordinalNum = "1st";
                break;
            case 1:
                ordinalNum = "2nd";
                break;
            case 2:
                ordinalNum = "3rd";
                break;
            default:
                ordinalNum = "Final";
                break;
        }

        return ordinalNum;
    }

    private String afterGoalScorer(String description){
        String goalScorer = "";
        int startIndex = description.indexOf('(') - 1;
        int endIndex = description.indexOf(':');
        if(endIndex == -1) {
            goalScorer = description.substring(startIndex);
        }else{
            goalScorer = description.substring(startIndex, endIndex);
        }
        return goalScorer;
    }

    private String typeOfGoal(String description){
        description = afterGoalScorer(description);
        return description.substring(0, description.indexOf(','));
    }

    private String afterAssistOne(String description) {
        description = removeBracket(description);
        if(description.length() > 0) {
            description = description.substring(description.indexOf(':'));
            int startIndex = description.indexOf('(') - 1;
            int endIndex = description.indexOf(',') + 2;
            String assist = "";
            try {
                assist = description.substring(startIndex, endIndex);
            } catch (IndexOutOfBoundsException e) {
                assist = "";
            }
            return assist;
        }
        return "";
    }

    private String afterAssistTwo(String description) {
        description = removeBracket(description);
        description = removeBracket(description);
        if(description.length() > 0) {
            int startIndex = description.indexOf('(') - 1;
            String assist = "";
            try {
                assist = description.substring(startIndex);
            } catch (IndexOutOfBoundsException e) {
                assist = "";
            }
            return assist;
        }
        return "";
    }

    private String removeBracket(String description){
        int firstBracket = description.indexOf(')');
        try{
            description = description.substring(firstBracket + 1);
        }catch(IndexOutOfBoundsException e) {
            description = "";
        }

        return description;
    }

    private void launchPlayerPage(int textId){
        Intent intent = new Intent(getApplicationContext(), Player.class);
        Bundle extras = new Bundle();
        extras.putString("PLAYER_NAME", idToPlayerName.get(textId));
        extras.putString("PLAYER_URL", idToPlayerURL.get(textId));
        intent.putExtras(extras);
        startActivity(intent);
    }
}
