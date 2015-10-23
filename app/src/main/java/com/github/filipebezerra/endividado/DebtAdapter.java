package com.github.filipebezerra.endividado;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * .
 *
 * @author Fbs
 * @version #, 22/10/2015
 * @since #
 */
public class DebtAdapter extends RecyclerView.Adapter<DebtAdapter.DebtViewHolder> {
    private static final Firebase sFirebaseRef = new Firebase(AppUtils.FIREBASE);
    private List<Debt> mDebtList = new ArrayList<>();
    private List<Debt> mFilteredDebtList = new ArrayList<>();

    private boolean mIsFiltered;

    private static NumberFormat sDecimalFormat = DecimalFormat.getCurrencyInstance();

    public DebtAdapter() {
        sFirebaseRef.child(Debt.CHILD).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                add(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                update(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    @Override
    public DebtViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_debt, parent, false);
        return new DebtViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(DebtViewHolder holder, int position) {
        final Debt debt = mIsFiltered ? mFilteredDebtList.get(position) : mDebtList.get(position);
        holder.originalAmount.setText(sDecimalFormat.format(debt.getOriginalAmout()));
        holder.relatedTo.setText(debt.getRelatedTo());
    }

    @Override
    public int getItemCount() {
        return mIsFiltered ? mFilteredDebtList.size() : mDebtList.size();
    }

    public void filterByMonth(final int month) throws IllegalArgumentException {
        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Invalid month");
        }

        if (!mDebtList.isEmpty()) {
            mIsFiltered = true;
            mFilteredDebtList.clear();
            final Calendar calendar = Calendar.getInstance();

            for (Debt debt : mDebtList) {
                calendar.setTimeInMillis(debt.getDuoDate());
                final int debtMonth = calendar.get(Calendar.MONTH);

                if (debtMonth == month) {
                    mFilteredDebtList.add(debt);
                }
            }

            notifyDataSetChanged();
        }
    }

    private void add(@NonNull final DataSnapshot snapshot) {
        final Debt debt = retrieveDebt(snapshot);
        if (!debt.isSettled()) {
            final int size = mDebtList.size();
            if (mDebtList.add(debt)) {
                notifyItemInserted(size);
            }
        }
    }

    private void update(@NonNull final DataSnapshot snapshot) {
        final Debt debt = retrieveDebt(snapshot);
        if (debt.isSettled()) {
            final int indexOf = mDebtList.indexOf(debt);
            if (mDebtList.remove(debt)) {
                notifyItemRemoved(indexOf);
            }
        }
    }

    private Debt retrieveDebt(@NonNull final DataSnapshot snapshot) {
        final Debt debt = snapshot.getValue(Debt.class);
        debt.setKey(snapshot.getKey());
        return debt;
    }

    public void clearFilter() {
        if (mIsFiltered) {
            mIsFiltered = false;
            mFilteredDebtList.clear();
            notifyDataSetChanged();
        }
    }

    public class DebtViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.original_amount) TextView originalAmount;
        @Bind(R.id.related_to) TextView relatedTo;

        public DebtViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.settle_debt)
        public void settleDebt() {
            Debt debt;
            if (mIsFiltered) {
                debt = mFilteredDebtList.get(getAdapterPosition());
            } else {
                debt = mDebtList.get(getAdapterPosition());
            }

            debt.setSettled(true);

            sFirebaseRef.child(Debt.CHILD).child(debt.getKey()).setValue(debt);
        }
    }
}
