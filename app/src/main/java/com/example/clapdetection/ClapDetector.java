package com.example.clapdetection;

import android.util.Log;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.util.fft.FFT;

    public class ClapDetector implements AudioProcessor {
    private static final double ENERGY_THRESHOLD = 0.1; // Ngưỡng năng lượng tiếng vỗ tay

    private FFT fft;
    private float[] amplitudes;

    public ClapDetector(int bufferSize) {
        fft = new FFT(bufferSize);
        amplitudes = new float[bufferSize / 2 + 1];
    }

    @Override
    public boolean process(AudioEvent audioEvent) {
        float[] audioBuffer = audioEvent.getFloatBuffer();

        fft.forwardTransform(audioBuffer);
        fft.modulus(audioBuffer, amplitudes);

        float energy = getEnergy(amplitudes);

        if (energy > ENERGY_THRESHOLD) {
            Log.d("AAAAAAAAAAAAAAAAAAAAAAAAA", "clap detected!");
        }

        return true;
    }

    private float getEnergy(float[] amplitudes) {
        float energy = 0.0F;

        for (float amplitude : amplitudes) {
            energy += amplitude * amplitude;
        }
        return energy;
    }

    @Override
    public void processingFinished() {
        // Xử lý khi kết thúc xử lý âm thanh
    }
}