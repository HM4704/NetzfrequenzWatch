package com.hmgmbh.netzfrequenzwatch.data;

import android.util.Log;

import java.util.ArrayList;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class FrequStatus {
    private final String NoFreqVal = "--.--";
    private final MutableLiveData<Integer> countWarning  = new MutableLiveData<Integer>(0);
    private final MutableLiveData<Integer> countError  = new MutableLiveData<Integer>(0);
    private final MutableLiveData<Integer> countBlackout  = new MutableLiveData<Integer>(0);
    private final MutableLiveData<Integer> countNoNet  = new MutableLiveData<Integer>(0);
    private final MutableLiveData<FreqState> status  = new MutableLiveData<FreqState>(FreqState.NoNet);
    private final MutableLiveData<Double> minFreq  = new MutableLiveData<Double>(100.0);
    private final MutableLiveData<Double> maxFreq  = new MutableLiveData<Double>(0.0);
    private final MutableLiveData<String> mActFreq = new MutableLiveData<String>(NoFreqVal);
    private final ArrayList<FreqEntry> mEntries = new ArrayList<>();
    private final ArrayList<FreqUpdate> mObservers = new ArrayList<>();

    public FrequStatus() {
    }

    public void setFreq(String[] values, String timestamp) {
        Double value = new Double(0);
        Long ts = new Long(0);
        String sValue = determineState(values);
        mActFreq.postValue(sValue);

        try {
            value = Double.parseDouble(sValue);
            ts = Long.parseLong(timestamp);
            mEntries.add(new FreqEntry(value, ts));
            for (int i = 0; i < mObservers.size(); i++) {
                mObservers.get(i).actFreqChanged(value, ts);
            }
            Log.d("FrequStatus", "setFreq " + value.toString());
        }
        catch (NumberFormatException e) {
            Log.d("FrequStatus", "setFreq no valid freq!!!");
        }
    }

    public LiveData<String> getActFreq() {
        return mActFreq;
    }

    public LiveData<Integer> getWarnings() {
        return countWarning;
    }

    public LiveData<Integer> getErrors() {
        return countError;
    }

    public LiveData<Integer> getBlackout() {
        return countBlackout;
    }

    public LiveData<Integer> getNotNet() {
        return countNoNet;
    }
    public void incrNoNet() {
        countNoNet.postValue(countNoNet.getValue() + 1);
        status.postValue(FreqState.NoNet);
        mActFreq.postValue(NoFreqVal);
    }

    public LiveData<FreqState> getState() {
        return status;
    }
    public LiveData<Double> getMinFreq() {
        return minFreq;
    }
    public LiveData<Double> getMaxFreq() {
        return maxFreq;
    }
    public ArrayList<FreqEntry> getEntries() {
        return mEntries;
    }

    public long getTimestampForIndex(int index) {
        if (index >= 0 && index < mEntries.size()) {
            return mEntries.get(index).getTimestamp();
        }
        else {
            return 0;
        }
    }

    public void registerObserver(FreqUpdate o) {
        mObservers.add(o);
    }

    public void unregisterObserver(FreqUpdate o) {
        mObservers.remove(o);
    }

    private String determineState(String[] values) {
        if (values.length == 2) {
            status.postValue(evaluate(values[0]));
            return values[0];
        } else {
            status.postValue(FreqState.NoNet);
            countNoNet.postValue(countNoNet.getValue() + 1);
            return "??.??";
        }
    }

    private FreqState evaluate(String sValue) {
        double value = Double.parseDouble(sValue);

        if (value < minFreq.getValue())  {
            minFreq.postValue(value);
        }
        if (value > maxFreq.getValue())  {
            maxFreq.postValue(value);
        }

        if ((value <= 47.50) || (value >=51.50)) {
            countBlackout.postValue(countBlackout.getValue() + 1);
            return FreqState.Blackout;
        }
        if ((value <= 49.50) || (value >=50.50)) {
            countError.postValue(countError.getValue() + 1);
            return FreqState.Error;
        }
        if ((value > 49.80) && (value < 50.20)) {
            return FreqState.Ok;
        }
        countWarning.postValue(countWarning.getValue() + 1);
        return FreqState.Warning;
    }
}
