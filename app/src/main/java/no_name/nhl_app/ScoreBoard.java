package no_name.nhl_app;

import android.app.ActionBar;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.*;

import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.lang.String;

public class ScoreBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String customDate = null;
    TextView scoreDate, gameTimeText;
    Calendar mCurrentDate;
    int day, month, year;
    String currentPeriod = "";
    String periodTimeRemaining = "";
    int count = 0;
    ImageView goBackOneD, goFwdOneD;
    String boxScoreURL;
    int pixelWidth = 0;
    RelativeLayout loadingPanel = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_board);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        pixelWidth = size.x;

        loadingPanel = findViewById(R.id.loadingPanel123);
        Intent initialIntent = new Intent();
        refreshData(initialIntent);

        goBackOneD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goBackOneDay();
            }
        });

        goFwdOneD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goFwdOneDay();
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void refreshData(Intent intent){
        customDate = intent.getExtras() == null ? "?" : intent.getExtras().getString("CUSTOM_DATE");
        String url = "https://statsapi.web.nhl.com/api/v1/schedule" + customDate + (customDate == "?" ? "" : "&") + "hydrate=game(seriesSummary)";

        scoreDate = (TextView) findViewById(R.id.date_editor);
        goBackOneD = (ImageView) findViewById(R.id.go_back_date);
        goFwdOneD = (ImageView) findViewById(R.id.go_fwd_date);

        mCurrentDate = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMM");

        String dayOfWeek = intent.getExtras() == null ? simpleDateFormat.format(mCurrentDate.getTime()) : intent.getExtras().getString("DAY_OF_WEEK");
        String monthString = intent.getExtras() == null ? monthDateFormat.format(mCurrentDate.getTime()) : intent.getExtras().getString("MONTH_STRING");
        day = intent.getExtras() == null ? mCurrentDate.get(Calendar.DAY_OF_MONTH) : Integer.parseInt(customDate.substring(14));
        month = intent.getExtras() == null ? mCurrentDate.get(Calendar.MONTH) : Integer.parseInt(customDate.substring(11,13));
        year = intent.getExtras() == null ? mCurrentDate.get(Calendar.YEAR) : Integer.parseInt(customDate.substring(6,10));

        month = intent.getExtras() == null ? month + 1 : month;
        scoreDate.setText(dayOfWeek+ ", " +monthString+ " " + day+" " +year);

        scoreDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ScoreBoard.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear = monthOfYear+1;
                        setDate(year, monthOfYear, dayOfMonth);
                    }
                }, year, month-1, day);
                datePickerDialog.show();
            }
        });

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                addTextToLinearLayout(response);
                if(loadingPanel != null){
                    loadingPanel.setVisibility(View.GONE);
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

    private void setDate(int year, int monthOfYear, int dayOfMonth){
        String customMonth = monthOfYear < 10 ? "0"+Integer.toString(monthOfYear) : Integer.toString(monthOfYear);
        String customDay = dayOfMonth < 10 ? "0"+Integer.toString(dayOfMonth) : Integer.toString(dayOfMonth);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMM");
        Date date = new Date(year, dayOfMonth < 2? monthOfYear : monthOfYear-1, dayOfMonth-1);
        String dayOfWeek = simpleDateFormat.format(date);
        String monthString = monthDateFormat.format(date);
        customDate = "?date="+year+"-"+customMonth+"-"+customDay;

        Intent intent = new Intent(getApplicationContext(), ScoreBoard.class);
        Bundle extras = new Bundle();
        extras.putString("CUSTOM_DATE", customDate);
        extras.putString("DAY_OF_WEEK", dayOfWeek);
        extras.putString("MONTH_STRING", monthString);
        intent.putExtras(extras);
        refreshData(intent);
    }

    private void launchBoxScore(int textViewID){
        Intent intent = new Intent(getApplicationContext(), BoxScore.class);
        intent.putExtra("BOX_SCORE_URL", idToBoxScoreURL.get(textViewID));
        startActivity(intent);
    }

    public void addTextToLinearLayout(JSONObject response){
        try {
            TextView copyRight = (TextView) findViewById(R.id.copyrightID);
            String toDisplay = response.getString("copyright");
            int totalGames = response.getInt("totalGames");

            LinearLayout ll = (LinearLayout) findViewById(R.id.scoreboard_linear_layout);
            ll.removeAllViews();
            if(totalGames == 0){
                displayNoGamesMsg(ll);
            }

            for(int i = 0; i < totalGames * 2 ; i++){
                //this weird stuff is to have home team on bottom of game table
                int gameIndex = i % 2 == 0 ? i + 1 : i - 1;
                JSONArray games = getGame(response);
                String score = getScore(gameIndex, games);
                String team = getTeamName(gameIndex, games);
                String gameTime = setGameTime(getGameDate(gameIndex, games), gameIndex);

                makeGameScoreTable(ll, score, team, gameTime, gameIndex, games);
            }

            copyRight.setText(toDisplay);
        } catch (JSONException e){
            System.out.println("UNEXPECTED JSON EXCEPTION!");
        }

    }

    HashMap<Integer, String> idToBoxScoreURL = new HashMap<Integer, String>();
    HashMap<Integer, String> idToTeamName = new HashMap<Integer, String>();

    private void makeGameScoreTable(LinearLayout ll, String score, String team, String gameTime, int i, JSONArray games){
        TableRow row = new TableRow(this);
        TableRow leagueRecordRow = new TableRow(this);
        LinearLayout llForRow = new LinearLayout(this);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        TextView teamName = new TextView(this);
        TextView scoreText = new TextView(this);
        TextView leagueRecordText = new TextView(this);
        ImageView teamLogo = new ImageView(this);

        String teamLogoFileName = team.toLowerCase();
        teamLogoFileName = teamLogoFileName.replace(" ", "_");
        teamLogoFileName = teamLogoFileName.replace("é", "e");
        teamLogoFileName = teamLogoFileName.replace(".", "");
        teamLogoFileName = teamLogoFileName.replace("(", "");
        teamLogoFileName = teamLogoFileName.replace(")", "");
        int drawableID = getResources().getIdentifier(teamLogoFileName, "drawable", getPackageName());
        teamLogo.setImageResource(drawableID);
        teamLogo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        teamLogo.setLayoutParams(new LinearLayout.LayoutParams(logoWidthSpacing(),LinearLayout.LayoutParams.MATCH_PARENT));
        teamLogo.setPadding(0,20,0,10);
        teamLogo.setId(5*i+138);
        idToTeamName.put(5*i+138, teamLogoFileName);
        teamLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TeamLinkMapHelper.launchTeamSite(idToTeamName.get(view.getId()), view, view.getId());
            }
        });

        teamName.setText(team);
        teamName.setId(3*i);
        teamName.setTextSize(20);
        teamName.setLayoutParams(params);
        teamName.setPadding(50, 20, 0, 0);
        boxScoreURL = getLiveGameFeedAndStatsLink(i, games);
        idToBoxScoreURL.put(3*i, boxScoreURL);
        teamName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchBoxScore(view.getId());
            }
        });

        String gameState = getAbstractGameState(i, games);
        if(gameState.equals("Preview")){
            score = " ---";
        }
        scoreText.setText(score);
        scoreText.setId(3*i+1);
        scoreText.setTextSize(24);
        scoreText.setPadding(0, 20, scoreTextRightSpace(), 0);

        String leagueRecord = getTeamRecord(i, games);
        leagueRecordText.setText(leagueRecord);
        leagueRecordText.setTextSize(12);
        leagueRecordText.setPadding(50+logoWidthSpacing(), pixelWidth >= 1080 ? -10 : -5, 0, 0);

        llForRow.addView(teamLogo);
        llForRow.addView(teamName);
        llForRow.addView(scoreText);
        row.addView(llForRow);

        leagueRecordRow.addView(leagueRecordText);

        ll.addView(row);
        ll.addView(leagueRecordRow);
        if(i % 2 == 0){
            TableRow gameTimeRow = new TableRow(this);
            TableRow spacerRow = new TableRow(this);
            gameTimeText = new TextView(this);
            TextView blankView = new TextView(this);

            switch(gameState){
                case "Preview":
                    String detailedGameState = getDetailedGameState(i, games);
                    String seriesSummary = getPlayoffSeriesSummary(i, games);
                    gameTime = detailedGameState.equals("Scheduled (Time TBD)") ? "TBD" : gameTime;
                    gameTimeText.setText(gameTime + seriesSummary);
                    gameTimeText.setTextSize(gameTimeFontSize(seriesSummary));
                    break;

                case "Live":
                    setLiveGameStateTimePeriod(new VolleyCallback() {
                        @Override
                        public void onSuccess(String result, int idTag, String seriesSummary) {
                            gameTimeText = findViewById(idTag);
                            gameTimeText.setText(result + seriesSummary);
                            gameTimeText.setTextSize(gameTimeFontSize(seriesSummary));
                        }
                    }, i, games);

                    break;
                default:
                    setLiveGameStateTimePeriod(new VolleyCallback() {
                        @Override
                        public void onSuccess(String result, int idTag, String seriesSummary) {
                            gameTimeText = findViewById(idTag);
                            if(result.indexOf("SO") > -1 || result.indexOf("OT") > -1){
                                gameTimeText.setText(result + seriesSummary);
                            }else{
                                gameTimeText.setText("Final" + seriesSummary);
                            }
                            gameTimeText.setTextSize(gameTimeFontSize(seriesSummary));
                        }
                    }, i, games);
            }

            gameTimeText.setId(3*i+2);
            gameTimeText.setPadding(50+logoWidthSpacing(), 5, 0, 0);

            spacerRow.addView(blankView);
            gameTimeRow.addView(gameTimeText);
            ll.addView(gameTimeRow);
            ll.addView(spacerRow);
        }
    }

    private int scoreTextRightSpace(){

        if(pixelWidth >= 1440){
            return 150;
        }else if (pixelWidth >= 1080){
            return 112;
        }else if (pixelWidth >= 720) {
            return 75;
        }

        return 37;
    }

    private int logoWidthSpacing(){

        if(pixelWidth >= 1440){
            return 250;
        }else if (pixelWidth >= 1080){
            return 185;
        }else if (pixelWidth >= 720) {
            return 125;
        }

        return 63;
    }

    private int noGameMsgTopPadding(){
        if(pixelWidth >= 1440){
            return 600;
        }else if (pixelWidth >= 1080){
            return 450;
        }else if (pixelWidth >= 720){
            return 300;
        }
        return 150;
    }

    private int gameTimeFontSize(String gameTimeText){
        if(pixelWidth >= 1440){
            return gameTimeText.indexOf("|") > -1 ? 16 : 18;
        }
        return gameTimeText.indexOf("|") > -1 ? 14 : 18;
    }

    private void setLGSTHelper(JSONObject response){

        try{
            JSONObject liveData = response.getJSONObject("liveData");
            JSONObject linescore = liveData.getJSONObject("linescore");
            periodTimeRemaining = linescore.getString("currentPeriodTimeRemaining");
            currentPeriod = linescore.getString("currentPeriodOrdinal");

        } catch(JSONException e){
            System.out.println("UNEXPECTED JSON EXCEPTION");
        }
    }

    private String getCurrentPeriod(){
        return currentPeriod;
    }

    private String getPeriodTimeRemaining(){
        return periodTimeRemaining;
    }

    public interface VolleyCallback{
        void onSuccess(String result, int idTag, String seriesSummary);
    }

    private void setLiveGameStateTimePeriod(final VolleyCallback callback, int i, JSONArray games){
        String url = getLiveGameFeedAndStatsLink(i, games);
        final String seriesSummary = getPlayoffSeriesSummary(i, games);
        final int id = 3*i+2;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                setLGSTHelper(response);
                callback.onSuccess(getPeriodTimeRemaining() + " " + getCurrentPeriod(), id, seriesSummary);
                count++;
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

    private void displayNoGamesMsg(LinearLayout ll){
        TextView msg = new TextView(this);
        msg.setText("No games on this date");
        msg.setGravity(Gravity.CENTER);
        msg.setPadding(0, noGameMsgTopPadding(), 0, 0);
        msg.setTextSize(24);
        ll.addView(msg);
    }

    private String getLiveGameFeedAndStatsLink(int i, JSONArray games){
        String gameLink = "";
        if(games != null) {
            try {
                gameLink = games.getJSONObject(i / 2).getString("link");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return "https://statsapi.web.nhl.com" + gameLink;
    }

    private JSONArray getGame(JSONObject response){
        try{
            JSONArray dates = response.getJSONArray("dates");
            JSONObject elem = null;
            JSONArray games = null;
            if(dates != null)
                elem = dates.getJSONObject(0);

            if(elem != null)
                games = elem.getJSONArray("games");

            return games;
        } catch(JSONException e){
            System.out.println("UNEXPECTED JSON EXCEPTION");
        }

        return null;
    }

    private String getAbstractGameState(int i, JSONArray games){
        String gameState = "";
        if(games != null) {
            try {
                gameState = games.getJSONObject(i / 2).getJSONObject("status").getString("abstractGameState");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return gameState;
    }

    private String getDetailedGameState(int i, JSONArray games){
        String gameState = "";
        if(games != null) {
            try {
                gameState = games.getJSONObject(i / 2).getJSONObject("status").getString("detailedState");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return gameState;
    }

    private String getPlayoffSeriesSummary(int i, JSONArray games){
        String seriesSummary = "";
        if(games != null) {
            try {
                JSONObject seriesSummaryObj = games.getJSONObject(i / 2).getJSONObject("seriesSummary");
                seriesSummary = " | " + seriesSummaryObj.getString("gameLabel") + " " + seriesSummaryObj.getString("seriesStatusShort");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return seriesSummary;
    }

    private String getScore(int i, JSONArray games){
        String score = "-";
        String teamType = "away";
        if(i % 2 == 0)
            teamType = "home";
        if(games != null) {
            try {
                score = Integer.toString(games.getJSONObject(i / 2).getJSONObject("teams").getJSONObject(teamType).getInt("score"));
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return score;
    }

    private String getTeamRecord(int i, JSONArray games){
        String wins = "";
        String losses = "";
        String ties = "";
        String OTL = "";
        String teamType = "away";
        String leagueRecordString = "";
        if(i % 2 == 0)
            teamType = "home";
        if(games != null) {
            try {
                JSONObject leagueRecord = games.getJSONObject(i / 2).getJSONObject("teams").getJSONObject(teamType).getJSONObject("leagueRecord");
                if(leagueRecord.has("wins")){
                    wins = Integer.toString(leagueRecord.getInt("wins"));
                }
                if(leagueRecord.has("losses")){
                    losses = Integer.toString(leagueRecord.getInt("losses"));
                }
                if(leagueRecord.has("ties")){
                    ties = Integer.toString(leagueRecord.getInt("ties"));
                }
                if(leagueRecord.has("ot")){
                    OTL = Integer.toString(leagueRecord.getInt("ot"));
                }

                leagueRecordString = (wins.length() > 0 ? wins : "") + (losses.length() > 0 ? "-" + losses : "") +
                        (ties.length() > 0 ? "-" + ties : "") + (OTL.length() > 0 ? "-" + OTL : "");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return leagueRecordString;
    }

    private String getTeamName(int i, JSONArray games){
        String team = "";
        String teamType = "away";
        if(i % 2 == 0)
            teamType = "home";
        if(games != null) {
            try {
                team = games.getJSONObject(i / 2).getJSONObject("teams").getJSONObject(teamType).getJSONObject("team").getString("name");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return team;
    }

    private String getGameDate(int i, JSONArray games){
        String gameDate = "";
        if(games != null) {
            try {
                gameDate = games.getJSONObject(i / 2).getString("gameDate");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return gameDate;
    }

    private String formatGameTime(String gameTime){
        String hour = gameTime.substring(11,13);
        if(Integer.parseInt(hour) >= 12){
            hour = Integer.parseInt(hour) == 12? Integer.toString(12) : Integer.toString(Integer.parseInt(hour) - 12);
            return hour + gameTime.substring(13,16) + "PM PST";
        }
        return hour + gameTime.substring(13,16) + "AM PST";
    }

    private String setGameTime(String gameDate, int i){
        String gameTime = "";
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date date = df.parse(gameDate);
            SimpleDateFormat dfPST = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dfPST.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            gameTime = dfPST.format(date).toString();
        } catch (ParseException e){
            System.out.println("bad");
        }
        if(gameTime.length() > 0){
            return formatGameTime(gameTime);
        }
        return gameTime;
    }

    private void goFwdOneDay() {
        switch(month){
            case 2:
                if(year % 4 == 0){
                    month = day == 29? month+1 : month;
                    day = day == 29? 1: day + 1;
                }else{
                    month = day == 28? month+1 : month;
                    day = day == 28? 1: day + 1;
                }
                break;
            case 4:
                month = day == 30? month+1 : month;
                day = day == 30? 1: day+1;
                break;
            case 6:
                month = day == 30? month+1 : month;
                day = day == 30? 1: day+1;
                break;
            case 9:
                month = day == 30? month+1 : month;
                day = day == 30? 1: day+1;
                break;
            case 11:
                month = day == 30? month+1 : month;
                day = day == 30? 1: day+1;
                break;
            case 12:
                month = day == 31? 1 : month;
                day = day == 31? 1: day + 1;
                break;
            default:
                month = day == 31? month+1 : month;
                day = day == 31? 1: day+1;
                break;
        }
        setDate((month == 1 && day == 1)? year + 1: year, month, day);
    }

    private void goBackOneDay() {
        switch(month){
            case 1:
                month = day == 1? 12: month;
                day = day == 1? 31: day -1;
                break;
            case 2:
                month = day == 1? month-1 : month;
                day = day == 1? 31: day-1;
                break;
            case 4:
                month = day == 1? month-1 : month;
                day = day == 1? 31: day-1;
                break;
            case 6:
                month = day == 1? month-1 : month;
                day = day == 1? 31: day-1;
                break;
            case 9:
                month = day == 1? month-1 : month;
                day = day == 1? 31: day-1;
                break;
            case 11:
                month = day == 1? month-1 : month;
                day = day == 1? 31: day-1;
                break;
            case 3:
                if(year % 4 == 0){
                    month = day == 1? month-1 : month;
                    day = day == 1? 29: day-1;
                }else{
                    month = day == 1? month-1 : month;
                    day = day == 1? 28: day-1;
                }
                break;
            default:
                month = day == 1? month-1 : month;
                day = day == 1? 30: day-1;
                break;
        }
        setDate((month == 12 && day == 31)? year - 1 : year, month, day);
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
        getMenuInflater().inflate(R.menu.score_board, menu);
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
            setDate(year, month, day);
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


