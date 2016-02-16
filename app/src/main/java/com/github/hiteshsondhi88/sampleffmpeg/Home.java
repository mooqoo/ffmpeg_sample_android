package com.github.hiteshsondhi88.sampleffmpeg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.github.hiteshsondhi88.sampleffmpeg.Util.Util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Home extends Activity implements View.OnClickListener {

    private static final String TAG = Home.class.getSimpleName();

    @Inject
    FFmpeg ffmpeg;

    @InjectView(R.id.command)
    EditText commandEditText;

    @InjectView(R.id.command_output)
    LinearLayout outputLayout;

    @InjectView(R.id.run_command)
    Button runButton;

    private ProgressDialog progressDialog;

    private ArrayList<String> fileNameList = new ArrayList<>();
    private ArrayList<VideoDetail> VideoDetailList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);
        ObjectGraph.create(new DaggerDependencyModule(this)).inject(this);

        loadFFMpegBinary();
        initUI();
        Util.copyBinaryFromAssetsToData(this, "sample_videos/sample_2.mp4", "sample_2.mp4");
        Util.copyBinaryFromAssetsToData(this, "logowatermark.png", "logowatermark.png");

        // test ffmpeg command
        testCommand();

    }

    private void testCommand() {
        // test merge using complex filter
        /*
        String[] command = new String[]{
          "-i", "/sdcard/DCIM/Camera/Front.mp4", "-i", "/sdcard/DCIM/Camera/Front2.mp4",
          "-filter_complex", "[0:v:0] [0:a:0] [1:v:0] [1:a:0] concat=n=2:v=1:a=1 [v] [a]", "-map",
          "[v]", "-map", "[a]", "-strict", "-2", "-vcodec", "libx264", "-preset", "ultrafast", "/sdcard/concatOutput3.mp4"
        };
         */

        /*
        // rotate video and clear metadata
        String[] rotate = new String[]{
          "-i", "/sdcard/DCIM/Camera/Front.mp4", "-vf", "transpose=1", "-metadata:s:v:0", "rotate=0",
          "-strict", "-2", "-vcodec", "libx264", "-preset", "ultrafast", "/sdcard/tmp.mp4",
        };
        execFFmpegBinary(rotate);

        String[] rotate2 = new String[]{
          "-i", "/sdcard/DCIM/Camera/Back.mp4", "-vf", "transpose=2", "-metadata:s:v:0", "rotate=0",
          "-strict", "-2", "-vcodec", "libx264", "-preset", "ultrafast", "/sdcard/tmp2.mp4",
        };
        execFFmpegBinary(rotate2);
        */

        // test watermark
        File watermark = new File(getFilesDir(), "logowatermark.png");
        Log.d(TAG, "watermark = " + watermark.getAbsolutePath());

        // test video directory
        File dir = new File(Environment.getExternalStoragePublicDirectory(
          Environment.DIRECTORY_MOVIES), MediaFileHelper.DIRECTORY_ANCHOR);
        File video1 = new File(dir.getPath() + File.separator + "Test.mp4");
        File video2 = new File(dir.getPath() + File.separator + "Test2.mp4");
        File video3 = new File(dir.getPath() + File.separator + "Test3.mp4");

        Log.d(TAG, "video1=" + video1.getAbsolutePath());
        Log.d(TAG, "video2=" + video2.getAbsolutePath());

        fileNameList.add(video1.toString());
        fileNameList.add(video2.toString());
        fileNameList.add(video3.toString());


        /*
        String[] command = new String[]{
          "-i", video1.getAbsolutePath(), //"/sdcard/Download/tmp.mp4",
          "-i", video2.getAbsolutePath(), //"/sdcard/Download/tmp2.mp4",
          "-i", watermark.getAbsolutePath(), //"/sdcard/Download/watermark.png",
          "-filter_complex",
          //"[0:v:0] [1:v:0] [0:a:0] [1:a:0] concat=n=2:v=1:a=1 [v] [a]",
          "[0:v:0] scale=720:1280 [in1]; [1:v:0] scale=720:1280 [in2]; " +
            "[in1][0:a:0][in2][1:a:0] concat=n=2:v=1:a=1 [v][a]; " +
            "[v][2] overlay=main_w-overlay_w-30:main_h-overlay_h-30 [v_water]",
          "-map", "[v_water]", "-map", "[a]",
          "-strict", "-2", "-vcodec", "libx264", "-preset", "ultrafast",
          "/sdcard/watermark2.mp4"
        };
        */

        String[] command = new String[fileNameList.size()*2];
        for(int i = 0; i < fileNameList.size(); i++) {
            command[i*2] = "-i";
            command[i*2 + 1] = fileNameList.get(i);
        }

        /*
        String[] command = new String[] {
          "-i", video1.getAbsolutePath(),
          "-i", video2.getAbsolutePath(),
          "-i", video3.getAbsolutePath()
        };
        */

        // run the command
        execFFmpegBinary(command);

    }

    private void generateNoteOnSD(String sFileName, String sBody){
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void initUI() {
        runButton.setOnClickListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
    }

    private void loadFFMpegBinary() {
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        }
    }

    private void regexInfo(String msg) {
        //VideoDetail videoDetail = null;
        //regex string here
        Pattern pName = Pattern.compile("/.+\\.mp4");
        Matcher mName = pName.matcher(msg);
        while (mName.find()) {
            Log.d(TAG, "match result = " + mName.group());
            VideoDetailList.add(new VideoDetail(mName.group()));
        }

        // width / height
        Pattern p = Pattern.compile("\\d{3,4}x\\d{3,4}");
        Matcher m = p.matcher(msg);
        int index = 0;
        while (m.find()) {
            String[] dimens = m.group().split("x");
            Log.d(TAG, "match result = " + dimens[0] + ", " + dimens[1]);
            if(VideoDetailList.get(index) != null) {
                VideoDetailList.get(index).setWidth(Integer.parseInt(dimens[0]));
                VideoDetailList.get(index).setHeight(Integer.parseInt(dimens[1]));
                index++;
            }
        }

        //rotate          : 90
        Pattern p2 = Pattern.compile("rotate.*\\d");
        Matcher m2 = p2.matcher(msg);
        index = 0;
        while (m2.find()) {
            String[] rotate = m2.group().split(":\\s");
            Log.d(TAG, "match result = " + rotate[1]);
            if(VideoDetailList.get(index) != null) {
                VideoDetailList.get(index).setRotation(Integer.parseInt(rotate[1]));
                index++;
            }
        }

        // print log
        for(int j = 0; j < VideoDetailList.size(); j++) {
            Log.d(TAG, "videoDetail = " + VideoDetailList.get(j).toString());
        }
    }

    private void execFFmpegBinary(final String[] command) {
        try {
            ffmpeg.execute(command, new ExecuteBinaryResponseHandler() {
                @Override
                public void onFailure(String s) {
                    Log.d(TAG, "onFailure : ffmpeg " + s);
                    addTextViewToLayout("FAILED with output : " + s);

                    if(command.length %2 == 0) {
                        Log.d(TAG, "starts with -i, and ends with .mp4, its asking for video information");
                        regexInfo(s);
                    }
                }

                @Override
                public void onSuccess(String s) {
                    Log.d(TAG, "onSuccess : ffmpeg " + s);
                    addTextViewToLayout("SUCCESS with output : " + s);
                }

                @Override
                public void onProgress(String s) {
                    Log.d(TAG, "onProgress : ffmpeg " + s);
                    addTextViewToLayout("progress : " + s);
                    progressDialog.setMessage("Processing\n" + s);
                }

                @Override
                public void onStart() {
                    outputLayout.removeAllViews();

                    //Log.d(TAG, "Started command : ffmpeg " + command);
                    progressDialog.setMessage("Processing...");
                    progressDialog.show();
                }

                @Override
                public void onFinish() {
                    //Log.d(TAG, "Finished command : ffmpeg " + command);
                    progressDialog.dismiss();
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // do nothing for now
        }
    }

    private void addTextViewToLayout(String text) {
        TextView textView = new TextView(Home.this);
        textView.setText(text);
        outputLayout.addView(textView);
    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(Home.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(getString(R.string.device_not_supported))
                .setMessage(getString(R.string.device_not_supported_message))
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Home.this.finish();
                    }
                })
                .create()
                .show();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.run_command:
                String cmd = commandEditText.getText().toString();
                String[] command = cmd.split(" ");
                if (command.length != 0) {
                    execFFmpegBinary(command);
                } else {
                    Toast.makeText(Home.this, getString(R.string.empty_command_toast), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private File getMp4SampleFile() {
        return getSampleFile("sample_2.mp4");
    }

    private File getSampleFile(String sampleName) {
        File sample = new File(getFilesDir(), sampleName);
        Log.d(TAG,"getSampleFile: "+sampleName+ ", sample="+ sample);
        return sample;
    }
}
