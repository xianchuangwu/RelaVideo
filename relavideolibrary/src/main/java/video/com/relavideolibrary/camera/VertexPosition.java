package video.com.relavideolibrary.camera;

/**
 * Created by hirochin on 22/12/2017.
 */

public enum VertexPosition {
    None,
    Left,
    Right,
    Flip_Vertical,
    Flip_Horizontal,
    Right_And_Flip_Vertical,
    Right_And_Flip_Horizontal,
    Left_And_Flip_Horizontal,
    Up_Down;

    public float[] matrix() {
        switch (this) {
            case None:
                float resultNone[] = {
                        0.0f, 0.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                };
                return resultNone;
            case Left:
                float resultLeft[] = {
                        1.0f, 0.0f,
                        1.0f, 1.0f,
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                };
                return resultLeft;
            case Right:
                float resultRight[] = {
                        0.0f, 1.0f,
                        0.0f, 0.0f,
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                };
                return resultRight;
            case Flip_Vertical:
                float resultFlipVertical[] = {
                        0.0f, 1.0f,
                        1.0f, 1.0f,
                        0.0f, 0.0f,
                        1.0f, 0.0f,
                };
                return resultFlipVertical;
            case Flip_Horizontal:
                float resultFlipHorizontal[] = {
                        1.0f, 0.0f,
                        0.0f, 0.0f,
                        1.0f, 1.0f,
                        0.0f, 1.0f,
                };
                return resultFlipHorizontal;
            case Right_And_Flip_Vertical:
                float resultRightAndFlipVertical[] = {
                        0.0f, 0.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        1.0f, 1.0f,
                };
                return resultRightAndFlipVertical;
            case Right_And_Flip_Horizontal:
                float resultRightAndFlipHorizontal[] = {
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        0.0f, 0.0f,
                };
                return resultRightAndFlipHorizontal;
            case Up_Down:
                float resultUpDown[] = {
                        1.0f, 1.0f,
                        0.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 0.0f,
                };
                return resultUpDown;
            case Left_And_Flip_Horizontal:
                float resultLeftAndFlipHorizontal[] = {
                        1.0f, 1.0f,
                        1.0f, 0.0f,
                        0.0f, 1.0f,
                        0.0f, 0.0f,
                };
                return resultLeftAndFlipHorizontal;

        }
        float defaultM[] = {0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};
        return defaultM;
    }
}
