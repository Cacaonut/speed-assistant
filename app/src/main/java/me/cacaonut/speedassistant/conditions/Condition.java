package me.cacaonut.speedassistant.conditions;

import android.content.Context;

public interface Condition {
    String toPrettyString(Context context, String displayMode);
    boolean applies();
    boolean isApplicable();
}
