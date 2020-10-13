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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.huawei.hms.image.render.IBindCallBack;
import com.huawei.hms.image.render.ImageRender;
import com.huawei.hms.image.render.ImageRenderImpl;
import com.huawei.hms.image.render.RenderView;
import com.huawei.hms.image.render.ResultCode;
import com.huawei.image.render.sample.R;
import com.huawei.image.render.sample.util.Utils;

import java.io.File;

/**
 * 功能描述
 *
 * @author c00511068
 * @since 2020-09-07
 */
public class ImageKitRenderDemoActivity1 extends Activity {
    /**
     * TAG
     */
    public static final String TAG = "ImageKitRenderDemo1";

    /**
     * 布Layout container
     */
    private FrameLayout contentView;

    // imageRender object
    private ImageRenderImpl imageRenderAPI;

    // Resource folder, which can be set as you want.
    private String sourcePath;

    /**
     * requestCode for applying for permissions.
     */
    public static final int PERMISSION_REQUEST_CODE = 0x01;

    private String hashCode;

    private View view;

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(R.layout.activity_image_kit_demo_1);
        sourcePath = getFilesDir().getPath() + File.separator + ImageKitRenderDemoActivity.SOURCE_PATH;
        initView();
        initImageRender();
    }


    /**
     * Initialize the view.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        contentView = findViewById(R.id.content);
    }

    /**
     * Get ImageRenderAPI
     */
    private void initImageRender() {
        Log.i(TAG, "timerecorde" + SystemClock.elapsedRealtime());
        Log.d(TAG, "initImageRender time = " + SystemClock.uptimeMillis());
        ImageRender.getInstance(this, new ImageRender.RenderCallBack() {
            @Override
            public void onSuccess(ImageRenderImpl imageRender) {
                imageRenderAPI = imageRender;
            }

            @Override
            public void onFailure(int i) {
                Log.e(TAG, "Get ImageRender instance failed, errorCode:" + i);
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        if (null != imageRenderAPI) {
            imageRenderAPI.bindRenderView(sourcePath, Utils.getAuthJson(), new IBindCallBack() {
                @Override
                public void onParseEnd() {

                }

                @Override
                public void onBind(RenderView renderView, int i) {
                    if (renderView != null) {
                        if (renderView.getResultCode() == ResultCode.SUCCEED) {
                            view = renderView.getView();
                            if (null != view) {
                                hashCode = String.valueOf(view.hashCode());
                                contentView.addView(view);
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
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
     * Start the animation
     *
     * @param view button
     */
    public void startAnimation(View view) {
        Log.i(TAG, "Start animation");
        if (null != imageRenderAPI) {
            int playResult = imageRenderAPI.playAnimation();
            Log.i(TAG, "Start animation:" + playResult);
        } else {
            Log.w(TAG, "Start animation fail, please init first.");
        }

    }

    /**
     * Pause the animation
     *
     * @param view button
     */
    public void pauseAnimation(View view) {
        // imageRender停止renderView动画
        Log.i(TAG, "Pause animation");
        if (null != imageRenderAPI) {
            int pauseResult = imageRenderAPI.pauseAnimation(true);
            if (pauseResult == ResultCode.SUCCEED) {
                Log.i(TAG, "Pause animation success");
            } else {
                Log.i(TAG, "Pause animation failure");
            }
        } else {
            Log.w(TAG, "Pause animation fail, please init first.");
        }
    }

    /**
     * Resume the animation
     *
     * @param view button
     */
    public void restartAnimation(View view) {
        Log.i(TAG, "restart animation");
        if (null != imageRenderAPI) {
            int playResult = imageRenderAPI.resumeAnimation();
            if (playResult == ResultCode.SUCCEED) {
                Log.i(TAG, "Restart animation success");
            } else {
                Log.i(TAG, "Restart animation failure");
            }
        } else {
            Log.w(TAG, "Restart animation fail, please init first.");
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
}
