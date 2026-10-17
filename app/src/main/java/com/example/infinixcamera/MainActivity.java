
package com.example.infinixcamera;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(32, 32, 32, 32);

        TextView title = new TextView(this);
        title.setText("مراقب الكاميرا");
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);

        TextView status = new TextView(this);
        status.setText("الكاميرا متوقفة");
        status.setTextSize(18);
        status.setGravity(Gravity.CENTER);

        Button start = new Button(this);
        start.setText("بدء البث");

        Button stop = new Button(this);
        stop.setText("إيقاف البث");
        stop.setEnabled(false);

        start.setOnClickListener(v -> {
            status.setText(
                "واجهة التطبيق جاهزة. لم يبدأ بث الفيديو بعد."
            );
        });

        stop.setOnClickListener(v -> {
            status.setText("الكاميرا متوقفة");
        });

        layout.addView(title);
        layout.addView(status);
        layout.addView(start);
        layout.addView(stop);

        setContentView(layout);
    }
}
