package exercise.find.roots;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

import java.math.BigInteger;

public class MainActivity extends AppCompatActivity {

  private BroadcastReceiver broadcastReceiverForSuccess = null;
  private BroadcastReceiver broadcastReceiverForFailure = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ProgressBar progressBar = findViewById(R.id.progressBar);
    EditText editTextUserInput = findViewById(R.id.editTextInputNumber);
    Button buttonCalculateRoots = findViewById(R.id.buttonCalculateRoots);
    TextView alertTextView = findViewById(R.id.AlertTextView);

    // set initial UI:
    progressBar.setVisibility(View.GONE); // hide progress
    editTextUserInput.setText(""); // cleanup text in edit-text
    editTextUserInput.setEnabled(true); // set edit-text as enabled (user can input text)
    buttonCalculateRoots.setEnabled(false); // set button as disabled (user can't click)

    // set listener on the input written by the keyboard to the edit-text
    editTextUserInput.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
      public void onTextChanged(CharSequence s, int start, int before, int count) { }
      public void afterTextChanged(Editable s) {
        // text did change
        String newText = editTextUserInput.getText().toString();
        boolean onlyDigits = TextUtils.isDigitsOnly(newText);
        if (!onlyDigits || (newText.length() > 0 && newText.charAt(0) == '0')) {
          buttonCalculateRoots.setEnabled(false);
          if (!newText.equals("")) {
            alertTextView.setVisibility(View.VISIBLE);
          }
          return;
        }
        else if (!newText.equals("") && new BigInteger(newText).compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0)
        {
          buttonCalculateRoots.setEnabled(false);
          alertTextView.setVisibility(View.VISIBLE);
          return;
        }
        buttonCalculateRoots.setEnabled(true);
        alertTextView.setVisibility(View.INVISIBLE);
      }
    });

    // set click-listener to the button
    buttonCalculateRoots.setOnClickListener(v -> {
      Intent intentToOpenService = new Intent(MainActivity.this, CalculateRootsService.class);
      String userInputString = editTextUserInput.getText().toString();
      long userInputLong = Long.parseLong(userInputString);
      intentToOpenService.putExtra("number_for_service", userInputLong);
      startService(intentToOpenService);
      buttonCalculateRoots.setEnabled(false);
      editTextUserInput.setEnabled(false);
      progressBar.setVisibility(View.VISIBLE);
    });

    // register a broadcast-receiver to handle action "found_roots"
    broadcastReceiverForSuccess = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent incomingIntent) {
        if (incomingIntent == null || !incomingIntent.getAction().equals("found_roots")) return;
        progressBar.setVisibility(View.INVISIBLE);
        editTextUserInput.setEnabled(true);
        editTextUserInput.setText("");
        buttonCalculateRoots.setEnabled(false);

        long originalNumber = incomingIntent.getLongExtra("original_number", 0);
        long root1 = incomingIntent.getLongExtra("root1", 0);
        long root2 = incomingIntent.getLongExtra("root2",0);
        long calcTime = (long) (incomingIntent.getLongExtra("calc_time", 0) / 1000f);
        startSuccessActivity(root1, root2, originalNumber, calcTime);

      }
    };
    registerReceiver(broadcastReceiverForSuccess, new IntentFilter("found_roots"));

    broadcastReceiverForFailure = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent incomingIntent) {
        if (incomingIntent == null || !incomingIntent.getAction().equals("stopped_calculations")) return;
        long timePassed = (long) (incomingIntent.getLongExtra("time_until_give_up_seconds", 0) / 1000f);
        String toastAlertText = String.format("calculation aborted after %d seconds", timePassed);
        Toast toast = Toast.makeText(MainActivity.this, toastAlertText, Toast.LENGTH_SHORT);
        toast.show();
        progressBar.setVisibility(View.INVISIBLE);
        editTextUserInput.setEnabled(true);
        editTextUserInput.setText("");
        buttonCalculateRoots.setEnabled(false);
      }
    };
    registerReceiver(broadcastReceiverForFailure, new IntentFilter("stopped_calculations"));
  }

  protected void startSuccessActivity(long root1, long root2, long originalNumber, long calcTime)
  {
    Intent intentToOpenSuccessActivity = new Intent(MainActivity.this, SuccessActivity.class);
    intentToOpenSuccessActivity.putExtra("original_number", originalNumber);
    intentToOpenSuccessActivity.putExtra("root_1", root1);
    intentToOpenSuccessActivity.putExtra("root_2", root2);
    intentToOpenSuccessActivity.putExtra("calc_time", calcTime);
    startActivity(intentToOpenSuccessActivity);
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();
    this.unregisterReceiver(broadcastReceiverForFailure);
    this.unregisterReceiver(broadcastReceiverForSuccess);
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    EditText editText = findViewById(R.id.editTextInputNumber);
    outState.putString("curText", editText.getText().toString());
  }

  @Override
  protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    EditText editText = findViewById(R.id.editTextInputNumber);
    editText.setText(savedInstanceState.getString("curText"));
  }
}


/*
the spec is:

upon launch, Activity starts out "clean":
* progress-bar is hidden
* "input" edit-text has no input and it is enabled
* "calculate roots" button is disabled

the button behavior is:
* when there is no valid-number as an input in the edit-text, button is disabled
* when we triggered a calculation and still didn't get any result, button is disabled
* otherwise (valid number && not calculating anything in the BG), button is enabled

the edit-text behavior is:
* when there is a calculation in the BG, edit-text is disabled (user can't input anything)
* otherwise (not calculating anything in the BG), edit-text is enabled (user can tap to open the keyboard and add input)

the progress behavior is:
* when there is a calculation in the BG, progress is showing
* otherwise (not calculating anything in the BG), progress is hidden

when "calculate roots" button is clicked:
* change states for the progress, edit-text and button as needed, so user can't interact with the screen

when calculation is complete successfully:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* open a new "success" screen showing the following data:
  - the original input number
  - 2 roots combining this number (e.g. if the input was 99 then you can show "99=9*11" or "99=3*33"
  - calculation time in seconds

when calculation is aborted as it took too much time:
* change states for the progress, edit-text and button as needed, so the screen can accept new input
* show a toast "calculation aborted after X seconds"


upon screen rotation (saveState && loadState) the new screen should show exactly the same state as the old screen. this means:
* edit-text shows the same input
* edit-text is disabled/enabled based on current "is waiting for calculation?" state
* progress is showing/hidden based on current "is waiting for calculation?" state
* button is enabled/disabled based on current "is waiting for calculation?" state && there is a valid number in the edit-text input


 */