package com.example.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import android.content.Intent;
import android.net.Uri;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

  private WebView webView;
  private static final String CHANNEL_ID = "webview_channel";
  private static final int REQUEST_IMAGE_CAPTURE = 1;

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EdgeToEdge.enable(this);
    setContentView(R.layout.activity_main);

    ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
      Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
      return insets;
    });

    // 🔥 Permission (Notifikasi + Kamera)
    if (Build.VERSION.SDK_INT >= 33) {
      requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1);
    }

    if (Build.VERSION.SDK_INT >= 23) {
      requestPermissions(new String[]{
        android.Manifest.permission.CAMERA
      }, 2);
    }

    webView = findViewById(R.id.webView);

    WebSettings webSettings = webView.getSettings();

    // WAJIB untuk Angular
    webSettings.setJavaScriptEnabled(true);
    webSettings.setDomStorageEnabled(true);
    webSettings.setAllowFileAccess(true);
    webSettings.setAllowContentAccess(true);
    webSettings.setLoadWithOverviewMode(true);
    webSettings.setUseWideViewPort(true);

    webSettings.setAllowUniversalAccessFromFileURLs(true);
    webSettings.setAllowFileAccessFromFileURLs(true);
    webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

    webSettings.setUserAgentString(
      "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 Chrome/99.0.0.0 Mobile Safari/537.36"
    );

    // COOKIE LOGIN
    CookieManager cookieManager = CookieManager.getInstance();
    cookieManager.setAcceptCookie(true);
    cookieManager.setAcceptThirdPartyCookies(webView, true);

    webView.setWebViewClient(new WebViewClient());
    webView.setWebChromeClient(new WebChromeClient());

    // 🔥 HUBUNGKAN WEB ↔ ANDROID
    webView.addJavascriptInterface(new AndroidBridge(), "Android");

    // LOAD WEB
    webView.loadUrl("https://stmikpontianak.cloud/011100862/angular011100862");

    // 🔔 Buat channel notifikasi
    createNotificationChannel();
  }

  // 🔙 Tombol back
  @Override
  public void onBackPressed() {
    if (webView.canGoBack()) {
      webView.goBack();
    } else {
      super.onBackPressed();
    }
  }

  // 🔔 Channel Notifikasi
  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      CharSequence name = "WebView Notification";
      String description = "Channel notifikasi dari WebView";
      int importance = NotificationManager.IMPORTANCE_HIGH;

      NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
      channel.setDescription(description);

      NotificationManager manager = getSystemService(NotificationManager.class);
      manager.createNotificationChannel(channel);
    }
  }

  // 📸 HASIL KAMERA
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
      Toast.makeText(this, "Foto berhasil diambil 📸", Toast.LENGTH_SHORT).show();
    }
  }

  // 🔥 BRIDGE
  public class AndroidBridge {

    // 🔔 NOTIFIKASI
    @JavascriptInterface
    public void showNotification(String title, String message) {
      runOnUiThread(() -> {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, CHANNEL_ID)
          .setSmallIcon(android.R.drawable.ic_dialog_info)
          .setContentTitle(title)
          .setContentText(message)
          .setPriority(NotificationCompat.PRIORITY_HIGH)
          .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(1, builder.build());
      });
    }

    // 📞 TELEPON
    @JavascriptInterface
    public void showCall() {
      Intent intent = new Intent(Intent.ACTION_DIAL);
      intent.setData(Uri.parse("tel:08123456789"));
      startActivity(intent);
    }

    // 💬 WHATSAPP
    @JavascriptInterface
    public void showWhatsApp() {
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse("https://wa.me/628123456789"));
      startActivity(intent);
    }

    // 📸 KAMERA BENERAN
    @JavascriptInterface
    public void showCamera() {
      runOnUiThread(() -> {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
      });
    }
  }
}
