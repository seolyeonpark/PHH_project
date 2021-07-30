package com.example.phh;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class httppost extends Thread{


    public void start() {
    }

    public interface  EventListener { //콜백 리스너로 사용할 인터ㅍㅔ이스
            void onHttpResult(String result);
            void onHttpFailed();
        }
        private Handler handler;
        private EventListener listener;


        public httppost(Context context,  EventListener eventListener){//생성자
            this.handler = new Handler(Looper.getMainLooper());
            this.listener = eventListener;
        }
        @Override
        public void run(){//네트워크 조회 코드가 구현될 run 메서드

            OkHttpClient client = new OkHttpClient().newBuilder().build();

            MediaType mediaType = MediaType.parse("application/vnd.onem2m-res+json; ty=4");
            RequestBody body = RequestBody.create(mediaType, "{\"m2m:cin\": {\"con\": \"1\"}}");

            Request request = new Request.Builder()
                    .url("http://203.253.128.161:7579/Mobius/P1/state")
                    .method("POST", body)
                    .addHeader("Accept", "application/json")
                    .addHeader("X-M2M-RI", "12345")
                    .addHeader("X-M2M-Origin", "SP1") // aei = change to YOUR aei
                    .addHeader("Content-Type", "application/vnd.onem2m-res+json; ty=4")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.code() != 201) {
                    System.out.println("There was a problem. Status Code: " + response.code());
                    return;
                }

                JSONObject obj = null;
                try {
                    obj = new JSONObject(response.body().string());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject o1 = null;
                try {
                    o1 = obj.getJSONObject("m2m:cin");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(o1);

                String con = null;
                try {
                    con = o1.getString("con");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println(con);
                onHttpResult("성공!");
            } catch (IOException io) {
                io.printStackTrace(System.out);
                System.out.println("실패!");
            }
        }


        private void onHttpResult(final String result){
            if(listener != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onHttpResult(result);
                    }
                });
            }
        }

        private  void onHttpFailed(){
            if (listener != null){
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onHttpFailed();
                    }
                });
            }
        }

}
