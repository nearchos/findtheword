package io.github.nearchos.findtheword;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

public class MainActivity extends Activity {

    public static final String TAG = "find-the-word";

    public static final int [] BUTTON_IDS = {
            R.id.button_alpha, R.id.button_beta, R.id.button_gamma, R.id.button_delta,
            R.id.button_epsilon, R.id.button_zita, R.id.button_eta, R.id.button_theta,
            R.id.button_giota, R.id.button_kappa, R.id.button_lambda, R.id.button_mi,
            R.id.button_ni, R.id.button_ksi, R.id.button_omikron, R.id.button_pi,
            R.id.button_rho, R.id.button_sigma, R.id.button_tau, R.id.button_ypsilon,
            R.id.button_phi, R.id.button_chi, R.id.button_psi, R.id.button_omega
    };

    private Button easyButton;
    private Button mediumButton;
    private Button hardButton;
    private ImageButton muteUnmuteButton;

    private ImageView swimmerImageView;
    private TextView textView;

    private boolean [] letters = new boolean[25]; // 25 and not 24 because sigma has 2 versions so omega is 25th

    private MediaPlayer correctMediaPlayer;
    private MediaPlayer wrongMediaPlayer;
    private MediaPlayer winMediaPlayer;
    private MediaPlayer bubblesMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); //Remove title bar
        setContentView(R.layout.activity_main);

        easyButton = findViewById(R.id.easyButton);
        mediumButton = findViewById(R.id.mediumButton);
        hardButton = findViewById(R.id.hardButton);
        muteUnmuteButton = findViewById(R.id.muteUnmuteButton);

        swimmerImageView = findViewById(R.id.swimmer);
        textView = findViewById(R.id.text_view);

        correctMediaPlayer = MediaPlayer.create(this, R.raw.correct);
        wrongMediaPlayer = MediaPlayer.create(this, R.raw.wrong);
        winMediaPlayer = MediaPlayer.create(this, R.raw.win);
        bubblesMediaPlayer = MediaPlayer.create(this, R.raw.bubbles);
    }

    private String selectedWord;
    private String word = "";

    @Override
    protected void onStart() {
        super.onStart();

        final Difficulty difficulty = getPreferredDifficulty();
        updateDifficultyView(difficulty);

        resetWord();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mute = getPreferences(MODE_PRIVATE).getBoolean("mute", true);
        updateMuteIcon(mute);
    }

    public void showDialogStartNewGame(final View view) {
        if(getGameState() != GameState.ACTIVE || !letterTouched) {
            resetGameState();
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name)
                    .setMessage(R.string.Confirm_reset_message)
                    .setPositiveButton(R.string.Yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            resetGameState();
                        }
                    })
                    .setNegativeButton(R.string.No, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
    }

    private void resetGameState() {
        resetWord();
        resetKeyboard();
        resetSwimmer();
        letterTouched = false;
    }

    private Vector<String> easyWords = null;
    private Vector<String> mediumWords = null;
    private Vector<String> hardWords = null;

    private void resetWord() {
        switch (getPreferredDifficulty()) {
            case EASY:
                if(easyWords == null) easyWords = getWordsFromAssets("easy.txt");
                selectedWord = pickRandom(easyWords);
                break;
            case MEDIUM:
                if(mediumWords == null) mediumWords = getWordsFromAssets("medium.txt");
                selectedWord = pickRandom(mediumWords);
                break;
            case HARD:
                if(hardWords == null) hardWords = getWordsFromAssets("hard.txt");
                selectedWord = pickRandom(hardWords);
                break;
        }
        word = createEmptyWord(selectedWord.length());

        textView.setText(word);
    }

    private void resetKeyboard() {
        for(int i = 0; i < letters.length; i++) {
            letters[i] = false;
        }
        for(final int buttonResourceId : BUTTON_IDS) {
            findViewById(buttonResourceId).setBackgroundColor(Color.WHITE);
        }
    }

    private enum WaterLevel { L0, L1, L2, L3, L4, L5, L6, L7, L8, LOSE, WIN};

    private WaterLevel currentWaterLevel = WaterLevel.L0;

    private void resetSwimmer() {
        currentWaterLevel = WaterLevel.L0;
        updateSwimmerStateView();
    }

    private void progressSwimmer() {
        switch (currentWaterLevel) {
            case L0: currentWaterLevel = WaterLevel.L1; break;
            case L1: currentWaterLevel = WaterLevel.L2; break;
            case L2: currentWaterLevel = WaterLevel.L3; break;
            case L3: currentWaterLevel = WaterLevel.L4; break;
            case L4: currentWaterLevel = WaterLevel.L5; break;
            case L5: currentWaterLevel = WaterLevel.L6; break;
            case L6: currentWaterLevel = WaterLevel.L7; break;
            case L7: currentWaterLevel = WaterLevel.L8; break;
            case L8: currentWaterLevel = WaterLevel.LOSE; break;
        }
    }

    private void updateSwimmerStateView() {
        updateSwimmerStateView(currentWaterLevel);
    }

    private void updateSwimmerStateView(final WaterLevel waterLevel) {
        switch (waterLevel) {
            case L0: swimmerImageView.setImageResource(R.drawable.p0); break;
            case L1: swimmerImageView.setImageResource(R.drawable.p1); break;
            case L2: swimmerImageView.setImageResource(R.drawable.p2); break;
            case L3: swimmerImageView.setImageResource(R.drawable.p3); break;
            case L4: swimmerImageView.setImageResource(R.drawable.p4); break;
            case L5: swimmerImageView.setImageResource(R.drawable.p5); break;
            case L6: swimmerImageView.setImageResource(R.drawable.p6); break;
            case L7: swimmerImageView.setImageResource(R.drawable.p7); break;
            case L8: swimmerImageView.setImageResource(R.drawable.p8); break;
            case LOSE: swimmerImageView.setImageResource(R.drawable.p9); break;
            case WIN: swimmerImageView.setImageResource(R.drawable.win); break;
        }
    }

    private boolean letterTouched = false;

    public void letterClicked(final View view) {

        if(getGameState() != GameState.ACTIVE) {
            showDialogStartNewGame(view);
            return;
        }

        letterTouched = true;

        final char letter = ((TextView) view).getText().charAt(0);
        if(letters[letter - 'Α']) {
            return; // ignore used letter
        }

        Log.d(TAG, "letter: " + letter + ", index: " + (letter - 'Α'));
        letters[letter - 'Α'] = true;
        final boolean correctLetter = check(letter);

        if(correctLetter) { // correct letter chosen
            view.setBackgroundColor(getResources().getColor(R.color.colorCorrectButton));
        } else { // wrong letter chosen
            view.setBackgroundColor(getResources().getColor(R.color.colorWrongButton));
            progressSwimmer();
        }

        word = amendWord(letter);
        textView.setText(word);

        switch (getGameState()) {
            case WON:
                currentWaterLevel = WaterLevel.WIN;
                // play won sound
                if(!mute) winMediaPlayer.start();
                break;
            case LOST:
                // play lost sound
                if(!mute) bubblesMediaPlayer.start();
                break;
            case ACTIVE:
                // play key sound
                if(correctLetter) { if(!mute) correctMediaPlayer.start();}
                else { if(!mute) wrongMediaPlayer.start(); }
                break;
        }

        updateSwimmerStateView();
    }

    private String createEmptyWord(final int length) {
        final StringBuilder word = new StringBuilder();
        for(int i = 0; i < length; i++) {
            word.append(" _");
        }
        word.append(" ");
        return word.toString();
    }

    private enum GameState { ACTIVE, WON, LOST };

    private GameState getGameState() {
        if(!word.contains("_")) {
            return GameState.WON;
        } else if(currentWaterLevel == WaterLevel.LOSE) {
            return GameState.LOST;
        } else {
            return GameState.ACTIVE;
        }
    }

    private boolean check(final char letter) {
        Log.d(TAG, "selectedWord: " + selectedWord + ", letter: " + letter + ", " + (selectedWord.indexOf(letter) > -1));
        return sanitize(selectedWord).indexOf(letter) > -1;
    }

    static String sanitize(final String unsanitized) {
        return unsanitized.toLowerCase()
                .replaceAll("ά", "α")
                .replaceAll("έ", "ε")
                .replaceAll("ή", "η")
                .replaceAll("ί", "ι")
                .replaceAll("ό", "ο")
                .replaceAll("ύ", "υ")
                .replaceAll("ώ", "ω")
                .toUpperCase();
    }

    private String amendWord(final char letter) {
        Log.d(TAG, "letter: " + letter);
        final char [] letters = word.toCharArray();
        Log.d(TAG, "letters: " + Arrays.toString(letters));
        final String sanitizedWord = sanitize(selectedWord);
        for(int i = 0; i < selectedWord.length(); i++) {
            if(sanitizedWord.charAt(i) == letter) {
                letters[1 + 2 * i] = selectedWord.toUpperCase().charAt(i);
            }
        }
        Log.d(TAG, "letters: " + Arrays.toString(letters));
        return new String(letters);
    }

    private enum Difficulty { EASY, MEDIUM, HARD };

    private Difficulty getPreferredDifficulty() {
        final int ordinal = getPreferences(MODE_PRIVATE).getInt("difficulty", Difficulty.EASY.ordinal());
        return Difficulty.values()[ordinal];
    }

    private void setPreferredDifficulty(final Difficulty difficulty) {
        getPreferences(MODE_PRIVATE).edit().putInt("difficulty", difficulty.ordinal()).apply();
    }

    public void setDifficulty(final View view) {
        if(view == easyButton) {
            setPreferredDifficulty(Difficulty.EASY);
            updateDifficultyView(Difficulty.EASY);
        } else if(view == mediumButton) {
            setPreferredDifficulty(Difficulty.MEDIUM);
            updateDifficultyView(Difficulty.MEDIUM);
        } else { // view == hardButton
            setPreferredDifficulty(Difficulty.HARD);
            updateDifficultyView(Difficulty.HARD);
        }
        showDialogStartNewGame(view);
    }

    private void updateDifficultyView(final Difficulty difficulty) {
        switch (difficulty) {
            default:
            case EASY:
                easyButton.setBackgroundColor(getResources().getColor(R.color.colorYellow));
                mediumButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                hardButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                break;
            case MEDIUM:
                easyButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                mediumButton.setBackgroundColor(getResources().getColor(R.color.colorYellow));
                hardButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                break;
            case HARD:
                easyButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                mediumButton.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                hardButton.setBackgroundColor(getResources().getColor(R.color.colorYellow));
                break;
        }
    }

    private boolean mute;

    private boolean isMute() {
        return mute;
    }

    public void muteUnmute(View view) {
        final boolean updatedMuteState = !isMute();
        getPreferences(MODE_PRIVATE).edit().putBoolean("mute", updatedMuteState).apply();
        updateMuteIcon(updatedMuteState);
        this.mute = updatedMuteState;
    }

    private void updateMuteIcon(final boolean mute) {
        muteUnmuteButton.setImageResource(mute ? R.drawable.ic_volume_off_black_24dp : R.drawable.ic_volume_up_black_24dp);
    }

    private Vector<String> getWordsFromAssets(final String filename) {
        final Vector<String> words = new Vector<>();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open(filename), "UTF-8"));

            // do reading, usually loop until end of file reading
            String word;
            while ((word = bufferedReader.readLine()) != null) {
                // process word
                words.add(word);
            }
        } catch (IOException ioe) {
            //log the exception
            Log.e(TAG, "IO error while reading asset file: " + ioe.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    //log the exception
                    Log.e(TAG, "IO error while closing asset file: " + e.getMessage());
                }
            }
        }

        return words;
    }

    private String pickRandom(final Vector<String> words) {
        final int randomIndex = new Random().nextInt(words.size());
        return sanitize(words.get(randomIndex));
    }

    public void showSettings(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }
}