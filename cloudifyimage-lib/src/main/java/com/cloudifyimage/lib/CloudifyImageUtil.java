package com.cloudifyimage.lib;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Base64;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Created by RahulT on 7/22/2017.
 */
public class CloudifyImageUtil {

    private String serverUrl = null;
    private String apiKey = null;
    private String apiSecret = null;
    private ProgressDialog progressDialog = null;
    private CloudifyImageListener listener = null;

    public CloudifyImageUtil(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public CloudifyImageUtil(String serverUrl, String apiKey, String apiSecret) {
        this.serverUrl = serverUrl;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    public CloudifyImageUtil() {}

    public CloudifyImageUtil addServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    public CloudifyImageUtil addCredentials(String apiKey, String apiSecret) {
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
        return this;
    }

    public CloudifyImageUtil addProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
        return this;
    }

    public CloudifyImageUtil addListener(CloudifyImageListener listener) {
        this.listener = listener;
        return this;
    }

    public void send(String collection, String filePath) throws Exception {
        if (serverUrl == null) {
            throw new Exception("ServerUrl not defined!");
        }
        if ((apiKey == null) || (apiSecret == null)) {
            throw new Exception("Invalid or No Credentials!");
        }
        new CloudifyImageAsyncTask().execute(collection, filePath);
    }

    private static String encodeBase64(String data) {
        byte[] bytes = null;
        try {
            bytes = data.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private class CloudifyImageAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (progressDialog != null) {
                progressDialog.setMax(100);
                progressDialog.show();
            }
        }

        @Override
        protected String doInBackground(String... strings) {
            String apiKeySecret = apiKey + ":" + apiSecret;
            String auth = encodeBase64(apiKeySecret);

            String response = null;
            try {
                MultipartUtility utility = new MultipartUtility(serverUrl, "UTF-8");
                utility.addFormField("collection", strings[0]);
                utility.addFormField("auth", auth);
                File file = new File(strings[1]);
                utility.addFilePart("file", file, new FileUploadListener() {
                    @Override
                    public void onProgress(int progress) {
                        publishProgress(progress);
                    }
                });
                List<String> list = utility.finish();
                if ((list != null) && (list.size() > 0)) {
                    response = list.get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                return response;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // setting progress percentage
            if (progressDialog != null) {
                progressDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(String response) {
            // dismiss the dialog after the file was uploaded
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            if (listener != null) {
                if (response == null) {
                    listener.onFailure("Unable to upload the image! something went wrong!!!");
                    return;
                }
                try {
                    JSONObject resp = new JSONObject(response);
                    if (resp.getInt("returnCode") == 0) {
                        listener.onSuccess(response);
                    } else {
                        String errorMessage = resp.getString("message");
                        listener.onFailure(errorMessage);
                    }
                } catch (Exception e) {
                    listener.onFailure(e.getMessage());
                }

            }
        }
    }

}
