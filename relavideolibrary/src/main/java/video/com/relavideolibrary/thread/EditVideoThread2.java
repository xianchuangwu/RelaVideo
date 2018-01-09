package video.com.relavideolibrary.thread;

import android.graphics.BitmapFactory;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Build;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.util.TimingLogger;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import baidu.encode.ExtractDecodeEditEncodeMux;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLookupFilter;
import video.com.relavideolibrary.RelaVideoSDK;
import video.com.relavideolibrary.Utils.Constant;
import video.com.relavideolibrary.Utils.FileManager;
import video.com.relavideolibrary.audio.AudioCodec;
import video.com.relavideolibrary.jni.AudioJniUtils;
import video.com.relavideolibrary.manager.VideoManager;

/**
 * Created by chad
 * Time 18/1/9
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class EditVideoThread2 extends Thread implements ExtractDecodeEditEncodeMux.ResultListener {

    public static final String TAG = "EditVideoThread";

    private EditVideoThread.EditVideoListener editVideoListener;

    private final String mVideoPath;
    private final int filterId;
    //adb shell setprop log.tag.EditVideoThread VERBOSE
    private final TimingLogger timingLogger;

    public EditVideoThread2(EditVideoThread.EditVideoListener editVideoListener) {
        this.editVideoListener = editVideoListener;
        mVideoPath = VideoManager.getInstance().getVideoBean().videoPath;
        filterId = VideoManager.getInstance().getVideoBean().filterId;
        timingLogger = new TimingLogger(TAG, "edit video");
    }

    @Override
    public void run() {
        super.run();
        if (filterId == -1) {
            printMsg("未添加滤镜,直接合成背景乐");
            addBGM(mVideoPath);
        } else {
            printMsg("开始合成滤镜");
            generateFilter();
        }
    }

    private void generateFilter() {
        ExtractDecodeEditEncodeMux test = new ExtractDecodeEditEncodeMux(RelaVideoSDK.context);
        if (filterId != -1) {
            test.setFilterType(generateGPUImageFilter(filterId));
        }
        try {
            test.testExtractDecodeEditEncodeMuxAudioVideo(mVideoPath, this);
        } catch (Throwable tr) {
            if (editVideoListener != null)
                editVideoListener.onEditVideoError("generate filter failed");

        }
    }

    private void addBGM(String path) {
        String musicPath = VideoManager.getInstance().getMusicBean().url;
        if (TextUtils.isEmpty(musicPath)) {
            printMsg("未添加背景乐");
            timingLogger.dumpToLog();
            if (editVideoListener != null) editVideoListener.onEditVideoSuccess(path);
        } else {
            printMsg("开始合成背景乐");

            ArrayMap<MediaExtractor, MediaFormat> rawMp4VideoExtractorMap = extractorVideoTrack(path);
            ArrayMap<MediaExtractor, MediaFormat> rawMp4AudioExtractorMap = extractorAudioTrack(path);
            ArrayMap<MediaExtractor, MediaFormat> rawMp3AudioExtractorMap = extractorAudioTrack(musicPath);

            String mp4AudioPCM = setFile("raw_mp4_audio.pcm");
            audioTrack2PCM(rawMp4AudioExtractorMap.keyAt(0), rawMp4AudioExtractorMap.valueAt(0), mp4AudioPCM);
            String mp3AudioPCM = setFile("raw_mp3_audio.pcm");
            audioTrack2PCM(rawMp3AudioExtractorMap.keyAt(0), rawMp3AudioExtractorMap.valueAt(0), mp3AudioPCM);

            //测试
//            PCMEncodeAAC(mp4AudioPCM,setFile("raw_mp4_audio.aac"));
//            PCMEncodeAAC(mp3AudioPCM,setFile("raw_mp3_audio.aac"));
//            if (editVideoListener != null) editVideoListener.onEditVideoSuccess("");

            String audioMergePath = setFile("merge.aac");
            mulitPCMMix(mp4AudioPCM, mp3AudioPCM, audioMergePath, VideoManager.getInstance().getVideoVolumn(), VideoManager.getInstance().getMusicVolumn());

            ArrayMap<MediaExtractor, MediaFormat> mergeAudioExtractorMap = extractorAudioTrack(audioMergePath);
            mixVideo(rawMp4VideoExtractorMap.keyAt(0), rawMp4VideoExtractorMap.valueAt(0)
                    , mergeAudioExtractorMap.keyAt(0), mergeAudioExtractorMap.valueAt(0)
                    , setFile("final.mp4"));

        }
    }

    private ArrayMap<MediaExtractor, MediaFormat> extractorVideoTrack(String srcVideo) {
        MediaExtractor rawMp4VideoExtractor = new MediaExtractor();
        try {
            rawMp4VideoExtractor.setDataSource(srcVideo);
            MediaFormat rawMp4VideoFormat = null;
            for (int i = 0; i < rawMp4VideoExtractor.getTrackCount(); i++) {
                MediaFormat trackFormat = rawMp4VideoExtractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    printMsg("raw mp4 video mime :" + mime);
                    rawMp4VideoExtractor.selectTrack(i);
                    rawMp4VideoFormat = trackFormat;
                    printMsg("raw mp4 video format :" + rawMp4VideoFormat.toString());
                    break;
                }
            }

            if (rawMp4VideoFormat == null) {
                printMsg("raw mp4 video format null");
                rawMp4VideoExtractor.release();
                return null;
            }
            ArrayMap<MediaExtractor, MediaFormat> mediaExtractorMediaFormatMap = new ArrayMap<>();
            mediaExtractorMediaFormatMap.put(rawMp4VideoExtractor, rawMp4VideoFormat);
            return mediaExtractorMediaFormatMap;
        } catch (IOException e) {
            e.printStackTrace();
            printMsg("extractorVideoTrack error");
            if (editVideoListener != null) editVideoListener.onEditVideoError(e.getMessage());
            return null;
        }
    }

    private ArrayMap<MediaExtractor, MediaFormat> extractorAudioTrack(String srcAudio) {
        MediaExtractor rawAudioExtractor = new MediaExtractor();
        MediaFormat rawAudioFormat = null;
        try {
            rawAudioExtractor.setDataSource(srcAudio);
            int rawMp3TrackCount = rawAudioExtractor.getTrackCount();
            printMsg("raw mp3 track count :" + rawMp3TrackCount);
            for (int i = 0; i < rawMp3TrackCount; i++) {
                MediaFormat trackFormat = rawAudioExtractor.getTrackFormat(i);
                String mime = trackFormat.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("audio/")) {
                    printMsg("raw mp3 audio mime :" + mime);
                    rawAudioExtractor.selectTrack(i);
                    rawAudioFormat = trackFormat;
                    printMsg("raw mp3 audio format :" + rawAudioFormat.toString());
                    break;
                }
            }

            if (rawAudioFormat == null) {
                printMsg("raw mp3 audio format null");
                rawAudioExtractor.release();
                return null;
            }
            ArrayMap<MediaExtractor, MediaFormat> mediaExtractorMediaFormatMap = new ArrayMap<>();
            mediaExtractorMediaFormatMap.put(rawAudioExtractor, rawAudioFormat);
            return mediaExtractorMediaFormatMap;
        } catch (Exception e) {
            e.printStackTrace();
            printMsg("extractorTrackFromAudio failed !!!");
            if (editVideoListener != null) editVideoListener.onEditVideoError(e.getMessage());
            return null;
        }
    }

    private void audioTrack2PCM(MediaExtractor extractor, MediaFormat mediaFormat, String outFile) {
        int TIMEOUT_USEC = 0;

        try {
            //初始化音频的解码器
            MediaCodec audioCodec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));
            audioCodec.configure(mediaFormat, null, null, 0);

            audioCodec.start();

            ByteBuffer[] inputBuffers = audioCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = audioCodec.getOutputBuffers();
            MediaCodec.BufferInfo decodeBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo inputInfo = new MediaCodec.BufferInfo();
            boolean codeOver = false;
            boolean inputDone = false;//整体输入结束标志
            FileOutputStream fos = new FileOutputStream(outFile);
            while (!codeOver) {
                if (!inputDone) {
                    for (int i = 0; i < inputBuffers.length; i++) {
                        //遍历所以的编码器 然后将数据传入之后 再去输出端取数据
                        int inputIndex = audioCodec.dequeueInputBuffer(TIMEOUT_USEC);
                        if (inputIndex >= 0) {
                            /**从分离器中拿到数据 写入解码器 */
                            ByteBuffer inputBuffer = inputBuffers[inputIndex];//拿到inputBuffer
                            inputBuffer.clear();//清空之前传入inputBuffer内的数据
                            int sampleSize = extractor.readSampleData(inputBuffer, 0);//MediaExtractor读取数据到inputBuffer中

                            if (sampleSize < 0) {
                                audioCodec.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                inputDone = true;
                            } else {

                                inputInfo.offset = 0;
                                inputInfo.size = sampleSize;
                                inputInfo.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                                inputInfo.presentationTimeUs = extractor.getSampleTime();
                                printMsg("往解码器写入数据---当前帧的时间戳----"+inputInfo.presentationTimeUs);

                                audioCodec.queueInputBuffer(inputIndex, inputInfo.offset, sampleSize, inputInfo.presentationTimeUs, 0);//通知MediaDecode解码刚刚传入的数据
                                extractor.advance();//MediaExtractor移动到下一取样处
                            }
                        }
                    }
                }

                boolean decodeOutputDone = false;
                byte[] chunkPCM;
                while (!decodeOutputDone) {
                    int outputIndex = audioCodec.dequeueOutputBuffer(decodeBufferInfo, TIMEOUT_USEC);
                    if (outputIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        /**没有可用的解码器output*/
                        decodeOutputDone = true;
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        outputBuffers = audioCodec.getOutputBuffers();
                    } else if (outputIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        MediaFormat newFormat = audioCodec.getOutputFormat();
                    } else if (outputIndex < 0) {
                    } else {
                        ByteBuffer outputBuffer;
                        if (Build.VERSION.SDK_INT >= 21) {
                            outputBuffer = audioCodec.getOutputBuffer(outputIndex);
                        } else {
                            outputBuffer = outputBuffers[outputIndex];
                        }

                        chunkPCM = new byte[decodeBufferInfo.size];
                        outputBuffer.get(chunkPCM);
                        outputBuffer.clear();

                        fos.write(chunkPCM);//数据写入文件中
                        fos.flush();
                        printMsg("---释放输出流缓冲区----:::"+outputIndex);
                        audioCodec.releaseOutputBuffer(outputIndex, false);
                        if ((decodeBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            /**
                             * 解码结束，释放分离器和解码器
                             * */
                            extractor.release();

                            audioCodec.stop();
                            audioCodec.release();
                            codeOver = true;
                            decodeOutputDone = true;
                        }
                    }

                }
            }
            fos.close();//输出流释放
        } catch (Exception e) {
            e.printStackTrace();
            printMsg("audioTrack2PCM error");
            if (editVideoListener != null) editVideoListener.onEditVideoError(e.getMessage());
        }
    }

    /**
     * 原始pcm数据，转aac音频
     */
    boolean isDecodeOver = false;

    private void mulitPCMMix(String firstPath, String secondPath, final String outFile,
                            float firstVol, float secondVol) {
        try {
            // 将需要合音的音频解码后的文件放到数组里,多个音频都可以一起合成，这里是两个
            File[] rawAudioFiles = new File[2];
            rawAudioFiles[0] = new File(firstPath);
            rawAudioFiles[1] = new File(secondPath);
            if (!rawAudioFiles[0].exists() || !rawAudioFiles[1].exists()) {
                printMsg("pcm file no exist");
                if (editVideoListener != null)
                    editVideoListener.onEditVideoError("pcm file no exist");
                return;
            }
            File file = new File(outFile);
            if (file.exists()) {
                file.delete();
            }

            final int fileSize = rawAudioFiles.length;

            FileInputStream[] audioFileStreams = new FileInputStream[fileSize];
            File audioFile = null;

            FileInputStream inputStream;
            byte[][] allAudioBytes = new byte[fileSize][];
            boolean[] streamDoneArray = new boolean[fileSize];
            byte[] buffer = new byte[8 * 1024];


            for (int fileIndex = 0; fileIndex < fileSize; ++fileIndex) {
                audioFile = rawAudioFiles[fileIndex];
                audioFileStreams[fileIndex] = new FileInputStream(audioFile);
            }
            final boolean[] isStartEncode = {false};
            while (true) {

                for (int streamIndex = 0; streamIndex < fileSize; ++streamIndex) {

                    inputStream = audioFileStreams[streamIndex];
                    if (!streamDoneArray[streamIndex] && (inputStream.read(buffer)) != -1) {
                        allAudioBytes[streamIndex] = Arrays.copyOf(buffer, buffer.length);
                    } else {
                        streamDoneArray[streamIndex] = true;
                        allAudioBytes[streamIndex] = new byte[8 * 1024];
                    }
                }

                byte[] mixBytes = nativeAudioMix(allAudioBytes, firstVol, secondVol);
                putPCMData(mixBytes);
                //mixBytes 就是混合后的数据
                printMsg("-----混音后的数据---" + mixBytes.length + "---isStartEncode--" + isStartEncode[0]);
                if (!isStartEncode[0]) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            isStartEncode[0] = true;
                            PCM2AAC(outFile);
                        }
                    }).start();
                }
                boolean done = true;
                for (boolean streamEnd : streamDoneArray) {
                    if (!streamEnd) {
                        done = false;
                    }
                }

                if (done) {
                    isDecodeOver = true;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            printMsg("mulitPCMMix failed !!!");
            if (editVideoListener != null) editVideoListener.onEditVideoError(e.getMessage());
        }

    }

    private void PCM2AAC(String outputFile) {

        try {
            int inputIndex;
            ByteBuffer inputBuffer;
            int outputIndex;
            ByteBuffer outputBuffer;
            byte[] chunkAudio;
            int outBitSize;
            int outPacketSize;
            byte[] chunkPCM;
            //初始化编码器
            MediaFormat encodeFormat = MediaFormat.createAudioFormat(Constant.EncodeConfig.OUTPUT_AUDIO_MIME_TYPE
                    , Constant.EncodeConfig.OUTPUT_AUDIO_SAMPLE_RATE_HZ
                    , Constant.EncodeConfig.OUTPUT_AUDIO_CHANNEL_COUNT);//mime type 采样率 声道数
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, Constant.EncodeConfig.OUTPUT_AUDIO_BIT_RATE);//比特率
            encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, Constant.EncodeConfig.OUTPUT_AUDIO_AAC_PROFILE);
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 500 * 1024);

            MediaCodec mediaEncode = MediaCodec.createEncoderByType(Constant.EncodeConfig.OUTPUT_AUDIO_MIME_TYPE);
            mediaEncode.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mediaEncode.start();

            ByteBuffer[] encodeInputBuffers = mediaEncode.getInputBuffers();
            ByteBuffer[] encodeOutputBuffers = mediaEncode.getOutputBuffers();
            MediaCodec.BufferInfo encodeBufferInfo = new MediaCodec.BufferInfo();

            //初始化文件写入流
            FileOutputStream fos = new FileOutputStream(new File(outputFile));
            BufferedOutputStream bos = new BufferedOutputStream(fos, 500 * 1024);
            printMsg("--encodeBufferInfo---" + encodeBufferInfo.size);

            while (!chunkPCMDataContainer.isEmpty() || !isDecodeOver) {
                for (int i = 0; i < encodeInputBuffers.length - 1; i++) {
                    chunkPCM = getPCMData();//获取解码器所在线程输出的数据 代码后边会贴上
                    if (chunkPCM == null) {
                        break;
                    }
                    printMsg("--AAC编码器--取数据---" + chunkPCM.length);

                    inputIndex = mediaEncode.dequeueInputBuffer(-1);
                    inputBuffer = encodeInputBuffers[inputIndex];
                    inputBuffer.clear();//同解码器
                    inputBuffer.limit(chunkPCM.length);
                    inputBuffer.put(chunkPCM);//PCM数据填充给inputBuffer
                    mediaEncode.queueInputBuffer(inputIndex, 0, chunkPCM.length, 0, 0);//通知编码器 编码
                }

                outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);//同解码器
                while (outputIndex >= 0) {//同解码器

                    outBitSize = encodeBufferInfo.size;
                    outPacketSize = outBitSize + 7;//7为ADTS头部的大小
                    outputBuffer = encodeOutputBuffers[outputIndex];//拿到输出Buffer
                    outputBuffer.position(encodeBufferInfo.offset);
                    outputBuffer.limit(encodeBufferInfo.offset + outBitSize);
                    chunkAudio = new byte[outPacketSize];
                    addADTStoPacket(chunkAudio, outPacketSize);//添加ADTS 代码后面会贴上
                    outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中 偏移量offset=7 你懂得
                    outputBuffer.position(encodeBufferInfo.offset);
                    try {
                        printMsg("---保存文件----" + chunkAudio.length);
                        bos.write(chunkAudio, 0, chunkAudio.length);//BufferOutputStream 将文件保存到内存卡中 *.aac
                        bos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (editVideoListener != null)
                            editVideoListener.onEditVideoError(e.getMessage());
                    }

                    mediaEncode.releaseOutputBuffer(outputIndex, false);
                    outputIndex = mediaEncode.dequeueOutputBuffer(encodeBufferInfo, 10000);
                }
            }
            mediaEncode.stop();
            mediaEncode.release();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            printMsg("PCM2AAC error");
            if (editVideoListener != null)
                editVideoListener.onEditVideoError(e.getMessage());
        }
    }

    private void PCMEncodeAAC(String srcPath,String dstPath) {
        FileInputStream fisRawAudio = null;
        FileOutputStream fosAccAudio = null;
        try {
            fisRawAudio = new FileInputStream(srcPath);
            fosAccAudio = new FileOutputStream(dstPath);

            MediaCodec audioEncoder = MediaCodec.createEncoderByType(Constant.EncodeConfig.OUTPUT_AUDIO_MIME_TYPE);
            MediaFormat format = new MediaFormat();
            format.setString(MediaFormat.KEY_MIME, Constant.EncodeConfig.OUTPUT_AUDIO_MIME_TYPE);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
            format.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2);
            format.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE,
                    MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            audioEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            audioEncoder.start();

            ByteBuffer[] audioInputBuffers = audioEncoder.getInputBuffers();
            ByteBuffer[] audioOutputBuffers = audioEncoder.getOutputBuffers();
            boolean sawInputEOS = false;
            boolean sawOutputEOS = false;
            long audioTimeUs = 0;
            MediaCodec.BufferInfo outBufferInfo = new MediaCodec.BufferInfo();

            boolean readRawAudioEOS = false;
            byte[] rawInputBytes = new byte[4096];
            int readRawAudioCount = 0;
            int rawAudioSize = 0;
            long lastAudioPresentationTimeUs = 0;

            int inputBufIndex, outputBufIndex;
            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    inputBufIndex = audioEncoder.dequeueInputBuffer(10000);
                    if (inputBufIndex >= 0) {
                        ByteBuffer inputBuffer = audioInputBuffers[inputBufIndex];
                        inputBuffer.clear();

                        int bufferSize = inputBuffer.remaining();
                        if (bufferSize != rawInputBytes.length) {
                            rawInputBytes = new byte[bufferSize];
                        }

                        if (!readRawAudioEOS) {
                            readRawAudioCount = fisRawAudio.read(rawInputBytes);
                            if (readRawAudioCount == -1) {
                                readRawAudioEOS = true;
                            }
                        }

                        if (readRawAudioEOS) {
                            audioEncoder.queueInputBuffer(inputBufIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            sawInputEOS = true;
                        } else {
                            inputBuffer.put(rawInputBytes, 0, readRawAudioCount);
                            rawAudioSize += readRawAudioCount;
                            audioEncoder.queueInputBuffer(inputBufIndex, 0, readRawAudioCount, audioTimeUs, 0);
                            audioTimeUs = (long) (1000000 * (rawAudioSize / 2.0) / 44100 * 16 / 8);
                        }
                    }
                }

                outputBufIndex = audioEncoder.dequeueOutputBuffer(outBufferInfo, 10000);
                if (outputBufIndex >= 0) {

                    // Simply ignore codec config buffers.
                    if ((outBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        audioEncoder.releaseOutputBuffer(outputBufIndex, false);
                        continue;
                    }

                    if (outBufferInfo.size != 0) {
                        ByteBuffer outBuffer = audioOutputBuffers[outputBufIndex];
                        outBuffer.position(outBufferInfo.offset);
                        outBuffer.limit(outBufferInfo.offset + outBufferInfo.size);
                        if (lastAudioPresentationTimeUs < outBufferInfo.presentationTimeUs) {
                            lastAudioPresentationTimeUs = outBufferInfo.presentationTimeUs;
                            int outBufSize = outBufferInfo.size;
                            int outPacketSize = outBufSize + 7;

                            outBuffer.position(outBufferInfo.offset);
                            outBuffer.limit(outBufferInfo.offset + outBufSize);

                            byte[] outData = new byte[outBufSize + 7];
                            addADTStoPacket(outData, outPacketSize);
                            outBuffer.get(outData, 7, outBufSize);

                            fosAccAudio.write(outData, 0, outData.length);
                        }
                    }

                    audioEncoder.releaseOutputBuffer(outputBufIndex, false);

                    if ((outBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        sawOutputEOS = true;
                    }
                } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    audioOutputBuffers = audioEncoder.getOutputBuffers();
                } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    MediaFormat audioFormat = audioEncoder.getOutputFormat();
                }
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (fisRawAudio != null)
                    fisRawAudio.close();
                if (fosAccAudio != null)
                    fosAccAudio.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 音视频混合成mp4
     */
    private void mixVideo(MediaExtractor rawMp4VideoExtractor, MediaFormat rawMp4VideoFormat, MediaExtractor audioExtractor, MediaFormat audioFormat, String outputPath) {


        MediaMuxer mediaMuxer = null;
        try {
            mediaMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            int videoTrackIndex = -1;
            videoTrackIndex = mediaMuxer.addTrack(rawMp4VideoFormat);
            int audioTrackIndex = -1;
            audioTrackIndex = mediaMuxer.addTrack(audioFormat);

            //处理视频方向
            MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(VideoManager.getInstance().getVideoBean().videoPath);
            String rotationStr = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
            int degree = Integer.parseInt(rotationStr);
            if (degree >= 0) mediaMuxer.setOrientationHint(degree);

            if (Build.VERSION.SDK_INT == 21) {
                int videoProfile = rawMp4VideoFormat.getInteger(MediaFormat.KEY_PROFILE);
                if (videoProfile != MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline) {
                    convertBaseLine();
                }
            }

            mediaMuxer.start();

            int MAX_SAMPLE_SIZE = 500 * 1024;
            if (videoTrackIndex != -1) {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                info.presentationTimeUs = 0;
                ByteBuffer buffer = ByteBuffer.allocate(MAX_SAMPLE_SIZE);
                while (true) {
                    int sampleSize = rawMp4VideoExtractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        break;
                    }

                    info.offset = 0;
                    info.size = sampleSize;
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                    info.presentationTimeUs = rawMp4VideoExtractor.getSampleTime();
                    printMsg("video presentationTimeUs :" + info.presentationTimeUs);
                    mediaMuxer.writeSampleData(videoTrackIndex, buffer, info);
                    printMsg("video writeSampleData buffer:" + buffer);

                    rawMp4VideoExtractor.advance();
                }

                rawMp4VideoExtractor.release();
                printMsg("rawMp4VideoExtractor release");
            }

            if (audioTrackIndex != -1) {
                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                info.presentationTimeUs = 0;
                ByteBuffer buffer = ByteBuffer.allocate(MAX_SAMPLE_SIZE);
                while (true) {
                    int sampleSize = audioExtractor.readSampleData(buffer, 0);
                    if (sampleSize < 0) {
                        break;
                    }

                    info.offset = 0;
                    info.size = sampleSize;
                    info.flags = MediaCodec.BUFFER_FLAG_SYNC_FRAME;
                    info.presentationTimeUs = audioExtractor.getSampleTime();
                    mediaMuxer.writeSampleData(audioTrackIndex, buffer, info);
                    printMsg("audio writeSampleData buffer:" + buffer);

                    audioExtractor.advance();
                }

                audioExtractor.release();
                printMsg("audioExtractor release");
            }

            printMsg("bgm video mix complete");
            mediaMuxer.stop();
            mediaMuxer.release();
            printMsg("mediaMuxer release");
            timingLogger.addSplit("add bgm");
            timingLogger.dumpToLog();
            if (editVideoListener != null) editVideoListener.onEditVideoSuccess(outputPath);
        } catch (IOException e) {
            e.printStackTrace();
            if (editVideoListener != null) editVideoListener.onEditVideoError(e.getMessage());
        }
    }

    /**
     * android目前只支持h.264 baseline规格,如果不是这个规格，MediaMuxer合成时会报错（时间戳不是线性增长）
     */
    private void convertBaseLine() throws IOException {
        printMsg("raw video is not h.264 baseline");
        //经过ExtractDecodeEditEncodeMuxUtils解码编码，视频已经被转成android默认的baseline规格
    }

    /**
     * jni进行音频的混音处理，提升速度
     */
    private byte[] nativeAudioMix(byte[][] allAudioBytes, float firstVol, float secondVol) {
        if (allAudioBytes == null || allAudioBytes.length == 0)
            return null;

        byte[] realMixAudio = allAudioBytes[0];

        //如果只有一个音频的话，就返回这个音频数据
        if (allAudioBytes.length == 1)
            return realMixAudio;

        return AudioJniUtils.audioMix(allAudioBytes[0], allAudioBytes[1], realMixAudio, firstVol, secondVol);
    }

    private ArrayList<byte[]> chunkPCMDataContainer;

    private void putPCMData(byte[] pcmChunk) {
        synchronized (AudioCodec.class) {//记得加锁
            if (chunkPCMDataContainer == null) {
                chunkPCMDataContainer = new ArrayList<>();
            }
            chunkPCMDataContainer.add(pcmChunk);
        }
    }

    private byte[] getPCMData() {
        synchronized (AudioCodec.class) {//记得加锁
            if (chunkPCMDataContainer.isEmpty()) {
                return null;
            }

            byte[] pcmChunk = chunkPCMDataContainer.get(0);//每次取出index 0 的数据
            chunkPCMDataContainer.remove(pcmChunk);//取出后将此数据remove掉 既能保证PCM数据块的取出顺序 又能及时释放内存
            return pcmChunk;
        }
    }

    /**
     * 写入ADTS头部数据
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE

        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    private GPUImageFilter generateGPUImageFilter(int resId) {

        GPUImageLookupFilter lookupFilter = new GPUImageLookupFilter();
        lookupFilter.setBitmap(BitmapFactory.decodeResource(RelaVideoSDK.context.getResources(), resId));
        return lookupFilter;
    }

    private String setFile(String fileName) {
        return FileManager.getOtherFile(fileName);
    }

    private void printMsg(String msg) {
        Log.d(TAG, msg);
    }

    @Override
    public void onFilterGenerateResult(String extra) {
        timingLogger.addSplit("合成滤镜");
        addBGM(extra);
    }
}
