package com.example.baza;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateValueFormatter extends ValueFormatter implements IAxisValueFormatter {

    private SimpleDateFormat dateFormat;

    public DateValueFormatter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()); // Format daty
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        Date date = new Date((long) value); // Przekształcenie wartości na datę
        return dateFormat.format(date); // Zwrócenie daty w odpowiednim formacie
    }
}
