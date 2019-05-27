package no_name.nhl_app;

import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
                setProfileActionShot(response);
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
        String careerStatsUrl = "https://statsapi.web.nhl.com/api/v1/people/"+playerId+"?expand=person.stats&stats=yearByYear,careerRegularSeason,yearByYearPlayoffs,careerPlayoffs";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, careerStatsUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                setPlayerCareerStatsTotal(response, 1, R.id.last_row);
                setPlayerCareerStats(response, 0, R.id.player_career_stats_table);
                setPlayerCareerStatsTotal(response, 3, R.id.last_row_playoffs);                  //for playoffs
                setPlayerCareerStats(response, 2, R.id.player_career_stats_table_playoffs);      //for playoffs

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

    private void setPlayerCareerStatsTotal(JSONObject response, int statIndex, int layoutId){

        LinearLayout statsTable = (LinearLayout) findViewById(layoutId);
        try{
            JSONArray statsYearByYear = response.getJSONArray("people").getJSONObject(0)
                    .getJSONArray("stats").getJSONObject(statIndex).getJSONArray("splits");
            String position = response.getJSONArray("people").getJSONObject(0).getJSONObject("primaryPosition").getString("abbreviation");
            if(position.equals("G")){
                makeGoalieCareerStatRow(statsYearByYear.getJSONObject(0), statsTable, true, 1);
            }else{
                makeCareerStatRow(statsYearByYear.getJSONObject(0), statsTable, true, 1);
            }
        }catch (JSONException e){
            System.out.println("setPlayerCareerStatsTotal method in Player.java error");
        }
    }

    private void setPlayerCareerStats(JSONObject response, int statIndex, int layoutId){
        try{
            JSONArray statsYearByYear = response.getJSONArray("people").getJSONObject(0)
                    .getJSONArray("stats").getJSONObject(statIndex).getJSONArray("splits");
            LinearLayout statsTable = (LinearLayout) findViewById(layoutId);

            if(statsYearByYear.length() == 0){
                TextView playoffStatsText = (TextView) findViewById(R.id.Playoffs);
                playoffStatsText.setVisibility(View.GONE);
            }

            JSONObject player = response.getJSONArray("people").getJSONObject(0);
            String position = player.getJSONObject("primaryPosition").getString("abbreviation");
            if(position.equals("G")){
                makeGoalieStatRowTitle(statsTable);
            }else{
                makeCareerStatRowTitle(statsTable);
            }
            for(int i = 0; i < statsYearByYear.length(); i++){

                if(position.equals("G")){
                    makeGoalieCareerStatRow(statsYearByYear.getJSONObject(i), statsTable, false, i);
                }else{
                    makeCareerStatRow(statsYearByYear.getJSONObject(i), statsTable, false, i);
                }

            }
        }catch (JSONException e){
            System.out.println("setPlayerCareerStats method in Player.java error");
        }
    }

    private void makeGoalieStatRowTitle(LinearLayout statsTable){
        LinearLayout tableHead = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView year = new TextView(this);
        TextView league = new TextView(this);
        TextView team = new TextView(this);
        TextView games = new TextView(this);
        TextView gamesStarted = new TextView(this);
        TextView wins = new TextView(this);
        TextView losses = new TextView(this);
        TextView ot = new TextView(this);
        TextView shutouts = new TextView(this);
        TextView goalsAgainstAverage = new TextView(this);
        TextView savePercentage = new TextView(this);
        TextView goalsAgainst  = new TextView(this);
        TextView shotsAgainst = new TextView(this);
        TextView toi = new TextView(this);
        TextView saves = new TextView(this);
        TextView ppSaves = new TextView(this);
        TextView evenSaves = new TextView(this);
        TextView shortHandedSaves = new TextView(this);
        TextView ppSA= new TextView(this);
        TextView shSA = new TextView(this);
        TextView eSA = new TextView(this);
        TextView ppSP = new TextView(this);
        TextView shSP = new TextView(this);
        TextView eSP = new TextView(this);

        year.setText("Year");
        setTitleParams(year, yearSpacing(), true);

        league.setText("Lge");
        setTitleParams(league, leagueSpacing(), true);

        team.setText("Team");
        setTitleParams(team, teamNameSpacing(), true);
        team.setGravity(Gravity.LEFT);

        games.setText("GP");
        setTitleParams(games, mainColumnSpacing(), true);

        gamesStarted.setText("GS");
        setTitleParams(gamesStarted, mainColumnSpacing(), true);

        wins.setText("W");
        setTitleParams(wins, mainColumnSpacing(), true);

        losses.setText("L");
        setTitleParams(losses, mainColumnSpacing(), true);

        ot.setText("OT");
        setTitleParams(ot, mainColumnSpacing(), true);

        shutouts.setText("S/O");
        setTitleParams(shutouts, mainColumnSpacing(), true);

        goalsAgainstAverage.setText("GAA");
        setTitleParams(goalsAgainstAverage, mainColumnSpacing(), true);

        savePercentage.setText("S%");
        setTitleParams(savePercentage, mainColumnSpacing(), true);

        goalsAgainst.setText("GA");
        setTitleParams(goalsAgainst, mainColumnSpacing(), true);

        shotsAgainst.setText("SA");
        setTitleParams(shotsAgainst, mainColumnSpacing(), true);

        toi.setText("TOI");
        setTitleParams(toi, yearSpacing(), true);

        saves.setText("Saves");
        setTitleParams(saves, mainColumnSpacing(), true);

        ppSaves.setText("ppS");
        setTitleParams(ppSaves, mainColumnSpacing(), true);

        evenSaves.setText("eS");
        setTitleParams(evenSaves, mainColumnSpacing(), true);

        shortHandedSaves.setText("shS");
        setTitleParams(shortHandedSaves, mainColumnSpacing(), true);

        ppSA.setText("ppSA");
        setTitleParams(ppSA, mainColumnSpacing(), true);

        shSA.setText("shSA");
        setTitleParams(shSA, mainColumnSpacing(), true);

        eSA.setText("eSA");
        setTitleParams(eSA, mainColumnSpacing(), true);

        ppSP.setText("ppS%");
        setTitleParams(ppSP, mainColumnSpacing(), true);

        shSP.setText("shS%");
        setTitleParams(shSP, mainColumnSpacing(), true);

        eSP.setText("eS%");
        setTitleParams(eSP, mainColumnSpacing(), true);

        tableHead.addView(year);
        tableHead.addView(league);
        tableHead.addView(team);
        tableHead.addView(games);
        tableHead.addView(gamesStarted);
        tableHead.addView(wins);
        tableHead.addView(losses);
        tableHead.addView(ot);
        tableHead.addView(shutouts);
        tableHead.addView(goalsAgainstAverage);
        tableHead.addView(savePercentage);
        tableHead.addView(goalsAgainst);
        tableHead.addView(shotsAgainst);
        tableHead.addView(toi);
        tableHead.addView(saves);
        tableHead.addView(ppSaves);
        tableHead.addView(evenSaves);
        tableHead.addView(shortHandedSaves);
        tableHead.addView(ppSA);
        tableHead.addView(shSA);
        tableHead.addView(eSA);
        tableHead.addView(ppSP);
        tableHead.addView(shSP);
        tableHead.addView(eSP);

        row.addView(tableHead);
        statsTable.addView(row);
    }

    private void makeGoalieCareerStatRow(JSONObject statsForYear, LinearLayout statsTable, boolean total, int rowNum){
        LinearLayout horizontalRow = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView year = new TextView(this);
        TextView league = new TextView(this);
        TextView team = new TextView(this);
        TextView games = new TextView(this);
        TextView gamesStarted = new TextView(this);
        TextView wins = new TextView(this);
        TextView losses = new TextView(this);
        TextView ot = new TextView(this);
        TextView shutouts = new TextView(this);
        TextView goalsAgainstAverage = new TextView(this);
        TextView savePercentage = new TextView(this);
        TextView goalsAgainst  = new TextView(this);
        TextView shotsAgainst = new TextView(this);
        TextView toi = new TextView(this);
        TextView saves = new TextView(this);
        TextView ppSaves = new TextView(this);
        TextView evenSaves = new TextView(this);
        TextView shortHandedSaves = new TextView(this);
        TextView ppSA= new TextView(this);
        TextView shSA = new TextView(this);
        TextView eSA = new TextView(this);
        TextView ppSP = new TextView(this);
        TextView shSP = new TextView(this);
        TextView eSP = new TextView(this);

        try{
            String leagueName = "";
            if(statsForYear.has("league")){
                 leagueName = statsForYear.getJSONObject("league").getString("name");
            }
            league.setText(leagueName.equals("National Hockey League") ? "NHL" : leagueName);
            setTitleParams(league, leagueSpacing(), false);

            String season = total ? "" : statsForYear.getString("season").substring(0,4) + "-" + statsForYear.getString("season").substring(4);
            year.setText(season);
            setTitleParams(year, yearSpacing(), false);

            team.setText(total ? "NHL Totals" : statsForYear.getJSONObject("team").getString("name"));
            setTitleParams(team, teamNameSpacing(), false);
            team.setGravity(Gravity.LEFT);
            i++;
            team.setId(5*i+7);
            if(statsForYear.has("team")){
                idToTeamName.put(5*i+7, formatTeamName(statsForYear.getJSONObject("team").getString("name")));
                team.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        TeamLinkMapHelper.launchTeamSite(idToTeamName.get(view.getId()), view, view.getId());
                    }
                });
            }


            games.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("games")));
            setTitleParams(games, mainColumnSpacing(), false);

            if(statsForYear.getJSONObject("stat").has("gamesStarted")){
                gamesStarted.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("gamesStarted")));
                setTitleParams(gamesStarted, mainColumnSpacing(), false);
            }else{
                gamesStarted.setText("");
                setTitleParams(gamesStarted, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("wins")){
                wins.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("wins")));
                setTitleParams(wins, mainColumnSpacing(), false);
            }else{
                wins.setText("");
                setTitleParams(wins, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("losses")){
                losses.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("losses")));
                setTitleParams(losses, mainColumnSpacing(), false);
            }else{
                losses.setText("");
                setTitleParams(losses, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("ot")){
                ot.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("ot")));
                setTitleParams(ot, mainColumnSpacing(), false);
            }else{
                ot.setText("");
                setTitleParams(ot, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("shutouts")){
                shutouts.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("shutouts")));
                setTitleParams(shutouts, mainColumnSpacing(), false);
            }else{
                shutouts.setText("");
                setTitleParams(shutouts, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("goalAgainstAverage")){
                goalsAgainstAverage.setText(new DecimalFormat("#.##").format(statsForYear.getJSONObject("stat").getDouble("goalAgainstAverage")));
                setTitleParams(goalsAgainstAverage, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("savePercentage")){
                savePercentage.setText(new DecimalFormat("#.###").format(statsForYear.getJSONObject("stat").getDouble("savePercentage")));
                setTitleParams(savePercentage, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("goalsAgainst")){
                goalsAgainst.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("goalsAgainst")));
                setTitleParams(goalsAgainst, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("shotsAgainst")){
                shotsAgainst.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("shotsAgainst")));
                setTitleParams(shotsAgainst, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("timeOnIce")){
                toi.setText(statsForYear.getJSONObject("stat").getString("timeOnIce"));
                setTitleParams(toi, yearSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("saves")){
                saves.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("saves")));
                setTitleParams(saves, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("powerPlaySaves")){
                ppSaves.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("powerPlaySaves")));
                setTitleParams(ppSaves, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("evenSaves")){
                evenSaves.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("evenSaves")));
                setTitleParams(evenSaves, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("shortHandedSaves")){
                shortHandedSaves.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("shortHandedSaves")));
                setTitleParams(shortHandedSaves, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("powerPlayShots")){
                ppSA.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("powerPlayShots")));
                setTitleParams(ppSA, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("shortHandedShots")){
                shSA.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("shortHandedShots")));
                setTitleParams(shSA, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("evenShots")){
                eSA.setText(Integer.toString(statsForYear.getJSONObject("stat").getInt("evenShots")));
                setTitleParams(eSA, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("powerPlaySavePercentage")){
                ppSP.setText(new DecimalFormat("#.##").format(statsForYear.getJSONObject("stat").getDouble("powerPlaySavePercentage")));
                setTitleParams(ppSP, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("shortHandedSavePercentage")){
                shSP.setText(new DecimalFormat("#.##").format(statsForYear.getJSONObject("stat").getDouble("shortHandedSavePercentage")));
                setTitleParams(shSP, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("evenStrengthSavePercentage")){
                eSP.setText(new DecimalFormat("#.##").format(statsForYear.getJSONObject("stat").getDouble("evenStrengthSavePercentage")));
                setTitleParams(eSP, mainColumnSpacing(), false);
            }

        } catch (JSONException e){
            System.out.println("addGoalieStatToTeamSummary method Boxscore.java exception");
        }

        if(rowNum % 2 == 0){
            year.setTextColor(Color.WHITE);
            league.setTextColor(Color.WHITE);
            team.setTextColor(Color.WHITE);
            games.setTextColor(Color.WHITE);
            gamesStarted.setTextColor(Color.WHITE);
            wins.setTextColor(Color.WHITE);
            losses.setTextColor(Color.WHITE);
            ot.setTextColor(Color.WHITE);
            shutouts.setTextColor(Color.WHITE);
            goalsAgainstAverage.setTextColor(Color.WHITE);
            savePercentage.setTextColor(Color.WHITE);
            goalsAgainst.setTextColor(Color.WHITE);
            shotsAgainst.setTextColor(Color.WHITE);
            toi.setTextColor(Color.WHITE);
            saves.setTextColor(Color.WHITE);
            ppSaves.setTextColor(Color.WHITE);
            evenSaves.setTextColor(Color.WHITE);
            shortHandedSaves.setTextColor(Color.WHITE);
            ppSA.setTextColor(Color.WHITE);
            shSA.setTextColor(Color.WHITE);
            eSA.setTextColor(Color.WHITE);
            ppSP.setTextColor(Color.WHITE);
            shSP.setTextColor(Color.WHITE);
            eSP.setTextColor(Color.WHITE);
            row.setBackgroundColor(Color.GRAY);
        }

        horizontalRow.addView(year);
        horizontalRow.addView(league);
        horizontalRow.addView(team);
        horizontalRow.addView(games);
        horizontalRow.addView(gamesStarted);
        horizontalRow.addView(wins);
        horizontalRow.addView(losses);
        horizontalRow.addView(ot);
        horizontalRow.addView(shutouts);
        horizontalRow.addView(goalsAgainstAverage);
        horizontalRow.addView(savePercentage);
        horizontalRow.addView(goalsAgainst);
        horizontalRow.addView(shotsAgainst);
        horizontalRow.addView(toi);
        horizontalRow.addView(saves);
        horizontalRow.addView(ppSaves);
        horizontalRow.addView(evenSaves);
        horizontalRow.addView(shortHandedSaves);
        horizontalRow.addView(ppSA);
        horizontalRow.addView(shSA);
        horizontalRow.addView(eSA);
        horizontalRow.addView(ppSP);
        horizontalRow.addView(shSP);
        horizontalRow.addView(eSP);

        row.addView(horizontalRow);
        statsTable.addView(row);
    }

    private void makeCareerStatRow(JSONObject statsForYear, LinearLayout statsTable, boolean total, int rowNum){
        LinearLayout horizontalRow = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView year = new TextView(this);
        TextView league = new TextView(this);
        TextView team = new TextView(this);
        TextView gamesPlayed = new TextView(this);
        TextView goals = new TextView(this);
        TextView assists = new TextView(this);
        TextView points = new TextView(this);
        TextView pim = new TextView(this);
        TextView shots = new TextView(this);
        TextView hits = new TextView(this);
        TextView plusMinus = new TextView(this);
        TextView shotPct = new TextView(this);
        TextView faceOffPct = new TextView(this);


        try{
            String leagueName = "";
            if(statsForYear.has("league")){
                leagueName = statsForYear.getJSONObject("league").getString("name");
            }
            league.setText(leagueName.equals("National Hockey League") ? "NHL" : leagueName);
            setTitleParams(league, leagueSpacing(), false);

            String season = total ? "" : statsForYear.getString("season").substring(0,4) + "-" + statsForYear.getString("season").substring(4);
            year.setText(season);
            setTitleParams(year, yearSpacing(), false);

            team.setText(total ? "NHL Totals" : statsForYear.getJSONObject("team").getString("name"));
            setTitleParams(team, teamNameSpacing(), false);
            team.setGravity(Gravity.LEFT);
            i++;
            team.setId(5*i+7);
            idToTeamName.put(5*i+7, formatTeamName(total ? "NHL Totals" : statsForYear.getJSONObject("team").getString("name")));
            team.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    TeamLinkMapHelper.launchTeamSite(idToTeamName.get(view.getId()), view, view.getId());
                }
            });

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

            if(statsForYear.getJSONObject("stat").has("shotPct")){
                shotPct.setText(Double.toString(statsForYear.getJSONObject("stat").getDouble("shotPct")));
                setTitleParams(shotPct, mainColumnSpacing(), false);
            }

            if(statsForYear.getJSONObject("stat").has("faceOffPct")){
                faceOffPct.setText(Double.toString(statsForYear.getJSONObject("stat").getDouble("faceOffPct")));
                setTitleParams(faceOffPct, mainColumnSpacing(), false);
            }
        } catch (JSONException e ){
            System.out.println("makeCareerStatRow method in player.java error");
        }

        if(rowNum % 2 == 0){
            year.setTextColor(Color.WHITE);
            league.setTextColor(Color.WHITE);
            team.setTextColor(Color.WHITE);
            gamesPlayed.setTextColor(Color.WHITE);
            goals.setTextColor(Color.WHITE);
            assists.setTextColor(Color.WHITE);
            points.setTextColor(Color.WHITE);
            pim.setTextColor(Color.WHITE);
            shots.setTextColor(Color.WHITE);
            hits.setTextColor(Color.WHITE);
            plusMinus.setTextColor(Color.WHITE);
            shotPct.setTextColor(Color.WHITE);
            faceOffPct.setTextColor(Color.WHITE);
            row.setBackgroundColor(Color.GRAY);
        }

        horizontalRow.addView(year);
        horizontalRow.addView(league);
        horizontalRow.addView(team);
        horizontalRow.addView(gamesPlayed);
        horizontalRow.addView(goals);
        horizontalRow.addView(assists);
        horizontalRow.addView(points);
        horizontalRow.addView(pim);
        horizontalRow.addView(shots);
        horizontalRow.addView(hits);
        horizontalRow.addView(plusMinus);
        horizontalRow.addView(shotPct);
        horizontalRow.addView(faceOffPct);

        row.addView(horizontalRow);
        statsTable.addView(row);
    }

    private void makeCareerStatRowTitle(LinearLayout statsTable){
        LinearLayout tableHead = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView year = new TextView(this);
        TextView league = new TextView(this);
        TextView team = new TextView(this);
        TextView gamesPlayed = new TextView(this);
        TextView goals = new TextView(this);
        TextView assists = new TextView(this);
        TextView points = new TextView(this);
        TextView pim = new TextView(this);
        TextView shots = new TextView(this);
        TextView hits = new TextView(this);
        TextView plusMinus = new TextView(this);
        TextView shotPct = new TextView(this);
        TextView faceOffPct = new TextView(this);

        year.setText("Year");
        setTitleParams(year, yearSpacing(), true);

        league.setText("Lge");
        setTitleParams(league, leagueSpacing(), true);

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

        shotPct.setText("Shot%");
        setTitleParams(shotPct, mainColumnSpacing(), true);

        faceOffPct.setText("F/O%");
        setTitleParams(faceOffPct, mainColumnSpacing(), true);

        tableHead.addView(year);
        tableHead.addView(league);
        tableHead.addView(team);
        tableHead.addView(gamesPlayed);
        tableHead.addView(goals);
        tableHead.addView(assists);
        tableHead.addView(points);
        tableHead.addView(pim);
        tableHead.addView(shots);
        tableHead.addView(hits);
        tableHead.addView(plusMinus);
        tableHead.addView(shotPct);
        tableHead.addView(faceOffPct);

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

    private int leagueSpacing(){

        if(pixelWidth >= 1440){
            return 190;
        }else if (pixelWidth >= 1080){
            return 145;
        }else if (pixelWidth >= 720) {
            return 110;
        }

        return 110;
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

    Boolean imageNotLoaded = true;
    private void setProfileActionShot(JSONObject response){
        try{
            int id = response.getJSONArray("people").getJSONObject(0).getInt("id");
            String imageLink = "https://nhl.bamcontent.com/images/actionshots/" + Integer.toString(id) + ".jpg";
            final AtomicBoolean loaded = new AtomicBoolean();
            Picasso.get().load(imageLink).into((ImageView) findViewById(R.id.profile_action_pic), new Callback.EmptyCallback() {
                @Override public void onSuccess() {
                    loaded.set(true);
                }
            });
            if (loaded.get() && !imageNotLoaded) {
                imageNotLoaded = false;
                ImageView actionShot = (ImageView) findViewById(R.id.profile_action_pic);
                actionShot.setVisibility(View.GONE);
            }
        }catch (JSONException e){
            System.out.println("setProfileActionShot in Player.java error");
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
            String teamName="";
            try{
                teamName = player.getJSONObject("currentTeam").getString("name");
                setLogo(teamName, playerTeamLogo);
            }catch(JSONException e){
                teamName = "Retired";
                setLogo("nhl", playerTeamLogo);
            }

            playerTeamText.setText(teamName);
            if(teamName.length() > 18){
                playerTeamText.setTextSize(18);
            }else {
                playerTeamText.setTextSize(20);
            }
            playerTeamText.setGravity(Gravity.CENTER);

            String currentAge = "";
            try{
                currentAge = Integer.toString(player.getInt("currentAge"));
            }catch(JSONException exception){
                currentAge = "N/A";
            }

            playerPositionText.setText(player.getJSONObject("primaryPosition").getString("abbreviation")
                                        + " | " + Integer.toString(player.getInt("weight")) + " lb"
                                        + " | " + player.getString("height")
                                        + " | Age: " + currentAge);
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
            System.out.println("setPlayerProfile in Player.java error");
        }
    }

    int i = 0;
    HashMap<Integer, String> idToTeamName = new HashMap<Integer, String>();
    private void setLogo(String team, ImageView teamLogo){
        i++;
        String teamLogoFileName = formatTeamName(team);
        int drawableID = getResources().getIdentifier(teamLogoFileName, "drawable", getPackageName());
        teamLogo.setImageResource(drawableID);
        teamLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        teamLogo.setId(5*i+7);
        idToTeamName.put(5*i+7, teamLogoFileName);
        teamLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TeamLinkMapHelper.launchTeamSite(idToTeamName.get(view.getId()), view, view.getId());
            }
        });
    }

    private String formatTeamName(String team){

        String teamLogoFileName = team.toLowerCase();
        teamLogoFileName = teamLogoFileName.replace(" ", "_");
        teamLogoFileName = teamLogoFileName.replace("é", "e");
        teamLogoFileName = teamLogoFileName.replace(".", "");
        teamLogoFileName = teamLogoFileName.replace("(", "");
        teamLogoFileName = teamLogoFileName.replace(")", "");
        return teamLogoFileName;
    }

}
