package com.example.healthcareassitant;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HealthRecommendationActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private LinearLayout recommendationContainer;
    private String userId;
    private static final String TAG = "HealthRecommendation";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_recommendation);

        recommendationContainer = findViewById(R.id.recommendationContainer);
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            fetchUserData();
        } else {
            addRecommendationCard("Error", "User not logged in.", Color.RED, R.drawable.ic_error);
        }
    }

    private void fetchUserData() {
        Log.d(TAG, "📡 Fetching user profile data...");

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d(TAG, "✅ User profile data found: " + documentSnapshot.getData());

                        int age;
                        try {
                            // ✅ First, try to get age as a Number
                            age = documentSnapshot.getLong("age").intValue();
                        } catch (Exception e) {
                            try {
                                // ✅ If that fails, try to parse it as a String
                                age = Integer.parseInt(documentSnapshot.getString("age"));
                            } catch (Exception ex) {
                                Log.e(TAG, "❌ Error: Age is not a valid number format!", ex);
                                recommendationContainer.removeAllViews();
                                addRecommendationCard("Error", "Invalid age format in Firestore.", Color.RED, R.drawable.ic_error);
                                return;
                            }
                        }

                        String diabetesTypeStr = documentSnapshot.getString("diabetesType");
                        if (diabetesTypeStr == null) {
                            Log.e(TAG, "❌ Error: Diabetes Type missing!");
                            recommendationContainer.removeAllViews();
                            addRecommendationCard("Error", "Diabetes type missing.", Color.RED, R.drawable.ic_error);
                            return;
                        }

                        int diabetesType = Integer.parseInt(diabetesTypeStr);
                        fetchGlucoseData(age, diabetesType);
                    } else {
                        Log.e(TAG, "❌ No user profile found in Firestore.");
                        recommendationContainer.removeAllViews();
                        addRecommendationCard("Error", "User data not found.", Color.RED, R.drawable.ic_error);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "❌ Firestore error: " + e.getMessage()));
    }


    private void fetchGlucoseData(int age, int diabetesType) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String userId = user.getUid();
        CollectionReference logsRef = db.collection("users").document(userId).collection("blood_sugar_logs");

        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, -6); // Get 7 days ago
        Date sevenDaysAgo = calendar.getTime();

        logsRef.whereGreaterThanOrEqualTo("timestamp", new Timestamp(sevenDaysAgo))
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Integer> allReadings = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Long bloodSugar = doc.getLong("value");
                            if (bloodSugar != null) {
                                allReadings.add(bloodSugar.intValue());
                            }
                        }

                        double avgGlucose = calculateAverage(allReadings);
                        Log.d("HealthRecommendation", "✅ Avg Glucose (7 Days): " + avgGlucose);
                        generateHealthRecommendations(age, diabetesType, allReadings);
                    }
                });
    }


    private void generateHealthRecommendations(int age, int diabetesType, List<Integer> glucoseReadings) {
        if (glucoseReadings.isEmpty()) {
            addRecommendationCard("No Data", "Please log your blood sugar levels.", Color.GRAY, R.drawable.ic_warning);
            return;
        }

        double avgGlucose = calculateAverage(glucoseReadings);

        // **Health Status**
        if (avgGlucose > 140) {
            addRecommendationCard("⚠️ High Glucose", "Your glucose is too high. Take action!", Color.parseColor("#FFAB91"), R.drawable.ic_warning);
            addRecommendationCard("🥦 Food Advice", "Eat more fiber-rich foods like vegetables, nuts, and whole grains.", Color.parseColor("#FFE0B2"), R.drawable.ic_diet);
            addRecommendationCard("🥤 Drink Advice", "Avoid sugary drinks like soda and fruit juice. Drink water instead.", Color.parseColor("#BBDEFB"), R.drawable.ic_water);
            addRecommendationCard("🏃 Exercise", "Try light cardio like walking or cycling for 30 minutes.", Color.parseColor("#E1BEE7"), R.drawable.ic_exercise);
        }
        else if (avgGlucose < 70) {
            addRecommendationCard("⚠️ Low Glucose", "Your glucose is too low. Eat something immediately.", Color.parseColor("#FFAB91"), R.drawable.ic_warning);
            addRecommendationCard("🍞 Food Advice", "Eat a fast-acting carbohydrate like bread, rice, or bananas.", Color.parseColor("#FFE0B2"), R.drawable.ic_diet);
            addRecommendationCard("🧃 Drink Advice", "Drink fruit juice or milk to quickly raise glucose levels.", Color.parseColor("#BBDEFB"), R.drawable.ic_water);
            addRecommendationCard("🚶 Activity", "Rest and avoid strenuous activity until glucose normalizes.", Color.parseColor("#E1BEE7"), R.drawable.ic_exercise);
        }
        else {
            addRecommendationCard("✅ Normal Glucose", "Your glucose is stable. Keep up the good work!", Color.parseColor("#C8E6C9"), R.drawable.ic_check);
            addRecommendationCard("🥗 Healthy Eating", "Maintain a balanced diet with proteins, veggies, and whole grains.", Color.parseColor("#FFE0B2"), R.drawable.ic_diet);
            addRecommendationCard("💧 Stay Hydrated", "Drink plenty of water and avoid excessive caffeine.", Color.parseColor("#BBDEFB"), R.drawable.ic_water);
            addRecommendationCard("🏃 Daily Exercise", "Stay active with at least 30 minutes of movement per day.", Color.parseColor("#E1BEE7"), R.drawable.ic_exercise);
        }

        // **Doctor Advice**
        addRecommendationCard("👨‍⚕️ Doctor Advice", "If glucose fluctuates frequently, consult your doctor.", Color.YELLOW, R.drawable.ic_doctor);
    }


    private double calculateAverage(List<Integer> values) {
        if (values.isEmpty()) return 0;
        int sum = 0;
        for (int value : values) sum += value;
        return (double) sum / values.size();
    }

    private void addRecommendationCard(String title, String message, int bgColor, int iconRes) {
        View cardView = LayoutInflater.from(this).inflate(R.layout.item_recommendation, recommendationContainer, false);
        CardView card = cardView.findViewById(R.id.cardView);
        TextView titleText = cardView.findViewById(R.id.titleText);
        TextView messageText = cardView.findViewById(R.id.messageText);
        ImageView icon = cardView.findViewById(R.id.icon);

        card.setCardBackgroundColor(bgColor);
        titleText.setText(title);
        messageText.setText(message);
        icon.setImageResource(iconRes);

        recommendationContainer.addView(cardView);
    }
}
