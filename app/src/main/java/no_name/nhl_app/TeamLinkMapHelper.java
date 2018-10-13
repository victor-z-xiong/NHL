package no_name.nhl_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import static android.support.v4.content.ContextCompat.startActivity;

/**
 * Created by user on 2018-10-05.
 */

public class TeamLinkMapHelper {
    static final Map<String, String> TeamLinkMap = new HashMap<String, String>() {{
        put("anaheim_ducks", "https://www.nhl.com/ducks");
        put("arizona_coyotes", "https://www.nhl.com/coyotes");
        put("atlanta_flames", "https://www.nhl.com/flames");
        put("atlanta_thrashers", "https://www.nhl.com/jets");
        put("boston_bruins", "https://www.nhl.com/bruins");
        put("buffalo_sabres", "https://www.nhl.com/sabres");
        put("calgary_flames", "https://www.nhl.com/flames");
        put("california_golden_seals", "https://www.nhl.com/sharks");
        put("carolina_hurricanes", "https://www.nhl.com/hurricanes");
        put("chicago_blackhawks", "https://www.nhl.com/blackhawks");
        put("cleveland_barons", "https://www.nhl.com/sharks");
        put("colorado_avalanche", "https://www.nhl.com/avalanche");
        put("colorado_rockies", "https://www.nhl.com");
        put("columbus_blue_jackets", "https://www.nhl.com/bluejackets");
        put("dallas_stars", "https://www.nhl.com/stars");
        put("detroit_cougars", "https://www.nhl.com/redwings");
        put("detroit_falcons", "https://www.nhl.com/redwings");
        put("detroit_red_wings", "https://www.nhl.com/redwings");
        put("edmonton_oilers", "https://www.nhl.com/oilers");
        put("florida_panthers", "https://www.nhl.com/panthers");
        put("hamilton_tigers", "https://www.nhl.com");
        put("hartford_whalers", "https://www.nhl.com/hurricanes");
        put("kansas_city_scouts", "https://www.nhl.com/devils");
        put("los_angeles_kings", "https://www.nhl.com/kings");
        put("minnesota_north_stars", "https://www.nhl.com/stars");
        put("minnesota_wild", "https://www.nhl.com/wild");
        put("montreal_canadiens", "https://www.nhl.com/canadiens");
        put("montreal_maroons", "https://www.nhl.com");
        put("montreal_wanderers", "https://www.nhl.com");
        put("nashville_predators", "https://www.nhl.com/predators");
        put("new_jersey_devils", "https://www.nhl.com/devils");
        put("new_york_americans", "https://www.nhl.com");
        put("new_york_islanders", "https://www.nhl.com/islanders");
        put("new_york_rangers", "https://www.nhl.com/rangers");
        put("oakland_seals", "https://www.nhl.com/sharks");
        put("ottawa_senators", "https://www.nhl.com/senators");
        put("ottawa_senators_1917", "https://www.nhl.com/senators");
        put("philadelphia_flyers", "https://www.nhl.com/flyers");
        put("philadelphia_quakers", "https://www.nhl.com/flyers");
        put("phoenix_coyotes", "https://www.nhl.com/coyotes");
        put("pittsburgh_penguins", "https://www.nhl.com/penguins");
        put("pittsburgh_pirates", "https://www.nhl.com/penguins");
        put("quebec_bulldogs", "https://www.nhl.com");
        put("quebec_nordiques", "https://www.nhl.com/avalanche");
        put("san_jose_sharks", "https://www.nhl.com/sharks");
        put("st_louis_blues", "https://www.nhl.com/blues");
        put("st_louis_eagles", "https://www.nhl.com/blues");
        put("tampa_bay_lightning", "https://www.nhl.com/lightning");
        put("toronto_arenas", "https://www.nhl.com/mapleleafs");
        put("toronto_maple_leafs", "https://www.nhl.com/mapleleafs");
        put("toronto_st_patricks", "https://www.nhl.com/mapleleafs");
        put("vancouver_canucks", "https://www.nhl.com/canucks");
        put("vegas_golden_knights", "https://www.nhl.com/goldenknights");
        put("washington_capitals", "https://www.nhl.com/capitals");
        put("winnipeg_jets", "https://www.nhl.com/jets");
        put("winnipeg_jets_1979", "https://www.nhl.com/jets");
    }};

    public static void launchTeamSite(String teamName, View parentView, int test){

        Intent intent = new Intent(parentView.getContext(), replayPlayer.class);
        Bundle extras = new Bundle();
        String link = TeamLinkMap.get(teamName) == null ? "https://www.nhl.com" : TeamLinkMap.get(teamName);
        extras.putString("REPLAY_URL", link);
        intent.putExtras(extras);
        parentView.getContext().startActivity(intent);
    }
}
