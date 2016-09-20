package cn.leaf.ximage;

import java.io.File;
import java.util.List;

/**
 * Created by lenovo on 2016/9/8.
 */
public interface OnCompressionListener {
    /**
     * 开始
     */
    void onStart();
    /**
     * 压缩成功
     * @param file
     */
    void onSuccess(List<File> file);

    /**
     * 压缩失败
     * @param e
     */
    void onError(Throwable e);
}
