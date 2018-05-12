package io.github.nearchos.findtheword;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    public static final String TAG = "find-the-word";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
        super.onCreate(savedInstanceState);
        final ActionBar actionBar = getActionBar();
        if(actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        // load settings fragment
        getFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new MainPreferenceFragment())
                .commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {

        private SwitchPreference customWordSwitchPreference;
        private EditTextPreference customWordEditTextPreference;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            customWordSwitchPreference = (SwitchPreference) getPreferenceManager().findPreference("useCustomWord");
            assert customWordSwitchPreference != null;
            customWordEditTextPreference = (EditTextPreference) getPreferenceManager().findPreference("customWord");
            assert customWordEditTextPreference != null;
            customWordEditTextPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                // check word
                final String wordToBeChecked = MainActivity.sanitize(newValue.toString().trim());
                boolean purelyGreekWord = true;
                for(final Character c : wordToBeChecked.toCharArray()) {
                    if(c < 'Α' || c > 'Ω') {
                        purelyGreekWord = false;
                        break;
                    }
                }
                if(purelyGreekWord && !wordToBeChecked.isEmpty()) {
                    customWordEditTextPreference.setSummary(wordToBeChecked);
                } else {
                    Toast.makeText(getActivity(), R.string.ChooseAWord, Toast.LENGTH_SHORT).show();
                    customWordEditTextPreference.setSummary(R.string.ChooseAWord);
                    customWordSwitchPreference.setChecked(false);
                }
                return true;
            });
            final Preference rateTheAppPreference = getPreferenceManager().findPreference("rateTheApp");
            assert rateTheAppPreference != null;
            rateTheAppPreference.setOnPreferenceClickListener(preference -> rateTheApp(getActivity()));
            final Preference shareTheAppPreference = getPreferenceManager().findPreference("shareTheApp");
            assert shareTheAppPreference != null;
            shareTheAppPreference.setOnPreferenceClickListener(preference -> shareTheApp(getActivity()));

            final int numOfEasyWords = MainActivity.getWordsFromAssets(getActivity(), "easy.txt").size();
            final int numOfMediumWords = MainActivity.getWordsFromAssets(getActivity(), "medium.txt").size();
            final int numOfHardWords = MainActivity.getWordsFromAssets(getActivity(), "hard.txt").size();
            final int totalNumOfWords = numOfEasyWords + numOfMediumWords + numOfHardWords;
            final Preference statisticsPreference = getPreferenceManager().findPreference("statistics");
            assert statisticsPreference != null;
            statisticsPreference.setSummary(getString(R.string.NumOfWords, totalNumOfWords));
            statisticsPreference.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getActivity(), getString(R.string.NumOfWordsByCategory, numOfEasyWords, numOfMediumWords, numOfHardWords), Toast.LENGTH_LONG).show();
                return true;
            });

            final Preference developmentDetailsPreference = getPreferenceManager().findPreference("developmentDetails");
            assert developmentDetailsPreference != null;
            developmentDetailsPreference.setSummary(getString(R.string.DevelopedBy, BuildConfig.VERSION_NAME));
        }

        @Override
        public void onResume() {
            super.onResume();

            final String initialCustomWord = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("customWord", getActivity().getString(R.string.NoWordChosen));
            customWordEditTextPreference.setSummary(initialCustomWord);
            Log.d(TAG, "initialCustomWord: " + initialCustomWord);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private static boolean rateTheApp(final Context context) {
        final Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
        final Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            context.startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
        return true;
    }

    private static boolean shareTheApp(final Context context) {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.ShareMessage));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Δοκίμασε την εφαρμογή 'Βρες τη Λέξη' στο http://github.io/nearchos/android-companion/findtheword");
        context.startActivity(Intent.createChooser(shareIntent, context.getResources().getString(R.string.Share)));
        return true;
    }
}