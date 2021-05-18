package me.cacaonut.speedassistant.conditions;

import android.content.Context;

import java.time.LocalDateTime;
import java.time.LocalTime;

import me.cacaonut.speedassistant.classes.Weekday;

public class TimeCondition implements Condition {
    private final Weekday startDay;
    private final Weekday endDay;
    private final int startHours;
    private final int startMinutes;
    private final int endHours;
    private final int endMinutes;

    public TimeCondition(Weekday startDay, Weekday endDay, int startHours, int startMinutes, int endHours, int endMinutes) {
        this.startDay = startDay;
        this.endDay = endDay;
        this.startHours = startHours;
        this.startMinutes = startMinutes;
        this.endHours = endHours;
        this.endMinutes = endMinutes;
    }

    public static TimeCondition from(String data) {
        Weekday startDay = null;
        Weekday endDay = null;
        int startHours = -1;
        int startMinutes = -1;
        int endHours = -1;
        int endMinutes = -1;

        String[] parts = data.split(" ");
        for (String part : parts) {
            if (!part.equals("")) {
                if (part.matches("^(Mo|Tu|We|Th|Fr|Sa|So)$")) {
                    startDay = endDay = Weekday.valueOf(part);
                } else if (part.matches("^(Mo|Tu|We|Th|Fr|Sa|So)-(Mo|Tu|We|Th|Fr|Sa|So)$")) {
                    String[] days = part.split("-");
                    startDay = Weekday.valueOf(days[0]);
                    endDay = Weekday.valueOf(days[1]);
                } else if (part.matches("^(\\d\\d:\\d\\d-\\d\\d:\\d\\d)$")) {
                    String[] times = part.split("[-:]");
                    startHours = Integer.parseInt(times[0]);
                    startMinutes = Integer.parseInt(times[1]);
                    endHours = Integer.parseInt(times[2]);
                    endMinutes = Integer.parseInt(times[3]);
                }
            }
        }

        return new TimeCondition(startDay, endDay, startHours, startMinutes, endHours, endMinutes);
    }

    public String toPrettyString(Context context, String displayMode) {
        StringBuilder stringBuilder = new StringBuilder();
        // Weekdays
        if (startDay != null && endDay != null) {
            if (startDay == endDay)
                stringBuilder.append(startDay.toLocalizedString(context));
            else
                stringBuilder.append(startDay.toLocalizedString(context)).append("-").append(endDay.toLocalizedString(context));
            stringBuilder.append("\n");
        }

        // Time
        switch (displayMode) {
            case "de":
                if (startHours != -1) {
                    stringBuilder.append(startHours);
                    if (startMinutes > 0) {
                        stringBuilder.append(":").append(String.format("%02d", startMinutes));
                    }
                    stringBuilder.append("-").append(endHours);
                    if (endMinutes > 0) {
                        stringBuilder.append(":").append(String.format("%02d", endMinutes));
                    }
                    stringBuilder.append(" h");
                }
                break;
            default:
                if (startHours != -1) {
                    stringBuilder.append(String.format("%02d", startHours));
                    stringBuilder.append(":").append(String.format("%02d", startMinutes));
                    if (startDay == null || endDay == null) stringBuilder.append("\n");
                    stringBuilder.append("-").append(String.format("%02d", endHours));
                    stringBuilder.append(":").append(String.format("%02d", endMinutes));
                }
        }
        return stringBuilder.toString();
    }

    public boolean applies() {
        LocalDateTime date = LocalDateTime.now();
        return (startDay == null || endDay == null || Weekday.fromDate(date).isInBetween(startDay, endDay))
                && (startHours == -1 || endHours == -1 || isInBetween(date.toLocalTime()));
    }

    @Override
    public boolean isApplicable() {
        return true;
    }

    private boolean isInBetween(LocalTime now) {
        LocalTime start = LocalTime.of(startHours, startMinutes);
        LocalTime end = LocalTime.of(endHours, endMinutes);
        return (start.isBefore(end) && now.isAfter(start) && now.isBefore(end))
                || (!start.isBefore(end) && (now.isBefore(end) || now.isAfter(start)));
    }
}
