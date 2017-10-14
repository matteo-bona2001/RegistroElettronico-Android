package com.sharpdroid.registroelettronico.Utils;

import android.widget.FrameLayout;

public class LayoutHelper {

    public static final int MATCH_PARENT = -1;
    public static final int WRAP_CONTENT = -2;

    private static int getSize(float size) {
        return (int) (size < 0 ? size : Metodi.dp(size));
    }

    public static FrameLayout.LayoutParams createFrame(int width, float height, int gravity, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(getSize(width), getSize(height), gravity);
        layoutParams.setMargins(Metodi.dp(leftMargin), Metodi.dp(topMargin), Metodi.dp(rightMargin), Metodi.dp(bottomMargin));
        return layoutParams;
    }
}
