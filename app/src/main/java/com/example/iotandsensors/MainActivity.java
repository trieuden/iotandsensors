package com.example.iotandsensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private AudioManager audioManager;
    private TextView volumeStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        volumeStatus = findViewById(R.id.volume_status);

        // Khởi tạo AudioManager
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        // Khởi tạo SensorManager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Đăng ký listener cho cảm biến gia tốc
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
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Lấy giá trị trục X (nghiêng trái/phải)
            float x = event.values[0];

            // Lấy mức âm lượng tối đa và hiện tại
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            // Điều chỉnh âm lượng dựa trên độ nghiêng
            if (x > 2 && currentVolume < maxVolume) {
                // Nghiêng sang trái để tăng âm lượng
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume + 1, 0);
            } else if (x < -2 && currentVolume > 0) {
                // Nghiêng sang phải để giảm âm lượng
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume - 1, 0);
            }

            // Cập nhật hiển thị trạng thái âm lượng
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            volumeStatus.setText("Âm lượng: " + currentVolume + "/" + maxVolume);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý thay đổi độ chính xác
    }
}