package tr.bcxip.hummingbird.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import tr.bcxip.hummingbird.R;

/**
 * Created by Hikari on 10/20/14.
 */
public class ErrorView extends LinearLayout {

    public static Map<Integer, String> ERRORS = new HashMap<Integer, String>();

    ImageView mErrorImage;
    TextView mErrorTitle;
    TextView mErrorDetail;
    TextView mRetryButton;

    int errorImageRes;
    int errorImageHeight;
    int errorImageWidth;
    String errorTitle;
    String errorDetail;
    boolean showTitle;
    boolean showDetail;
    boolean showRetryButton;
    int retryButtonBackground;
    int retryButtonTextColor;


    RetryListener listener;

    public ErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        populateErrorsMap();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ErrorView, 0, 0);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_error, this, true);

        mErrorImage = (ImageView) findViewById(R.id.error_image);
        mErrorTitle = (TextView) findViewById(R.id.error_text);
        mErrorDetail = (TextView) findViewById(R.id.error_exception);
        mRetryButton = (TextView) findViewById(R.id.error_retry);

        try {
            errorImageRes = a.getResourceId(R.styleable.ErrorView_ew_errorImage, R.drawable.cloud_off_large);
            errorImageHeight = a.getDimensionPixelSize(R.styleable.ErrorView_ew_errorImageHeight, 0);
            errorImageWidth = a.getDimensionPixelSize(R.styleable.ErrorView_ew_errorImageWidth, 0);
            errorTitle = a.getString(R.styleable.ErrorView_ew_errorTitle);
            errorDetail = a.getString(R.styleable.ErrorView_ew_errorDetail);
            showTitle = a.getBoolean(R.styleable.ErrorView_ew_showTitle, true);
            showDetail = a.getBoolean(R.styleable.ErrorView_ew_showDetail, true);
            showRetryButton = a.getBoolean(R.styleable.ErrorView_ew_showRetryButton, true);
            retryButtonBackground = a.getResourceId(R.styleable.ErrorView_ew_retryButtonBackground,
                    R.drawable.selector_dark);
            retryButtonTextColor = a.getColor(R.styleable.ErrorView_ew_retryButtonTextColor,
                    getResources().getColor(R.color.apptheme_primary));


            if (errorImageRes != 0)
                setErrorImageResource(errorImageRes);

            if (errorImageHeight != 0)
                setErrorImageHeight(errorImageHeight);

            if (errorImageWidth != 0)
                setErrorImageWidth(errorImageWidth);

            if (errorTitle != null)
                setErrorTitle(errorTitle);

            if (errorDetail != null)
                setErrorDetail(errorDetail);

            if (!showTitle)
                mErrorTitle.setVisibility(GONE);

            if (!showDetail)
                mErrorDetail.setVisibility(GONE);

            if (!showRetryButton)
                mRetryButton.setVisibility(GONE);

            mRetryButton.setTextColor(retryButtonTextColor);
        } finally {
            a.recycle();
        }

        mRetryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) listener.onRetry();
            }
        });
    }

    public interface RetryListener {
        public void onRetry();
    }

    public void setOnRetryListener(RetryListener listener) {
        this.listener = listener;
    }

    public void setError(int errorCode) {
        if (ERRORS.containsKey(errorCode))
            setErrorDetail(errorCode + " " + ERRORS.get(errorCode));
    }

    public void setErrorImageResource(int res) {
        mErrorImage.setImageResource(res);
    }

    public void setErrorImageDrawable(Drawable drawable) {
        mErrorImage.setImageDrawable(drawable);
    }

    public void setErrorImageBitmap(Bitmap bitmap) {
        mErrorImage.setImageBitmap(bitmap);
    }

    public void setErrorImageSize(int size) {
        mErrorImage.getLayoutParams().width = size;
        mErrorImage.getLayoutParams().height = size;
    }

    public void setErrorImageSize(int x, int y) {
        setErrorImageWidth(x);
        setErrorImageHeight(y);
    }

    public void setErrorImageHeight(int height) {
        mErrorImage.getLayoutParams().height = height;
    }

    public void setErrorImageWidth(int width) {
        mErrorImage.getLayoutParams().width = width;
    }

    public void setErrorTitle(String text) {
        mErrorTitle.setText(text);
    }

    public void setErrorTitle(int res) {
        mErrorTitle.setText(res);
    }

    public void setErrorDetail(String exception) {
        mErrorDetail.setText(exception);
    }

    public void setErrorDetail(int res) {
        mErrorDetail.setText(res);
    }

    public ImageView getErrorImageView() {
        return mErrorImage;
    }

    public TextView getErrorTitleView() {
        return mErrorTitle;
    }

    public TextView getErrorDetailView() {
        return mErrorDetail;
    }

    public String getErrorTitle() {
        return mErrorTitle.getText().toString();
    }

    public String getErrorDetail() {
        return mErrorDetail.getText().toString();
    }

    public void hideDetailText() {
        mErrorDetail.setVisibility(View.GONE);
    }

    public void showDetailText() {
        mErrorDetail.setVisibility(View.GONE);
    }

    private void populateErrorsMap() {
        ERRORS.put(400, "Bad Request");
        ERRORS.put(401, "Unauthorized");
        ERRORS.put(402, "Payment Required");
        ERRORS.put(403, "Forbidden");
        ERRORS.put(404, "Not Found");
        ERRORS.put(405, "Method Not Allowed");
        ERRORS.put(406, "Not Acceptable");
        ERRORS.put(407, "Proxy Authentication Required");
        ERRORS.put(408, "Request Timeout");
        ERRORS.put(409, "Conflict");
        ERRORS.put(410, "Gone");
        ERRORS.put(411, "Length Required");
        ERRORS.put(412, "Precondition Failed");
        ERRORS.put(413, "Request Entity Too Large");
        ERRORS.put(414, "Request-URI Too Long");
        ERRORS.put(415, "Unsupported Media Type");
        ERRORS.put(416, "Requested Range Not Satisfiable");
        ERRORS.put(417, "Expectation Failed");

        ERRORS.put(500, "Internal Server Error");
        ERRORS.put(501, "Not Implemented");
        ERRORS.put(502, "Bad Gateway");
        ERRORS.put(503, "Service Unavailable");
        ERRORS.put(504, "Gateway Timeout");
        ERRORS.put(505, "HTTP Version Not Supported");
    }
}
