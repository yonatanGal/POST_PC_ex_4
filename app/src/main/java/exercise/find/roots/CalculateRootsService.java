package exercise.find.roots;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class CalculateRootsService extends IntentService {


  public CalculateRootsService() {
    super("CalculateRootsService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    if (intent == null) return;
    long timeStartMs = System.currentTimeMillis();
    long numberToCalculateRootsFor = intent.getLongExtra("number_for_service", 0);
    if (numberToCalculateRootsFor <= 0) {
      Log.e("CalculateRootsService", "can't calculate roots for non-positive input" + numberToCalculateRootsFor);
      return;
    }

    Intent successIntent = new Intent("found_roots");
    Intent failureIntent = new Intent("stopped_calculations");

    for (long i = 2; ; i++)
    {
      if (numberToCalculateRootsFor % i == 0)
      {
        successIntent.putExtra("original_number", numberToCalculateRootsFor);
        successIntent.putExtra("root1", i);
        successIntent.putExtra("root2", numberToCalculateRootsFor / i);
        long calc_time = System.currentTimeMillis() - timeStartMs;
        successIntent.putExtra("calc_time", calc_time);
        this.sendBroadcast(successIntent);
        return;
      }

      long timePassed = System.currentTimeMillis() - timeStartMs;
      if (timePassed > 20000)
      {
        failureIntent.putExtra("original_number", numberToCalculateRootsFor);
        failureIntent.putExtra("time_until_give_up_seconds", timePassed);
        this.sendBroadcast(failureIntent);
        return;
      }
    }
  }
}