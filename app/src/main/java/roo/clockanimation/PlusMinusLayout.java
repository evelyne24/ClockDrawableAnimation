package roo.clockanimation;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;

import roo.clockanimation.PlusMinusButton.OnPlusMinusClickListener;

import static android.view.LayoutInflater.from;
import static java.lang.Integer.parseInt;

/**
 * Created by evelina on 15/07/2016.
 */

public class PlusMinusLayout extends LinearLayout implements OnPlusMinusClickListener {

    public interface OnChangeListener {
        void onChange(int days, int hours, int minutes);
    }

    private EditText dayText;
    private EditText hourText;
    private EditText minuteText;

    private PlusMinusButton plusMinusDays;
    private PlusMinusButton plusMinusHours;
    private PlusMinusButton plusMinusMinutes;

    private OnChangeListener listener;

    public PlusMinusLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlusMinusLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    public PlusMinusLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER);
        from(context).inflate(R.layout.plus_minus_date_time_layout, this, true);
        setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        dayText = (EditText) findViewById(R.id.day);
        hourText = (EditText) findViewById(R.id.hour);
        minuteText = (EditText) findViewById(R.id.minute);

        init(dayText);
        init(hourText);
        init(minuteText);

        plusMinusDays = (PlusMinusButton) findViewById(R.id.dayPlusMinus);
        plusMinusHours = (PlusMinusButton) findViewById(R.id.hourPlusMinus);
        plusMinusMinutes = (PlusMinusButton) findViewById(R.id.minPlusMinus);

        plusMinusDays.setListener(this);
        plusMinusHours.setListener(this);
        plusMinusMinutes.setListener(this);
    }

    @Override public void onPlusClicked(View view) {
        if (view == plusMinusDays) {
            increase(dayText);
        } else if (view == plusMinusHours) {
            increase(hourText);
        } else if (view == plusMinusMinutes) {
            increase(minuteText);
        }
    }

    @Override public void onMinusClicked(View view) {
        if (view == plusMinusDays) {
            decrease(dayText);
        } else if (view == plusMinusHours) {
            decrease(hourText);
        } else if (view == plusMinusMinutes) {
            decrease(minuteText);
        }
    }

    public int getDays() {
        return parseValue(dayText);
    }

    public int getHours() {
        return parseValue(hourText);
    }

    public int getMinutes() {
        return parseValue(minuteText);
    }

    public void setListener(OnChangeListener listener) {
        this.listener = listener;
    }

    public void reset() {
        reset(dayText);
        reset(hourText);
        reset(minuteText);
        notifyChangeListener();
    }

    private void init(EditText editText) {
        setText(editText, "0");
        editText.setOnFocusChangeListener(new CustomFocusListener(editText));
    }

    private void reset(EditText editText) {
        editText.setOnFocusChangeListener(null);
        init(editText);
    }

    private void notifyChangeListener() {
        if (listener != null) {
            listener.onChange(getDays(), getHours(), getMinutes());
        }
    }

    private void increase(EditText editText) {
        setText(editText, change(editText, 1));
        notifyChangeListener();
    }

    private void decrease(EditText editText) {
        setText(editText, change(editText, -1));
        notifyChangeListener();
    }

    private static String change(EditText editText, int value) {
        return String.valueOf(parseValue(editText) + value);
    }

    private static int parseValue(EditText editText) {
        return isEmpty(editText) ? 0 : parseInt(getText(editText));
    }

    private static String getText(EditText editText) {
        return editText.getText().toString();
    }

    private static boolean isEmpty(EditText editText) {
        return TextUtils.isEmpty(getText(editText));
    }

    private static boolean isZero(EditText editText) {
        return "0".equals(getText(editText));
    }

    private static void setText(EditText editText, String text) {
        editText.setText(text);
        editText.setSelection(getText(editText).length());
    }


    private class CustomFocusListener implements OnFocusChangeListener {
        private final EditText editText;

        public CustomFocusListener(EditText editText) {
            this.editText = editText;
        }

        @Override public void onFocusChange(View view, boolean hasFocus) {
            if (editText != view) {
                return;
            }

            if (hasFocus && isZero(editText)) {
                setText(editText, "");

            } else if (!hasFocus) {
                if (isEmpty(editText)) {
                    setText(editText, "0");
                }
                notifyChangeListener();
            }
        }
    }

}
