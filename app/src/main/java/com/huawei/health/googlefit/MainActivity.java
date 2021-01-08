package com.huawei.health.googlefit;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 创建凭据 https://console.developers.google.com/apis/credentials/oauthclient/46627530181-d25fii76om705pqko8dp9qtvh94jgabj.apps.googleusercontent.com?project=superb-virtue-293810
 */
public class MainActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();

    FitnessOptions fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(view -> {
            GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(MainActivity.this, fitnessOptions);

            if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
                GoogleSignIn.requestPermissions(
                        MainActivity.this, // your activity
                        1, // e.g. 1
                        account,
                        fitnessOptions);
            } else {
                accessGoogleFit();
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                accessGoogleFit();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void accessGoogleFit() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .bucketByTime(1, TimeUnit.DAYS)
                .build();

        GoogleSignInAccount account = GoogleSignIn
                .getAccountForExtension(this, fitnessOptions);

        Fitness.getHistoryClient(this, account)
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(response -> {
                    // Use response data here
                    Log.d(TAG, "OnSuccess()");
                    int totalSteps = response.isEmpty()
                            ? 0
                            : response.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d(TAG, "OnSuccess()" + totalSteps);

                    ((TextView)findViewById(R.id.tvValue)).setText(totalSteps + "");
                })
                .addOnFailureListener(e -> {
                    Log.d(TAG, "OnFailure()", e);
                });
    }

}