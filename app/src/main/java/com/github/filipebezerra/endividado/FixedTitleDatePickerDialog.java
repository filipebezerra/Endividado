package com.github.filipebezerra.endividado;

import android.app.DatePickerDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.DatePicker;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 23/10/2015
 * @since #
 */
public class FixedTitleDatePickerDialog extends DatePickerDialog {
    private CharSequence mTitle;

    public FixedTitleDatePickerDialog(Context context,
            OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    public void setFixedTitle(@NonNull CharSequence title) {
        mTitle = title;
        setTitle(title);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        setTitle(mTitle);
    }
}
