package exercise.find.roots;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SuccessActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        Intent resultsIntent = getIntent();
        String root1 = String.valueOf(resultsIntent.getLongExtra("root_1", 0));
        String root2 = String.valueOf(resultsIntent.getLongExtra("root_2", 0));
        String origNumber = String.valueOf(resultsIntent.getLongExtra("original_number", 0));
        String calcTime = String.valueOf(resultsIntent.getLongExtra("calc_time", 0));

        TextView outputString = findViewById(R.id.successText);
        String result = String.format("Success! The calculation took %s sec.\n\nResult: %s = %s*%s", calcTime, origNumber, root1, root2);
        outputString.setText(result);
    }
}
