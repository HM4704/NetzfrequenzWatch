package com.hmgmbh.netzfrequenzwatch.ui.main;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.github.mikephil.charting.data.Entry;
import com.hmgmbh.netzfrequenzwatch.data.DataRepository;
import com.hmgmbh.netzfrequenzwatch.data.FreqUpdate;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class TimelineViewModel extends ViewModel implements FreqUpdate {

    private DataRepository mRepo = DataRepository.getInstance();
    private ArrayList<Entry> entries = new ArrayList<Entry>();
    private int index = 0;
    public MutableLiveData<Entry> latestEntry = new MutableLiveData<>();

    public TimelineViewModel() {
        mRepo.frequStatus().registerObserver(this);
        entries.clear();
        mRepo.frequStatus().getEntries().forEach((entry) -> {
            entries.add(new Entry(index, entry.getVal().floatValue()));
            index++;
        });
    }

    public ArrayList<Entry> getEntries() {
        return entries;
    }

    public long getTimestampForIndex(int index) {
        return mRepo.frequStatus().getTimestampForIndex(index);
    }

    @Override
    public void actFreqChanged(Double val, long timestamp) {
        Entry entry = new Entry(entries.size(), val.floatValue());
        // wird in TimelineFragment geadded
//        entries.add(entry);
//        latestEntry.postValue(entry);
        Log.d("TimelineViewModel", "add entry " + entry.toString());
    }

    @Override
    protected void onCleared() {
        mRepo.frequStatus().unregisterObserver(this);
        Log.d("TimelineViewModel", "onCleared ");
    }

}