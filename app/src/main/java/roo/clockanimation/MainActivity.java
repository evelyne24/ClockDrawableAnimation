package roo.clockanimation;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Locale;

import roo.clockanimation.PlusMinusLayout.OnChangeListener;

public class MainActivity extends AppCompatActivity {

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd MMM HH:mm")
            .withLocale(Locale.getDefault())
            .withZone(DateTimeZone.getDefault());

    private final LocalDateTime now = LocalDateTime.now().withTime(0, 0, 0, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        final ClockDrawable clockDrawable = new ClockDrawable(getResources());
        clockDrawable.setAnimateDays(false);
        ImageView imageView = (ImageView) findViewById(R.id.image);
        imageView.setImageDrawable(clockDrawable);

        final TextView dateTimeView = (TextView) findViewById(R.id.dateTime);
        dateTimeView.setText(DTF.print(now));

        final PlusMinusLayout plusMinusLayout = (PlusMinusLayout) findViewById(R.id.test);
        plusMinusLayout.setListener(new OnChangeListener() {
            @Override public void onChange(int days, int hours, int minutes) {
                LocalDateTime current = now.plusDays(days).plusHours(hours).plusMinutes(minutes);
                dateTimeView.setText(DTF.print(current));
                clockDrawable.start(current);
            }
        });

        final View reset = findViewById(R.id.reset);
        reset.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View view) {
                plusMinusLayout.reset();
                dateTimeView.setText(DTF.print(now));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
