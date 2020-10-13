/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
package com.huawei.image.render.sample.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.image.render.IBindCallBack;
import com.huawei.hms.image.render.IStreamCallBack;
import com.huawei.hms.image.render.ImageRender;
import com.huawei.hms.image.render.ImageRenderImpl;
import com.huawei.hms.image.render.RenderView;
import com.huawei.hms.image.render.ResultCode;
import com.huawei.image.render.sample.R;
import com.huawei.image.render.sample.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * The ImageRenderSample code provides examples of initializing the service, obtaining views, playing animation, pausing animation, and destroying resources.
 *
 * @author huawei
 * @since 5.0.0
 */
public class ImageKitRenderDemoActivity extends Activity {

    /**
     * TAG
     */
    public static final String TAG = "ImageKitRenderDemo";

    /**
     * Layout container
     */
    private FrameLayout contentView;

    // imageRender object
    private ImageRenderImpl imageRenderAPI;

    /**
     * Resource folder, which can be set as you want.
     */
    public static final String SOURCE_PATH = "sources";

    private String sourcePath;

    private TextView textProgress;

    private String mCurrentDemo;

    private int marqueeVis = 1;

    private String hashCode;

    /**
     * requestCode for applying for permissions.
     */
    public static final int PERMISSION_REQUEST_CODE = 0x01;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_kit_demo);
        sourcePath = getFilesDir().getPath() + File.separator + SOURCE_PATH;
        initView();
        int permissionCheck = ContextCompat.checkSelfPermission(ImageKitRenderDemoActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            initData();
            initImageRender();
        } else {
            ActivityCompat.requestPermissions(ImageKitRenderDemoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }


    /**
     * Initialize the view.
     */
    private void initView() {
        contentView = findViewById(R.id.content);
        textProgress = findViewById(R.id.text_progress);
        final Spinner spinner = findViewById(R.id.spinner_animations);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCurrentDemo = spinner.getAdapter().getItem(position).toString();
                changeAnimation(mCurrentDemo);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * change the animation which is choose in spinner
     * @param animationName animationName
     */
    private void changeAnimation(String animationName) {
        if(!Utils.copyAssetsFilesToDirs(this, animationName, sourcePath)){
            Log.e(TAG, "copy files failure, please check permissions");
            return;
        }
        if (imageRenderAPI == null) {
            Log.e(TAG, "initRemote fail, please check kit version");
            return;
        }
        if(contentView.getChildCount() > 0) {
            imageRenderAPI.removeRenderView();
            contentView.removeAllViews();
            addView();
        }
    }

    /**
     * Create default resources.
     * You can compile the manifest.xml file and image resource file. The code is for reference only.
     */
    private void initData() {
        // Absolute path of the resource files.

        if (!Utils.createResourceDirs(sourcePath)) {
            Log.e(TAG, "Create dirs fail, please check permission");
        }

        if (!Utils.copyAssetsFileToDirs(this, "AlphaAnimation" + File.separator + "aixin7.png", sourcePath + File.separator + "aixin7.png")) {
            Log.e(TAG, "Copy resource file fail, please check permission");
        }
        if (!Utils.copyAssetsFileToDirs(this, "AlphaAnimation" + File.separator + "bj.jpg", sourcePath + File.separator + "bj.jpg")) {
            Log.e(TAG, "Copy resource file fail, please check permission");
        }
        if (!Utils.copyAssetsFileToDirs(this, "AlphaAnimation" + File.separator + "manifest.xml", sourcePath + File.separator + "manifest.xml")) {
            Log.e(TAG, "Copy resource file fail, please check permission");
        }
    }

    /**
     * Use the ImageRender API.
     */
    private void initImageRender() {
        // Obtain an ImageRender object.
        ImageRender.getInstance(this, new ImageRender.RenderCallBack() {
            @Override
            public void onSuccess(ImageRenderImpl imageRender) {
                Log.i(TAG, "getImageRenderAPI success");
                imageRenderAPI = imageRender;
                useImageRender();
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "getImageRenderAPI failure, errorCode = " + i);
            }
        });
    }

    /**
     * The Image Render service is required.
     */
    private void useImageRender() {
        if (imageRenderAPI == null) {
            Log.e(TAG, "initRemote fail, please check kit version");
            return;
        }
        addView();
    }

    private void addView () {
        // Initialize the ImageRender object.
        int initResult = imageRenderAPI.doInit(sourcePath, Utils.getAuthJson());
        Log.i(TAG, "DoInit result == " + initResult);
        if (initResult == 0) {
            // Obtain the rendered view.
            RenderView renderView = imageRenderAPI.getRenderView();
            if (renderView.getResultCode() == ResultCode.SUCCEED) {
                View view = renderView.getView();
                if (null != view) {
                    // Add the rendered view to the layout.
                    contentView.addView(view);
                    hashCode = String.valueOf(view.hashCode());
                } else {
                    Log.w(TAG, "GetRenderView fail, view is null");
                }
            } else if (renderView.getResultCode() == ResultCode.ERROR_GET_RENDER_VIEW_FAILURE) {
                Log.w(TAG, "GetRenderView fail");
            } else if (renderView.getResultCode() == ResultCode.ERROR_XSD_CHECK_FAILURE) {
                Log.w(TAG, "GetRenderView fail, resource file parameter error, please check resource file.");
            } else if (renderView.getResultCode() == ResultCode.ERROR_VIEW_PARSE_FAILURE) {
                Log.w(TAG, "GetRenderView fail, resource file parsing failed, please check resource file.");
            } else if (renderView.getResultCode() == ResultCode.ERROR_REMOTE) {
                Log.w(TAG, "GetRenderView fail, remote call failed, please check HMS service");
            } else if (renderView.getResultCode() == ResultCode.ERROR_DOINIT) {
                Log.w(TAG, "GetRenderView fail, init failed, please init again");
            }
        } else {
            Log.w(TAG, "Do init fail, errorCode == " + initResult);
        }
    }

    @Override
    protected void onDestroy() {
        // Destroy the view.
        if (null != imageRenderAPI) {
            imageRenderAPI.removeRenderView();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (null != imageRenderAPI) {
            imageRenderAPI.bindRenderView(sourcePath, Utils.getAuthJson(), new IBindCallBack() {
                @Override
                public void onBind(RenderView renderView, int i) {
                    if (renderView != null) {
                        if (renderView.getResultCode() == ResultCode.SUCCEED) {
                            final View view = renderView.getView();
                            if (null != view) {
                                contentView.addView(view);
                                hashCode = String.valueOf(view.hashCode());
                            }
                        }
                    }
                }

                @Override
                public void onParseEnd() {

                }
            });
        }
    }

    /**
     * Play the animation.
     *
     * @param view button
     */
    public void startAnimation(View view) {
        // Play the rendered view.
        Log.i(TAG, "Start animation");
        if (null != imageRenderAPI) {
            int playResult = imageRenderAPI.playAnimation();
            if (playResult == ResultCode.SUCCEED) {
                Log.i(TAG, "Start animation success");
            } else {
                Log.i(TAG, "Start animation failure");
            }
        } else {
            Log.w(TAG, "Start animation fail, please init first.");
        }
    }

    /**
     * Stop the animation.
     *
     * @param view button
     */
    public void stopAnimation(View view) {
        // Stop the renderView animation.
        Log.i(TAG, "Stop animation");
        if (null != imageRenderAPI) {
            int playResult = imageRenderAPI.stopAnimation();
            if (playResult == ResultCode.SUCCEED) {
                Log.i(TAG, "Stop animation success");
            } else {
                Log.i(TAG, "Stop animation failure");
            }
        } else {
            Log.w(TAG, "Stop animation fail, please init first.");
        }
    }

    /**
     * Pause animation
     *
     * @param view button
     */
    public void pauseAnimation(View view) {
        if (imageRenderAPI != null) {
            int result = imageRenderAPI.pauseAnimation(true);
            Log.d(TAG, "pauseAnimation result == " + result);
        }
    }

    /**
     * Resume animation
     *
     * @param view button
     */
    public void resumeAnimation(View view) {
        if (imageRenderAPI != null) {
            int result = imageRenderAPI.resumeAnimation();
            Log.d(TAG, "resumeAnimation result == " + result);
        }
    }

    /**
     * Set variable value
     *
     * @param view button
     */
    public void setVariable(View view) {
        if ("Marquee".equals(mCurrentDemo)) {
            marqueeVis = 1 - marqueeVis;
            imageRenderAPI.setKeyValueInfo("SetVariable", "var", "" + marqueeVis);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        if (null != imageRenderAPI) {
            imageRenderAPI.unBindRenderView(hashCode);
        }
    }

    /**
     * Next page
     *
     * @param view button
     */
    public void nextPage(View view) {
        Intent intent =  new Intent(ImageKitRenderDemoActivity.this, ImageKitRenderDemoActivity1.class);
        startActivity(intent);
    }

    /**
     * Start record.
     *
     * @param view button
     */
    public void startRecord(View view) {
        Log.d("#######start record:", System.currentTimeMillis() + "");
        Toast.makeText(this, "start record", Toast.LENGTH_SHORT).show();
        if (null != imageRenderAPI) {
            final JSONObject result = getRecordInfo();
            int start = imageRenderAPI.startRecord(result, new IStreamCallBack() {
                @Override
                public void onRecordSuccess(HashMap<String, Object> hashMap) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImageKitRenderDemoActivity.this, "record success", Toast.LENGTH_SHORT).show();
                        }
                    });
                    saveRecordResult(hashMap);
                }

                @Override
                public void onRecordFailure(HashMap<String, Object> hashMap) {
                    int errorCode = (int) hashMap.get("errorCode");
                    String errorMessage = (String) hashMap.get("errorMessage");
                    Log.i(TAG, "back result" + errorCode + ";back msg" + errorMessage);
                }

                // progress:{1-100}
                @Override
                public void onProgress(final int progress) {
                    showRecordProgress(progress);
                }
            });
            if (start == ResultCode.SUCCEED) {
                Log.i(TAG, "start record success");
            } else {
                Log.i(TAG, "start record failure:" + start);
            }
        } else {
            Log.w(TAG, "start record fail, please init first.");
        }
    }

    private JSONObject getRecordInfo () {
        final JSONObject result = new JSONObject();
        final String recordType = ((EditText) findViewById(R.id.recordtype)).getText().toString();
        String videoScale = ((EditText) findViewById(R.id.videoscale)).getText().toString();
        String videoFps = ((EditText) findViewById(R.id.videofps)).getText().toString();
        String gifScale = ((EditText) findViewById(R.id.gifscale)).getText().toString();
        String gifFps = ((EditText) findViewById(R.id.giffps)).getText().toString();
        try {
            JSONObject videoJson = new JSONObject();
            JSONObject gifJson = new JSONObject();
            result.put("recordType", recordType);
            videoJson.put("videoScale", videoScale);
            videoJson.put("videoFps", videoFps);
            gifJson.put("gifScale", gifScale);
            gifJson.put("gifFps", gifFps);
            result.put("video", videoJson);
            result.put("gif", gifJson);
        } catch (JSONException e) {
            Log.w(TAG, e.getMessage());
        }
        return result;
    }

    private void saveRecordResult (HashMap<String, Object> hashMap) {
        String fileName = Environment.getExternalStorageDirectory()
                + File.separator
                + "VideoAndPic";
        File fileDir = new File(fileName);
        if (!fileDir.exists()) {
            if (!fileDir.mkdir()) {
                return;
            }
        }
        final String mp4Path = fileName + File.separator + System.currentTimeMillis() + ".mp4";
        final String gifPath = fileName + File.separator + System.currentTimeMillis() + ".gif";
        String recordType = (String) hashMap.get("recordType");
        byte[] videoBytes = (byte[]) hashMap.get("videoBytes");
        byte[] gifBytes = (byte[]) hashMap.get("gifBytes");
        try {
            if (recordType.equals("1")) {
                if (videoBytes != null) {
                    saveFile(videoBytes, mp4Path);
                }
            } else if (recordType.equals("2")) {
                if (gifBytes != null) {
                    saveFile(gifBytes, gifPath);
                }
            } else if (recordType.equals("3")) {
                if (videoBytes != null) {
                    saveFile(videoBytes, mp4Path);
                }

                if (gifBytes != null) {
                    saveFile(gifBytes, gifPath);
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e.getMessage());
        }
    }

    private void saveFile (byte[] bytes, String path) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(path));
        try {
            fos.write(bytes, 0, bytes.length);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private void showRecordProgress (final int progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textProgress.setText("progress:" + progress + "%");
            }
        });
    }

    /**
     * Stop record.
     *
     * @param view button
     */
    public void stopRecord(View view) {
        Toast.makeText(this, "stop record", Toast.LENGTH_SHORT).show();
        if (null != imageRenderAPI) {
            int result = imageRenderAPI.stopRecord();
            if (result == ResultCode.SUCCEED) {
                Log.i(TAG, "stop record success");
            } else {
                Log.i(TAG, "stop record failure:" + result);
            }
        } else {
            Log.w(TAG, "stop record fail, please init first.");
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // The permission is granted.
                initData();
                initImageRender();
            } else {
                // The permission is rejected.
                Log.w(TAG, "permission denied");
                Toast.makeText(ImageKitRenderDemoActivity.this, "Please grant the app the permission to read the SD card", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
