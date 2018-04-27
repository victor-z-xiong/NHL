package no_name.nhl_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ScoreBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String customDate = null;
    TextView scoreDate;
    Calendar mCurrentDate;
    int day, month, year;

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

        customDate = getIntent().getExtras() == null ? "" : getIntent().getExtras().getString("CUSTOM_DATE");
        String url = "https://statsapi.web.nhl.com/api/v1/schedule" + customDate;

        scoreDate = (TextView) findViewById(R.id.date_editor);

        mCurrentDate = Calendar.getInstance();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMM");

        String dayOfWeek = getIntent().getExtras() == null ? simpleDateFormat.format(mCurrentDate.getTime()) : getIntent().getExtras().getString("DAY_OF_WEEK");
        String monthString = getIntent().getExtras() == null ? monthDateFormat.format(mCurrentDate.getTime()) : getIntent().getExtras().getString("MONTH_STRING");
        day = getIntent().getExtras() == null ? mCurrentDate.get(Calendar.DAY_OF_MONTH) : Integer.parseInt(customDate.substring(14));
        month = getIntent().getExtras() == null ? mCurrentDate.get(Calendar.MONTH) : Integer.parseInt(customDate.substring(11,13));
        year = getIntent().getExtras() == null ? mCurrentDate.get(Calendar.YEAR) : Integer.parseInt(customDate.substring(6,10));

        month = getIntent().getExtras() == null ? month + 1 : month;
        scoreDate.setText("Scores for "+ dayOfWeek+ ", " +monthString+ " " + day+" " +year);

        scoreDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(ScoreBoard.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        monthOfYear = monthOfYear+1;
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
                        startActivity(intent);
                    }
                }, year, month-1, day);
                datePickerDialog.show();
            }
        });

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                addTextToLinearLayout(response);

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("Something is wrong");
                error.printStackTrace();
            }
        });


        GetScoresREST.getInstance().addToRequestQueue(jsonObjectRequest);

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void addTextToLinearLayout(JSONObject response){
        try {
            TextView copyRight = (TextView) findViewById(R.id.copyrightID);
            String toDisplay = response.getString("copyright");
            int totalGames = response.getInt("totalGames");


            LinearLayout ll = (LinearLayout) findViewById(R.id.scoreboard_linear_layout);

            if(totalGames == 0){
                displayNoGamesMsg(ll);
            }

            for(int i = 0; i < totalGames*2; i++){

                JSONArray games = getGame(response);
                int score = getScore(i, games);
                String team = getTeamName(i, games);
                String gameTime = setGameTime(getGameDate(i, games), i);

                makeGameScoreTable(ll, score, team, gameTime, i);

            }

            copyRight.setText(toDisplay);
        } catch (JSONException e){
            System.out.println("UNEXPECTED JSON EXCEPTION!");
        }

    }

    private void makeGameScoreTable(LinearLayout ll, int score, String team, String gameTime, int i){
        TableRow row = new TableRow(this);
        LinearLayout llForRow = new LinearLayout(this);
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        TextView teamName = new TextView(this);
        TextView scoreText = new TextView(this);
        teamName.setText(team);
        teamName.setId(3*i);
        teamName.setTextSize(24);
        teamName.setLayoutParams(params);
        teamName.setPadding(50, 20, 0, 0);
        scoreText.setText(Integer.toString(score));
        scoreText.setId(3*i+1);
        scoreText.setTextSize(24);
        scoreText.setPadding(0, 20, 200, 0);


        llForRow.addView(teamName);
        llForRow.addView(scoreText);
        row.addView(llForRow);

        ll.addView(row);
        if(i % 2 == 1){
            TableRow gameTimeRow = new TableRow(this);
            TableRow spacerRow = new TableRow(this);
            TextView gameTimeText = new TextView(this);
            TextView blankView = new TextView(this);
            gameTimeText.setText(gameTime);
            gameTimeText.setId(3*i+2);
            gameTimeText.setTextSize(18);
            gameTimeText.setPadding(50, 0, 0, 0);

            spacerRow.addView(blankView);
            gameTimeRow.addView(gameTimeText);
            ll.addView(gameTimeRow);
            ll.addView(spacerRow);
        }
    }

    private void displayNoGamesMsg(LinearLayout ll){
        TextView msg = new TextView(this);
        msg.setText("No games on this date");
        msg.setPadding(250, 600, 0, 0);
        msg.setTextSize(24);
        ll.addView(msg);
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
    private int getScore(int i, JSONArray games){
        int score = 0;
        String teamType = "away";
        if(i % 2 == 0)
            teamType = "home";
        if(games != null) {
            try {
                score = games.getJSONObject(i / 2).getJSONObject("teams").getJSONObject(teamType).getInt("score");
            } catch (JSONException e) {
                System.out.println("UNEXPECTED JSON EXCEPTION!");
            }
        }
        return score;
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
        if(Integer.parseInt(hour) > 12){
            hour = Integer.toString(Integer.parseInt(hour) - 12);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
