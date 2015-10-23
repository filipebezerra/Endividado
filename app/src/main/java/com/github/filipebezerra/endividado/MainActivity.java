package com.github.filipebezerra.endividado;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.squareup.otto.Subscribe;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity {
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    private DebtAdapter mDebtAdapter;
    private MaterialDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_activity_main);
        ButterKnife.bind(this);

        Toolbar toolbar = ButterKnife.findById(this, R.id.toolbar);
        setSupportActionBar(toolbar);

        Firebase.setAndroidContext(this);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(getLayoutManagerForCurrentScreenOrientation(null));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);

        if (mDebtAdapter == null) {
            mRecyclerView.setAdapter(mDebtAdapter = new DebtAdapter());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
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

        getLayoutManagerForCurrentScreenOrientation(newConfig);
    }

    private RecyclerView.LayoutManager getLayoutManagerForCurrentScreenOrientation(
            @Nullable Configuration configuration) {

        int currentOrientation;
        if (configuration == null) {
            currentOrientation = getResources().getConfiguration().orientation;
        } else {
            currentOrientation = configuration.orientation;
        }

        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return new GridLayoutManager(this, 2);
        } else {
            return new LinearLayoutManager(this);
        }
    }

    @Subscribe
    public void onLoadingNotificationEvent(LoadingNotificationEvent event) {
        switch (event.getLoadingType()) {
            case LOADING_STARTED:
                mDialog = DialogUtils.showIndeterminateProgress(this, null, "Carregando suas dívidas...");
                break;
            case LOADING_COMPLETE:
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
                break;
            case LOADING_FAILED:
                break;
        }
    }
}
