package com.hmgmbh.netzfrequenzwatch.ui.main;

import androidx.lifecycle.ViewModelProvider;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.hmgmbh.netzfrequenzwatch.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TimelineFragment extends Fragment implements OnChartValueSelectedListener {

    public static final String TAG = "TimelineFragment";

    private TimelineViewModel mViewModel;
    private LineChart chart;
    protected Typeface tfLight;
    private final float MAXFREQ =  51f; //51.6f;
    private final float MINFREQ =  49f; //47.5f;
    private Boolean nightMode = false;

    public static TimelineFragment newInstance() {
        return new TimelineFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(TimelineViewModel.class);
        nightMode = (getContext().getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()");

//        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        View view = inflater.inflate(R.layout.timeline_fragment, container, false);

        {   // // Chart Style // //
            chart = view.findViewById(R.id.chart1);

            // background color
//            chart.setBackgroundColor(Color.WHITE);

            // disable description text
            chart.getDescription().setEnabled(false);

            // enable touch gestures
            chart.setTouchEnabled(true);

            // set listeners
            chart.setOnChartValueSelectedListener(this);
            chart.setDrawGridBackground(false);

            // create marker to display box when values are selected
            ChartMarkerView mv = new ChartMarkerView(this.getContext(), R.layout.chart_marker_view,
                    mViewModel);

            // Set the marker to the chart
            mv.setChartView(chart);
            chart.setMarker(mv);

            // enable scaling and dragging
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            // chart.setScaleXEnabled(true);
            // chart.setScaleYEnabled(true);

            // force pinch zoom along both axis
            chart.setPinchZoom(true);
        }

        XAxis xAxis;
        {   // // X-Axis Style // //
            xAxis = chart.getXAxis();

            // vertical grid lines
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
//            xAxis.setTypeface(tfLight);
            xAxis.setTextSize(10f);
//            xAxis.setTextColor(Color.WHITE);
            xAxis.setDrawAxisLine(false);
            xAxis.setDrawGridLines(true);
            xAxis.setCenterAxisLabels(true);
            xAxis.setGranularity(5f); // 3 sec
            if (nightMode) {
                xAxis.setTextColor(Color.WHITE);
            }

            xAxis.setValueFormatter(new ValueFormatter() {

                private final SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);

                @Override
                public String getFormattedValue(float value) {
                    int index = (int) value;
                    long ts = mViewModel.getTimestampForIndex(index);
                    if (ts != 0) {

                        String format = mFormat.format(new Date(ts));
                        return format;
                    }
                    else {
                        return "";
                    }
                }
            });

        }

        YAxis yAxis;
        {   // // Y-Axis Style // //
            yAxis = chart.getAxisLeft();

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(false);

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f);

            // axis range
            yAxis.setAxisMaximum(MAXFREQ);
            yAxis.setAxisMinimum(MINFREQ);
            if (nightMode) {
                yAxis.setTextColor(Color.WHITE);
            }
        }


        {   // // Create Limit Lines // //
            LimitLine llXAxis = new LimitLine(9f, "Index 10");
            llXAxis.setLineWidth(1f);
            llXAxis.enableDashedLine(10f, 10f, 0f);
            llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            llXAxis.setTextSize(10f);

            LimitLine ll1 = new LimitLine(50.2f, "Warnung");
            ll1.setLineWidth(1f);
            ll1.enableDashedLine(10f, 10f, 0f);
            ll1.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            ll1.setTextSize(10f);
            ll1.setLineColor(Color.MAGENTA);
            ll1.setTextColor(Color.MAGENTA);

            LimitLine ll2 = new LimitLine(49.8f, "Warnung");
            ll2.setLineWidth(1f);
            ll2.enableDashedLine(10f, 10f, 0f);
            ll2.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            ll2.setTextSize(10f);
            ll2.setLineColor(Color.MAGENTA);
            ll2.setTextColor(Color.MAGENTA);

            LimitLine llErrH = new LimitLine(50.5f, "Fehler");
            llErrH.setLineWidth(1f);
            llErrH.enableDashedLine(10f, 10f, 0f);
            llErrH.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
            llErrH.setTextSize(10f);
            llErrH.setLineColor(Color.RED);
            llErrH.setTextColor(Color.RED);

            LimitLine llErrL = new LimitLine(49.5f, "Fehler");
            llErrL.setLineWidth(1f);
            llErrL.enableDashedLine(10f, 10f, 0f);
            llErrL.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
            llErrL.setTextSize(10f);
            llErrL.setLineColor(Color.RED);
            llErrL.setTextColor(Color.RED);

            // draw limit lines behind data instead of on top
            yAxis.setDrawLimitLinesBehindData(true);
            xAxis.setDrawLimitLinesBehindData(true);

            // add limit lines
            yAxis.addLimitLine(ll1);
            yAxis.addLimitLine(ll2);
            yAxis.addLimitLine(llErrH);
            yAxis.addLimitLine(llErrL);
        }

        return view;
    }

    private void setData(int count) {

        ArrayList<Entry> values = new ArrayList<>();

        for (int i = 0; i < count; i++) {

            float val = (float) (Math.random() * (MAXFREQ-MINFREQ)) + MINFREQ;
            values.add(new Entry(i, val));
        }

        setData(values);
    }

    private void setData(ArrayList<Entry> values) {
        LineDataSet set1;

        if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        } else {

            set1 = createSet(values);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);

            // set data
            chart.setData(data);
        }

        // draw points over time
        chart.animateX(1500);

        // get the legend (only possible after setting data)
        Legend l = chart.getLegend();

        // draw legend entries as lines
        l.setForm(Legend.LegendForm.LINE);
    }

    private LineDataSet createSet(ArrayList<Entry> values) {

        LineDataSet set1;
        if (values != null) {
            set1 = new LineDataSet(values, "aktuelle Netzfrequenz");
        } else {
            set1 = new LineDataSet(null, "aktuelle Netzfrequenz");
        }

        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setDrawIcons(false);

        set1.setDrawValues(false);
        set1.setDrawFilled(false);

        // draw dashed line
        set1.enableDashedLine(10f, 5f, 0f);

        // black lines and points
        if (nightMode) {
            set1.setColor(Color.WHITE);
            set1.setCircleColor(Color.WHITE);
        }
        else {
            set1.setColor(Color.BLACK);
            set1.setCircleColor(Color.BLACK);
        }

        // line thickness and point size
        set1.setLineWidth(1f);
        set1.setCircleRadius(2f);

        // draw points as solid circles
        set1.setDrawCircleHole(false);

        // customize legend entry
        set1.setFormLineWidth(1f);
        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
        set1.setFormSize(15.f);

        // text size of values
        set1.setValueTextSize(9f);

        // draw selection line as dashed
        set1.enableDashedHighlightLine(10f, 5f, 0f);

        // set color of filled area
//        set1.setFillColor(Color.BLACK);


        return set1;
    }

    private void addEntry(Entry entry) {

        LineData data = chart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet(null);
                data.addDataSet(set);
            }

            data.addEntry(entry, 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            chart.notifyDataSetChanged();

            // limit the number of visible entries
            chart.setVisibleXRangeMaximum(120);
            // chart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            chart.moveViewToX(data.getEntryCount());
            chart.getLegend().setEnabled(false);

            // this automatically refreshes the chart (calls invalidate())
            // chart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // TODO: Use the ViewModel
        Log.i(TAG, "onActivityCreated()");
        setData(mViewModel.getEntries());

        mViewModel.latestEntry.observe(getViewLifecycleOwner(), entry -> {
            // update UI
            Log.d("TimelineFragment", "add entry " + entry.toString());
            addEntry(entry);
        });
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOW HIGH", "low: " + chart.getLowestVisibleX() + ", high: " + chart.getHighestVisibleX());
        Log.i("MIN MAX", "xMin: " + chart.getXChartMin() + ", xMax: " + chart.getXChartMax() + ", yMin: " + chart.getYChartMin() + ", yMax: " + chart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }
}