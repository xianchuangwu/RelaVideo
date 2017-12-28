package baidu.encode;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.IOException;
import java.nio.ByteBuffer;

import video.com.relavideolibrary.Utils.FileManager;

import static junit.framework.Assert.assertEquals;

/**
 * Created by chad
 * Time 17/3/22
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */
@TargetApi(18)
public class MediaMerge {

    private static final String TAG = "MediaMuxerUtils";
    private static final boolean VERBOSE = false;
    private static final int MAX_SAMPLE_SIZE = 256 * 1024;
    private String[] sources;

    public MediaMerge(String[] sources) {
        this.sources = sources;
    }

    /**
     * Test: make sure the muxer handles both video and audio tracks correctly.
     */
    public void startMux(MediaMuxerListener mediaMuxerListener) throws Exception {
        this.mediaMuxerListener = mediaMuxerListener;
        String outputFile = FileManager.getVideoFile();
        Log.d(TAG, "outputFile:" + outputFile);

        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        Log.i(TAG, "source[0]:" + sources[0]);
        retrieverSrc.setDataSource(sources[0]);

        String testDegrees = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        Log.d(TAG, "source[0] degree is=" + testDegrees);
        cloneAndVerify(sources, outputFile, 2, Integer.parseInt(testDegrees));
    }

    /**
     * Using the MediaMerge to clone a media file.
     */
    private void cloneMediaUsingMuxer(String[] srcMedia, String dstMediaPath,
                                      int expectedTrackCount, int degrees) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcMedia[0]);
        int trackCount = extractor.getTrackCount();
        assertEquals("wrong number of tracks", expectedTrackCount, trackCount);
        // Set up MediaMerge for the destination.
        android.media.MediaMuxer muxer;
        muxer = new android.media.MediaMuxer(dstMediaPath, android.media.MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        // Set up the tracks.
        SparseIntArray indexMap = new SparseIntArray(trackCount);
        for (int i = 0; i < trackCount; i++) {
            extractor.selectTrack(i);
            MediaFormat format = extractor.getTrackFormat(i);
            int dstIndex = muxer.addTrack(format);
            indexMap.put(i, dstIndex);
        }
        // Copy the samples from MediaExtractor to MediaMerge.
        boolean sawEOS = false;
        int bufferSize = MAX_SAMPLE_SIZE;
        int frameCount = 0;
        int offset = 100;
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
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
                if (VERBOSE) {
                    Log.d(TAG, "saw input EOS.");
                }
                if (currentIndex < sources.length - 1) {
                    currentIndex++;
                    epochNow = lastPts;
                    extractor.release();
                    // reuse a new src
                    String path = srcMedia[currentIndex];
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
                if (VERBOSE) {
                    Log.d(TAG, "Frame (" + frameCount + ") " +
                            "PresentationTimeUs:" + bufferInfo.presentationTimeUs +
                            " Flags:" + bufferInfo.flags +
                            " TrackIndex:" + trackIndex +
                            " Size(KB) " + bufferInfo.size / 1024);
                }
            }
        }
        muxer.stop();
        muxer.release();
        return;
    }

    /**
     * Clones a media file and then compares against the source file to make
     * sure they match.
     */
    private void cloneAndVerify(String[] srcMedia, String outputMediaFile,
                                int expectedTrackCount, int degrees) throws IOException {
        try {
            cloneMediaUsingMuxer(srcMedia, outputMediaFile, expectedTrackCount, degrees);
            verifyAttributesMatch(srcMedia[0], outputMediaFile, degrees);
            // Check the sample on 1s and 0.5s.
//            verifySamplesMatch(srcMedia, outputMediaFile, 1000000);
//            verifySamplesMatch(srcMedia, outputMediaFile, 500000);
        } finally {
//            new File(outputMediaFile).delete();
        }
    }

    /**
     * Compares some attributes using MediaMetadataRetriever to make sure the
     * cloned media file matches the source file.
     */
    private void verifyAttributesMatch(String srcMedia, String testMediaPath,
                                       int degrees) {
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(srcMedia);
        MediaMetadataRetriever retrieverTest = new MediaMetadataRetriever();
        retrieverTest.setDataSource(testMediaPath);
        String testDegrees = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        if (testDegrees != null) {
            assertEquals("Different degrees", degrees,
                    Integer.parseInt(testDegrees));
        }
        String heightSrc = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        String heightTest = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        assertEquals("Different height", heightSrc,
                heightTest);
        String widthSrc = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String widthTest = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        assertEquals("Different height", widthSrc,
                widthTest);
        String durationSrc = retrieverSrc.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        String durationTest = retrieverTest.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        assertEquals("Different height", durationSrc,
                durationTest);
        retrieverSrc.release();
        retrieverTest.release();
        if (mediaMuxerListener != null) mediaMuxerListener.videoMergeSuccess(testMediaPath);
    }

    private MediaMuxerListener mediaMuxerListener;

    public interface MediaMuxerListener {
        void videoMergeSuccess(String outputPath);
    }
}
