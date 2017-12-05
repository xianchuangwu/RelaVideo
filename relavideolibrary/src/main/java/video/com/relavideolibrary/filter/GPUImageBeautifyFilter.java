package video.com.relavideolibrary.filter;

import android.opengl.GLES20;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;

/**
 * Created by chad
 * Time 17/12/5
 * Email: wuxianchuang@foxmail.com
 * Description: TODO 磨皮美颜滤镜
 */

public class GPUImageBeautifyFilter extends GPUImageFilter {

    public static final String BEAUTIFY_FRAGMENT_SHADER = ""
            + "precision highp float;\n"
            + "varying highp vec2 textureCoordinate;\n"
            + "uniform sampler2D inputImageTexture;\n"
            + ""
            + "uniform vec2 singleStepOffset;\n"
            + "uniform highp vec4 params;\n"
            + ""
            + "const highp vec3 W = vec3(0.299,0.587,0.114);\n"
            + " const mat3 saturateMatrix = mat3(\n"
            + " 1.1102,-0.0598,-0.061,\n"
            + "-0.0774,1.0826,-0.1186,\n"
            + " -0.0228,-0.0228,1.1772);\n"
            + "float hardlight(float color)\n"
            + "{\n"
            + " if(color <= 0.5)\n"
            + " {\n"
            + " color = color * color * 2.0;\n"
            + " }\n"
            + "else\n"
            + " {\n"
            + "    color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);\n"
            + " }\n"
            + " return color;\n"
            + " }\n"
            + "void main(){\n"
//            + "vec4 params = vec4(0.33, 0.63, 0.4, 0.35); \n"
            + " vec2 blurCoordinates[24];\n"
            + " blurCoordinates[0] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -10.0);\n"
            + " blurCoordinates[1] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 10.0);\n"
            + " blurCoordinates[2] = textureCoordinate.xy + singleStepOffset * vec2(-10.0, 0.0);\n"
            + "blurCoordinates[3] = textureCoordinate.xy + singleStepOffset * vec2(10.0, 0.0);\n"
            + "blurCoordinates[4] = textureCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);\n"
            + "blurCoordinates[5] = textureCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);\n"
            + "blurCoordinates[6] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);\n"
            + "blurCoordinates[7] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);\n"
            + "blurCoordinates[8] = textureCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);\n"
            + "blurCoordinates[9] = textureCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);\n"
            + "blurCoordinates[10] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);\n"
            + "blurCoordinates[11] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);\n"
            + "blurCoordinates[12] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -6.0);\n"
            + "blurCoordinates[13] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 6.0);\n"
            + "blurCoordinates[14] = textureCoordinate.xy + singleStepOffset * vec2(6.0, 0.0);\n"
            + "blurCoordinates[15] = textureCoordinate.xy + singleStepOffset * vec2(-6.0, 0.0);\n"
            + "blurCoordinates[16] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);\n"
            + "blurCoordinates[17] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);\n"
            + "blurCoordinates[18] = textureCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);\n"
            + "blurCoordinates[19] = textureCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);\n"
            + "blurCoordinates[20] = textureCoordinate.xy + singleStepOffset * vec2(-2.0, -2.0);\n"
            + "blurCoordinates[21] = textureCoordinate.xy + singleStepOffset * vec2(-2.0, 2.0);\n"
            + "blurCoordinates[22] = textureCoordinate.xy + singleStepOffset * vec2(2.0, -2.0);\n"
            + "blurCoordinates[23] = textureCoordinate.xy + singleStepOffset * vec2(2.0, 2.0);\n"
            + " float sampleColor = texture2D(inputImageTexture, textureCoordinate).g * 22.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[0]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[1]).g;\n"
            + " sampleColor += texture2D(inputImageTexture, blurCoordinates[2]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[3]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[4]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[5]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[6]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[7]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[8]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[9]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[10]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[11]).g;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[12]).g * 2.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[13]).g * 2.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[14]).g * 2.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[15]).g * 2.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[16]).g * 2.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[17]).g * 2.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[18]).g * 2.0;\n"
            + " sampleColor += texture2D(inputImageTexture, blurCoordinates[19]).g * 2.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[20]).g * 3.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[21]).g * 3.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[22]).g * 3.0;\n"
            + "sampleColor += texture2D(inputImageTexture, blurCoordinates[23]).g * 3.0;\n"
            + "sampleColor = sampleColor / 62.0;\n"
            + "vec3 centralColor = texture2D(inputImageTexture, textureCoordinate).rgb;\n"
            + "float highpass = centralColor.g - sampleColor + 0.5;\n"
            + "for(int i = 0; i < 5;i++)\n"
            + " {\n"
            + "    highpass = hardlight(highpass);\n"
            + " }\n"
            + "float lumance = dot(centralColor, W);\n"
            + " float alpha = pow(lumance, params.r);\n"
            + "vec3 smoothColor = centralColor + (centralColor-vec3(highpass))*alpha*0.1;\n"
            + " smoothColor.r = clamp(pow(smoothColor.r, params.g),0.0,1.0);\n"
            + "smoothColor.g = clamp(pow(smoothColor.g, params.g),0.0,1.0);\n"
            + "smoothColor.b = clamp(pow(smoothColor.b, params.g),0.0,1.0);\n"
            + "vec3 lvse = vec3(1.0)-(vec3(1.0)-smoothColor)*(vec3(1.0)-centralColor);\n"
            + " vec3 bianliang = max(smoothColor, centralColor);\n"
            + " vec3 rouguang = 2.0*centralColor*smoothColor + centralColor*centralColor - 2.0*centralColor*centralColor*smoothColor;\n"
            + "gl_FragColor = vec4(mix(centralColor, lvse, alpha), 1.0);\n"
            + " gl_FragColor.rgb = mix(gl_FragColor.rgb, bianliang, alpha);\n"
            + " gl_FragColor.rgb = mix(gl_FragColor.rgb, rouguang, params.b);\n"
            + " vec3 satcolor = gl_FragColor.rgb * saturateMatrix;\n"
            + " gl_FragColor.rgb = mix(gl_FragColor.rgb, satcolor, params.a);\n"
            + " }\n" + "";

    private int mSingleStepOffsetLocation;
    private int mParamsLocation;

    private int mBeautifyLevel;
    private int mInputWidth;
    private int mInputHeight;

    public GPUImageBeautifyFilter() {
        this(1280, 720, 0);
    }

    public GPUImageBeautifyFilter(int inWidth, int inHeight) {
        super(NO_FILTER_VERTEX_SHADER, BEAUTIFY_FRAGMENT_SHADER);
        mBeautifyLevel = 0;
        mInputWidth = inWidth;
        mInputHeight = inHeight;
    }

    public GPUImageBeautifyFilter(int inWidth, int inHeight, int level) {
        super(NO_FILTER_VERTEX_SHADER, BEAUTIFY_FRAGMENT_SHADER);
        mBeautifyLevel = level;
        mInputWidth = inWidth;
        mInputHeight = inHeight;
    }

    @Override
    public void onInit() {
        super.onInit();

        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(getProgram(),
                "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(getProgram(), "params");
    }

    @Override
    public void onInitialized() {
        super.onInitialized();

        setBeautifyLevel(mBeautifyLevel);
        setInputTextureSize(mInputWidth, mInputHeight);
    }

    // 0~4
    public void setBeautifyLevel(int level) {
        final float[][] beautify_level = {{1.0f, 1.0f, 0.15f, 0.15f},
                {0.8f, 0.9f, 0.2f, 0.2f}, {0.6f, 0.8f, 0.25f, 0.25f},
                {0.4f, 0.7f, 0.38f, 0.3f}, {0.33f, 0.63f, 0.4f, 0.35f}};
        level = level < 0 ? 0 : (level > 4 ? 4 : level);
        setFloatVec4(mParamsLocation, beautify_level[level]);
    }

    public void setInputTextureSize(int width, int height) {
        setFloatVec2(mSingleStepOffsetLocation, new float[]{2.0f / width,
                2.0f / height});
    }

}
