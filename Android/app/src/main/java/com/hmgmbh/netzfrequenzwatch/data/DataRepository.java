package com.hmgmbh.netzfrequenzwatch.data;

public class DataRepository {

    private static DataRepository sInstance;

    private FrequStatus mFrequStatus = new FrequStatus();

    private DataRepository() {
    }

    public static DataRepository getInstance() {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository();
                }
            }
        }
        return sInstance;
    }

    public FrequStatus frequStatus() {
        return mFrequStatus;
    }
}
