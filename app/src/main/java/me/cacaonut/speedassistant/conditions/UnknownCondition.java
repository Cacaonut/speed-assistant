package me.cacaonut.speedassistant.conditions;

import android.content.Context;

import me.cacaonut.speedassistant.R;

public class UnknownCondition implements Condition {

    public UnknownCondition() {}

    @Override
    public String toPrettyString(Context context, String displayMode) {
        return "???";
    }

    @Override
    public boolean applies() {
        return false;
    }

    @Override
    public boolean isApplicable() {
        return false;
    }
}
