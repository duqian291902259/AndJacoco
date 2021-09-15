package org.jacoco.agent.rt;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * FileName: CodeCoverageManager
 * Author: zhihao.wu@ttpai.cn
 * Date: 2020/9/16
 * Description: 代码覆盖工具 jacoco，运行时
 */
public class CodeCoverageManager {
    private static CodeCoverageManager sInstance = new CodeCoverageManager();
    //private static String URL_HOST = "http://10.10.17.105:8080";//内网 服务器地址
    private static String URL_HOST = "http://192.168.56.1:8090";//内网 服务器地址

    private static String APP_NAME;
    private static int versionCode;

    private static String dirPath;
    private static String filePath;

    private static final String TAG = "dq-jacoco-CodeCoverageManager";

    public static void init(Context context, String serverHost) {
        APP_NAME = context.getPackageName().replace(".", "");
        versionCode = getVersion(context);
        if (serverHost != null) {
            URL_HOST = serverHost;
        }
        //String path = context.getFilesDir().getAbsolutePath();
        //dirPath = path + "/jacoco/" + versionCode + "/";
        dirPath = context.getExternalCacheDir() + File.separator + "jacoco/";
        File dir = new File(dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        //非append的方式：
        // filePath = dirPath + UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + ".ec";
        filePath = dirPath + "test_android_jacoco.ec";

        File f = new File(filePath);
        Log.d(TAG, filePath + " canRead=" + f.canRead() + " canWrite=" + f.canWrite() + ",size=" + f.length() + "exist=" + f.exists());
    }

    public static void generateCoverageFile() {
        sInstance.writeToFile();
    }

    public static void uploadData() {
        sInstance.upload();
    }

    private static int getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private CodeCoverageManager() {
    }

    /**
     * 生成executionData
     */
    private void writeToFile() {
        if (filePath == null) return;
        OutputStream out = null;
        try {
            out = new FileOutputStream(filePath, true);
            IAgent agent = RT.getAgent();
            out.write(agent.getExecutionData(false));
            Log.i(TAG, " generateCoverageFile write success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, " generateCoverageFile Exception:" + e.toString());
        } finally {
            close(out);
        }
    }

    private void close(Closeable out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void upload() {
        if (filePath == null) {
            return;
        }
        new Thread() {
            @Override
            public void run() {
                super.run();
                Log.d(TAG, "开始上传 " + Thread.currentThread());
                try {
                    syncUploadFiles();
                } catch (IOException e) {
                    Log.e(TAG, "uploadData " + e.getMessage());
                }
                Log.d(TAG, "执行完成");
            }
        }.start();
    }


    private void syncUploadFiles() throws IOException {
        OkHttpClient client = buildHttpClient();
        File dir = new File(dirPath);
        if (dir.exists() && dir.list().length > 0) {

            Log.d(TAG, "File list=" + Arrays.toString(dir.list()));
            File[] files = dir.listFiles();
            for (File f : files) {
                String filename = f.getName();
                if (!filename.endsWith(".ec") || f.length() <= 0) {
                    continue;
                }

                RequestBody fileBody = RequestBody.create(MediaType.parse("application/plain"), f);
                //RequestBody fileBody = RequestBody.create(MediaType.get("multipart/form-data"), f);
                RequestBody body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", filename, fileBody)
                        .addFormDataPart("appName", "android")
                        .addFormDataPart("versionCode", "3.8.1")
                        .build();

                String url = URL_HOST + "/WebServer/JacocoApi/uploadEcFile";
                Log.e(TAG, "upload url =" + url + ",file=" + f.getAbsolutePath());

                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                Call call = client.newCall(request);
                /*call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, final IOException e) {
                        Log.e(TAG, "syncUploadFiles error =" + e);
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        handleResponse(response,f);
                    }
                });*/
                Response response = call.execute();
                handleResponse(response, f);
            }
        }

    }

    private void handleResponse(Response response, File f) {
        //请求结果
        ResponseBody responseBody = null;
        try {
            //获取请求结果 ResponseBody
            responseBody = response.body();
            String str = responseBody.string();
            Log.e(TAG, "syncUploadFiles str =" + str);
            if (response.isSuccessful()) {
                if (str.contains("200")) {
                    f.delete();
                }
            } else {
                Log.e(TAG, "syncUploadFiles error =" + response.code());
            }
        } catch (Exception e) {//发生异常，失败回调
            e.printStackTrace();
        } finally {//记得关闭操作
            if (null != responseBody) {
                responseBody.close();
            }
        }
    }

    private OkHttpClient buildHttpClient() {
        OkHttpClient client = new OkHttpClient.Builder()
                .callTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        return client;
    }
}
