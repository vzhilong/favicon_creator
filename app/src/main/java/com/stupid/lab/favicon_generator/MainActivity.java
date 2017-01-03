package com.stupid.lab.favicon_generator;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private EditText mHostInput;
    private Button mCreateBtn;
    private ImageView mCreatedFaviconView;
    private ImageView mOriginalFaviconView;
    private TextView mWebViewTitle;
    private TextView mWebViewUrl;
    private WebView mWebView;

    private int mIconSize;
    private int mCornerRadius;

    private String mInput;
    private String mTitle;
    private Bitmap mOriginalFavicon;
    private static int DEFAULT_CREATED_COLOR = Color.RED;

    private ExecutorService mExecutorService = Executors.newSingleThreadExecutor();
    private Handler mHandler = new Handler(Looper.getMainLooper());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);

        mIconSize = Math.round(getResources().getDimension(R.dimen.created_icon_size));
        mCornerRadius = Math.round(getResources().getDimension(R.dimen.created_icon_corner_radius));

        mHostInput = (EditText) findViewById(R.id.host_input);
        mCreateBtn = (Button) findViewById(R.id.create_btn);
        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String host = mHostInput.getText().toString();
                if (TextUtils.isEmpty(host)) {
                    return;
                }

                mInput = host;
                String url = "http://" + host;
                mWebViewTitle.setText(host);
                mWebViewTitle.setBackgroundColor(Color.TRANSPARENT);
                mWebViewUrl.setText(url);
                mWebViewUrl.setBackgroundColor(Color.TRANSPARENT);

                mWebView.stopLoading();
                mWebView.loadUrl(url);

                mTitle = null;
                mOriginalFavicon = null;
                mOriginalFaviconView.setImageResource(R.mipmap.ic_launcher);
                mCreatedFaviconView.setImageResource(R.mipmap.ic_launcher);

                fetchOriginFaviconFromNetwork(url);
            }
        });
        mCreatedFaviconView = (ImageView) findViewById(R.id.created_favicon);
        mOriginalFaviconView = (ImageView) findViewById(R.id.origin_favicon);
        mWebViewTitle = (TextView) findViewById(R.id.webview_title);
        mWebViewTitle.setBackgroundColor(Color.parseColor("#cccccc"));
        mWebViewTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mWebViewTitle.getPaint().setFakeBoldText(true);
        mWebViewUrl = (TextView) findViewById(R.id.webview_url);
        mWebViewUrl.setBackgroundColor(Color.parseColor("#dddddd"));
        mWebView = (WebView) findViewById(R.id.webview);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mWebViewUrl.setText(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                mWebView.loadUrl(url);
                return true;
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                mTitle = title;
                mWebViewTitle.setText(title);

                tryGenFavicon();
            }

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                mOriginalFavicon = icon;
                mOriginalFaviconView.setImageBitmap(icon);

                tryGenFavicon();
            }
        });
    }

    private void fetchOriginFaviconFromNetwork(final String url) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = IconGenHelper.getBitmapFromNetwork(url + "/favicon.ico");
                if (bitmap == null) {
                    return;
                }
                mOriginalFavicon = bitmap;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mOriginalFaviconView.setImageBitmap(bitmap);
                    }
                });
                tryGenFavicon();
            }
        });
    }

    private void tryGenFavicon() {
        int color = DEFAULT_CREATED_COLOR;
        if (mOriginalFavicon != null) {
            color = IconColorHelper.getColor(mOriginalFavicon, DEFAULT_CREATED_COLOR);
        }
        String title = mInput;
        if (!TextUtils.isEmpty(mTitle)) {
            title = mTitle;
        }

        genAndSetFaviconAsync(color, title);
    }

    private void genAndSetFaviconAsync(final int color, final String title) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap created = IconGenHelper.genIcon(
                        new IconGenHelper.Builder()
                                .setColor(color)
                                .setIconWidth(mIconSize)
                                .setIconHeight(mIconSize)
                                .setCornerRadius(mCornerRadius)
                                .setLetters(title)
                );

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mCreatedFaviconView.setImageBitmap(created);
                    }
                });
            }
        });
    }
}
