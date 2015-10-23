package com.github.filipebezerra.endividado;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.support.design.widget.Snackbar.LENGTH_LONG;
import static android.text.TextUtils.isEmpty;
import static android.view.inputmethod.EditorInfo.IME_ACTION_DONE;
import static android.view.inputmethod.EditorInfo.IME_ACTION_NEXT;

public class NewDebtActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener {
    private static final Firebase sFirebaseRef = new Firebase(AppUtils.FIREBASE);

    private static DecimalFormat sDecimalFormat = new DecimalFormat();

    @Bind(R.id.container) CoordinatorLayout mRootLayout;

    @Bind(R.id.original_amount_helper) TextInputLayout mOriginalAmountHelper;
    @Bind(R.id.original_amount) EditText mOriginalAmountView;

    @Bind(R.id.related_to_helper) TextInputLayout mRelatedToHelper;
    @Bind(R.id.related_to) EditText mRelatedToView;

    @Bind(R.id.creditor_helper) TextInputLayout mCreditorHelper;
    @Bind(R.id.creditor) EditText mCreditorView;

    @Bind(R.id.due_date_helper) TextInputLayout mDueDateHelper;
    @Bind(R.id.due_date) EditText mDueDateView;

    private Set<View> mViewsWithErrors = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_debt);
        ButterKnife.bind(this);

        sDecimalFormat.setMinimumFractionDigits(2);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_white_24dp);
        }

        mOriginalAmountView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                final String value = mOriginalAmountView.getText().toString();
                if (actionId == IME_ACTION_NEXT && !isEmpty(value)) {
                    mOriginalAmountView.setText(sDecimalFormat.format(Double.valueOf(value)));
                }
                return false;
            }
        });

        EditText creditor = ButterKnife.findById(this, R.id.creditor);
        creditor.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == IME_ACTION_DONE && isEmpty(mDueDateView.getText())) {
                    final DatePickerFragment picker = DatePickerFragment.newInstance(NewDebtActivity.this);
                    picker.show(getSupportFragmentManager(), "datePicker");
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_debt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.done:
                save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void removeError(@NonNull View errorView) {
        if (errorView instanceof TextView) {
            ((TextView) errorView).setText("");
        } else if (errorView instanceof TextInputLayout) {
            errorView.setEnabled(false);
            ((TextInputLayout) errorView).setError("");
        }
    }

    public void removeErrorInView(@NonNull View errorView) {
        if (mViewsWithErrors.remove(errorView)) {
            removeError(errorView);
        }
    }

    public void clearErrorsInViews() {
        if (hasErrorsInView()) {
            for (Iterator<View> iterator = mViewsWithErrors.iterator(); iterator.hasNext(); ) {
                final View view = iterator.next();
                removeError(view);
                iterator.remove();
            }
        }
    }

    public void enableErrorInView(@NonNull View errorView, @NonNull String error) {
        if (mViewsWithErrors.add(errorView)) {
            if (errorView instanceof TextView) {
                ((TextView) errorView).setText(error);
            } else if (errorView instanceof TextInputLayout) {
                errorView.setEnabled(true);
                ((TextInputLayout) errorView).setError(error);
            }
        }
    }

    public boolean hasErrorsInView() {
        return mViewsWithErrors.size() != 0;
    }

    private void save() {
        clearErrorsInViews();

        final String originalAmount = mOriginalAmountView.getText().toString();
        final String relatedTo = mRelatedToView.getText().toString();
        final String creditor = mCreditorView.getText().toString();
        final String dueDate = mDueDateView.getText().toString();

        if (isEmpty(originalAmount)) {
            enableErrorInView(mOriginalAmountHelper, "Valor original da dívida é requerido");
        }

        if (isEmpty(relatedTo)) {
            enableErrorInView(mRelatedToHelper, "Referente à é requerido");
        }

        if (isEmpty(creditor)) {
            enableErrorInView(mCreditorHelper, "Credor é requerido");
        }

        if (isEmpty(dueDate)) {
            enableErrorInView(mDueDateHelper, "Vencimento é requerido");
        }

        if (!hasErrorsInView()) {
            try {
                final long dateInMillis = DateUtils.getDateInMillis(dueDate);
                final Debt newDebt = new Debt(sDecimalFormat.parse(originalAmount).doubleValue(),
                        relatedTo, creditor, dateInMillis);
                sFirebaseRef.child(Debt.CHILD).push().setValue(newDebt,
                        new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null) {
                                    Snackbar.make(mRootLayout, "Falha ao salvar dados. " +
                                            firebaseError.getMessage(), LENGTH_LONG).
                                            show();
                                } else {
                                    final AtomicBoolean mActionClicked = new AtomicBoolean(false);

                                    Snackbar.
                                            make(mRootLayout, "Dívida registrada com sucesso",
                                                    LENGTH_LONG).
                                            setAction("Inserir outra", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    mActionClicked.set(true);

                                                    clearErrorsInViews();

                                                    mOriginalAmountView.setText("");
                                                    mRelatedToView.setText("");
                                                    mCreditorView.setText("");
                                                    mDueDateView.setText("");
                                                    mOriginalAmountView.requestFocus();
                                                }
                                            }).
                                            setCallback(new Snackbar.Callback() {
                                                @Override
                                                public void onDismissed(Snackbar snackbar,
                                                        int event) {
                                                    if (!mActionClicked.get()) {
                                                        finish();
                                                    }
                                                }
                                            }).
                                            show();
                                }
                            }
                        });
            } catch (ParseException e) {
                Log.e("Endividado", e.getMessage());
            }
        }
    }

    @OnClick(R.id.due_date)
    public void requestDueDate() {
        final DatePickerFragment picker = DatePickerFragment.newInstance(NewDebtActivity.this);
        picker.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, monthOfYear);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        mDueDateView.setText(DateUtils.getDateFormat().format(calendar.getTime()));
    }

    public static class DatePickerFragment extends DialogFragment {
        @Nullable private DatePickerDialog.OnDateSetListener mCallback;

        public static DatePickerFragment newInstance(@Nullable DatePickerDialog.OnDateSetListener callback) {
            final DatePickerFragment pickerFragment = new DatePickerFragment();
            pickerFragment.mCallback = callback;
            return pickerFragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar calendar = Calendar.getInstance();
            final int year = calendar.get(Calendar.YEAR);
            final int month = calendar.get(Calendar.MONTH);
            final int day = calendar.get(Calendar.DAY_OF_MONTH);

            final FixedTitleDatePickerDialog dialog = new FixedTitleDatePickerDialog(
                    getActivity(), mCallback, year, month, day);
            dialog.setFixedTitle("Selecione o vencimento");
            return dialog;
        }
    }
}
