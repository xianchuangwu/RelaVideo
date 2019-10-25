package video.com.relavideolibrary.Utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.function.Consumer;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import video.com.relavideolibrary.R;
import video.com.relavideolibrary.view.CustomDialog;

import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Created by chad
 * Time 2019-10-25
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */
public class SystemUtils {

    public interface PermissionCallback {
        void granted();//获取权限

        void denied();//拒绝权限

        void gotoSetting();//前往设置页设置权限
    }

    @SuppressLint("CheckResult")
    public static void requestCameraPermission(@NonNull final Activity activity, final PermissionCallback permissionCallback) {
        //oppo6.0以下，使用Camera时，系统自己会弹出权限请求
        Intent oppoIntent = activity.getPackageManager().getLaunchIntentForPackage("com.coloros.safecenter");
        if (oppoIntent != null && Build.VERSION.SDK_INT < Build.VERSION_CODES.M && permissionCallback != null) {
            permissionCallback.granted();
        } else {
            final String message = "在应用-Rela热拉-权限中开启相机、麦克风权限，以正常使用短视频、扫描二维码、拍照等功能";
            new RxPermissions(activity).
                    request(Manifest.permission.CAMERA
                            , Manifest.permission.RECORD_AUDIO
                            , Manifest.permission.READ_EXTERNAL_STORAGE
                            , Manifest.permission.WRITE_EXTERNAL_STORAGE).subscribe(new Observer<Boolean>() {
                @Override
                public void onSubscribe(Disposable d) {

                }

                @Override
                public void onNext(Boolean aBoolean) {
                    if (aBoolean) {
                        if (!isHasCameraPermission()) {
                            Toast.makeText(activity, "错误: 相机被占用！", Toast.LENGTH_SHORT).show();
                        } else if (!isHasAudioRecordPermission()) {
                            Toast.makeText(activity, "错误: 麦克风被占用！", Toast.LENGTH_SHORT).show();
                        } else {
                            // All requested permissions are granted
                            if (permissionCallback != null) permissionCallback.granted();
                        }
                    } else {
                        // At least one permission is denied
                        // Need to go to the settings
                        showGotoSettingDialog(activity, message, permissionCallback);
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onComplete() {

                }
            });
        }
    }

    private static void showGotoSettingDialog(@NonNull final Activity activity, final String message, final PermissionCallback permissionCallback) {

        CustomDialog.Builder builder = new CustomDialog.Builder(activity);
        builder.setMessage(message);
        builder.setTitle("权限申请");

        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (which == BUTTON_POSITIVE) {
                    if (permissionCallback != null) permissionCallback.gotoSetting();
                    //vivo和oppo的设置页无法设置权限，需要到'i 管家'app里设置
                    Intent vivoIntent = activity.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure");
                    Intent oppoIntent = activity.getPackageManager().getLaunchIntentForPackage("com.coloros.safecenter");

                    if (vivoIntent != null) {
                        activity.startActivity(vivoIntent);
                    } else if (oppoIntent != null) {
                        activity.startActivity(oppoIntent);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                    }
                } else {
                    dialog.dismiss();
                    if (permissionCallback != null) permissionCallback.denied();
                }
            }
        });
        builder.setNegativeButton(activity.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });


        builder.create().show();
    }

    /**
     * 针对定制系统做特殊权限判断
     * 获取app的录音权限是否打开
     */
    private static boolean isHasAudioRecordPermission() {
        // 音频获取源
        int audioSource = MediaRecorder.AudioSource.MIC;
        // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
        int sampleRateInHz = 44100;
        // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // 缓冲区字节大小
        int bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        //开始录制音频
        try {
            // 防止某些手机崩溃，例如联想
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        /**
         * 根据开始录音判断是否有录音权限
         */
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            return false;
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;
        return true;
    }

    /**
     * 针对定制系统做特殊权限判断
     * 捕获异常，判断是否有相机权限
     */
    private static boolean isHasCameraPermission() {
        try {
            Camera camera = Camera.open();
            Camera.Parameters mParameters = camera.getParameters();
            camera.setParameters(mParameters);
            camera.release();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
