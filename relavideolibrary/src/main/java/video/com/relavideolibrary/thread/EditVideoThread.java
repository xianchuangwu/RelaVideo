package video.com.relavideolibrary.thread;

/**
 * Created by chad
 * Time 17/12/21
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class EditVideoThread extends Thread {

    public EditVideoThread(EditVideoListener editVideoListener) {
        this.editVideoListener = editVideoListener;
    }

    @Override
    public void run() {
        super.run();
        if (editVideoListener != null) editVideoListener.onEditVideoSuccess("");
    }

    private EditVideoListener editVideoListener;

    public interface EditVideoListener {
        void onEditVideoSuccess(String path);

        void onEditVideoError(String message);
    }
}
