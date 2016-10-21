# XImage
android 图片压缩工具，提供单张和多张图片压缩，压缩算法通过QQ、微信逆向推算而来

# 使用方式
##导入
compile 'cn.leaf.ximage:ximage:1.0.1'

##处理单张图片
```
IMGCompression.get(this).loadFile(file)    // File 
                .setSavePath(path)        //只有单张图片才能自定义保存路径 （不填也有默认路径）
                .setListener(new OnCompressionListener() {
                    @Override
                    public void onStart() {
                       
                    }

                    @Override
                    public void onSuccess(List<File> fileList) {
                       
                    }

                    @Override
                    public void onError(Throwable e) {
                        
                    }
                }).start();
```
##处理多张图片
```
IMGCompression.get(this).loadFile(list) //List<File> fileList
                .setListener(new OnCompressionListener() {
                    @Override
                    public void onStart() {
                       
                    }

                    @Override
                    public void onSuccess(List<File> fileList) {
                        
                    }

                    @Override
                    public void onError(Throwable e) {
                        
                    }
                }).start();
```                
