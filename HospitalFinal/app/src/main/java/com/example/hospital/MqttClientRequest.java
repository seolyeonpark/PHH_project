package com.example.hospital;

public class MqttClientRequest {

    public static String notificationResponse(String response) {
        String responseMessage =
                "{\"rsc\":\"2000\",\n" +
                        "\"rqi\":\""+ response + "\",\n" +
                        "\"pc\":\"\"}";

        return responseMessage;
    }
}
