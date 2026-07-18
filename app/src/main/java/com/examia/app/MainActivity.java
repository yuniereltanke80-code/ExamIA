package com.examia.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.content.Intent;
import android.net.Uri;
import android.widget.Button;
import android.widget.Toast;
import java.io.IOException;
import java.net.URL;

public class MainActivity extends Activity {

    private WebView webView;
    private Button saveAsWebAppButton;
    private static final String APP_URL = "https://yuniereltanke80-code.github.io/ExamIA/";
    private static final String PROGRAM_NAME = "ExamIA";
    private static final String FALLBACK_URL = "file:///android_asset/ExamIA.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        saveAsWebAppButton = findViewById(R.id.addWebAppButton);

        setupWebView();

        saveAsWebAppButton.setOnClickListener(v -> {
            saveCurrentAppAsWebApp();
        });

        // Determinar URL a cargar
        String urlToLoad = APP_URL;

        // Si la app fue abierta con un deep link (enlace compartido)
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri data = intent.getData();
            if (data != null) {
                urlToLoad = data.toString();
            }
        }

        webView.loadUrl(urlToLoad);
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);

        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // Abrir enlaces externos (WhatsApp, etc.) en el navegador
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    if (url.contains("wa.me") || url.contains("whatsapp")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                        return true;
                    }
                    return false; // Cargar en el WebView
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (failingUrl != null && failingUrl.equals(APP_URL)) {
                    // Fallback a archivo local si no hay internet
                    view.loadUrl(FALLBACK_URL);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                return true;
            }

            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                runOnUiThread(() -> request.grant(request.getResources()));
            }
        });

        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }

    private void saveCurrentAppAsWebApp() {
        new android.app.AlertDialog.Builder(this)
            .setTitle("Guardar como Aplicación Web")
            .setMessage("¿Estás seguro de que deseas agregar este sitio web como aplicación a tu escritorio?")
            .setPositiveButton("Sí", (dialog, which) -> {
                showInstallInstructions();
            })
            .setNegativeButton("No", (dialog, which) -> {
                dialog.dismiss();
            })
            .show();
    }

    private void showInstallInstructions() {
        String message = "Para instalar cualquier sitio web como aplicación en Android:\n\n"
            + "1. Mantén presionado el botón de menú/acciones\n"
            + "2. Selecciona 'Agregar a la pantalla de inicio'\n"
            + "3. O visita a través de Firefox/Chrome y selecciona 'Instalar aplicación web'\n\n"
            + "El sitio actual será agregado como aplicación fácilmente.";

        new android.app.AlertDialog.Builder(this)
            .setTitle("Instalar como aplicación web")
            .setMessage(message)
            .setPositiveButton("Aceptar", (dialog, which) -> {
                Toast.makeText(this, "Puedes instalarlo manualmente ahora", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            })
            .show();
    }
}
