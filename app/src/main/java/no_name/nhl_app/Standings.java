package no_name.nhl_app;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Standings extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AdapterView.OnItemSelectedListener {

    int pixelWidth = 0;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_standings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pixelWidth = size.x;

        //getStandingsData(new Intent());
        Bundle extras = getIntent().getExtras();

        Spinner seasonChanger = findViewById(R.id.season_dropdown_list);
        setSpinner(seasonChanger);
        seasonChanger.setSelected(false);
        seasonChanger.setSelection(extras == null ? 0 : extras.getInt("PREV_SELECTION"),false);
        seasonChanger.setOnItemSelectedListener(this);

        String SeasonToShow = extras != null ? extras.getString("SeasonToShow") : setCurrentYear();

        String SeasonToQueryAPI = SeasonToShow.substring(0,4) + SeasonToShow.substring(7);

        String presentStandingsUrl = "https://statsapi.web.nhl.com/api/v1/standings?season=" + SeasonToQueryAPI;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, presentStandingsUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                makeStandingsByDivision(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Something is wrong");
                error.printStackTrace();
            }
        });


        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {

        String season = parent.getItemAtPosition(pos).toString();
        Intent intent = new Intent(getApplicationContext(), Standings.class);
        intent.putExtra("SeasonToShow", season);
        intent.putExtra("PREV_SELECTION", pos);
        startActivity(intent);
        //getStandingsData(intent);
    }

    private void getStandingsData(Intent intent){
        Bundle extras = intent.getExtras();

        Spinner seasonChanger = findViewById(R.id.season_dropdown_list);
        setSpinner(seasonChanger);
        seasonChanger.setSelected(false);
        seasonChanger.setSelection(extras == null ? 0 : extras.getInt("PREV_SELECTION"),false);
        seasonChanger.setOnItemSelectedListener(this);

        String SeasonToShow = extras != null ? extras.getString("SeasonToShow") : setCurrentYear();

        String SeasonToQueryAPI = SeasonToShow.substring(0,4) + SeasonToShow.substring(7);

        String presentStandingsUrl = "https://statsapi.web.nhl.com/api/v1/standings?season=" + SeasonToQueryAPI;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, presentStandingsUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                makeStandingsByDivision(response);

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

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
        return;
    }

    private String setCurrentYear(){
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        thisYear = thisMonth < 9 ? thisYear - 1 : thisYear;

        return Integer.toString(thisYear) + " - " + Integer.toString(thisYear + 1);
    }

    private void setSpinner(Spinner seasonChanger){
        ArrayList<String> years = new ArrayList<String>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        int thisMonth = Calendar.getInstance().get(Calendar.MONTH);
        thisYear = thisMonth < 9 ? thisYear - 1 : thisYear;
        int nextYear;
        for (int i = thisYear; i >= 1917; i--) {
            nextYear = i + 1;
            years.add(Integer.toString(i) + " - " + Integer.toString(nextYear));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, years);

        seasonChanger.setAdapter(adapter);
    }

    private void makeStandingsByDivision(JSONObject response){
        LinearLayout standingsContent = findViewById(R.id.standings_content);
        standingsContent.removeAllViews();
        LinearLayout easternConference = new LinearLayout(this);
        LinearLayout westernConference = new LinearLayout(this);
        easternConference.setOrientation(LinearLayout.VERTICAL);
        westernConference.setOrientation(LinearLayout.VERTICAL);
        TextView Season = new TextView(this);
        try{
            JSONArray divisions = response.getJSONArray("records");
            //addSeasonBanner(divisions, Season, standingsContent);
            JSONObject division;

            division = divisions.getJSONObject(0);
            if(division.getJSONObject("conference") != null){
                addConferenceBanner("Eastern Conference", easternConference);
                addConferenceBanner("Western Conference", westernConference);

                for(int i = 0; i < divisions.length(); i++){
                    division = divisions.getJSONObject(i);
                    if(division.getJSONObject("conference").getString("name").equals("Eastern")){
                        makeDivisionLayout(division, easternConference);
                    }else{
                        makeDivisionLayout(division, westernConference);
                    }
                }

                standingsContent.addView(easternConference);
                standingsContent.addView(westernConference);
            }else{
                makeDivisionLayout(division, standingsContent);
            }

        } catch (JSONException e) {
            System.out.println("makeStandingsByDivision JSON Exception");
            addConferenceBanner("NHL", easternConference);
            try{
                JSONArray divisions = response.getJSONArray("records");
                for(int i = 0; i < divisions.length(); i++){
                    JSONObject division = divisions.getJSONObject(i);
                    makeDivisionLayout(division, easternConference);
                }
                standingsContent.addView(easternConference);
            }catch(JSONException exception){
                System.out.println("Second makeStandingsByDivision JSon exception lollll");
            }
        }

    }

    private void addDivisionTitle(JSONObject division, LinearLayout divisionContent){
        TableRow DivisionTitleRow  = new TableRow(this);
        TextView DivisionTitle = new TextView(this);
        try{
            DivisionTitle.setText(division.getJSONObject("division").getString("name"));
            DivisionTitleRow.addView(DivisionTitle);
            divisionContent.addView(DivisionTitleRow);
        } catch (JSONException e) {
            System.out.println("addDivisionTitle JSON Exception");
        }
    }

    private void addConferenceBanner(String confTitle, LinearLayout conference){
        TableRow conferenceRow = new TableRow(this);
        TextView title = new TextView(this);
        title.setText(confTitle);
        title.setTextColor(Color.WHITE);
        if(confTitle.equals("Eastern Conference")){     
            title.setBackgroundColor(Color.RED);
        }else if (confTitle.equals("Western Conference")){
            title.setBackgroundColor(Color.BLUE);
        }else{
            title.setBackgroundColor(Color.GRAY);
        }
        title.setTextSize(14);
        title.setTypeface(null, Typeface.BOLD);
        conferenceRow.addView(title);
        conference.addView(conferenceRow);
    }

    private void addSeasonBanner(JSONArray divisions, TextView Season, LinearLayout standingsContent){
        LinearLayout titleLayout = findViewById(R.id.season);
        TableRow SeasonRow = new TableRow(this);
        try{
            String presentSeason = divisions.length() == 0 ? "20042005" : divisions.getJSONObject(0).getString("season");
            Season.setText(presentSeason.substring(0,4) + " - " + presentSeason.substring(4) + " Season");
            Season.setTypeface(null, Typeface.BOLD);
            SeasonRow.addView(Season);
            titleLayout.addView(SeasonRow);
            //standingsContent.addView(titleLayout);
        } catch (JSONException e) {
            System.out.println("addSeasonBanner JSON Exception");
        }
    }

    private void makeDivisionLayout(JSONObject division, LinearLayout asd){
        TableRow bigDivisionRow = new TableRow(this);
        LinearLayout divisionContent = new LinearLayout(this);
        divisionContent.setOrientation(LinearLayout.VERTICAL);
        addDivisionTitle(division, divisionContent);
        boolean hasTies = true;
        try{

            if(Integer.parseInt(division.getString("season").substring(0,4)) < 2005) {
                hasTies = true;
            } else {
                hasTies = false;
            }
            addTeamStatsHeader(divisionContent, hasTies);
            JSONArray teams = division.getJSONArray("teamRecords");
            JSONObject team;
            for(int i = 0; i < teams.length(); i++){
                team = teams.getJSONObject(i);
                testIfClinched(team, divisionContent, hasTies);
            }

            bigDivisionRow.addView(divisionContent);
            asd.addView(bigDivisionRow);
        } catch (Exception e) {
            System.out.println("makeDivisionLayout JSON Exception");
        }
    }

    private void testIfClinched(JSONObject team, LinearLayout divisionContent, boolean hasTies){
        LinearLayout tableHead = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView rank = new TextView(this);
        ImageView teamLogo = new ImageView(this);
        TextView clinched = new TextView(this);
        try{
            rank.setText(team.getString("divisionRank"));
            setTitleParams(rank, mainColumnSpacing(), false);

            setLogo(team.getJSONObject("team").getString("name"), teamLogo);

            tableHead.addView(rank);
            tableHead.addView(teamLogo);

            clinched.setText(team.getString("clinchIndicator"));
            setTitleParams(clinched, mainColumnSpacing(), false);

            tableHead.addView(clinched);

            makeTeamRow(team, tableHead, true, hasTies);
        } catch (JSONException e) {
            makeTeamRow(team, tableHead, false, hasTies);
        }
        row.addView(tableHead);
        divisionContent.addView(row);
    }

    private void makeTeamRow(JSONObject team, LinearLayout tableHead, boolean madePlayOffs, boolean hasTies){
        TextView clinched = new TextView(this);
        TextView gamesPlayed = new TextView(this);
        TextView wins = new TextView(this);
        TextView losses = new TextView(this);
        TextView ot = new TextView(this);
        TextView tie = new TextView(this);
        TextView points = new TextView(this);
        TextView ROW = new TextView(this);
        TextView  GA = new TextView(this);
        TextView GF = new TextView(this);
        TextView diff = new TextView(this);
        TextView streak = new TextView(this);

        try{
            if(!madePlayOffs){
                clinched.setText("   ");
                setTitleParams(clinched, mainColumnSpacing(), false);
            }

            gamesPlayed.setText(team.getString("gamesPlayed"));
            setTitleParams(gamesPlayed, mainColumnSpacing(), false);

            int gp = Integer.parseInt(team.getString("gamesPlayed"));
            int w = Integer.parseInt(team.getJSONObject("leagueRecord").getString("wins"));
            int l = Integer.parseInt(team.getJSONObject("leagueRecord").getString("losses"));
            int loserPoints = gp - w - l;
            int p = w * 2 + loserPoints;;

            wins.setText(team.getJSONObject("leagueRecord").getString("wins"));
            setTitleParams(wins, mainColumnSpacing(), false);

            losses.setText(team.getJSONObject("leagueRecord").getString("losses"));
            setTitleParams(losses, mainColumnSpacing(), false);

            try{
                ot.setText(team.getJSONObject("leagueRecord").getString("ot"));
                setTitleParams(ot, mainColumnSpacing(), false);
            }
            catch(JSONException e){
                ot.setText("");
                setTitleParams(ot, mainColumnSpacing(), false);
            }

            if(hasTies){
                tie.setText(team.getJSONObject("leagueRecord").getString("ties"));
                setTitleParams(tie, mainColumnSpacing(), false);
            }

            String totalPoints = team.getString("points").length() == 0 ? Integer.toString(p) : team.getString("points");
            points.setText(team.getString("points"));
            setTitleParams(points, mainColumnSpacing(), false);

            ROW.setText(team.getString("row"));
            setTitleParams(ROW, mainColumnSpacing(), false);

            int goalsAgainst = Integer.parseInt(team.getString("goalsAgainst"));
            int goalsFor = Integer.parseInt(team.getString("goalsScored"));
            int difference = goalsFor - goalsAgainst;
            GA.setText(team.getString("goalsAgainst"));
            setTitleParams(GA, mainColumnSpacing(), false);

            GF.setText(team.getString("goalsScored"));
            setTitleParams(GF, mainColumnSpacing(), false);

            diff.setText(Integer.toString(difference));
            setTitleParams(diff,  mainColumnSpacing(), false);

            streak.setText(team.getJSONObject("streak").getString("streakCode"));
            setTitleParams(streak, mainColumnSpacing(), false);
        } catch (JSONException e) {
            System.out.println("makeTeamRow JSON Exception");
        }

        if(!madePlayOffs){
            tableHead.addView(clinched);
        }
        tableHead.addView(gamesPlayed);
        tableHead.addView(wins);
        tableHead.addView(losses);
        tableHead.addView(ot);
        if(hasTies){
            tableHead.addView(tie);
        }
        tableHead.addView(points);
        tableHead.addView(ROW);
        tableHead.addView(GA);
        tableHead.addView(GF);
        tableHead.addView(diff);
        tableHead.addView(streak);
    }

    private void addTeamStatsHeader(LinearLayout divisionContent, boolean hasTies){
        LinearLayout tableHead = new LinearLayout(this);
        TableRow row = new TableRow(this);
        TextView rank = new TextView(this);
        TextView team = new TextView(this);
        TextView clinched = new TextView(this);
        TextView gamesPlayed = new TextView(this);
        TextView wins = new TextView(this);
        TextView losses = new TextView(this);
        TextView ot = new TextView(this);
        TextView tie = new TextView(this);
        TextView points = new TextView(this);
        TextView ROW = new TextView(this);
        TextView  GA = new TextView(this);
        TextView GF = new TextView(this);
        TextView diff = new TextView(this);
        TextView streak = new TextView(this);

        rank.setText("Rank");
        setTitleParams(rank, mainColumnSpacing(), true);

        team.setText("Team");
        setTitleParams(team, mainColumnSpacing(), true);

        clinched.setText("   ");
        setTitleParams(clinched, mainColumnSpacing(), true);

        gamesPlayed.setText("GP");
        setTitleParams(gamesPlayed, mainColumnSpacing(), true);

        wins.setText("W");
        setTitleParams(wins, mainColumnSpacing(), true);

        losses.setText("L");
        setTitleParams(losses, mainColumnSpacing(), true);

        ot.setText("OTL");
        setTitleParams(ot, mainColumnSpacing(), true);

        if(hasTies){
            tie.setText("T");
            setTitleParams(tie, mainColumnSpacing(), true);
        }

        points.setText("PTS");
        setTitleParams(points, mainColumnSpacing(), true);

        ROW.setText("ROW");
        setTitleParams(ROW, mainColumnSpacing(), true);

        GA.setText("GA");
        setTitleParams(GA, mainColumnSpacing(), true);

        GF.setText("GF");
        setTitleParams(GF, mainColumnSpacing(), true);

        diff.setText("DIFF");
        setTitleParams(diff,  mainColumnSpacing(), true);

        streak.setText("STRK");
        setTitleParams(streak, mainColumnSpacing(), true);

        tableHead.addView(rank);
        tableHead.addView(team);
        tableHead.addView(clinched);
        tableHead.addView(gamesPlayed);
        tableHead.addView(wins);
        tableHead.addView(losses);
        tableHead.addView(ot);
        if(hasTies){
            tableHead.addView(tie);
        }
        tableHead.addView(points);
        tableHead.addView(ROW);
        tableHead.addView(GA);
        tableHead.addView(GF);
        tableHead.addView(diff);
        tableHead.addView(streak);

        row.addView(tableHead);
        divisionContent.addView(row);
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

    HashMap<Integer, String> idToTeamName = new HashMap<Integer, String>();

    private void setLogo(String team, ImageView teamLogo){
        i++;
        String teamLogoFileName = team.toLowerCase();
        teamLogoFileName = teamLogoFileName.replace(" ", "_");
        teamLogoFileName = teamLogoFileName.replace("é", "e");
        teamLogoFileName = teamLogoFileName.replace(".", "");
        teamLogoFileName = teamLogoFileName.replace("(", "");
        teamLogoFileName = teamLogoFileName.replace(")", "");
        int drawableID = getResources().getIdentifier(teamLogoFileName, "drawable", getPackageName());
        teamLogo.setLayoutParams(new LinearLayout.LayoutParams(logoWidthSpacing(),LinearLayout.LayoutParams.MATCH_PARENT));
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

    private int logoWidthSpacing(){
        if(pixelWidth >= 1440){
            return 130;
        }else if (pixelWidth >= 1080){
            return 100;
        }else if (pixelWidth >= 720) {
            return 65;
        }

        return 63;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.standings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_scores) {
            Intent intent = new Intent(getApplicationContext(), ScoreBoard.class);
            startActivity(intent);
        } else if (id == R.id.nav_standings) {
            Intent intent = new Intent(getApplicationContext(), Standings.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
