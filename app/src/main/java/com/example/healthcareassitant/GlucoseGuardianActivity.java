package com.example.healthcareassitant;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class GlucoseGuardianActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LineChart bloodSugarChart;
    private TextView avg7DaysText, avgTodayText, statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glucose_guardian);

        db = FirebaseFirestore.getInstance();

        bloodSugarChart = findViewById(R.id.bloodSugarChart);
        avg7DaysText = findViewById(R.id.avg7DaysText);
        avgTodayText = findViewById(R.id.avgTodayText);
        statusText = findViewById(R.id.statusText);

        Button loggingButton = findViewById(R.id.loggingButton);
        loggingButton.setOnClickListener(v -> {
            Intent intent = new Intent(GlucoseGuardianActivity.this, BloodSugarLoggingActivity.class);
            startActivity(intent);
        });

        setupChart();
        fetchBloodSugarData(); // Initial fetch when activity is created
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchBloodSugarData(); // Refresh data when returning to this activity
    }

    private void fetchBloodSugarData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        CollectionReference logsRef = db.collection("users").document(userId).collection("blood_sugar_logs");

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -6);
        Date sevenDaysAgo = calendar.getTime();

        logsRef.whereGreaterThanOrEqualTo("timestamp", new Timestamp(sevenDaysAgo))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("Firestore", "Error fetching data", error);
                        return;
                    }
                    if (queryDocumentSnapshots == null) return;

                    Log.d("Firestore", "New data detected: " + queryDocumentSnapshots.size());

                    LinkedHashMap<String, ArrayList<Integer>> dailyReadings = new LinkedHashMap<>();
                    ArrayList<Entry> chartEntries = new ArrayList<>();
                    ArrayList<String> dateLabels = new ArrayList<>();
                    ArrayList<Integer> allReadings = new ArrayList<>();
                    ArrayList<Integer> todayReadings = new ArrayList<>();

                    // Initialize last 7 days with empty lists
                    for (int i = 0; i < 7; i++) {
                        String dateKey = getFormattedDate(calendar.getTime());
                        dailyReadings.put(dateKey, new ArrayList<>());
                        calendar.add(Calendar.DAY_OF_YEAR, 1);
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Timestamp timestamp = doc.getTimestamp("timestamp");
                        Long bloodSugar = doc.getLong("value");

                        if (timestamp != null && bloodSugar != null) {
                            String dateKey = getFormattedDate(timestamp.toDate());

                            if (dailyReadings.containsKey(dateKey)) {
                                dailyReadings.get(dateKey).add(bloodSugar.intValue());
                            }

                            allReadings.add(bloodSugar.intValue());

                            if (isSameDay(timestamp.toDate(), today)) {
                                todayReadings.add(bloodSugar.intValue());
                            }
                        }
                    }

                    int index = 0;
                    for (Map.Entry<String, ArrayList<Integer>> entry : dailyReadings.entrySet()) {
                        double dailyAvg = calculateAverage(entry.getValue());
                        chartEntries.add(new Entry(index++, (float) dailyAvg));
                        dateLabels.add(entry.getKey());
                    }

                    double avg7Days = calculateAverage(allReadings);
                    double avgToday = calculateAverage(todayReadings);

                    runOnUiThread(() -> {
                        avg7DaysText.setText(String.format(Locale.getDefault(), "%.1f mg/dL", avg7Days));
                        avgTodayText.setText(String.format(Locale.getDefault(), "%.1f mg/dL", avgToday));
                        updateStatusText(avg7Days);
                        setChartData(chartEntries, dateLabels);
                        bloodSugarChart.invalidate(); // Ensure chart updates immediately
                    });
                });
    }

    private String getFormattedDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
        return sdf.format(date);
    }

    private double calculateAverage(ArrayList<Integer> values) {
        if (values.isEmpty()) return 0;
        int sum = 0;
        for (int value : values) {
            sum += value;
        }
        return (double) sum / values.size();
    }

    private void updateStatusText(double avg7Days) {
        if (avg7Days < 70) {
            statusText.setText("Low Level ⚠️");
            statusText.setTextColor(Color.parseColor("#CC9900"));
        } else if (avg7Days < 140) {
            statusText.setText("Good Level ✅");
            statusText.setTextColor(Color.GREEN);
        } else {
            statusText.setText("Bad Level ❌");
            statusText.setTextColor(Color.RED);
        }
    }

    private void setupChart() {
        bloodSugarChart.getDescription().setEnabled(false);
        bloodSugarChart.setTouchEnabled(true);
        bloodSugarChart.setDragEnabled(true);
        bloodSugarChart.setScaleEnabled(true);
        bloodSugarChart.setPinchZoom(true);

        XAxis xAxis = bloodSugarChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setAxisLineColor(Color.DKGRAY);

        YAxis leftAxis = bloodSugarChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setAxisLineColor(Color.DKGRAY);

        bloodSugarChart.getAxisRight().setEnabled(false);
    }

    private void setChartData(ArrayList<Entry> values, ArrayList<String> labels) {
        if (values.isEmpty()) {
            values.add(new Entry(0, 100));
        }

        LineDataSet dataSet = new LineDataSet(values, "Blood Sugar Levels");
        dataSet.setColor(Color.BLUE);
        dataSet.setCircleColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleRadius(4f);
        dataSet.setValueTextSize(12f);

        LineData lineData = new LineData(dataSet);
        bloodSugarChart.setData(lineData);

        XAxis xAxis = bloodSugarChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(labels.size());

        bloodSugarChart.invalidate();
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}
