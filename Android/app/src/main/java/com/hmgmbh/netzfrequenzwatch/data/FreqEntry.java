package com.hmgmbh.netzfrequenzwatch.data;

public class FreqEntry {

    private Double mFreqVal;

    private Long mTimestamp;

    public FreqEntry(Double val, Long timestamp) {
        mFreqVal = val;
        mTimestamp = timestamp;
    }

    public Double getVal() {
        return mFreqVal;
    }

    public Long getTimestamp() {
        return mTimestamp;
    }
}
