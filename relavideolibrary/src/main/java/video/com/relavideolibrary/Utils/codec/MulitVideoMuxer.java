package video.com.relavideolibrary.Utils.codec;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import video.com.relavideolibrary.Utils.FileManager;

import static junit.framework.Assert.assertEquals;

/**
 * Created by chad
 * Time 18/1/18
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 合成几个格式相同的video
 */
@TargetApi(18)
public class MulitVideoMuxer {

    public static final String TAG = "MulitVideoMuxer";
    private static final int MAX_SAMPLE_SIZE = 256 * 1024;
    private List<String> sources = new ArrayList<>();
    private String outputPath;

    public MulitVideoMuxer(List<String> pathList) {
        sources.addAll(pathList);
        outputPath = FileManager.getVideoFile();
    }

    /**
     * Test: make sure the muxer handles both video and audio tracks correctly.
     */
    public void start(MediaMuxerListener listener) {
        this.mediaMuxerListener = listener;
        if (sources.size() == 1) {//只有一段视频，跳过合成
            if (mediaMuxerListener != null) mediaMuxerListener.videoMergeSuccess(sources.get(0));
            return;
        }

        MediaMuxer muxer = null;
        boolean sawEOS = false;
        try {

            //当由于录制时间过短，可能有些视频会有问题，在这里过滤一边
            List<String> list = new ArrayList<>();
            for (int i = 0; i < sources.size(); i++) {
                MediaExtractor mediaExtractor = null;
                try {
                    mediaExtractor = new MediaExtractor();
                    mediaExtractor.setDataSource(sources.get(i));
                    int trackCount = mediaExtractor.getTrackCount();
                    //只合成包含视频信道和音频信道的视频
                    if (trackCount == 2) list.add(sources.get(i));
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(TAG, "No such file or directory");
                } finally {
                    if (mediaExtractor != null) mediaExtractor.release();
                }

            }
            sources = list;
            if (sources.size() == 0 && mediaMuxerListener != null) {
                mediaMuxerListener.videoMergeError("录制片段太短，无法合并");
                return;
            }

            MediaMetadataRetriever retrieverTest = new MediaMetadataRetriever();
            retrieverTest.setDataSource(sources.get(0));
            String degreesStr = retrieverTest.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            int degrees = 0;
            if (!TextUtils.isEmpty(degreesStr)) {
                degrees = Integer.parseInt(degreesStr);
            }
            retrieverTest.release();

            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(sources.get(0));
            int trackCount = extractor.getTrackCount();
            assertEquals("wrong number of tracks", 2, trackCount);
            // Set up MulitVideoMuxer for the destination.
            muxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            // Set up the tracks.
            SparseIntArray indexMap = new SparseIntArray(trackCount);
            for (int i = 0; i < trackCount; i++) {
                extractor.selectTrack(i);
                MediaFormat format = extractor.getTrackFormat(i);
                int dstIndex = muxer.addTrack(format);
                indexMap.put(i, dstIndex);
            }
            // Copy the samples from MediaExtractor to MulitVideoMuxer.
            int frameCount = 0;
            int offset = 100;
            ByteBuffer dstBuf = ByteBuffer.allocate(MAX_SAMPLE_SIZE);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            if (degrees >= 0) {
                muxer.setOrientationHint(degrees);
            }
            muxer.start();
            int currentIndex = 0;
            long lastPts = 0L;
            long epochNow = 0L;
            while (!sawEOS) {
                bufferInfo.offset = offset;
                bufferInfo.size = extractor.readSampleData(dstBuf, offset);
                if (bufferInfo.size < 0) {
                    Log.d(TAG, "saw input EOS.");
                    if (currentIndex < sources.size() - 1) {
                        currentIndex++;
                        epochNow = lastPts;
                        extractor.release();
                        // reuse a new src
                        String path = sources.get(currentIndex);
                        extractor = new MediaExtractor();
                        extractor.setDataSource(path);
                        trackCount = extractor.getTrackCount();
                        for (int i = 0; i < trackCount; i++) {
                            extractor.selectTrack(i);
                        }

                    } else {
                        sawEOS = true;
                        bufferInfo.size = 0;
                    }

                } else {
                    bufferInfo.presentationTimeUs = epochNow + extractor.getSampleTime();
                    lastPts = bufferInfo.presentationTimeUs;
                    bufferInfo.flags = extractor.getSampleFlags();
                    Log.d(TAG, "flags = " + bufferInfo.flags + ";pts=" + bufferInfo.presentationTimeUs);
//                switch () {
//                    case MediaCodec.BUFFER_FLAG_END_OF_STREAM:
//                        break;
//                    default:
//                        break;
//                }
                    int trackIndex = extractor.getSampleTrackIndex();
                    muxer.writeSampleData(indexMap.get(trackIndex), dstBuf,
                            bufferInfo);
                    extractor.advance();
                    frameCount++;
                    Log.d(TAG, "Frame (" + frameCount + ") " +
                            "PresentationTimeUs:" + bufferInfo.presentationTimeUs +
                            " Flags:" + bufferInfo.flags +
                            " TrackIndex:" + trackIndex +
                            " Size(KB) " + bufferInfo.size / 1024);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (mediaMuxerListener != null) mediaMuxerListener.videoMergeError(e.getMessage());
        } finally {
            if (muxer != null) {
                muxer.stop();
                muxer.release();
            }
            if (sawEOS && mediaMuxerListener != null)
                mediaMuxerListener.videoMergeSuccess(outputPath);
        }
    }

    private MediaMuxerListener mediaMuxerListener;

    public interface MediaMuxerListener {
        void videoMergeSuccess(String outputPath);

        void videoMergeError(String messge);
    }
}
