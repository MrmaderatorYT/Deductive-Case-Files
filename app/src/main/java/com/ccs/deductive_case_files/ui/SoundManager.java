package com.ccs.deductive_case_files.ui;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

/** Tiny synthesized UI sounds; no audio assets are shipped. */
public final class SoundManager {
    public static final int TAP = 0, MARK = 1, OK = 2, BAD = 3, WIN = 4;
    private static final int RATE = 22050;
    private static final SoundManager INSTANCE = new SoundManager();

    private final AudioTrack[] tracks = new AudioTrack[5];
    private volatile boolean enabled = true, ready, loading;

    public static SoundManager get() {
        return INSTANCE;
    }

    public void setEnabled(boolean on) {
        enabled = on;
        if (on && !ready && !loading) {
            loading = true;
            new Thread(this::build, "sound-init").start();
        }
    }

    public void play(int sound) {
        if (!enabled || !ready) return;
        AudioTrack tr = tracks[sound];
        if (tr == null) return;
        try {
            if (tr.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) tr.stop();
            tr.reloadStaticData();
            tr.play();
        } catch (IllegalStateException ignored) {
            // audio is best-effort
        }
    }

    private void build() {
        try {
            tracks[TAP] = make(new float[]{1400}, 0.022f, 0.10f);
            tracks[MARK] = make(new float[]{880}, 0.05f, 0.16f);
            tracks[OK] = make(new float[]{660, 990}, 0.11f, 0.22f);
            tracks[BAD] = make(new float[]{233, 196}, 0.12f, 0.22f);
            tracks[WIN] = make(new float[]{523, 659, 784, 1047}, 0.13f, 0.24f);
            ready = true;
        } catch (RuntimeException ignored) {
            // device without audio output: stay silent
        }
        loading = false;
    }

    private static AudioTrack make(float[] notes, float noteSec, float volume) {
        int per = (int) (RATE * noteSec), total = per * notes.length;
        short[] pcm = new short[total];
        for (int k = 0; k < notes.length; k++) {
            for (int i = 0; i < per; i++) {
                float env = (float) Math.sin(Math.PI * i / per);
                double s = Math.sin(2 * Math.PI * notes[k] * i / RATE);
                pcm[k * per + i] = (short) (s * env * volume * Short.MAX_VALUE);
            }
        }
        AudioTrack tr = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                .setAudioFormat(new AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
                .setTransferMode(AudioTrack.MODE_STATIC)
                .setBufferSizeInBytes(total * 2)
                .build();
        tr.write(pcm, 0, total);
        return tr;
    }
}
