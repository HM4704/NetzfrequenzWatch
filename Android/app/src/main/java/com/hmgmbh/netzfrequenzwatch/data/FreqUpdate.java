package com.hmgmbh.netzfrequenzwatch.data;

public interface FreqUpdate {
    void actFreqChanged(Double val, long timestamp);
}
