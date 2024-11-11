/*package com.example.baza;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SendReportWorker extends Worker {

    private static final String TAG = "SendReportWorker";
    private FirebaseFirestore db;

    public SendReportWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("OfflineReports", Context.MODE_PRIVATE);

        // Retrieve report data
        String type = prefs.getString("type", null);
        String location = prefs.getString("location", null);
        String description = prefs.getString("description", null);
        String userId = prefs.getString("user", null);
        String timeCreated = prefs.getString("timeCreated", null);

        if (type == null || location == null || description == null || userId == null || timeCreated == null) {
            // Nothing to send
            return Result.success();
        }

        // Create a map for the report data
        Map<String, Object> dangerData = new HashMap<>();
        dangerData.put("type", type);
        dangerData.put("location", location);
        dangerData.put("description", description);
        dangerData.put("user", userId);
        dangerData.put("timeCreated", timeCreated);
        dangerData.put("accepted", false);

        try {
            db.collection("dangers")
                    .add(dangerData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d(TAG, "Danger sent successfully with ID: " + documentReference.getId());

                        // Clear saved data on success
                        prefs.edit().clear().apply();
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "Error sending danger", e));

            return Result.success();
        } catch (Exception e) {
            Log.e(TAG, "Failed to send danger", e);
            return Result.retry();
        }
    }
}*/
