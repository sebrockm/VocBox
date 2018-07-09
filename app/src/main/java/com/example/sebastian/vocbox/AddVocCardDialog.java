package com.example.sebastian.vocbox;

import android.app.Dialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

public class AddVocCardDialog extends Dialog {

    private VocBoxActivity mVocBoxActivity;

    public AddVocCardDialog(VocBoxActivity vocBoxActivity) {
        super(vocBoxActivity);
        mVocBoxActivity = vocBoxActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.voccard_dialog);

        final EditText nativeTextInput = (EditText)findViewById(R.id.nativeTextInput);
        final EditText foreignTextInput = (EditText)findViewById(R.id.foreignTextInput);

        foreignTextInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_DONE)
                    return false;

                String nativeWord = nativeTextInput.getText().toString();
                String foreignWord = foreignTextInput.getText().toString();
                mVocBoxActivity.addVocCard(new VocCaseModel.VocCardModel(nativeWord, foreignWord));

                nativeTextInput.getText().clear();
                foreignTextInput.getText().clear();

                nativeTextInput.requestFocus();
                return true;
            }
        });
    }
}