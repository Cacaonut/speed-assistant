package me.cacaonut.speedassistant.classes;

import android.content.Context;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public enum Weekday {
    Mo, Tu, We, Th, Fr, Sa, Su;

    public String toLocalizedString(Context context) {
        String packageName = context.getPackageName();
        int resId = context.getResources().getIdentifier(name(), "string", packageName);
        return context.getString(resId);
    }

    public boolean isInBetween(Weekday start, Weekday end) {
        return (start.ordinal() <= end.ordinal() && ordinal() >= start.ordinal() && ordinal() <= end.ordinal())
                || (start.ordinal() > end.ordinal() && (ordinal() <= end.ordinal() || ordinal() >= start.ordinal()));
    }

    public static Weekday fromDate(LocalDateTime date) {
        String name = date.format(DateTimeFormatter.ofPattern("EE", Locale.ENGLISH));
        name = name.substring(0, name.length() - 1);
        return Weekday.valueOf(name);
    }
}
