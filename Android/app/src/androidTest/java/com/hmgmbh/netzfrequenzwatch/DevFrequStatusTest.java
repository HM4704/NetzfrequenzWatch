package com.hmgmbh.netzfrequenzwatch;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.hmgmbh.netzfrequenzwatch.data.FreqState;
import com.hmgmbh.netzfrequenzwatch.data.FrequStatus;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith(AndroidJUnit4.class)
public class DevFrequStatusTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void test_ok() {

        FrequStatus fs = new FrequStatus();
        assertEquals(fs.getState().getValue(), FreqState.NoNet);

        fs.setFreq(new String[] {"50.00", "49.00"}, "111111");

        try {
            assertEquals(LiveDataTestUtil.<FreqState>getOrAwaitValue(fs.getState()), FreqState.Ok);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Bereiche 49.80 - 49.49, 50.20 - 50.49   :  gestörter Betrieb
    @Test
    public void test_Warning() {
        FrequStatus fs = new FrequStatus();
        assertEquals(fs.getState().getValue(), FreqState.NoNet);
        assertEquals(fs.getWarnings().getValue(), new Integer(0));
        fs.setFreq(new String[] {"49.79", "49.79"}, "1111111");
        try {
            assertEquals(LiveDataTestUtil.<FreqState>getOrAwaitValue(fs.getState()), FreqState.Warning);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            assertEquals(LiveDataTestUtil.<Integer>getOrAwaitValue(fs.getWarnings()), new Integer(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fs = new FrequStatus();
        assertEquals(fs.getState().getValue(), FreqState.NoNet);
        fs.setFreq(new String[] {"50.21", "50.21"}, "1111111");
        try {
            assertEquals(LiveDataTestUtil.<FreqState>getOrAwaitValue(fs.getState()), FreqState.Warning);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            assertEquals(LiveDataTestUtil.<Integer>getOrAwaitValue(fs.getWarnings()), new Integer(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Bereiche <=49.50, >= 50.50  :  Betrieb ausserhalb Spezifikation
    @Test
    public void test_Error() {
        FrequStatus fs = new FrequStatus();
        assertEquals(fs.getState().getValue(), FreqState.NoNet);
        assertEquals(fs.getErrors().getValue(), new Integer(0));
        fs.setFreq(new String[] {"49.5", "49.5"}, "111111");
        try {
            assertEquals(LiveDataTestUtil.<FreqState>getOrAwaitValue(fs.getState()), FreqState.Error);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            assertEquals(LiveDataTestUtil.<Integer>getOrAwaitValue(fs.getErrors()), new Integer(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fs = new FrequStatus();
        assertEquals(fs.getState().getValue(), FreqState.NoNet);
        fs.setFreq(new String[] {"50.50", "50.51"}, "1111111");
        try {
            assertEquals(LiveDataTestUtil.<FreqState>getOrAwaitValue(fs.getState()), FreqState.Error);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Bereiche <= 47,50, >= 51,50
    @Test
    public void test_Blackout() {
        FrequStatus fs = new FrequStatus();
        assertEquals(fs.getState().getValue(), FreqState.NoNet);
        assertEquals(fs.getBlackout().getValue(), new Integer(0));
        fs.setFreq(new String[] {"47.5", "47.5"}, "11111");
        try {
            assertEquals(LiveDataTestUtil.<FreqState>getOrAwaitValue(fs.getState()), FreqState.Blackout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            assertEquals(LiveDataTestUtil.<Integer>getOrAwaitValue(fs.getBlackout()), new Integer(1));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        fs = new FrequStatus();
        assertEquals(fs.getState().getValue(), FreqState.NoNet);
        fs.setFreq(new String[] {"51.50", "51.51"}, "1111111");
        try {
            assertEquals(LiveDataTestUtil.<FreqState>getOrAwaitValue(fs.getState()), FreqState.Blackout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
