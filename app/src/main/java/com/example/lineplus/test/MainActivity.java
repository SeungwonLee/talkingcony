package com.example.lineplus.test;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.Image;
import android.media.MediaRecorder;
import android.media.MediaSyncEvent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends ActionBarActivity {

    private static int RECORDER_SAMPLERATE = 8000; // 어떤 음질로 녹음할지에 대한 상수.
    private static int PLAY_SAMPLERATE = 11025; // 어떤 음질로 녹음할지에 대한 상수.
    private static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO; // CHANNEL 생성될 음성 파일의 채널을 이야기한다. 모노 채널은 1, 스테레오 채널은 2.
    private static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT; // PCM FORMAT 샘플링 레이트 당 비트 수. 인코딩 될 음성 파일의 데이터 포맷에 따라 달라진다.

    // file
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private static final String AUDIO_RECORDER_RAW = "raw.pcm";

    int bufferSizeInBytes;
    AudioTrack audioTrack;
    AudioRecord audioRecord;

    boolean keepPlaying = false;
    boolean keepAudioChecking = false;

    private static final int MIN_VOLUME = 0;
    private static final int MAX_VOLUME = 18000;

    TextView txt;
    ImageView imgView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        txt = (TextView) findViewById(R.id.txt);
        imgView = (ImageView) findViewById(R.id.imgview);

        bufferSizeInBytes = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE,
                RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING
        );

        audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                PLAY_SAMPLERATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes,
                AudioTrack.MODE_STREAM);

        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                RECORDER_SAMPLERATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSizeInBytes);

        (new RecordAudio1()).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void recodClose() {
        if (audioRecord != null) {
            System.out.println("stopRecord");
            keepAudioChecking = false;//Stop the file write
            audioRecord.stop();
            audioRecord.release();//Release resources
            audioRecord = null;
        }

    }

    private void playClose() {
        if (audioTrack != null) {
            System.out.println("stopPlay");
            keepPlaying = false;//Stop the file write
            audioTrack.stop();
            audioTrack.release();//Release resources
            audioTrack = null;
        }
    }

    private void PlayAudio() {

        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath + "/" + AUDIO_RECORDER_FOLDER, AUDIO_RECORDER_RAW);

        Log.d("playing", "audio playing.." + file.length());
        if (!keepPlaying || file.length() == 0) {
            return;
        }

        if (file.exists()) {
            InputStream is = null;
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            BufferedInputStream bis = new BufferedInputStream(is);
            DataInputStream dis = new DataInputStream(bis);
            int audioLength = (int) (file.length() / 2);
            short[] audioBuffer = new short[audioLength];

            Log.d("test", audioBuffer.length / 2 + "");

            int i = 0;
            try {
                while (dis.available() > 0) {
                    audioBuffer[i] = dis.readShort();
                    i++;
//                    Log.d("test", audioLength+"/"+i);
//                    if(audioLength <= i) {
//                        audioTrack.play();
//                        audioTrack.write(audioBuffer, 0, audioLength);
//                        keepAudioChecking = true;
//                        keepPlaying = false;
//                        file.delete();
//                        break;
//                    }
                }

                audioTrack.setNotificationMarkerPosition(audioLength);
                audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                    @Override
                    public void onPeriodicNotification(AudioTrack track) {
                        // nothing to do
                    }
                    @Override
                    public void onMarkerReached(AudioTrack track) {
                        Log.d("audio", "Audio track end of file reached...");
                        keepAudioChecking = true;
                        keepPlaying = false;
//                        file.delete();
                    }
                });
                audioTrack.play();
                audioTrack.write(audioBuffer, 0, audioLength);

                dis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
//        keepPlaying = false;
//        keepAudioChecking = true;

        // Close the input streams.
    }

    private DataOutputStream makeFile() {

        File file = null;
        OutputStream os = null;
        BufferedOutputStream bos = null;
        DataOutputStream dos = null;

        try {
            String filepath = Environment.getExternalStorageDirectory().getPath();
            file = new File(filepath + "/" + AUDIO_RECORDER_FOLDER, AUDIO_RECORDER_RAW);

            if (os != null){
                os.flush();
                os.close();
            }
            if(bos != null){
                bos.flush();
                bos.close();
            }
            if(dos != null){
                dos.flush();
                dos.close();
            }
            if (!file.exists())
                file.mkdir();

            if (file.exists()) {
                file.delete();
            }

            file.createNewFile();

            os = new FileOutputStream(file);
            bos = new BufferedOutputStream(os);
            dos = new DataOutputStream(bos);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return dos;
    }

    private class RecordAudio extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            keepAudioChecking = true;
            byte[] audioBuffer = new byte[bufferSizeInBytes];
            int totalCount = 0;
            int blockCount = 0;
            byte[] totalByteBuffer = new byte[60 * 2 * RECORDER_SAMPLERATE]; //PCM 데이터 처리량 -> 초당 데이터 량 = 샘플레이트 * 채널 수 * 비트

            DataOutputStream dos = null;

//            Queue q = new LinkedList<Array>();
//            q.add(audioBuffer);

            try {
                String filepath = Environment.getExternalStorageDirectory().getPath();
                File file = new File(filepath, AUDIO_RECORDER_FOLDER);

                if (!file.exists())
                    file.mkdir();

                if (file.exists()) {
                    file.delete();
                } else {
                    file.createNewFile();
                }

                String fileName = file.getAbsolutePath() + "/" + AUDIO_RECORDER_RAW;

                OutputStream os = new FileOutputStream(fileName);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                dos = new DataOutputStream(bos);

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                audioRecord.startRecording();

                while (true) {
                    if (keepAudioChecking) {
                        int numberOfByte = audioRecord.read(audioBuffer, 0, bufferSizeInBytes);

                        // Analyze Sound.
                        float totalAbsValue = 0.0f;
                        short sample = 0;

                        // Analyze Sound.
                        for (int i = 0; i < bufferSizeInBytes / 2; i++) {
                            sample = (short) ((audioBuffer[i]) | audioBuffer[i + 1] << 8); // short to byte
                            totalAbsValue += Math.abs(sample);
                        }
                        double avgValue = Math.abs(totalAbsValue / (numberOfByte / 2));
                        Log.d("avgValue", avgValue + "");


                        if (avgValue > MIN_VOLUME && avgValue < MAX_VOLUME) { // stop recording when playing

                        } else if (avgValue >= MAX_VOLUME && !keepPlaying) {
                            Log.d("playing", "recording");
                            if (blockCount < 8) {
                                for (int i = 0; i < numberOfByte; i++)
                                    totalByteBuffer[i + totalCount] = audioBuffer[i];

//                            totalCount += numberOfByte;

                            } else { // buffer to file
                                Log.d("playing", "init");
                                try {
                                    dos.write(totalByteBuffer);
                                    keepPlaying = true;
                                    keepAudioChecking = false;
                                    PlayAudio();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }

                                totalCount = 0;
                                blockCount = 0;
                                try {
                                    dos.flush();
                                    dos.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                    Log.d("L", "Listening");

                }
//
//                while(keepAudioChecking) {
//                    int bufferReadResult = audioRecord.read(audioBuffer, 0, bufferSizeInBytes);
//
//                    for (int i = 0; i < bufferReadResult; i++) {
//                        dos.writeByte(audioBuffer[i]);
//                    }
////Analyze Sound.
//                    float totalAbsValue = 0.0f;
//                    short sample = 0;
//
//                    // Analyze Sound.
//                    for (int i = 0; i < bufferSizeInBytes; i ++) {
//                        sample = (short) ((audioBuffer[i]) | audioBuffer[i + 1] << 8); // short to byte
//                        totalAbsValue += Math.abs(sample);
//                    }
//
//                    double avgValue = Math.abs(totalAbsValue / (bufferReadResult / 2));
//
//                    if (avgValue >= MAX_VOLUME && !keepPlaying) {
//
//                    }

//
//                }
//
//


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        recodClose();
        playClose();
        super.onDestroy();
    }

    private class RecordAudio1 extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            if (values[0] == 1) {
                Toast.makeText(getApplicationContext(), values[0] + "", Toast.LENGTH_LONG);
                txt.setText("recording");
                imgView.setImageResource(R.drawable.cony_speaking1);
            } else if (values[0] == 0) {
                txt.setText("listening");
                imgView.setImageResource(R.drawable.cony_listening1);

            } else if (values[0] == 2){
                txt.setText("speaking");
                imgView.setImageResource(R.drawable.cony_speaking1);
            }

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            keepAudioChecking = true;
            short[] audioBuffer = new short[bufferSizeInBytes];
            int blockCount = 0;
            boolean checkRecording = false;

            DataOutputStream dos = null;

            try {
                audioRecord.startRecording();


                dos = makeFile();

                while (true) {
                    if (!keepAudioChecking) continue;
//                for(int j=0; j<50; j++){
                    int numberOfByte = audioRecord.read(audioBuffer, 0, bufferSizeInBytes);

                    // Analyze Sound.
                    float totalAbsValue = 0.0f;
                    short sample = 0;

                    // Analyze Sound.
                    for (int i = 0; i < bufferSizeInBytes / 2; i++) {
                        sample = (short) ((audioBuffer[i]) | audioBuffer[i + 1] << 8); // short to byte
                        totalAbsValue += Math.abs(sample);
                    }

                    double avgValue = Math.abs(totalAbsValue / (numberOfByte / 2));
                    Log.d("audio", "avgValue" + avgValue + "/" + "blockCount:" + blockCount);

                    if (avgValue > MAX_VOLUME) { // recording
                        publishProgress((int) avgValue);
                        for (int i = 0; i < bufferSizeInBytes / 2; i++) {
                            dos.writeShort(audioBuffer[i]);
//                            totalByteBuffer[i + totalCount] = audioBuffer[i];
                        }
                        checkRecording = true;
                        Log.d("audio", "recording");
                    } else { // playing
                        publishProgress(0);
                        Log.d("Audio", "Listening");
                        if (checkRecording && blockCount > 10) {
                            checkRecording = false;
                            int x;
                            keepAudioChecking = false;
                            keepPlaying = true;
                            PlayAudio();
                            do{ // Montior playback to find when done
                                if(!keepPlaying)
                                    break;
                            }while (true);

                            blockCount = 0;
                            dos.flush();
                            dos.close();
                            dos = makeFile();

                        } else if (checkRecording) {
                            blockCount++;
                            publishProgress(1);
                            for (int i = 0; i < bufferSizeInBytes / 2; i++) {
                                dos.writeShort(audioBuffer[i]);
//                            totalByteBuffer[i + totalCount] = audioBuffer[i];
                            }
                            checkRecording = true;
                        }
                    }
                }
//                dos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
