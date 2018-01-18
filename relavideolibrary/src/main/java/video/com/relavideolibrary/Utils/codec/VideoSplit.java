package video.com.relavideolibrary.Utils.codec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by chad
 * Time 18/1/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 视频剪裁
 */
@TargetApi(18)
public class VideoSplit {
    private final static String TAG = "VideoDecoder";
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;

    public VideoSplit(SplitVideoListener listener) {
        this.splitVideoListener = listener;
    }

    /**
     * 视频剪裁 /也可以实现去音频操作(startms = -1,endms = -1,userAudio = false,userVideo = true)
     *
     * @param srcPath
     * @param dstPath
     * @param startMs
     * @param endMs
     * @param useAudio
     * @param useVideo
     * @throws IOException
     */
    public void start(String srcPath, String dstPath, long startMs, long endMs, boolean useAudio, boolean useVideo) {

        MediaMuxer muxer = null;
        try {
            // Set up MediaExtractor to read from the source.
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(srcPath);
            int trackCount = extractor.getTrackCount();
            // Set up MediaMuxer for the destination.
            muxer = new MediaMuxer(dstPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            // Set up the tracks and retrieve the max buffer size for selected
            // tracks.
            SparseIntArray indexMap = new SparseIntArray(trackCount);
            int bufferSize = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                boolean selectCurrentTrack = false;
                if (mime.startsWith("audio/") && useAudio) {
                    selectCurrentTrack = true;
                } else if (mime.startsWith("video/") && useVideo) {
                    selectCurrentTrack = true;
                }
                if (selectCurrentTrack) {
                    extractor.selectTrack(i);
                    int dstIndex = muxer.addTrack(format);
                    indexMap.put(i, dstIndex);
                    if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) {
                        int newSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE);
                        bufferSize = newSize > bufferSize ? newSize : bufferSize;
                    }
                }
            }
            if (bufferSize < 0) {
                bufferSize = DEFAULT_BUFFER_SIZE;
            }
            // Set up the orientation and starting time for extractor.
            MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
            retrieverSrc.setDataSource(srcPath);
            String degreesString = retrieverSrc.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            if (degreesString != null) {
                int degrees = Integer.parseInt(degreesString);
                if (degrees >= 0) {
                    muxer.setOrientationHint(degrees);
                }
            }
            if (startMs > 0) {
                extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            }
            // Copy the samples from MediaExtractor to MediaMuxer. We will loop
            // for copying each sample and stop when we get to the end of the source
            // file or exceed the end time of the trimming.
            int offset = 0;
            int trackIndex = -1;
            ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();

            muxer.start();
            while (true) {
                bufferInfo.offset = offset;
                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
                if (bufferInfo.size < 0) {
                    Log.d(TAG, "Saw input EOS.");
                    bufferInfo.size = 0;
                    break;
                } else {
                    bufferInfo.presentationTimeUs = extractor.getSampleTime();
                    if (endMs > 0 && bufferInfo.presentationTimeUs > (endMs * 1000)) {
                        Log.d(TAG, "The current sample is over the trim end time.");
                        break;
                    } else {
                        bufferInfo.flags = extractor.getSampleFlags();
                        trackIndex = extractor.getSampleTrackIndex();
                        muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
                                bufferInfo);
                        extractor.advance();
                    }
                }
            }
            muxer.stop();
            if (splitVideoListener != null) splitVideoListener.onSplitVideoSuccess(dstPath);
        } catch (Exception e) {
            // Swallow the exception due to malformed source.
            Log.w(TAG, "The source video file is malformed");
            if (splitVideoListener != null) splitVideoListener.onSplitVideoError(e.getMessage());
        } finally {
            if (muxer != null) {
//                muxer.stop();
                muxer.release();
            }
        }
    }

    private SplitVideoListener splitVideoListener;

    public interface SplitVideoListener {
        void onSplitVideoSuccess(String output);

        void onSplitVideoError(String message);
    }
}
