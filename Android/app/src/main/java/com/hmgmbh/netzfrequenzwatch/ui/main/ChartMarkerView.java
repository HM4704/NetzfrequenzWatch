package com.hmgmbh.netzfrequenzwatch.ui.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;
import com.hmgmbh.netzfrequenzwatch.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public class ChartMarkerView extends MarkerView {

    private final TextView tvContent;
    private final TextView tvDate;
    private TimelineViewModel mViewModel = null;
    private final SimpleDateFormat mFormat = new SimpleDateFormat("dd MMM HH:mm:ss", Locale.ENGLISH);

    public ChartMarkerView(Context context, int layoutResource, TimelineViewModel viewModel) {
        super(context, layoutResource);

        tvContent = findViewById(R.id.tvContent);
        tvDate = findViewById(R.id.tvDate);
        mViewModel = viewModel;
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    @Override
    public void refreshContent(Entry e, Highlight highlight) {

        tvContent.setText(Utils.formatNumber(e.getY(), 2, true));

        long ts = mViewModel.getTimestampForIndex((int)e.getX());
        String date = mFormat.format(new Date(ts));
        tvDate.setText(date);

        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2), -(getHeight()+5));
    }
}
