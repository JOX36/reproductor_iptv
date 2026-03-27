package com.jox3.iptvplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.view.View;

public class MainActivity extends Activity {
    private WebView webView;
    private long lastBackPress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        webView = new WebView(this);
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(
            "Mozilla/5.0 (Linux; Android 11; Mobile) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/112.0.0.0 Mobile Safari/537.36"
        );

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, android.webkit.WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("intent:")) {
                    try {
                        android.content.Intent intent = android.content.Intent.parseUri(url, android.content.Intent.URI_INTENT_SCHEME);
                        startActivity(intent);
                    } catch (Exception e) {}
                    return true;
                }
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            private View customView;
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                customView = view;
                setContentView(customView);
            }
            @Override
            public void onHideCustomView() {
                setContentView(webView);
            }
        });

        webView.loadUrl("file:///android_asset/index.html");
    }

    @Override
    public void onBackPressed() {
        // Primero intentar navegación interna del HTML
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }

        // Llamar a la función JS de navegación TV
        webView.evaluateJavascript("typeof tvHandleBack === 'function' ? tvHandleBack() : true", result -> {
            if ("true".equals(result)) {
                // No hay más niveles — mostrar diálogo de salida
                runOnUiThread(() -> showExitDialog());
            }
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Salir")
            .setMessage("¿Deseas salir de IPTV JOX3?")
            .setPositiveButton("Sí", (dialog, which) -> finish())
            .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
            .setDefaultButton(AlertDialog.BUTTON_NEGATIVE)
            .show();
    }
}
