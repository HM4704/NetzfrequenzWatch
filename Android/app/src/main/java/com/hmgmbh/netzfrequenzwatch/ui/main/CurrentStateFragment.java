package com.hmgmbh.netzfrequenzwatch.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hmgmbh.netzfrequenzwatch.MainActivity;
import com.hmgmbh.netzfrequenzwatch.R;
import com.hmgmbh.netzfrequenzwatch.data.FreqState;
import com.hmgmbh.netzfrequenzwatch.databinding.FragmentMainBinding;

import java.util.regex.Pattern;

/**
 * A placeholder fragment containing a simple view.
 */
public class CurrentStateFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;
    private FragmentMainBinding binding;

    public static CurrentStateFragment newInstance(int index) {
        CurrentStateFragment fragment = new CurrentStateFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = new ViewModelProvider(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        binding = FragmentMainBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pageViewModel.getActFreq().observe(this, actFreq -> {
                // update UI
                TextView freqText = (TextView) getView().findViewById(R.id.textActFreq);
                freqText.setText(roundString(actFreq));

        });

        pageViewModel.getMinFreq().observe(this, minFreq -> {
            // update UI
            TextView freqText = (TextView) getView().findViewById(R.id.textMinFreq);
            freqText.setText(String.format("min: %1$,.2f", minFreq));

        });
        pageViewModel.getMaxFreq().observe(this, maxFreq -> {
            // update UI
            TextView freqText = (TextView) getView().findViewById(R.id.textMaxFreq);
            freqText.setText(String.format("max: %1$,.2f", maxFreq));

        });
        pageViewModel.getState().observe(this, state -> {
            // update UI
            String s = "??";
            int color = Color.RED;
            if (state == FreqState.Ok) {
                s = "OK";
                color = Color.GREEN;
            }
            else if (state == FreqState.Warning) {
                s = "WARN";
                color = Color.MAGENTA;
            }
            else if (state == FreqState.Error) {
                s = "ERR";
                color = Color.RED;
            }
            else if (state == FreqState.Blackout) {
                s = "BLACKOUT";
                color = Color.RED;
            }
            else if (state == FreqState.NoNet) {
                s = "NONET";
                color = Color.DKGRAY;
            }

            TextView stateText = (TextView) getView().findViewById(R.id.textActState);
            stateText.setText(s);
            stateText.setTextColor(color);
        });
        pageViewModel.getBlackout().observe(this, count -> {
            // update UI
            TextView freqText = (TextView) getView().findViewById(R.id.textCountBlackout);
            freqText.setText(String.format("Anzahl Blackouts:\t\t\t\t%d", count));
        });
        pageViewModel.getErrors().observe(this, count -> {
            // update UI
            TextView freqText = (TextView) getView().findViewById(R.id.textCountErrors);
            freqText.setText(String.format("Anzahl Fehler:\t\t\t\t%d", count));
        });
        pageViewModel.getWarnings().observe(this, count -> {
            // update UI
            TextView freqText = (TextView) getView().findViewById(R.id.textCountWarnings);
            freqText.setText(String.format("Anzahl Warnungen:\t\t\t\t%d", count));
        });
        pageViewModel.getNotNet().observe(this, count -> {
            // update UI
            TextView freqText = (TextView) getView().findViewById(R.id.textCountNoNet);
            freqText.setText(String.format("Anzahl Netzwerkfehler:\t\t%d", count));
        });

        binding.btnStartService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                ((MainActivity)getActivity()).startService();
            }
        });
        binding.btnStopService.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                ((MainActivity)getActivity()).stopService();
            }
        });
        binding.btnTestAlarm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                pageViewModel.testSetFreq("49.79");
            }
        });

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String roundString(String value) {
        String[] lineSplitted = value.split(Pattern.quote("."));
        String ret = value;
        int len = lineSplitted.length;
        if (len > 1) {
            String decimals = lineSplitted[1];
            if (decimals.length() > 2) {
                String stopEnd = decimals.substring(0, 2);
                ret = lineSplitted[0] + "." + stopEnd;
            }
        }
        return ret;
    }
}