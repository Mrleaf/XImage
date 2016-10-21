# XImage
android 图片压缩工具，提供单张和多张图片压缩
压缩算法借鉴了Luban（https://github.com/Curzibn/Luban）
在算法上进行了优化，对大部分常见比例图片和长图，分别测试了QQ和微信图片进行逆向推算。

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
                       //处理后的图片
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
                        //处理后的图片
                    }

                    @Override
                    public void onError(Throwable e) {
                        
                    }
                }).start();
```                
