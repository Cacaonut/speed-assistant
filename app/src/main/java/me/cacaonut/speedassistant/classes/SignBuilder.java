package me.cacaonut.speedassistant.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.res.ResourcesCompat;

import me.cacaonut.speedassistant.R;
import timber.log.Timber;

public class SignBuilder {
    public static Bitmap buildSign(Context context, int speedLimit, String displayMode, int textColor) {
        if (speedLimit < 0 || speedLimit >= 999)
            return buildSpecialSign(context, speedLimit, displayMode, textColor);
        else return buildStandardSign(context, speedLimit, displayMode, textColor);
    }

    private static Bitmap buildStandardSign(Context context, int speedLimit, String displayMode, int textColor) {
        switch (displayMode) {
            case "de":
                return buildStandardSign(context, (int) speedLimit + "", R.drawable.speed_sign_de, Color.BLACK, 57, 50, R.font.din1451, R.font.din1451_condensed, 500);
            case "us":
                return buildStandardSign(context, (int) speedLimit + "", R.drawable.speed_sign_us, Color.BLACK, 45, 73, R.font.highway_gothic_wide, R.font.highway_gothic, 500);
            default:
                return buildTextSign(speedLimit + "", textColor, 60, 2000);
        }
    }

    private static Bitmap buildStandardSign(Context context, String text, int emptySign, int textColor, int textSize, int textY, int textFont, int textFont3Digit, int textWeight) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), emptySign, context.getTheme());
        if (drawable != null) {
            Bitmap sign = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sign);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTextSize(textSize * canvas.getHeight() / 100f);
            paint.setTextAlign(Paint.Align.CENTER);
            Typeface font = Typeface.DEFAULT;
            if (text.length() >= 3 && textFont3Digit != -1)
                font = ResourcesCompat.getFont(context, textFont3Digit);
            else if (textFont != -1) font = ResourcesCompat.getFont(context, textFont);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                paint.setTypeface(Typeface.create(font, textWeight, false));
            else paint.setTypeface(font);
            Rect bounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), bounds);
            int height = bounds.height();
            int y = (int) (textY * canvas.getHeight() / 100f + 0.5f * height);
            canvas.drawText(text, 0.5f * canvas.getWidth(), y, paint);
            return sign;
        }
        return null;
    }

    private static Bitmap buildTextSign(String text, int textColor, int textSize, int bitmapHeight) {
        Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
        Bitmap sign = Bitmap.createBitmap(2000, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(sign);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        Paint paint = new Paint();
        paint.setColor(textColor);
        paint.setTextSize(textSize * canvas.getHeight() / 100f);
        paint.setTextAlign(Paint.Align.CENTER);
        Typeface font = Typeface.DEFAULT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            paint.setTypeface(Typeface.create(font, 300, false));
        else paint.setTypeface(font);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        int height = bounds.height();
        int y = (int) (0.5f * canvas.getHeight() + 0.5f * height);
        canvas.drawText(text, 0.5f * canvas.getWidth(), y, paint);
        return sign;
    }

    private static Bitmap buildSpecialSign(Context context, int speedLimit, String displayMode, int textColor) {
        switch (displayMode) {
            case "de":
                if (speedLimit == -2) return buildDrawableSign(context, R.drawable.walk_sign_de);
                else if (speedLimit == 999)
                    return buildDrawableSign(context, R.drawable.none_speed_sign_de);
                else
                    return buildStandardSign(context, "?", R.drawable.speed_sign_de, Color.BLACK, 57, 50, R.font.din1451, R.font.din1451_condensed, 500);
            case "us":
                if (speedLimit == -2)
                    return buildStandardSign(context, "walk", R.drawable.speed_sign_us, Color.BLACK, 35, 73, R.font.highway_gothic_wide, R.font.highway_gothic, 500);
                else if (speedLimit == 999)
                    return buildStandardSign(context, "none", R.drawable.speed_sign_us, Color.BLACK, 35, 73, R.font.highway_gothic_wide, R.font.highway_gothic, 500);
                else
                    return buildStandardSign(context, "???", R.drawable.speed_sign_us, Color.BLACK, 40, 73, R.font.highway_gothic_wide, R.font.highway_gothic, 500);
            default:
                String speedLimitStr;
                if (speedLimit == -2)
                    speedLimitStr = context.getResources().getString(R.string.speed_walk);
                else if (speedLimit == 999)
                    speedLimitStr = context.getResources().getString(R.string.speed_none);
                else speedLimitStr = context.getResources().getString(R.string.speed_unknown);

                return buildTextSign(speedLimitStr, textColor, 35, 2000);
        }
    }

    private static Bitmap buildDrawableSign(Context context, int drawableSign) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), drawableSign, context.getTheme());
        if (drawable != null) {
            Bitmap sign = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sign);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return sign;
        }
        return null;
    }

    private static Bitmap buildConditionSign(Context context, String[] lines, String displayMode, int textColor) {
        switch (displayMode) {
            case "de":
                return buildConditionSign(context, lines, R.drawable.condition_sign_de, 50, 35, 50, R.font.din1451, 500);
            case "us":
                return buildConditionSign(context, lines, R.drawable.condition_sign_de, 50, 35, 50, R.font.highway_gothic, 500);
            default:
                return buildConditionTextSign(lines, 60, 40, textColor);
        }
    }

    private static Bitmap buildConditionTextSign(String[] lines, int textSize, int textSizeTwoLines, int textColor) {
        if (lines.length > 0) {
            Drawable drawable = new ColorDrawable(Color.TRANSPARENT);
            Bitmap sign = Bitmap.createBitmap(2000, 1000, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sign);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTextAlign(Paint.Align.CENTER);
            Typeface font = Typeface.DEFAULT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                paint.setTypeface(Typeface.create(font, 500, false));
            else paint.setTypeface(font);
            if (lines.length == 1) {
                paint.setTextSize(textSize * canvas.getHeight() / 100f);
                String text = lines[0];
                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int height = bounds.height();
                int y = (int) (0.5f * canvas.getHeight() + 0.5 * height);
                canvas.drawText(text, 0.5f * canvas.getWidth(), y, paint);
            } else {
                paint.setTextSize(textSizeTwoLines * canvas.getHeight() / 100f);
                String text = lines[0];
                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int height = bounds.height();
                String text2 = lines[1];
                bounds = new Rect();
                paint.getTextBounds(text2, 0, text2.length(), bounds);
                int height2 = bounds.height();
                int combinedHeight = (int) (height + height2 + 0.14f * canvas.getHeight());
                int y = (int) (0.5f * canvas.getHeight() - 0.5 * combinedHeight + height);
                canvas.drawText(text, 0.5f * canvas.getWidth(), y, paint);
                y = (int) (0.5f * canvas.getHeight() + 0.5 * combinedHeight);
                if (text2.length() > 10)
                    paint.setTextSize((textSizeTwoLines - 5) * canvas.getHeight() / 100f);
                canvas.drawText(text2, 0.5f * canvas.getWidth(), y, paint);
            }
            return sign;
        }
        return null;
    }

    private static Bitmap buildConditionSign(Context context, String[] lines, int emptySign, int textSize, int textSizeTwoLines, int textY, int textFont, int textWeight) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), emptySign, context.getTheme());
        if (drawable != null && lines.length > 0) {
            Bitmap sign = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sign);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextAlign(Paint.Align.CENTER);
            Typeface font = ResourcesCompat.getFont(context, textFont);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                paint.setTypeface(Typeface.create(font, textWeight, false));
            else paint.setTypeface(font);
            if (lines.length == 1) {
                paint.setTextSize(textSize * canvas.getHeight() / 100f);
                String text = lines[0];
                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int height = bounds.height();
                int y = (int) (textY * canvas.getHeight() / 100f + 0.5 * height);
                canvas.drawText(text, 0.5f * canvas.getWidth(), y, paint);
            } else {
                paint.setTextSize(textSizeTwoLines * canvas.getHeight() / 100f);
                String text = lines[0];
                Rect bounds = new Rect();
                paint.getTextBounds(text, 0, text.length(), bounds);
                int height = bounds.height();
                String text2 = lines[1];
                bounds = new Rect();
                paint.getTextBounds(text2, 0, text2.length(), bounds);
                int height2 = bounds.height();
                int combinedHeight = (int) (height + height2 + 0.14f * canvas.getHeight());
                int y = (int) (textY * canvas.getHeight() / 100f - 0.5 * combinedHeight + height);
                if (text.length() > 10)
                    paint.setTextSize((textSizeTwoLines - 5) * canvas.getHeight() / 100f);
                canvas.drawText(text, 0.5f * canvas.getWidth(), y, paint);
                y = (int) (textY * canvas.getHeight() / 100f + 0.5 * combinedHeight);
                if (text2.length() > 10)
                    paint.setTextSize((textSizeTwoLines - 5) * canvas.getHeight() / 100f);
                canvas.drawText(text2, 0.5f * canvas.getWidth(), y, paint);
            }
            return sign;
        }
        return null;
    }

    public static Bitmap buildDualSign(Context context, int speedLimit, int conditionalLimit, String[] conditions, String displayMode, int textColor) {
        Bitmap firstSignToDraw = buildSign(context, speedLimit, displayMode, textColor);
        Bitmap secondSignToDraw = buildSign(context, conditionalLimit, displayMode, textColor);
        if (firstSignToDraw != null && secondSignToDraw != null) {
            // Draw primary sign;
            int width = 460;
            int firstHeight = (int) ((width / (float) firstSignToDraw.getWidth()) * firstSignToDraw.getHeight());
            int secondHeight = (int) ((width / (float) secondSignToDraw.getWidth()) * secondSignToDraw.getHeight());

            Bitmap sign = Bitmap.createBitmap(1000, Math.max(firstHeight, secondHeight) + conditions.length * 240, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sign);
            canvas.drawBitmap(firstSignToDraw, new Rect(0, 0, firstSignToDraw.getWidth(), firstSignToDraw.getHeight()), new Rect(0, 0, width, firstHeight), null);

            // Draw secondary sign
            canvas.drawBitmap(secondSignToDraw, new Rect(0, 0, secondSignToDraw.getWidth(), secondSignToDraw.getHeight()), new Rect(540, 0, 540 + width, secondHeight), null);

            // Draw conditional sign
            int height = 220;
            for (String condition : conditions) {
                String[] lines = condition.split("\n");
                Bitmap signToDraw = buildConditionSign(context, lines, displayMode, textColor);
                if (signToDraw != null) {
                    width = (int) ((height / (float) signToDraw.getHeight()) * signToDraw.getWidth());
                    int x = (int) (540 + (460 - width) * 0.5f);
                    int y = secondHeight + 20;
                    secondHeight = y + height;
                    canvas.drawBitmap(signToDraw, new Rect(0, 0, signToDraw.getWidth(), signToDraw.getHeight()), new Rect(x, y, x + width, y + height), null);
                }
            }

            return sign;
        }
        return null;
    }

    public static Bitmap buildTripleSign(Context context, int speedLimit, int conditionalLimit1, String[] conditions1, int conditionalLimit2, String[] conditions2, String displayMode, int textColor) {
        Bitmap firstSignToDraw = buildSign(context, speedLimit, displayMode, textColor);
        Bitmap secondSignToDraw = buildSign(context, conditionalLimit1, displayMode, textColor);
        Bitmap thirdSignToDraw = buildSign(context, conditionalLimit2, displayMode, textColor);
        if (firstSignToDraw != null) {
            // Draw primary sign;
            int width = 303;
            int firstHeight = (int) ((width / (float) firstSignToDraw.getWidth()) * firstSignToDraw.getHeight());
            int secondHeight = (int) ((width / (float) secondSignToDraw.getWidth()) * secondSignToDraw.getHeight());
            int thirdHeight = (int) ((width / (float) thirdSignToDraw.getWidth()) * thirdSignToDraw.getHeight());

            int conditionsHeight = Math.max(conditions1.length, conditions2.length);
            Bitmap sign = Bitmap.createBitmap(1000, Math.max(Math.max(firstHeight, secondHeight), thirdHeight) + conditionsHeight * 240, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(sign);
            canvas.drawBitmap(firstSignToDraw, new Rect(0, 0, firstSignToDraw.getWidth(), firstSignToDraw.getHeight()), new Rect(0, 0, width, firstHeight), null);

            // Draw first secondary sign
            canvas.drawBitmap(secondSignToDraw, new Rect(0, 0, secondSignToDraw.getWidth(), secondSignToDraw.getHeight()), new Rect(348, 0, 348 + width, secondHeight), null);

            // Draw first conditional signs
            int height = 150;
            Bitmap signToDraw;
            for (String condition : conditions1) {
                String[] lines = condition.split("\n");
                signToDraw = buildConditionSign(context, lines, displayMode, textColor);
                if (signToDraw != null) {
                    width = (int) ((height / (float) signToDraw.getHeight()) * signToDraw.getWidth());
                    int x = (int) (348 + (303 - width) * 0.5f);
                    int y = secondHeight + 20;
                    secondHeight = y + height;
                    canvas.drawBitmap(signToDraw, new Rect(0, 0, signToDraw.getWidth(), signToDraw.getHeight()), new Rect(x, y, x + width, y + height), null);
                }
            }

            // Draw second secondary sign
            width = 303;
            canvas.drawBitmap(thirdSignToDraw, new Rect(0, 0, thirdSignToDraw.getWidth(), thirdSignToDraw.getHeight()), new Rect(697, 0, 697 + width, thirdHeight), null);

            // Draw second conditional signs
            height = 150;
            for (String condition : conditions2) {
                String[] lines = condition.split("\n");
                signToDraw = buildConditionSign(context, lines, displayMode, textColor);
                if (signToDraw != null) {
                    width = (int) ((height / (float) signToDraw.getHeight()) * signToDraw.getWidth());
                    int x = (int) (697 + (303 - width) * 0.5f);
                    int y = thirdHeight + 20;
                    thirdHeight = y + height;
                    canvas.drawBitmap(signToDraw, new Rect(0, 0, signToDraw.getWidth(), signToDraw.getHeight()), new Rect(x, y, x + width, y + height), null);
                }
            }

            return sign;
        }
        return null;
    }
}
