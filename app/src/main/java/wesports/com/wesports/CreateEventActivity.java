package wesports.com.wesports;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.ParsePush;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


public class CreateEventActivity extends AppCompatActivity implements
        TimePickerDialog.OnTimeSetListener {

  private TextView mLocationButton;
  private TextView mTimeButton;
  private EditText mDetailsEdit;

  private double latitude;
  private double longitude;

  private Place place;
  private Calendar cal;
  private Spinner typeSpinner;
  private Spinner dateSpinner;

  private int timeSeconds;
  private int dateSeconds;

  protected static final String TAG = "HomeActivity";
  private static final int PLACE_PICKER_REQUEST = 1;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_create_game);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowHomeEnabled(true);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setTitle(R.string.create_event);
    getSupportActionBar().setElevation(10);

    mTimeButton = (TextView) findViewById(R.id.time_button);
    mLocationButton = (TextView) findViewById(R.id.location_button);
    mDetailsEdit = (EditText) findViewById(R.id.details_edit);

    // Current date and time.
    cal = Calendar.getInstance();
    Date date = cal.getTime();
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa");
    mTimeButton.setText(timeFormat.format(date));

    typeSpinner = (Spinner) findViewById(R.id.game_spinner);
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
            R.array.games_array, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    typeSpinner.setAdapter(adapter);

    dateSpinner = (Spinner) findViewById(R.id.date_spinner);
    ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(this,
            R.array.date_array, android.R.layout.simple_spinner_item);
    dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    dateSpinner.setAdapter(dateAdapter);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_create_event, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      // Back button.
      case android.R.id.home:
        finish();
        return true;
      case R.id.action_create:
        createGame();
    }

    return super.onOptionsItemSelected(item);
  }

  public void locationSelect(View view) {
    try {
      PlacePicker.IntentBuilder intentBuilder =
              new PlacePicker.IntentBuilder();
      Intent intent = intentBuilder.build(this);
      startActivityForResult(intent, PLACE_PICKER_REQUEST);

    } catch (GooglePlayServicesRepairableException e) {
      e.printStackTrace();
    } catch (GooglePlayServicesNotAvailableException e) {
      e.printStackTrace();
    }
  }

  private void createGame() {
    String type = typeSpinner.getSelectedItem().toString();
    String location = mLocationButton.getText().toString();
    boolean tomorrow = dateSpinner.getSelectedItem().toString().equals(getResources().getString(R.string.tomorrow));
    if (tomorrow) {
      // Increment by 1 dat.
      cal.add(Calendar.DATE, 1);
    }

    Event event = new Event();
    event.setUuidString();
    event.setDate(cal.getTime());
    event.setDetails(mDetailsEdit.getText().toString());
    event.setLocation(location);
    event.setType(type);
    event.setLat(String.valueOf(latitude));
    event.setLon(String.valueOf(longitude));
    event.setNumAttending(1);
    event.saveInBackground();

    // Send push notification to channel.
    ParsePush push = new ParsePush();
    push.setChannel(type);
    if (tomorrow) {
      push.setMessage(type + " tomorrow at " + location + "!");
    } else {
      push.setMessage(type + " today at " + location + "!");
    }
    push.sendInBackground();

    finish();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == PLACE_PICKER_REQUEST
            && resultCode == Activity.RESULT_OK) {
      place = PlacePicker.getPlace(data, this);
      latitude = place.getLatLng().latitude;
      longitude = place.getLatLng().longitude;
      final CharSequence name = place.getName();
      mLocationButton.setText(name);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  public void setTime(View view) {
    TimePickerDialog tpd = TimePickerDialog.newInstance(
            CreateEventActivity.this,
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
    );
    tpd.setThemeDark(true);
    tpd.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialogInterface) {
        Log.d("TimePicker", "Dialog was cancelled");
      }
    });
    tpd.show(getFragmentManager(), "Timepickerdialog");
  }

  @Override
  public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
    cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
    cal.set(Calendar.MINUTE, minute);
    Date date = cal.getTime();
    SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa", Locale.CANADA);
    mTimeButton.setText(timeFormat.format(date));
  }
}
