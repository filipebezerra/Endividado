package com.github.filipebezerra.endividado;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.client.Firebase;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    private DebtAdapter mDebtAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mDebtAdapter = new DebtAdapter());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.isCheckable()) {
            item.setChecked(!item.isChecked());
        }
        switch (item.getItemId()) {
            case R.id.all:
                mDebtAdapter.clearFilter();
                return true;
            case R.id.current_month:
                mDebtAdapter.filterByMonth(DateUtils.getCurrentMonthNumber());
                return true;
            case R.id.another_month:
                new MaterialDialog.Builder(this)
                        .title("Escolha o mês")
                        .items(R.array.months)
                        .itemsCallbackSingleChoice(-1,
                                new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View view,
                                            int which, CharSequence text) {
                                        try {
                                            mDebtAdapter.filterByMonth(
                                                    DateUtils.getMonthNumber(text.toString()));
                                        } catch (ParseException e) {
                                            Log.e("Endividado", e.getMessage());
                                        }
                                        return true;
                                    }
                                })
                        .positiveText("Selecionar")
                        .show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.fab)
    public void newDebt() {
        startActivity(new Intent(this, NewDebtActivity.class));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }
}
