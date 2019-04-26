package no_name.nhl_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class replayPlayer extends AppCompatActivity {
    WebView replayWindow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupFullscreenMode();
        setContentView(R.layout.activity_replay_player);
        replayWindow = findViewById(R.id.replayWindow);
        replayWindow.getSettings().setJavaScriptEnabled(true);
        replayWindow.setWebViewClient(new WebViewClient());
        replayWindow.loadUrl(getIntent().getExtras().getString("REPLAY_URL"));
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setFullscreen();
        }
    }

    @Override
    public void onBackPressed() {
        if(replayWindow.canGoBack()){
            replayWindow.goBack();
        } else{
            super.onBackPressed();
        }
    }

    private void setupFullscreenMode() {
        View decorView = setFullscreen();
        decorView
                .setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        setFullscreen();
                    }
                });
    }

    private View setFullscreen() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        return decorView;
    }
}
