package com.duong_21011224.btl;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.List;

public class CustomMarkerView extends MarkerView {

    private TextView tvValue;
    private List<String> timestamps;  // Danh sách thời gian tương ứng với Entry

    public CustomMarkerView(Context context, int layoutResource, List<String> timestamps) {
        super(context, layoutResource);
        this.timestamps = timestamps;
        tvValue = findViewById(R.id.tvValue);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        int index = (int) e.getX();
        String time = (index < timestamps.size()) ? timestamps.get(index) : "N/A";
        String value = String.format("%.1f", e.getY());

        tvValue.setText("Giá trị: " + value + "\nThời gian: " + time);
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth() / 2f), -getHeight());
    }
}

