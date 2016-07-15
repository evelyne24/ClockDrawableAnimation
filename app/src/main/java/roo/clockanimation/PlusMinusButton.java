package roo.clockanimation;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

/**
 * Created by evelina on 15/07/2016.
 */

public class PlusMinusButton extends LinearLayout implements OnClickListener {

    public interface OnPlusMinusClickListener {
        void onPlusClicked(View view);

        void onMinusClicked(View view);
    }

    private View plusButton;
    private View minusButton;
    private OnPlusMinusClickListener listener;

    public PlusMinusButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlusMinusButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = VERSION_CODES.LOLLIPOP)
    public PlusMinusButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attributeSet) {
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        LayoutInflater.from(context).inflate(R.layout.plus_minus_button, this, true);
        setBackgroundResource(R.drawable.abc_btn_colored_material);
        plusButton = findViewById(R.id.plus);
        minusButton = findViewById(R.id.minus);

        plusButton.setOnClickListener(this);
        minusButton.setOnClickListener(this);
    }

    public void setListener(OnPlusMinusClickListener listener) {
        this.listener = listener;
    }


    @Override public void onClick(View view) {
        if (listener != null) {
            if (view == plusButton) {
                listener.onPlusClicked(this);
            } else if (view == minusButton) {
                listener.onMinusClicked(this);
            }
        }
    }

}
