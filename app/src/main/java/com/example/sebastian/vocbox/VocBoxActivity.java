package com.example.sebastian.vocbox;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Locale;

public class VocBoxActivity extends AppCompatActivity {

    @Override
    public void onPause() {
        super.onPause();
        try (ObjectOutputStream output = new ObjectOutputStream(new BufferedOutputStream(getApplicationContext().openFileOutput("store.bin", Context.MODE_PRIVATE)))) {
            for (int i = 0; i < mVocCaseModels.length; ++i) {
                output.writeObject(mVocCaseModels[i]);
            }

            TabHost tabHost = findViewById(android.R.id.tabhost);
            output.writeObject(tabHost.getCurrentTabTag());
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG);
        }
    }

    private Intent mChoseFileResult;

    @Override
    public void onResume() {
        super.onResume();

        try (ObjectInputStream input = new ObjectInputStream(new BufferedInputStream(getApplicationContext().openFileInput("store.bin")))) {
            for (int i = 0; i < 5; ++i) {
                mVocCaseModels[i] = (VocCaseModel)input.readObject();
                mVocCaseViews[i].setTextFromModel(mVocCaseModels[i]);
            }

            TabHost tabHost = findViewById(android.R.id.tabhost);
            String currentTabTag = (String) input.readObject();
            tabHost.setCurrentTabByTag(currentTabTag);
        } catch (FileNotFoundException e) {

        } catch (IOException | ClassNotFoundException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }

        if (mChoseFileResult == null)
            return;

        Uri uri = mChoseFileResult.getData();
        mChoseFileResult = null;
        if (uri == null) throw new AssertionError();
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            for (VocCaseModel.VocCardModel card : CsvReader.readCards(inputStream)) {
                mVocCaseModels[0].addCardAtRandomPosition(card);
            }
            mVocCaseViews[0].setTextFromModel(mVocCaseModels[0]);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static final String currentCaseTab = "currentCaseTab";

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        TabHost tabHost = findViewById(android.R.id.tabhost);
        savedInstanceState.putString(currentCaseTab, tabHost.getCurrentTabTag());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        TabHost tabHost = findViewById(android.R.id.tabhost);
        String currentTabTag = savedInstanceState.getString(currentCaseTab, "");
        if (!currentTabTag.isEmpty())
            tabHost.setCurrentTabByTag(currentTabTag);
    }

    public void onClickAddCard(View view) {
        if (view.getId() != R.id.addCard)
            return;

        AddVocCardDialog addVocCardDialog = new AddVocCardDialog(this);
        addVocCardDialog.show();
    }

    public void addVocCard(VocCaseModel.VocCardModel vocCard) {
        mVocCaseModels[0].addCardAtRandomPosition(vocCard);
        mVocCaseViews[0].setTextFromModel(mVocCaseModels[0]);
        Toast.makeText(this, "Added card " + vocCard.getNative() + " ; " + vocCard.getForeign(), Toast.LENGTH_LONG).show();
    }

    public void onClickImportFile(View view) {
        if (view.getId() != R.id.importFile)
            return;

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(Intent.createChooser(intent, "Öffne Vokabeldatei"), OPEN_FILE_REQUEST);
    }

    private final static int OPEN_FILE_REQUEST = 756;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode != OPEN_FILE_REQUEST || resultCode != Activity.RESULT_OK)
            return;

        mChoseFileResult = resultData;
    }

    private VocCaseView[] mVocCaseViews;
    private VocCaseModel[] mVocCaseModels;

    private static final String tag1 = "tabtag1";
    private static final String tag2 = "tabtag2";
    private static final String tag3 = "tabtag3";
    private static final String tag4 = "tabtag4";
    private static final String tag5 = "tabtag5";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voc_box);

        TabHost tabHost = findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec(tag1);
        TabHost.TabSpec tab2 = tabHost.newTabSpec(tag2);
        TabHost.TabSpec tab3 = tabHost.newTabSpec(tag3);
        TabHost.TabSpec tab4 = tabHost.newTabSpec(tag4);
        TabHost.TabSpec tab5 = tabHost.newTabSpec(tag5);

        tab1.setIndicator("Fach 1");
        tab1.setContent(R.id.tab1);

        tab2.setIndicator("Fach 2");
        tab2.setContent(R.id.tab2);

        tab3.setIndicator("Fach 3");
        tab3.setContent(R.id.tab3);

        tab4.setIndicator("Fach 4");
        tab4.setContent(R.id.tab4);

        tab5.setIndicator("Fach 5");
        tab5.setContent(R.id.tab5);

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);
        tabHost.addTab(tab4);
        tabHost.addTab(tab5);

        mVocCaseModels = new VocCaseModel[]{
                new VocCaseModel(6),
                new VocCaseModel(1),
                new VocCaseModel(1),
                new VocCaseModel(1),
                new VocCaseModel(1)
        };

        initializeVocCardViews();

        mTextToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.SUCCESS)
                    Toast.makeText(VocBoxActivity.this, "Text-to-Speech initialization failed.", Toast.LENGTH_LONG).show();

                int result = mTextToSpeech.setLanguage(new Locale("ru","RU"));
                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    Toast.makeText(VocBoxActivity.this, "Russian locale is not supported.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initializeVocCardViews() {
        mVocCaseViews = new VocCaseView[]{
                new VocCaseView(
                        (TextView) findViewById(R.id.cardCount1),
                        (TextView) findViewById(R.id.nativeText1),
                        (TextView) findViewById(R.id.foreignText1),
                        (Button) findViewById(R.id.correct1),
                        (Button) findViewById(R.id.incorrect1)),
                new VocCaseView(
                        (TextView) findViewById(R.id.cardCount2),
                        (TextView) findViewById(R.id.nativeText2),
                        (TextView) findViewById(R.id.foreignText2),
                        (Button) findViewById(R.id.correct2),
                        (Button) findViewById(R.id.incorrect2)),
                new VocCaseView(
                        (TextView) findViewById(R.id.cardCount3),
                        (TextView) findViewById(R.id.nativeText3),
                        (TextView) findViewById(R.id.foreignText3),
                        (Button) findViewById(R.id.correct3),
                        (Button) findViewById(R.id.incorrect3)),
                new VocCaseView(
                        (TextView) findViewById(R.id.cardCount4),
                        (TextView) findViewById(R.id.nativeText4),
                        (TextView) findViewById(R.id.foreignText4),
                        (Button) findViewById(R.id.correct4),
                        (Button) findViewById(R.id.incorrect4)),
                new VocCaseView(
                        (TextView) findViewById(R.id.cardCount5),
                        (TextView) findViewById(R.id.nativeText5),
                        (TextView) findViewById(R.id.foreignText5),
                        (Button) findViewById(R.id.correct5),
                        (Button) findViewById(R.id.incorrect5))
        };

        for (int i = 0; i < mVocCaseViews.length; ++i) {
            mVocCaseViews[i].setTextFromModel(mVocCaseModels[i]);
        }
    }

    private TextToSpeech mTextToSpeech;

    public void onClickNative(View view) {
        int id;
        switch (view.getId()) {
            case R.id.nativeText1: id = 0; break;
            case R.id.nativeText2: id = 1; break;
            case R.id.nativeText3: id = 2; break;
            case R.id.nativeText4: id = 3; break;
            case R.id.nativeText5: id = 4; break;
            default: return;
        }

        if (mVocCaseModels[id].getCardCount() == 0)
            return;

        mVocCaseModels[id].setIsAnswerOpen(true);
        mVocCaseViews[id].setAnswerVisibility(true);
        mTextToSpeech.speak(mVocCaseModels[id].getCurrentCard().getForeign(), TextToSpeech.QUEUE_FLUSH, null);
    }

    public void onClickCorrect(View view) {
        int id;
        switch (view.getId()) {
            case R.id.correct1: id = 0; break;
            case R.id.correct2: id = 1; break;
            case R.id.correct3: id = 2; break;
            case R.id.correct4: id = 3; break;
            case R.id.correct5: id = 4; break;
            default: return;
        }
        int nextId = Math.min(id + 1, 4);

        if (mVocCaseModels[id].getCardCount() == 0)
            return;

        if (nextId == 4)
            mVocCaseModels[nextId].addCardAtBack(mVocCaseModels[id].removeCurrentCardNonRandomReplacement());
        else
            mVocCaseModels[nextId].addCardAtRandomPosition(mVocCaseModels[id].removeCurrentCardRandomReplacement());
        mVocCaseViews[id].setTextFromModel(mVocCaseModels[id]);
        mVocCaseViews[nextId].setTextFromModel(mVocCaseModels[nextId]);
        mVocCaseModels[id].setIsAnswerOpen(false);
        mVocCaseViews[id].setAnswerVisibility(false);
    }

    public void onClickIncorrect(View view) {
        int id;
        switch (view.getId()) {
            case R.id.incorrect1: id = 0; break;
            case R.id.incorrect2: id = 1; break;
            case R.id.incorrect3: id = 2; break;
            case R.id.incorrect4: id = 3; break;
            case R.id.incorrect5: id = 4; break;
            default: return;
        }

        if (id == 0)
            mVocCaseModels[0].recycleCurrentCard();
        else if (id == 4)
            mVocCaseModels[0].addCardAtRandomPosition(mVocCaseModels[id].removeCurrentCardNonRandomReplacement());
        else
            mVocCaseModels[0].addCardAtRandomPosition(mVocCaseModels[id].removeCurrentCardRandomReplacement());

        mVocCaseViews[id].setTextFromModel(mVocCaseModels[id]);
        mVocCaseViews[0].setTextFromModel(mVocCaseModels[0]);
        mVocCaseModels[id].setIsAnswerOpen(false);
        mVocCaseViews[id].setAnswerVisibility(false);
    }
}
