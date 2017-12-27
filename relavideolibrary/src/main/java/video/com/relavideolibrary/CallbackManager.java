package video.com.relavideolibrary;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chad
 * Time 17/12/27
 * Email: wuxianchuang@foxmail.com
 * Description: TODO
 */

public class CallbackManager {

    private static CallbackManager instance;

    public static CallbackManager getInstance() {
        if (instance == null) instance = new CallbackManager();
        return instance;
    }

    private CallbackManager() {
        callbackMap = new HashMap<>();
    }

    private Map<String, Object> callbackMap;

    public Map<String, Object> getCallbackMap() {
        return callbackMap;
    }
}
