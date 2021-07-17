package com.droiduino.final_guitar_tuning_app;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.droiduino.final_guitar_tuning_app.databinding.FragmentFirstBinding;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean recording = false;
        TextView text = getView().findViewById(R.id.textview_first);
        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //     art.onRequestPermissionsResult();
                getpitch();
            }
        });
        binding.highEStringButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //     art.onRequestPermissionsResult();
                getpitch();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public static int calculate(int sampleRate, short [] audioData){

        int numSamples = audioData.length;
        int numCrossing = 0;
        for (int p = 0; p < numSamples-1; p++)
        {
            if ((audioData[p] > 0 && audioData[p + 1] <= 0) ||
                    (audioData[p] < 0 && audioData[p + 1] >= 0))
            {
                numCrossing++;
            }
        }

        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
        float numCycles = ((float)numCrossing)/2;
        float frequency = numCycles/numSecondsRecorded;

        float adjustedFrequency = (float) (1.12 * frequency);

        return (int)adjustedFrequency;
    }
    public void getpitch(){
        int channel_config = AudioFormat.CHANNEL_IN_MONO;
        int format = AudioFormat.ENCODING_PCM_16BIT;
        int sampleSize = 44100;
        int bufferSize = 10000;
        TextView text = getView().findViewById(R.id.textview_first);
        AudioRecord audioInput = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleSize, channel_config, format, bufferSize);
        //TextView txtview = (TextView)findViewById(R.id.text);

        short[] audioBuffer = new short[bufferSize];
        audioInput.startRecording();
        audioInput.read(audioBuffer, 0, bufferSize);
        //recorder.startRecording();
        //recorder.read(audioBuffer, 0, bufferSize);
        //txtview.setText(""+calculate(8000,audioBuffer));
        audioInput.stop();
        text.setText("Frequency = " + calculate(sampleSize, audioBuffer));
        System.out.println(calculate(sampleSize,audioBuffer));
        audioBuffer = null;
        audioInput.release();
    }
}