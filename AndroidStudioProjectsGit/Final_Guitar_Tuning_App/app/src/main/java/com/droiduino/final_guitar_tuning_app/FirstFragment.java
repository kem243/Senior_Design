package com.droiduino.final_guitar_tuning_app;

import android.content.Intent;
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
import androidx.navigation.fragment.NavHostFragment;

import com.droiduino.final_guitar_tuning_app.databinding.FragmentFirstBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    public char selectedString = 'X';

    public double target_pitch_high_e_default = 329.63;
    public double target_pitch_b_default = 246.94;
    public double target_pitch_g_default = 196.00;
    public double target_pitch_d_default = 146.83;
    public double target_pitch_a_default = 110.00;
    public double target_pitch_low_e_default = 82.41;

    public boolean custom_tuning = false;

    public double target_pitch_high_e_custom = 0.0;
    public double target_pitch_b_custom = 0.0;
    public double target_pitch_g_custom = 0.0;
    public double target_pitch_d_custom = 0.0;
    public double target_pitch_a_custom = 0.0;
    public double target_pitch_low_e_custom = 0.0;

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
                FloatingActionButton highEStringButton = getView().findViewById(R.id.high_e_string_button);
                //highEStringButton.setBackgroundTintList(contextInstance.getResources().getColorStateList(R.color.your_xml_name));
                selectedString = 'E';
                System.out.println("selected E");
            }
        });
        binding.blueToothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), bluetoothActivity.class));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public int calculate(int sampleRate, short [] audioData){
        int sampleSize = 2048;
        FFT fourier = new FFT();
        Complex[] x = new Complex[sampleSize];
        int max = 600;
        int temp = audioData.length - sampleSize;
        for (int i = temp; i < audioData.length; i++){
            x[i - temp] = new Complex((double)audioData[i], 0);
        }
        Complex[] y = fourier.fft(x);
        double[] amp = new double[max];
        double total = 0;
        double ave = 0;
        for (int i = 0; i < max; i++){
            double temp2 = y[i].re();
            double nextTemp = temp2 * temp2 / sampleSize;
            total += nextTemp;
            amp[i] = nextTemp;
        }
        ave = total / max;
        double maximum = 0;
        int ind = 0;
        double mult = 2;
        for (int j = 0; j < max - 10; j++){
            if (amp[j] > 2 * ave){
                for (int i = j; i < j + 10; i++){
                    if (amp[i] > maximum){
                        maximum = amp[i];
                        ind = i;
                    }
                }
                break;
            }
            if (j == max - 11){
                mult -= .5;
                j = 0;
            }
        }
        x = null;
        y = null;
        calculateDiff(ind * sampleRate / sampleSize);
        return Math.round(ind * sampleRate / sampleSize);
    }

    public int calculateDiff(double currentFrequency){
        TextView text = getView().findViewById(R.id.textview_first2);
        double targetFrequency = 0.0;
        switch (selectedString) {
            case 'E':
                if (custom_tuning){
                    targetFrequency = target_pitch_high_e_custom;
                } else{
                    targetFrequency = target_pitch_high_e_default;
                }
                break;
            case 'b':
                if (custom_tuning){
                    targetFrequency = target_pitch_b_custom;
                } else{
                    targetFrequency = target_pitch_b_default;
                }
                break;
            case 'g':
                if (custom_tuning){
                    targetFrequency = target_pitch_g_custom;
                } else{
                    targetFrequency = target_pitch_g_default;
                }
                break;
            case 'd':
                if (custom_tuning){
                    targetFrequency = target_pitch_d_custom;
                } else{
                    targetFrequency = target_pitch_d_default;
                }
                break;
            case 'a':
                if (custom_tuning){
                    targetFrequency = target_pitch_a_custom;
                } else{
                    targetFrequency = target_pitch_a_default;
                }
                break;
            case 'e':
                if (custom_tuning){
                    targetFrequency = target_pitch_low_e_custom;
                } else{
                    targetFrequency = target_pitch_low_e_default;
                }
                break;
            default:
                System.out.println("default");
                return 0;
        }
        double diff = 0.0;
        double stepAmount = 0.0;
        if (targetFrequency > currentFrequency){
            diff = targetFrequency - currentFrequency;
        } else {
            diff = currentFrequency - targetFrequency;
        }
        text.setText("Difference = " + (int)diff);

        return (int)diff;
    }

    public void getpitch(){
        int channel_config = AudioFormat.CHANNEL_IN_MONO;
        int format = AudioFormat.ENCODING_PCM_16BIT;
        int sampleSize = 5512;
        int bufferSize = 2756;
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
        audioBuffer = null;
        audioInput.release();
    }
}