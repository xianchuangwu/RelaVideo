package video.com.relavideolibrary.camera;


public enum PrepreadShader {

    V_DEFAULT,
    F_DEFAULT,
    V_OES_DEFAULT,
    F_OES_DEFAULT,
    F_BEAUTY,
    F_BIG_EYE_AND_THIN_FACE;

    @Override
    public String toString() {
        String shader = "";
        switch (this) {
            case V_DEFAULT:
                shader = "attribute vec4 vPosition;\n" +
                        "attribute vec2 vCoord;\n" +
                        "uniform mat4 vMatrix;\n" +
                        "\n" +
                        "varying vec2 textureCoordinate;\n" +
                        "\n" +
                        "void main(){\n" +
                        "    gl_Position = vMatrix*vPosition;\n" +
                        "    textureCoordinate = vCoord;\n" +
                        "}";
                break;
            case F_DEFAULT:
                shader = "precision mediump float;\n" +
                        "varying vec2 textureCoordinate;\n" +
                        "uniform sampler2D vTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D( vTexture, textureCoordinate );\n" +
                        "}";
                break;
            case V_OES_DEFAULT:
                shader = "attribute vec4 vPosition;\n" +
                        "attribute vec2 vCoord;\n" +
                        "uniform mat4 vMatrix;\n" +
                        "varying vec2 textureCoordinate;\n" +
                        "\n" +
                        "void main(){\n" +
                        "    gl_Position = vMatrix*vPosition;\n" +
                        "    textureCoordinate = vCoord;\n" +
                        "}";
                break;
            case F_OES_DEFAULT:
                shader = "#extension GL_OES_EGL_image_external : require\n" +
                        "precision mediump float;\n" +
                        "varying vec2 textureCoordinate;\n" +
                        "uniform samplerExternalOES vTexture;\n" +
                        "void main() {\n" +
                        "    gl_FragColor = texture2D( vTexture, textureCoordinate );\n" +
                        "}";
                break;
            case F_BEAUTY:
                shader = ""
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
//                        + " gl_FragColor = texture2D(inputImageTexture, textureCoordinate);\n"
                        + " }\n" + "";
                break;
            case F_BIG_EYE_AND_THIN_FACE:
                shader = "precision highp float;\n" +
                        "#define MAX_CONTOUR_POINT_COUNT 20\n" +
                        "#define VIDEO_WIDTH 720.0\n" +
                        "#define VIDEO_HEIGHT 1280.0\n" +
                        " varying  vec2 textureCoordinate;\n" +
                        " uniform sampler2D inputImageTexture;\n" +

                        " uniform  float maxFaceWidth;\n" +
                        " uniform  float mouthOpen;\n" +
                        " uniform  float mouthPoint_x;\n" +
                        " uniform  float mouthPoint_y;\n" +
                        " uniform  float leftEyePoint_x;\n" +
                        " uniform  float leftEyePoint_y;\n" +
                        " uniform  float rightEyePoint_x;\n" +
                        " uniform  float rightEyePoint_y;\n" +
                        " uniform  float eyesScale;\n" +
                        " uniform  int arraySize;\n" +
                        " uniform float leftContourPoints[MAX_CONTOUR_POINT_COUNT*2];\n" +
                        " uniform float rightContourPoints[MAX_CONTOUR_POINT_COUNT*2];\n" +
                        " uniform float faceScale;\n" +
                        " vec2 warpPositionToUse(vec2 centerPostion, vec2 currentPosition, float radius, float scaleRatio, float aspectRatio)\n" +
                        " {\n" +
                        "     vec2 positionToUse = currentPosition;\n" +
                        "     \n" +
                        "     vec2 currentPositionToUse = vec2(currentPosition.x, currentPosition.y * aspectRatio + 0.5 - 0.5 * aspectRatio);\n" +
                        "     vec2 centerPostionToUse = vec2(centerPostion.x, centerPostion.y * aspectRatio + 0.5 - 0.5 * aspectRatio);\n" +
                        "     \n" +
                        "     float r = distance(currentPositionToUse, centerPostionToUse);\n" +
                        "     \n" +
                        "     if(r < radius)\n" +
                        "     {\n" +
                        "         float alpha = 1.0 - scaleRatio * (r/radius-1.0)*(r/radius-1.0);\n" +
                        "         positionToUse = centerPostion + alpha * (currentPosition - centerPostion);\n" +
                        "     }\n" +
                        "     \n" +
                        "     return positionToUse;\n" +
                        " }\n" +
                        " \n" +
                        "vec2 warpPositionToThinFace(vec2 currentPoint, vec2 contourPointA,  vec2 contourPointB, float radius, float delta, float aspectRatio)\n" +
                        "{\n" +
                        "    vec2 positionToUse = currentPoint;\n" +
                        "    \n" +
                        "    vec2 currentPointToUse = vec2(currentPoint.x, currentPoint.y * aspectRatio);\n" +
                        "    vec2 contourPointAToUse = vec2(contourPointA.x, contourPointA.y * aspectRatio);\n" +
                        "    \n" +
                        "    float r = distance(currentPointToUse, contourPointAToUse);\n" +
                        "    if(r < radius)\n" +
                        "    {\n" +
                        "        vec2 dir = normalize(contourPointB - contourPointA);\n" +
                        "        float dist = radius * radius - r * r;\n" +
                        "        float alpha = dist / (dist + (r-delta) * (r-delta));\n" +
                        "        alpha = alpha * alpha;\n" +
                        "        \n" +
                        "        positionToUse = positionToUse - alpha * delta * dir;\n" +
                        "        \n" +
                        "    }\n" +
                        "    return positionToUse;\n" +
                        "}\n" +
                        " void main()\n" +
                        " {\n" +
                        " vec2 st = textureCoordinate;\n" +
                        "     vec2 leftEyesCenterPostion = vec2(leftEyePoint_x,leftEyePoint_y);\n" +
                        "     vec2 rightEyeCenterPosition = vec2(rightEyePoint_x,rightEyePoint_y);\n" +
                        "     vec2 mouthCenterPosition = vec2(mouthPoint_x,mouthPoint_y);\n" +
                        "     float radius = maxFaceWidth;\n" +
                        "     float scaleRatio = eyesScale;\n" +
                        "     float aspectRatio = 1.77777;\n" +
                        "     vec2 positionToUse = textureCoordinate;\n" +
                        "     if ( arraySize > 0) {\n" +
                        "         positionToUse = warpPositionToUse(leftEyesCenterPostion, positionToUse,radius, scaleRatio, aspectRatio);\n" +
                        "         positionToUse = warpPositionToUse(rightEyeCenterPosition, positionToUse, radius, scaleRatio, aspectRatio);\n" +
//                        "         positionToUse = warpPositionToUse(leftEyesCenterPostion, positionToUse, 0.05, 0.4, aspectRatio);\n" +
//                        "         positionToUse = warpPositionToUse(rightEyeCenterPosition, positionToUse, 0.05, 0.4, aspectRatio);\n" +
                        "     }\n" +
                        "     float s = 6.0;\n" +
                        "     float dd = 1.0;\n" +
                        "     \n" +
                        "     if (arraySize > 0) {\n" +
                        "         vec2 p0 = vec2(leftContourPoints[0], leftContourPoints[1]);\n" +
                        "         vec2 p1 = vec2(leftContourPoints[2], leftContourPoints[3]);\n" +
                        "         dd = sqrt((p0.x - p1.x) * (p0.x - p1.x) + (p0.y - p1.y) * (p0.y - p1.y));\n" +
                        "     }\n" +
                        "     \n" +
                        "     float rrr = dd * faceScale;\n" +
                        "     \n" +
                        "     for(int i = 0; i < arraySize; i++) {\n" +
                        "         float delta = dd * 0.12;\n" +
                        "         \n" +
                        "         vec2 leftPoint = vec2(leftContourPoints[i * 2], leftContourPoints[i * 2 + 1]);\n" +
                        "         vec2 rightPoint = vec2(rightContourPoints[i * 2], rightContourPoints[i * 2 + 1]);\n" +
                        "         \n" +
                        "         if (leftPoint != rightPoint) {\n" +
                        "             positionToUse = warpPositionToThinFace(\n" +
                        "                                                    positionToUse,\n" +
                        "                                                    leftPoint,\n" +
                        "                                                    rightPoint,\n" +
                        "                                                    rrr,\n" +
                        "                                                    delta,\n" +
                        "                                                    aspectRatio\n" +
                        "                                                    );\n" +
                        "             positionToUse = warpPositionToThinFace(\n" +
                        "                                                    positionToUse,\n" +
                        "                                                    rightPoint,\n" +
                        "                                                    leftPoint,\n" +
                        "                                                    rrr,\n" +
                        "                                                    delta,\n" +
                        "                                                    aspectRatio\n" +
                        "                                                    );\n" +
                        "         }\n" +
                        "         else {\n" +
                        "             int j = 0; " +
                        "             vec2 centerPoint = (vec2(leftContourPoints[j], leftContourPoints[j + 1]) + vec2(rightContourPoints[j], rightContourPoints[j + 1])) * 0.5;\n" +
                        "             positionToUse = warpPositionToThinFace(\n" +
                        "                                                    positionToUse,\n" +
                        "                                                    leftPoint,\n" +
                        "                                                    centerPoint,\n" +
                        "                                                    rrr,\n" +
                        "                                                    delta * faceScale * 0.3,\n" +
                        "                                                    aspectRatio\n" +
                        "                                                    );\n" +
                        "         }\n" +
                        "     }\n" +
                        "     vec4 img = texture2D(inputImageTexture, positionToUse);\n" +
//                        " if (positionToUse != st) { img = vec4(mix(img.rgb, vec3(1.0), 0.5), 1.0); }" +
//                        "     gl_FragColor = vec4(img.r, leftEyePoint_x, leftEyePoint_y, 1.);\n" +
                        "     gl_FragColor = img;\n" +
                        " }";
                break;
        }
        return shader;
    }
}
