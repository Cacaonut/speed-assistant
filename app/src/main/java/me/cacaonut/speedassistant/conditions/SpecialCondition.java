package me.cacaonut.speedassistant.conditions;

import android.content.Context;

public class SpecialCondition implements Condition {

    private final int stringId;
    private final boolean applies;

    public SpecialCondition(int stringId, boolean applies) {
        this.stringId = stringId;
        this.applies = applies;
    }

    @Override
    public String toPrettyString(Context context, String displayMode) {
        return context.getString(stringId);
    }

    @Override
    public boolean applies() {
        return applies;
    }

    @Override
    public boolean isApplicable() {
        return false;
    }
}
