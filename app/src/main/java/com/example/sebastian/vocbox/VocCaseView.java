package com.example.sebastian.vocbox;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class VocCaseView {
    private TextView mCardCount;
    private TextView mNativeText;
    private TextView mForeignText;
    private Button mCorrect;
    private Button mIncorrect;

    VocCaseView(TextView cardCount, TextView nativeText,
                TextView foreignText, Button correct, Button incorrect) {
        mCardCount = cardCount;
        mNativeText = nativeText;
        mForeignText = foreignText;
        mCorrect = correct;
        mIncorrect = incorrect;
    }

    void setCardCountText(String cardCountText) {
        mCardCount.setText(cardCountText);
    }

    void setNativeText(String nativeText) {
        mNativeText.setText(nativeText);
    }

    void setForeignText(String foreignText) {
        mForeignText.setText(foreignText);
    }

    void setTextFromModel(VocCaseModel model) {
        setCardCountText("" + model.getCardCount());
        setNativeText(model.getCardCount() > 0 ? model.getCurrentCard().getNative() : "");
        setForeignText(model.getCardCount() > 0 ? model.getCurrentCard().getForeign() : "");
        setAnswerVisibility(model.isAnswerOpen());
    }

    void setAnswerVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.INVISIBLE;
        mForeignText.setVisibility(visibility);
        mCorrect.setVisibility(visibility);
        mIncorrect.setVisibility(visibility);
    }
}
