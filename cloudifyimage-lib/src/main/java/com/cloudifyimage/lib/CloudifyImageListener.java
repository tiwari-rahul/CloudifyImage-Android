package com.cloudifyimage.lib;

/**
 * Created by RahulT on 7/22/2017.
 */

public interface CloudifyImageListener {
    void onSuccess(String response);
    void onFailure(String errorMessage);
}
