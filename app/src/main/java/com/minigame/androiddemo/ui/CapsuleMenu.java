package com.minigame.androiddemo.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * A capsule-style menu button.
 * Contains "..." (Menu) and "O" (Close) buttons.
 */
public class CapsuleMenu extends LinearLayout {

    private final OnMenuActionListener listener;

    public interface OnMenuActionListener {
        void onRestart();
        void onExit();
    }

    private int mWidth;
    private int mHeight;

    public CapsuleMenu(Context context, OnMenuActionListener listener) {
        super(context);
        this.listener = listener;
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        
        // Container style
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        
        // Background: white semi-transparent rounded pill with border
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#99FFFFFF")); // Semi-transparent white
        bg.setCornerRadius(16 * density);
        bg.setStroke((int) (1 * density), Color.parseColor("#33000000")); // Light border
        setBackground(bg);

        // Dimensions
        mHeight = (int) (32 * density);
        mWidth = (int) (100 * density); // Standard capsule width ~87dp
        
        // Set layout params for self
        // Note: The parent layout params should be set by the caller (e.g. FrameLayout.LayoutParams)
        
        // 1. Menu Button (...)
        TextView menuBtn = createButton(context, "•••");
        menuBtn.setOnClickListener(v -> showMenuDialog(context));
        
        // 2. Divider
        View divider = new View(context);
        divider.setBackgroundColor(Color.parseColor("#33000000"));
        LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(
                (int) (1 * density), 
                (int) (18 * density));
        
        // 3. Close Button (O)
        TextView closeBtn = createButton(context, "◎"); // Using a similar unicode circle
        closeBtn.setOnClickListener(v -> {
            if (listener != null) listener.onExit();
        });

        // Add views
        // Distribute weight evenly or use fixed sizes.
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        
        addView(menuBtn, btnParams);
        addView(divider, dividerParams);
        addView(closeBtn, btnParams);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(
                View.MeasureSpec.makeMeasureSpec(mWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(mHeight, View.MeasureSpec.EXACTLY)
        );
    }

    private TextView createButton(Context context, String text) {
        TextView btn = new TextView(context);
        btn.setText(text);
        btn.setGravity(Gravity.CENTER);
        btn.setTextColor(Color.BLACK);
        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        // Make font bold for better visibility
        btn.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        return btn;
    }

    private void showMenuDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("Game Menu")
                .setItems(new String[]{"Restart Mini Game"}, (dialog, which) -> {
                    if (which == 0) {
                        if (listener != null) listener.onRestart();
                    }
                })
                .show();
    }
}
