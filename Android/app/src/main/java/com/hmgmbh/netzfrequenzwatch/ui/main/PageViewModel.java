package com.hmgmbh.netzfrequenzwatch.ui.main;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.hmgmbh.netzfrequenzwatch.data.DataRepository;
import com.hmgmbh.netzfrequenzwatch.data.FreqState;

public class PageViewModel extends ViewModel {

    private DataRepository mRepo = DataRepository.getInstance();

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mIndex, new Function<Integer, String>() {
        @Override
        public String apply(Integer input) {
            return "Hello world from section: " + input;
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<String> getText() {
        return mText;
    }

    public LiveData<String> getActFreq() {
        return mRepo.frequStatus().getActFreq();
    }

    public LiveData<FreqState> getState() {
        return mRepo.frequStatus().getState();
    }

    public LiveData<Integer> getWarnings() {
        return mRepo.frequStatus().getWarnings();
    }

    public LiveData<Integer> getErrors() {
        return mRepo.frequStatus().getErrors();
    }

    public LiveData<Integer> getBlackout() {
        return mRepo.frequStatus().getBlackout();
    }
    public LiveData<Integer> getNotNet() {
        return mRepo.frequStatus().getNotNet();
    }

    public LiveData<Double> getMinFreq() {
        return mRepo.frequStatus().getMinFreq();
    }
    public LiveData<Double> getMaxFreq() {
        return mRepo.frequStatus().getMaxFreq();
    }

    public void testSetFreq(String freq) {
        mRepo.frequStatus().setFreq(new String[] {freq, freq}, "1111111");
    }
}