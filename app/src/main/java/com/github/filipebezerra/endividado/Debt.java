package com.github.filipebezerra.endividado;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * .
 *
 * @author Filipe Bezerra
 * @version #, 22/10/2015
 * @since #
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Debt {
    public static final String CHILD = "debts";

    @JsonIgnore
    private String key;
    private Double originalAmout;
    private String relatedTo;
    private String creditor;
    private long duoDate;
    private boolean settled;

    public Debt() {
    }

    public Debt(Double originalAmout, String relatedTo, String creditor, long duoDate) {
        this.originalAmout = originalAmout;
        this.relatedTo = relatedTo;
        this.creditor = creditor;
        this.duoDate = duoDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Double getOriginalAmout() {
        return originalAmout;
    }

    public String getRelatedTo() {
        return relatedTo;
    }

    public String getCreditor() {
        return creditor;
    }

    public long getDuoDate() {
        return duoDate;
    }

    public boolean isSettled() {
        return settled;
    }

    public void setSettled(boolean settled) {
        this.settled = settled;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Debt) {
            final Debt anotherDebt = (Debt) obj;

            if (anotherDebt.getKey() != null && this.getKey() != null) {
                return anotherDebt.getKey().equals(this.getKey());
            }
        }
        return false;
    }
}
