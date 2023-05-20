package pri.prepare.lovehymn.client.tool;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;

/**
 * 音量工具类
 */
public class VolumeUtil {
    private AudioManager mAudioManager;

    public VolumeUtil() {
    }

    public VolumeUtil(Context context) {
        mAudioManager = (AudioManager) context.getSystemService(Service.AUDIO_SERVICE);
    }

    //获取最大多媒体音量
    public int getMediaMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
    }

    //获取当前多媒体音量
    public int getMediaVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    //获取最大通话音量
    public int getCallMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
    }

    //获取当前通话音量
    public int getCallVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
    }

    //获取最大系统音量
    public int getSystemMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
    }

    //获取当前系统音量
    public int getSystemVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_SYSTEM);
    }

    //获取最大提示音量
    public int getAlermMaxVolume() {
        return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
    }

    //获取当前提示音量
    public int getAlermVolume() {
        return mAudioManager.getStreamVolume(AudioManager.STREAM_ALARM);
    }

    // 设置多媒体音量
    public void setMediaVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, //音量类型
                volume,
                AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
    }

    /**
     * 如果音量为0 调节为40%
     */
    public void setMediaVolumeIfZero() {
        if (getMediaVolume() == 0)
            setMediaVolume(getMediaMaxVolume() * 4 / 10);
    }

    //设置通话音量
    public void setCallVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                volume,
                AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
    }

    //设置提示音量
    public void setAlermVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                volume, AudioManager.FLAG_PLAY_SOUND
                        | AudioManager.FLAG_SHOW_UI);
    }

    // 关闭/打开扬声器播放
    public void setSpeakerStatus(boolean on) {
        if (on) { //扬声器
            mAudioManager.setSpeakerphoneOn(true);
            mAudioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            // 设置最大音量
            int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, max, AudioManager.STREAM_VOICE_CALL);
            // 设置成听筒模式
            mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            mAudioManager.setSpeakerphoneOn(false);// 关闭扬声器
            mAudioManager.setRouting(AudioManager.MODE_NORMAL, AudioManager.ROUTE_EARPIECE, AudioManager.ROUTE_ALL);
        }
    }
}