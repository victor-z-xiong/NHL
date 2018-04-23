package no_name.nhl_app;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.*;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.TimeZone;

public class ScoreBoard extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String url = "https://statsapi.web.nhl.com/api/v1/schedule";
    private static ScoreBoard scoreBoardInstance;
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

            final TextView[] myTextViews = new TextView[totalGames*2];
            TextView tv;

            LinearLayout ll = (LinearLayout) findViewById(R.id.scoreboard_linear_layout);
            //setContentView(R.layout.content_score_board);
            for(int i = 0; i < totalGames*2; i++){
                tv = new TextView(this);
                JSONArray dates = response.getJSONArray("dates");
                JSONObject elem = null;
                JSONArray games = null;
                if(dates != null)
                    elem = dates.getJSONObject(0);

                if(elem != null)
                    games = elem.getJSONArray("games");

                String teamType = "away";
                String team = "";
                String gameTime = "";
                String gameDate = "";
                int score = 0;
                if(i % 2 == 0)
                    teamType = "home";

                if(games != null) {
                    gameDate = games.getJSONObject(i / 2).getString("gameDate");
                    team = games.getJSONObject(i / 2).getJSONObject("teams").getJSONObject(teamType).getJSONObject("team").getString("name");
                    score = games.getJSONObject(i / 2).getJSONObject("teams").getJSONObject(teamType).getInt("score");
                }

                try {
                    DateFormat dfPST = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                    dfPST.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
                    Date date = dfPST.parse(gameDate);
                    if(i % 2 == 0)
                        gameTime = dfPST.format(date).toString();
                } catch (ParseException e){
                    System.out.println("bad");
                }


                tv.setText(team + ": " + score+ "        " + gameTime);
                tv.setId(17+i);
                ll.addView(tv);

                myTextViews[i] = tv;

            }

            copyRight.setText(toDisplay);
        } catch (JSONException e){
            System.out.println("UNEXPECTED JSON EXCEPTION!");
        }

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
