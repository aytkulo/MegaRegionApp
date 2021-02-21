package com.kg.megaregionapp.helper;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.kg.bar.R;

import java.io.ByteArrayOutputStream;

public class Signature extends AppCompatActivity {

    LinearLayout mContent;
    private Bitmap mBitmap;
    signature mSignature;
    View mView;

    Button btnClear;
    Button btnSave;
    String encodedSignature;
    EditText nameSurname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width), (int) (height));

        mContent = (LinearLayout) findViewById(R.id.linearLayout);
        mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mView = mContent;

        mView.setDrawingCacheEnabled(true);

        btnClear = (Button) findViewById(R.id.btn_clear);
        btnSave = (Button) findViewById(R.id.btn_save);
        nameSurname = (EditText) findViewById(R.id.differentReceiver);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String operationType = extras.getString("OPERATION");
            if (operationType.equalsIgnoreCase("entry"))
                nameSurname.setVisibility(View.INVISIBLE);
        }

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignature.clear();
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                encodedSignature = mSignature.save(mView);
                Bundle b = new Bundle();
                b.putString("signature", encodedSignature);
                b.putString("nameSurname", nameSurname.getText().toString());
                b.putString("status", "OK");
                Intent intent = new Intent();
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }


    public class signature extends View {
        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();
        String encodedImage = "";

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public String save(View v) {
            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);

            }
            Canvas canvas = new Canvas(mBitmap);
            try {
                //      FileOutputStream mFileOutStream = new FileOutputStream("anyName");
                v.draw(canvas);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //     mBitmap.compress(Bitmap.CompressFormat.PNG, 90, mFileOutStream);
                mBitmap.compress(Bitmap.CompressFormat.PNG, 20, baos);
                byte[] b = baos.toByteArray();
                encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                //   mFileOutStream.close();
                //  String url = Images.Media.insertImage(getContentResolver(), mBitmap, "title", null);
                //If you want to convert the image to string use base64 converter


                //   byte[] a = Base64.decode(encodedImage, Base64.DEFAULT);
                // Bitmap image = BitmapFactory.decodeByteArray(a, 0, a.length);

                System.out.println(encodedImage);
            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }

            return encodedImage;
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();


            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }
}
