package com.example.iotandsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity3 extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private AudioManager audioManager;
    private TextView actionStatus;
    private long lastActionTime = 0;
    private static final long COOLDOWN = 5000; // 5 giây giữa các lần chuyển bài
    private static final float TILT_THRESHOLD = 3.0f; // Ngưỡng nghiêng (m/s²)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        actionStatus = findViewById(R.id.action_status);
        actionStatus.setText("Chờ xoay điện thoại");

        // Khởi tạo AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Khởi tạo SensorManager và accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accelerometer == null) {
            Toast.makeText(this, "Thiết bị không hỗ trợ cảm biến gia tốc", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký listener cho accelerometer
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Hủy đăng ký listener để tiết kiệm pin
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER) return;

        // Lấy giá trị trục X (xoay trái/phải)
        float x = event.values[0]; // m/s², dương: xoay trái, âm: xoay phải

        // Kiểm tra cooldown
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastActionTime < COOLDOWN) return;

        // Xử lý xoay điện thoại
        if (x > TILT_THRESHOLD) {
            // Xoay trái: Chuyển bài tiếp theo
            dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
            actionStatus.setText("Chuyển bài tiếp theo");
            Toast.makeText(this, "Chuyển bài tiếp theo", Toast.LENGTH_SHORT).show();
            lastActionTime = currentTime;
        } else if (x < -TILT_THRESHOLD) {
            // Xoay phải: Quay lại bài trước
            dispatchMediaKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            actionStatus.setText("Quay lại bài trước");
            Toast.makeText(this, "Quay lại bài trước", Toast.LENGTH_SHORT).show();
            lastActionTime = currentTime;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý thay đổi độ chính xác
    }

    private void dispatchMediaKeyEvent(int keyCode) {
        KeyEvent downEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        KeyEvent upEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        audioManager.dispatchMediaKeyEvent(downEvent);
        audioManager.dispatchMediaKeyEvent(upEvent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}