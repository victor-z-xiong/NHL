package no_name.nhl_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class replayPlayer extends AppCompatActivity {
    WebView replayWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replay_player);
        replayWindow = findViewById(R.id.replayWindow);
        replayWindow.getSettings().setJavaScriptEnabled(true);
        replayWindow.setWebViewClient(new WebViewClient());
        replayWindow.loadUrl(getIntent().getExtras().getString("REPLAY_URL"));
    }
}
