package com.fukayatsu.kina;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by fukayatsu on 14/11/09.
 */

public class VolleyHelper{

    public static final Object lock = new Object();

    public static RequestQueue requestQueue;

    /**
     * RequestQueueのシングルトン生成
     * @param context アプリケーションコンテキスト
     * @return
     */
    public static RequestQueue getRequestQueue(final Context context) {
        synchronized (lock) {
            if (requestQueue == null) {
                requestQueue = Volley.newRequestQueue(context);
            }
            return requestQueue;
        }
    }

    /** 以下省略**/

}
