package no_name.nhl_app;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.support.v4.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class BoxScore extends AppCompatActivity {

    String triCodeAway = "";
    String triCodeHome = "";
    int pixelWidth = 0;
    Boolean hasHighlights = true;
    int i = 0;
    String gameContentUrl = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_box_score);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pixelWidth = size.x;

        Bundle extras = getIntent().getExtras();
        final String url = extras.getString("BOX_SCORE_URL");
        gameContentUrl = url.replace("feed/live", "content");

        refreshBoxScoreData(url);

        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipelayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshBoxScoreData(url);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshBoxScoreData(String paramUrl){
        final String url = paramUrl;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                makeBanner(response);
                makeMiddleLayer(response, true);
                makeMiddleLayer(response, false);
                makeScoringSummary(response, url);
                makeTeamSummary(response);
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

    private void makeTeamSummary(JSONObject response){
        try{
            LinearLayout ll = (LinearLayout) findViewById(R.id.scoring_summary);
            addTeamSummaryBanner(ll);
            HorizontalScrollView teamSummaryView = new HorizontalScrollView(this);
            LinearLayout llForTeamSummaryView = new LinearLayout(this);
            llForTeamSummaryView.setOrientation(LinearLayout.VERTICAL);
            JSONObject awayTeamObject = response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams").getJSONObject("away");
            JSONObject homeTeamObject = response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams").getJSONObject("home");
            makeIndividualTeamSummary(awayTeamObject, llForTeamSummaryView);
            makeIndividualTeamSummary(homeTeamObject, llForTeamSummaryView);
            teamSummaryView.addView(llForTeamSummaryView);
            ll.addView(teamSummaryView);
        }catch(JSONException e){
            System.out.println("makeTeamSummary method in BoxScore.java exception");
        }
    }

    private void makeIndividualTeamSummary(JSONObject teamObject, LinearLayout scoringSummary){
        try{
            String teamName = teamObject.getJSONObject("team").getString("name");
            addTeamTitleToTeamStatsSummary(scoringSummary, teamName);
            JSONObject players = teamObject.getJSONObject("players");
            if(players.length() != 0){
                addTeamStatSummaryHeader(scoringSummary);
                addTeamPlayersToTeamSummary(scoringSummary, players);
                addSpacerRow(scoringSummary);
                addTeamTitleToTeamStatsSummary(scoringSummary, "Goalies");
                addGoalieStatSummaryHeader(scoringSummary);
                addTeamGoaliesToTeamSummary(scoringSummary, players);
                addSpacerRow(scoringSummary);
            }
        } catch (JSONException e){
            System.out.println("makeIndividualTeamSummary method Boxscore.java exception");
        }
    }

    private void addTeamGoaliesToTeamSummary(LinearLayout scoringSummary, JSONObject players){
        try{
            int counterForAlternatingRowColor = 0;
            for(int i = 0; i < players.names().length(); i++){
                JSONObject player = (JSONObject) players.get(players.names().getString(i));
                if(player.getJSONObject("stats").length() != 0){
                    if(player.getJSONObject("position").getString("name").equals("Goalie")){
                        addGoalieStatToTeamSummary(player, scoringSummary, counterForAlternatingRowColor);
                        counterForAlternatingRowColor++;
                    }
                }
            }
        }catch(JSONException e){
            System.out.println("addTeamGoaliesToTeamSummary method Boxscore.java exception");
        }
    }

    private void addTeamPlayersToTeamSummary(LinearLayout scoringSummary, JSONObject players){
        try{
            int counterForAlternatingRowColor = 0;
            for(int i = 0; i < players.names().length(); i++){
                JSONObject player = (JSONObject) players.get(players.names().getString(i));
                if(player.getJSONObject("stats").length() != 0){
                    if(!player.getJSONObject("position").getString("name").equals("Goalie")){
                        addPlayerStatToTeamSummary(player, scoringSummary, counterForAlternatingRowColor);
                        counterForAlternatingRowColor++;
                    }
                }
            }
        }catch(JSONException e){
            System.out.println("addTeamPlayersToTeamSummary method Boxscore.java exception");
        }
    }

    private void addSpacerRow(LinearLayout parentLayout)
    {
        LinearLayout spacerRow = new LinearLayout(this);
        TextView space = new TextView(this);
        spacerRow.addView(space);
        parentLayout.addView(spacerRow);
    }

    private void addGoalieStatSummaryHeader(LinearLayout scoringSummary){
        LinearLayout tableHead = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView number = new TextView(this);
        TextView playerName = new TextView(this);
        TextView position = new TextView(this);
        TextView toi = new TextView(this);
        TextView saves = new TextView(this);
        TextView shots = new TextView(this);
        TextView savePercentage = new TextView(this);
        TextView ppSaves = new TextView(this);
        TextView evenSaves = new TextView(this);
        TextView shSA = new TextView(this);
        TextView eSA = new TextView(this);
        TextView ppSA= new TextView(this);
        TextView eSP = new TextView(this);

        number.setText("#");
        setTitleParams(number, mainColumnSpacing(), true);

        playerName.setText("Name");
        setTitleParams(playerName, teamNameSpacing(), true);
        playerName.setGravity(Gravity.LEFT);

        position.setText("POS");
        setTitleParams(position, mainColumnSpacing(), true);

        toi.setText("TOI");
        setTitleParams(toi, mainColumnSpacing(), true);

        saves.setText("Saves");
        setTitleParams(saves, mainColumnSpacing(), true);

        shots.setText("S");
        setTitleParams(shots, mainColumnSpacing(), true);

        savePercentage.setText("S%");
        setTitleParams(savePercentage, mainColumnSpacing(), true);

        ppSaves.setText("ppS");
        setTitleParams(ppSaves, mainColumnSpacing(), true);

        evenSaves.setText("eS");
        setTitleParams(evenSaves, mainColumnSpacing(), true);

        shSA.setText("shSA");
        setTitleParams(shSA, mainColumnSpacing(), true);

        eSA.setText("eSA");
        setTitleParams(eSA, mainColumnSpacing(), true);

        ppSA.setText("ppSA");
        setTitleParams(ppSA, mainColumnSpacing(), true);

        eSP.setText("eS%");
        setTitleParams(eSP, mainColumnSpacing(), true);

        tableHead.addView(number);
        tableHead.addView(playerName);
        tableHead.addView(position);
        tableHead.addView(toi);
        tableHead.addView(saves);
        tableHead.addView(shots);
        tableHead.addView(savePercentage);
        tableHead.addView(ppSaves);
        tableHead.addView(evenSaves);
        tableHead.addView(shSA);
        tableHead.addView(eSA);
        tableHead.addView(ppSA);
        tableHead.addView(eSP);

        row.addView(tableHead);
        scoringSummary.addView(row);
    }

    private void addGoalieStatToTeamSummary(JSONObject playerObject, LinearLayout scoringSummary, int rowNum){
        LinearLayout horizontalRow = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView number = new TextView(this);
        TextView playerName = new TextView(this);
        TextView position = new TextView(this);
        TextView toi = new TextView(this);
        TextView saves = new TextView(this);
        TextView shots = new TextView(this);
        TextView savePercentage = new TextView(this);
        TextView ppSaves = new TextView(this);
        TextView evenSaves = new TextView(this);
        TextView shSA = new TextView(this);
        TextView eSA = new TextView(this);
        TextView ppSA= new TextView(this);
        TextView eSP = new TextView(this);

        try{
            idMaker++;
            JSONObject goalieStats = playerObject.getJSONObject("stats").getJSONObject("goalieStats");
            String playerUrl = playerObject.getJSONObject("person").getString("link");
            String name = playerObject.getJSONObject("person").getString("fullName");

            if(playerObject.has("jerseyNumber")){
                String jerseyNumber = playerObject.getString("jerseyNumber");
                number.setText(jerseyNumber);
                setTitleParams(number, mainColumnSpacing(), false);
            }

            playerName.setText(name);
            setTitleParams(playerName, teamNameSpacing(), false);
            playerName.setGravity(Gravity.LEFT);
            playerName.setId(17*idMaker+31);
            idToPlayerURL.put(17*idMaker+31, playerUrl);
            idToPlayerName.put(17*idMaker+31, name);
            playerName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchPlayerPage(view.getId());
                }
            });

            position.setText(playerObject.getJSONObject("position").getString("abbreviation"));
            setTitleParams(position, mainColumnSpacing(), false);

            toi.setText(goalieStats.getString("timeOnIce"));
            setTitleParams(toi, mainColumnSpacing(), false);

            saves.setText(Integer.toString(goalieStats.getInt("saves")));
            setTitleParams(saves, mainColumnSpacing(), false);

            shots.setText(Integer.toString(goalieStats.getInt("shots")));
            setTitleParams(shots, mainColumnSpacing(), false);

            savePercentage.setText(new DecimalFormat("#.##").format(goalieStats.getDouble("savePercentage")));
            setTitleParams(savePercentage, mainColumnSpacing(), false);

            ppSaves.setText(Integer.toString(goalieStats.getInt("powerPlaySaves")));
            setTitleParams(ppSaves, mainColumnSpacing(), false);

            evenSaves.setText(Integer.toString(goalieStats.getInt("evenSaves")));
            setTitleParams(evenSaves, mainColumnSpacing(), false);

            shSA.setText(Integer.toString(goalieStats.getInt("shortHandedShotsAgainst")));
            setTitleParams(shSA, mainColumnSpacing(), false);

            eSA.setText(Integer.toString(goalieStats.getInt("evenShotsAgainst")));
            setTitleParams(eSA, mainColumnSpacing(), false);

            ppSA.setText(Integer.toString(goalieStats.getInt("powerPlayShotsAgainst")));
            setTitleParams(ppSA, mainColumnSpacing(), false);

            eSP.setText(new DecimalFormat("#.##").format(goalieStats.getDouble("evenStrengthSavePercentage")));
            setTitleParams(eSP, mainColumnSpacing(), false);

        } catch (JSONException e){
            System.out.println("makeIndividualTeamSummary method Boxscore.java exception");
        }

        if(rowNum % 2 == 0){
            number.setTextColor(Color.WHITE);
            playerName.setTextColor(Color.WHITE);
            position.setTextColor(Color.WHITE);
            toi.setTextColor(Color.WHITE);
            shots.setTextColor(Color.WHITE);
            saves.setTextColor(Color.WHITE);
            savePercentage.setTextColor(Color.WHITE);
            ppSaves.setTextColor(Color.WHITE);
            evenSaves.setTextColor(Color.WHITE);
            shSA.setTextColor(Color.WHITE);
            eSA.setTextColor(Color.WHITE);
            ppSA.setTextColor(Color.WHITE);
            eSP.setTextColor(Color.WHITE);
            row.setBackgroundColor(Color.GRAY);
        }

        horizontalRow.addView(number);
        horizontalRow.addView(playerName);
        horizontalRow.addView(position);
        horizontalRow.addView(toi);
        horizontalRow.addView(saves);
        horizontalRow.addView(shots);
        horizontalRow.addView(savePercentage);
        horizontalRow.addView(ppSaves);
        horizontalRow.addView(evenSaves);
        horizontalRow.addView(shSA);
        horizontalRow.addView(eSA);
        horizontalRow.addView(ppSA);
        horizontalRow.addView(eSP);

        row.addView(horizontalRow);
        scoringSummary.addView(row);
    }


    private void addTeamStatSummaryHeader(LinearLayout scoringSummary){
        LinearLayout tableHead = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView number = new TextView(this);
        TextView playerName = new TextView(this);
        TextView position = new TextView(this);
        TextView toi = new TextView(this);
        TextView goals = new TextView(this);
        TextView assists = new TextView(this);
        TextView shotsOnGoal = new TextView(this);
        TextView hits = new TextView(this);
        TextView plusMinus = new TextView(this);
        TextView blockedShots = new TextView(this);
        TextView penaltyMinutes = new TextView(this);
        TextView evenTOI= new TextView(this);
        TextView powerPlayTOI= new TextView(this);
        TextView shortHandedTOI= new TextView(this);
        TextView powerPlayGoals = new TextView(this);
        TextView powerPlayAssists = new TextView(this);
        TextView faceOffPct = new TextView(this);
        TextView faceOffWins = new TextView(this);

        number.setText("#");
        setTitleParams(number, mainColumnSpacing(), true);

        playerName.setText("Name");
        setTitleParams(playerName, teamNameSpacing(), true);
        playerName.setGravity(Gravity.LEFT);

        position.setText("POS");
        setTitleParams(position, mainColumnSpacing(), true);

        toi.setText("TOI");
        setTitleParams(toi, mainColumnSpacing(), true);

        goals.setText("G");
        setTitleParams(goals, mainColumnSpacing(), true);

        assists.setText("A");
        setTitleParams(assists, mainColumnSpacing(), true);

        shotsOnGoal.setText("SOG");
        setTitleParams(shotsOnGoal, mainColumnSpacing(), true);

        hits.setText("Hits");
        setTitleParams(hits, mainColumnSpacing(), true);

        plusMinus.setText("+/-");
        setTitleParams(plusMinus, mainColumnSpacing(), true);

        blockedShots.setText("BLKS");
        setTitleParams(blockedShots, mainColumnSpacing(), true);

        penaltyMinutes.setText("PIM");
        setTitleParams(penaltyMinutes, mainColumnSpacing(), true);

        evenTOI.setText("eTOI");
        setTitleParams(evenTOI, mainColumnSpacing(), true);

        powerPlayTOI.setText("PPT");
        setTitleParams(powerPlayTOI, mainColumnSpacing(), true);

        shortHandedTOI.setText("SHT");
        setTitleParams(shortHandedTOI, mainColumnSpacing(), true);

        powerPlayGoals.setText("PPG");
        setTitleParams(powerPlayGoals, mainColumnSpacing(), true);

        powerPlayAssists.setText("PPA");
        setTitleParams(powerPlayAssists, mainColumnSpacing(), true);

        faceOffPct.setText("F/O%");
        setTitleParams(faceOffPct, mainColumnSpacing(), true);

        faceOffWins.setText("F/O(w)");
        setTitleParams(faceOffWins, mainColumnSpacing(), true);

        tableHead.addView(number);
        tableHead.addView(playerName);
        tableHead.addView(position);
        tableHead.addView(toi);
        tableHead.addView(goals);
        tableHead.addView(assists);
        tableHead.addView(shotsOnGoal);
        tableHead.addView(hits);
        tableHead.addView(plusMinus);
        tableHead.addView(blockedShots);
        tableHead.addView(evenTOI);
        tableHead.addView(powerPlayTOI);
        tableHead.addView(shortHandedTOI);
        tableHead.addView(powerPlayGoals);
        tableHead.addView(powerPlayAssists);
        tableHead.addView(faceOffPct);
        tableHead.addView(faceOffWins);

        row.addView(tableHead);
        scoringSummary.addView(row);
    }


    private void addPlayerStatToTeamSummary(JSONObject playerObject, LinearLayout scoringSummary, int rowNum){
        LinearLayout horizontalRow = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView number = new TextView(this);
        TextView playerName = new TextView(this);
        TextView position = new TextView(this);
        TextView toi = new TextView(this);
        TextView goals = new TextView(this);
        TextView assists = new TextView(this);
        TextView shotsOnGoal = new TextView(this);
        TextView hits = new TextView(this);
        TextView plusMinus = new TextView(this);
        TextView blockedShots = new TextView(this);
        TextView penaltyMinutes = new TextView(this);
        TextView evenTOI= new TextView(this);
        TextView powerPlayTOI= new TextView(this);
        TextView shortHandedTOI= new TextView(this);
        TextView powerPlayGoals = new TextView(this);
        TextView powerPlayAssists = new TextView(this);
        TextView faceOffPct = new TextView(this);
        TextView faceOffWins = new TextView(this);

        try{
            idMaker++;
            JSONObject skaterStats = playerObject.getJSONObject("stats").getJSONObject("skaterStats");
            String playerUrl = playerObject.getJSONObject("person").getString("link");
            String name = playerObject.getJSONObject("person").getString("fullName");

            if(playerObject.has("jerseyNumber")){
                String jerseyNumber = playerObject.getString("jerseyNumber");
                number.setText(jerseyNumber);
                setTitleParams(number, mainColumnSpacing(), false);
            }

            playerName.setText(name);
            setTitleParams(playerName, teamNameSpacing(), false);
            playerName.setGravity(Gravity.LEFT);
            playerName.setId(17*idMaker+31);
            idToPlayerURL.put(17*idMaker+31, playerUrl);
            idToPlayerName.put(17*idMaker+31, name);
            playerName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchPlayerPage(view.getId());
                }
            });

            position.setText(playerObject.getJSONObject("position").getString("abbreviation"));
            setTitleParams(position, mainColumnSpacing(), false);

            toi.setText(skaterStats.getString("timeOnIce"));
            setTitleParams(toi, mainColumnSpacing(), false);

            goals.setText(Integer.toString(skaterStats.getInt("goals")));
            setTitleParams(goals, mainColumnSpacing(), false);

            assists.setText(Integer.toString(skaterStats.getInt("assists")));
            setTitleParams(assists, mainColumnSpacing(), false);

            shotsOnGoal.setText(Integer.toString(skaterStats.getInt("shots")));
            setTitleParams(shotsOnGoal, mainColumnSpacing(), false);

            hits.setText(Integer.toString(skaterStats.getInt("hits")));
            setTitleParams(hits, mainColumnSpacing(), false);

            plusMinus.setText(Integer.toString(skaterStats.getInt("plusMinus")));
            setTitleParams(plusMinus, mainColumnSpacing(), false);

            blockedShots.setText(Integer.toString(skaterStats.getInt("blocked")));
            setTitleParams(blockedShots, mainColumnSpacing(), false);

            penaltyMinutes.setText(Integer.toString(skaterStats.getInt("penaltyMinutes")));
            setTitleParams(penaltyMinutes, mainColumnSpacing(), false);

            evenTOI.setText(skaterStats.getString("evenTimeOnIce"));
            setTitleParams(evenTOI, mainColumnSpacing(), false);

            powerPlayTOI.setText(skaterStats.getString("powerPlayTimeOnIce"));
            setTitleParams(powerPlayTOI, mainColumnSpacing(), false);

            shortHandedTOI.setText(skaterStats.getString("shortHandedTimeOnIce"));
            setTitleParams(shortHandedTOI, mainColumnSpacing(), false);

            powerPlayGoals.setText(Integer.toString(skaterStats.getInt("powerPlayGoals")));
            setTitleParams(powerPlayGoals, mainColumnSpacing(), false);

            powerPlayAssists.setText(Integer.toString(skaterStats.getInt("powerPlayAssists")));
            setTitleParams(powerPlayAssists, mainColumnSpacing(), false);

            faceOffPct.setText(Integer.toString(skaterStats.getInt("faceOffPct")));
            setTitleParams(faceOffPct, mainColumnSpacing(), false);

            faceOffWins.setText(Integer.toString(skaterStats.getInt("faceOffWins")));
            setTitleParams(faceOffWins, mainColumnSpacing(), false);

        } catch (JSONException e){
            System.out.println("makeIndividualTeamSummary method Boxscore.java exception");
        }

        if(rowNum % 2 == 0){
            number.setTextColor(Color.WHITE);
            playerName.setTextColor(Color.WHITE);
            position.setTextColor(Color.WHITE);
            toi.setTextColor(Color.WHITE);
            goals.setTextColor(Color.WHITE);
            assists.setTextColor(Color.WHITE);
            shotsOnGoal.setTextColor(Color.WHITE);
            hits.setTextColor(Color.WHITE);
            plusMinus.setTextColor(Color.WHITE);
            blockedShots.setTextColor(Color.WHITE);
            evenTOI.setTextColor(Color.WHITE);
            powerPlayTOI.setTextColor(Color.WHITE);
            shortHandedTOI.setTextColor(Color.WHITE);
            powerPlayGoals.setTextColor(Color.WHITE);
            powerPlayAssists.setTextColor(Color.WHITE);
            faceOffPct.setTextColor(Color.WHITE);
            faceOffWins.setTextColor(Color.WHITE);
            row.setBackgroundColor(Color.GRAY);
        }

        horizontalRow.addView(number);
        horizontalRow.addView(playerName);
        horizontalRow.addView(position);
        horizontalRow.addView(toi);
        horizontalRow.addView(goals);
        horizontalRow.addView(assists);
        horizontalRow.addView(shotsOnGoal);
        horizontalRow.addView(hits);
        horizontalRow.addView(plusMinus);
        horizontalRow.addView(blockedShots);
        horizontalRow.addView(evenTOI);
        horizontalRow.addView(powerPlayTOI);
        horizontalRow.addView(shortHandedTOI);
        horizontalRow.addView(powerPlayGoals);
        horizontalRow.addView(powerPlayAssists);
        horizontalRow.addView(faceOffPct);
        horizontalRow.addView(faceOffWins);

        row.addView(horizontalRow);
        scoringSummary.addView(row);
    }

    private void addTeamTitleToTeamStatsSummary(LinearLayout scoringSummary, String teamName){
        LinearLayout teamSummaryBanner = new LinearLayout(this);
        TextView teamSummaryText = new TextView(this);
        teamSummaryText.setText(teamName);
        teamSummaryText.setTextSize(14);
        teamSummaryText.setTypeface(null, Typeface.BOLD);
        teamSummaryBanner.addView(teamSummaryText);
        scoringSummary.addView(teamSummaryBanner);
    }

    private void addTeamSummaryBanner(LinearLayout scoringSummary){
        LinearLayout teamSummaryBanner = new LinearLayout(this);
        TextView teamSummaryText = new TextView(this);
        teamSummaryText.setText("Team Summary");
        teamSummaryText.setTextSize(18);
        teamSummaryText.setTypeface(null, Typeface.BOLD);
        teamSummaryText.setGravity(Gravity.CENTER);
        teamSummaryBanner.addView(teamSummaryText);
        scoringSummary.addView(teamSummaryBanner);
    }

    private void makeScoringSummary(JSONObject response, String url){

        ArrayList<Integer> scoringPlays = new ArrayList<Integer>();
        try {
            JSONArray goals = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("scoringPlays");
            JSONArray periods = response.getJSONObject("liveData").getJSONObject("linescore").getJSONArray("periods");
            String currentPeriodOrdinal = response.getJSONObject("liveData").getJSONObject("linescore").has("currentPeriodOrdinal") ?
                    response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodOrdinal") : "";
            int endOfPeriod, startOfPeriod;
            LinearLayout ll = (LinearLayout) findViewById(R.id.scoring_summary);
            int numChildren = ll.getChildCount();
            if(numChildren > 12){
                ll.removeViews(12, numChildren - 12);  // for refreshing the box score
            }                                                    // 12 is number of children in scoring summary view on first load of box score
            TableRow periodRow;
            TextView periodText;
            String periodString;
            JSONArray allPlays = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("allPlays");
            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableLayout.LayoutParams tableRowParams2 = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
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

                makeScoringSummaryPeriod(scoringPlays, ll, periodRow, periodText, allPlays, tableRowParams, tableRowParams2, url, false);
            }

            if(currentPeriodOrdinal.equals("SO")){
                endOfPeriod  = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(4).getInt("endIndex");
                startOfPeriod = response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(4).getInt("startIndex");
                scoringPlays.clear();
                for(int k = startOfPeriod; k <= endOfPeriod; k++){
                    if(allPlays.getJSONObject(k).has("players")){
                        scoringPlays.add(k);
                        goalToEventID.put(k, allPlays.getJSONObject(k).getJSONObject("about").getInt("eventId"));
                    }
                }

                periodRow = new TableRow(this);
                periodText = new TextView(this);
                periodText.setText("SO");
                periodText.setTextSize(18);
                periodText.setPadding(0,0,0,20);

                makeScoringSummaryPeriod(scoringPlays, ll, periodRow, periodText, allPlays, tableRowParams, tableRowParams2, url, true);
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
                                          String url, boolean isSO){
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
                String typeOfGoal = typeOfGoal(description, isSO);
                String afterAssistOne = afterAssistOne(description, isSO);
                String afterAssistTwo = afterAssistTwo(description);
                int numPlayersInPlay = playObject.getJSONArray("players").length();
                boolean isEmptyNetPlay = isSO ? false : playObject.getJSONObject("result").getString("emptyNet") == "true";
                if((numPlayersInPlay == 4 && !isEmptyNetPlay) || (numPlayersInPlay == 3 && isEmptyNetPlay)) {
                    goalScorer = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("fullName");
                    assistOne = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("fullName");
                    assistTwo = playObject.getJSONArray("players").getJSONObject(2).getJSONObject("player").getString("fullName");
                    goalScorerUrl = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("link");
                    assistOneUrl = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("link");
                    assistTwoUrl = playObject.getJSONArray("players").getJSONObject(2).getJSONObject("player").getString("link");
                }
                else if ((numPlayersInPlay == 3 && !isEmptyNetPlay) || (numPlayersInPlay == 2 && isEmptyNetPlay)) {
                    goalScorer = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("fullName");
                    assistOne = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("fullName");
                    assistTwo = "";
                    goalScorerUrl = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("link");
                    assistOneUrl = playObject.getJSONArray("players").getJSONObject(1).getJSONObject("player").getString("link");
                    assistTwoUrl = "";
                }
                else{
                    goalScorer = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("fullName");
                    assistOne = "";
                    assistTwo = "";
                    goalScorerUrl = playObject.getJSONArray("players").getJSONObject(0).getJSONObject("player").getString("link");
                    assistOneUrl = "";
                    assistTwoUrl = "";
                    unassisted = true;
                }

                String timeOfGoal = playObject.getJSONObject("about").getString("periodTimeRemaining");
                String strength = isSO ? "EVEN" : playObject.getJSONObject("result").getJSONObject("strength").getString("code");
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
                afterGoalScocerText.setText(!isSO ? typeOfGoal : " -" + typeOfGoal);
                assistOneText.setText(assistOne);
                assistOneText.setTextSize(assistOne.length() + assistTwo.length() > 25 && pixelWidth < 1080 ? 11 : 14);
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
                assistTwoText.setTextSize(assistOne.length() + assistTwo.length() > 25 && pixelWidth < 1080 ? 11 : 14);
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
                assistTitleText.setText(!isSO ? "Assists: " : (isShootoutGoalPlay(playObject) ? "GOAL" : playObject.getJSONObject("result").getString("event")));
                unassistedText.setText(!isSO ? "unassisted" : "");

                replayButton.setId(17*idMaker+37);
                putReplayURLInHashMap(17*idMaker+37, url, scoringPlays.get(i), replayButton);
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
                if(hasHighlights && !isSO){
                    row2LinearLayout.addView(replayButton);
                } else if(hasHighlights && isSO){
                    rowMidLinearLayout.addView(replayButton);
                }

                if(isSO){
                    row2.setVisibility(View.GONE);
                }

                row1.addView(row1LinearLayout);
                row2.addView(row2LinearLayout);
                midRow.addView(rowMidLinearLayout);
                tableRowParams.setMargins(20, 0,20, 0);
                row1.setLayoutParams(tableRowParams);
                tableRowParams2.setMargins(20, 0,20, 30);
                midRow.setLayoutParams(isSO ? tableRowParams2 :  tableRowParams);
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

    private boolean isShootoutGoalPlay(JSONObject playObject){
        try{
            return playObject.getJSONObject("result").getString("event").equals("Goal");
        }catch(JSONException e){
            System.out.println("isShootoutGoalPlay threw an exception");
        }
        return false;
    }

    private void launchReplay(int replayID){

        //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(idToReplayURL.get(replayID))));
        Intent intent = new Intent(getApplicationContext(), replayPlayer.class);
        Bundle extras = new Bundle();
        extras.putString("REPLAY_URL", idToReplayURL.get(replayID));
        intent.putExtras(extras);
        startActivity(intent);

    }

    private void putReplayURLInHashMap(int replayIdImageView, String url, int goalId, final ImageView replayButton){

        String urlContent = url.substring(0, 52) + "content";
        final int goalID = goalId;
        final int replayIDImageView = replayIdImageView;
        final ImageView replayButtonFinal = replayButton;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, urlContent, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

               goToGameContentURL(response, goalID, replayIDImageView, replayButtonFinal);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Something is wrong");
                error.printStackTrace();
                replayButtonFinal.setVisibility(View.GONE);
            }
        });


        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    private void goToGameContentURL(JSONObject response, int gameId, int replayId, ImageView replayButtonFinal){
        try{
            JSONArray items = response.getJSONObject("highlights").getJSONObject("gameCenter").getJSONArray("items");
            int eventId = goalToEventID.get(gameId);
            JSONArray keywords;
            boolean replayFound = false;
            if(items.length() == 0){
                hasHighlights = false;
            }
            for(int i = 0; i < items.length(); i++){
                keywords = items.getJSONObject(i).getJSONArray("keywords");
                for(int j = 0; j < keywords.length(); j++){
                    String type = keywords.getJSONObject(j).getString("type");
                    if(type.equals("statsEventId")){
                        if(Integer.parseInt(keywords.getJSONObject(j).getString("value")) == eventId){
                            String replayURL = items.getJSONObject(i).getJSONArray("playbacks").getJSONObject(3).getString("url");
                            idToReplayURL.put(replayId, replayURL);
                            replayFound = true;
                        }
                    }
                }
            }
            if(!replayFound){
                replayButtonFinal.setVisibility(View.GONE);
            }
        } catch (JSONException e){
            System.out.println("Something is wrong");
            replayButtonFinal.setVisibility(View.GONE);
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
            String periodStateText = "";
            if(response.getJSONObject("liveData").getJSONObject("linescore").has("currentPeriodTimeRemaining")){
                periodStateText = response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodTimeRemaining");
            }

            JSONObject gameData = response.getJSONObject("gameData");
            JSONObject teams = gameData.getJSONObject("teams");
            setLogo(teams.getJSONObject("away").getString("name"), awayBig, R.id.away_banner_logo);
            setLogo(teams.getJSONObject("home").getString("name"), homeBig, R.id.home_banner_logo);

            triCodeAway = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("away").getString("triCode");
            triCodeHome = response.getJSONObject("gameData").getJSONObject("teams").getJSONObject("home").getString("triCode");

            String currentPeriodOrdinal = response.getJSONObject("liveData").getJSONObject("linescore").getString("currentPeriodOrdinal");
            timeLeft.setText(currentPeriodOrdinal);
            timeLeft.setLayoutParams(params);
            periodState.setText(periodStateText);
            periodState.setTextSize(pixelWidth >= 1080 ? 14 : 11);

            int awayGoals = response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                    .getJSONObject("away").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt("goals");

            int homeGoals = response.getJSONObject("liveData").getJSONObject("boxscore").getJSONObject("teams")
                    .getJSONObject("home").getJSONObject("teamStats").getJSONObject("teamSkaterStats").getInt("goals");

            if(currentPeriodOrdinal.equals("SO") && periodStateText.equals("Final")){
                boolean awayTeamWins = shootoutWinnerIsAwayTeam(
                        response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(4).getInt("endIndex"),
                        response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("playsByPeriod").getJSONObject(4).getInt("startIndex"),
                        response.getJSONObject("liveData").getJSONObject("plays").getJSONArray("allPlays"));
                if(awayTeamWins){
                    awayGoals++;
                }else{
                    homeGoals++;
                }
            }
            awayScore.setText(Integer.toString(awayGoals));
            awayScore.setTextSize(pixelWidth >= 1080 ? 18 : 16);
            awayScore.setLayoutParams(params);
            awayScore.setGravity(Gravity.CENTER);
            awayScore.setTypeface(null, Typeface.BOLD);
            homeScore.setText(Integer.toString(homeGoals));
            homeScore.setTextSize(pixelWidth >= 1080 ? 18 : 16);
            homeScore.setLayoutParams(params);
            homeScore.setGravity(Gravity.CENTER);
            homeScore.setTypeface(null, Typeface.BOLD);
            if(!periodStateText.equals("Final")){
                hideHighlightsLayout();
            }else{
                goToGameContent();
            }
        } catch (JSONException e){
            System.out.println("makeBanner JSON exception BoxScore.java");
            hideHighlightsLayout();
        }
    }

    private boolean shootoutWinnerIsAwayTeam(int endOfPeriod, int startOfPeriod, JSONArray allPlays)
    {
        try{
            int awaySOGoal = 0;
            int homeSOGoal = 0;
            for(int k = startOfPeriod; k <= endOfPeriod; k++){
                JSONObject playObject = allPlays.getJSONObject(k);
                if(playObject.has("players") && isShootoutGoalPlay(playObject)){
                    if(playObject.getJSONObject("team").getString("triCode").equals(triCodeAway)){
                        awaySOGoal++;
                    }else{
                        homeSOGoal++;
                    }
                }
            }
            return awaySOGoal > homeSOGoal;
        }catch(JSONException e){
            System.out.println("shootoutWinnerIsAwayTeam threw an exception");
        }

        return false;
    }

    private void hideHighlightsLayout(){
        LinearLayout recapLayout = findViewById(R.id.recap_layout);
        LinearLayout highlightLayout = findViewById(R.id.hightlight_layout);
        recapLayout.setVisibility(View.GONE);
        highlightLayout.setVisibility(View.GONE);
    }

    private void goToGameContent(){

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, gameContentUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                assignGameHighlightLinks(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("addGameHighlightLinks BoxScore.java exception thrown");
                error.printStackTrace();
                hideHighlightsLayout();
            }
        });


        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest);
    }

    String extendedHighlightsUrl = "";
    String recapUrl = "";
    private void assignGameHighlightLinks(JSONObject gameContentResponse){
        TextView recapTextView = findViewById(R.id.Recap);
        TextView highlightTextView = findViewById(R.id.ExtendedHighlights);

        try{
            JSONArray epg = gameContentResponse.getJSONObject("media").getJSONArray("epg");
            JSONObject extendedHighlights = getExtendedHighlights(epg);
            JSONObject recap = getRecap(epg);
            extendedHighlightsUrl = extendedHighlights.getJSONArray("items").getJSONObject(0).getJSONArray("playbacks").getJSONObject(3).getString("url");
            recapUrl = recap.getJSONArray("items").getJSONObject(0).getJSONArray("playbacks").getJSONObject(3).getString("url");

            recapTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchHighlight(true);
                }
            });

            highlightTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchHighlight(false);
                }
            });

        }catch(JSONException e){
            System.out.println("assignGameHighlightLinks exception BoxScore.java");
            hideHighlightsLayout();
        }
    }

    private JSONObject getExtendedHighlights(JSONArray epg){
        try{
            for(int i = 0; i < epg.length(); i++){
                if(epg.getJSONObject(i).getString("title").equals("Extended Highlights")){
                    return epg.getJSONObject(i);
                }
            }
            hideHighlightsLayout();
        }catch(JSONException e){
            System.out.println("getExtendedHighlights exception BoxScore.java");
            hideHighlightsLayout();
        }

        return null;
    }

    private JSONObject getRecap(JSONArray epg){
        try{
            for(int i = 0; i < epg.length(); i++){
                if(epg.getJSONObject(i).getString("title").equals("Recap")){
                    return epg.getJSONObject(i);
                }
            }
            hideHighlightsLayout();
        }catch(JSONException e){
            System.out.println("getRecap exception BoxScore.java");
            hideHighlightsLayout();
        }

        return null;
    }

    private void launchHighlight(boolean isRecap){

        Intent intent = new Intent(getApplicationContext(), replayPlayer.class);
        Bundle extras = new Bundle();
        extras.putString("REPLAY_URL", isRecap? recapUrl : extendedHighlightsUrl);
        intent.putExtras(extras);
        startActivity(intent);

    }

    private void makeMiddleLayer(JSONObject response, Boolean scoring){
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        LinearLayout head = findViewById(scoring ? R.id.Header_box_score : R.id.Header_shot_total);
        LinearLayout away = findViewById(scoring ? R.id.away_box_score : R.id.away_shot_total);
        LinearLayout home = findViewById(scoring ? R.id.home_box_score : R.id.home_shot_total);
        head.removeViews(0, head.getChildCount());
        away.removeViews(0, away.getChildCount());
        home.removeViews(0, home.getChildCount());
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
            setLogo(teams.getJSONObject("away").getString("name"), awaySmall, 0);
            setLogo(teams.getJSONObject("home").getString("name"), homeSmall, 0);
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

    HashMap<Integer, String> idToTeamName = new HashMap<Integer, String>();
    private void setLogo(String team, ImageView teamLogo, int logoId){
        i++;
        int id = logoId == 0 ? 5 * i + 7 : logoId;
        String teamLogoFileName = team.toLowerCase();
        teamLogoFileName = teamLogoFileName.replace(" ", "_");
        teamLogoFileName = teamLogoFileName.replace("é", "e");
        teamLogoFileName = teamLogoFileName.replace(".", "");
        teamLogoFileName = teamLogoFileName.replace("(", "");
        teamLogoFileName = teamLogoFileName.replace(")", "");
        int drawableID = getResources().getIdentifier(teamLogoFileName, "drawable", getPackageName());
        teamLogo.setImageResource(drawableID == 0 ? getResources().getIdentifier("nhl", "drawable", getPackageName()) : drawableID);
        teamLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        teamLogo.setId(logoId);
        idToTeamName.put(logoId, teamLogoFileName);
        teamLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TeamLinkMapHelper.launchTeamSite(idToTeamName.get(view.getId()), view, view.getId());
            }
        });
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

    private String typeOfGoal(String description, boolean isSO){
        if(isSO){
            if(description.indexOf('-') > -1){
                return description.substring(description.indexOf('-')+1);
            }
            description = description.substring(description.indexOf(' ')+1);
            description = description.substring(description.indexOf(' '));
            return description;
        }
        description = afterGoalScorer(description);
        return description.substring(0, description.indexOf(','));
    }

    private String afterAssistOne(String description, boolean isSO) {
        description = removeBracket(description);
        if(description.length() > 0 && !isSO) {
            description = description.substring(description.indexOf(':'));
            int startIndex = description.indexOf('(') - 1;
            int endIndex = description.indexOf(',') + 2;
            String assist = "";
            try {
                assist = endIndex == 1 ? description.substring(startIndex) : description.substring(startIndex, endIndex);
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

    private void setTitleParams(TextView headerTitle, int width, boolean bold){
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ActionBar.LayoutParams.WRAP_CONTENT);
        headerTitle.setGravity(Gravity.CENTER);
        headerTitle.setLayoutParams(params);
        if(bold){
            headerTitle.setTypeface(null, Typeface.BOLD);
        }
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

    private void launchPlayerPage(int textId){
        Intent intent = new Intent(getApplicationContext(), Player.class);
        Bundle extras = new Bundle();
        extras.putString("PLAYER_NAME", idToPlayerName.get(textId));
        extras.putString("PLAYER_URL", idToPlayerURL.get(textId));
        intent.putExtras(extras);
        startActivity(intent);
    }
}
