package com.example.phh;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.phh.AE;
import com.example.phh.ApplicationEntityObject;
import com.example.phh.CSEBase;
import com.example.phh.ContentSubscribeObject;
import com.example.phh.MqttClientRequest;
import com.example.phh.MqttClientRequestParser;
import com.example.phh.ParseElementXml;
import com.example.phh.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.arnaudguyon.xmltojsonlib.XmlToJson;


public class PatientState extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    public Button btnRetrieve;
    public Button submit;
    public Switch Switch_MQTT;
    public TextView textViewData;
    public TextView heart_state;
    public TextView temp_state;
    public TextView oxygen_state;
    // added by J. Yun, SCH Univ.
    public TextView textTemp;
    public TextView heart_value;
    public TextView oxygen_value;
    public TextView oxygen_value2;
    public  TextView temp_value;
    public Handler handler;
    public ToggleButton btnAddr_Set;
    //추가내용
    public TextView textName;

    private static CSEBase csebase = new CSEBase();
    private static AE ae = new AE();
    private static String TAG = "PatientState";
    private String MQTTPort = "1883";

    // Modify this variable associated with your AE name in Mobius, by J. Yun, SCH Univ.
    private String ServiceAEName = "P1";

    private String MQTT_Req_Topic = "";
    private String MQTT_Resp_Topic = "";
    private MqttAndroidClient mqttClient = null;
    private EditText EditText_Address =null;
    private String Mobius_Address ="";
    public PatientState() {
        handler = new Handler();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_state);

        submit = (Button)findViewById(R.id.button);
        textTemp = (TextView)findViewById(R.id.temp_value);//온도 값
        heart_value = (TextView)findViewById(R.id.heart_value);
        oxygen_value = (TextView)findViewById(R.id.oxygen_value);
        temp_state = (TextView)findViewById(R.id.temp_state);
        heart_state = (TextView)findViewById(R.id.heart_state);
        oxygen_state = (TextView)findViewById(R.id.oxygen_state);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                temp_state.setText("정상");
                heart_state.setText("정상");
                oxygen_state.setText("정상");
            }
        }, 5000);


        System.out.println("뷰 불러옴");
        GetAEInfo();
        System.out.println("인포됨");



    }
    private void postStatevalue() {
        httppost httpThread = new httppost(PatientState.this, new httppost.EventListener() {
            @Override
            public void onHttpResult(String result) {
                System.out.println(result);
            }

            @Override
            public void onHttpFailed() {
                System.out.println("http 실패!");
            }
        });
        httpThread.start();
    }

    public void GetAEInfo() {

        // You can put the IP address directly in code,
        // but also get it from EditText window
        //Mobius_Address = EditText_Address.getText().toString();
        // Mobius_Address = "203.253.128.177";
        // csebase.setInfo(Mobius_Address,"7579","Mobius","1883");
        csebase.setInfo("203.253.128.177","7579","Mobius","1883");

        // AE Create for Android AE
        ae.setAppName("ncubeapp");
        aeCreateRequest aeCreate = new aeCreateRequest();
        aeCreate.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        Log.d(TAG, "** AE Create ResponseCode[" + msg + "]");
                        if (Integer.parseInt(msg) == 201) {
                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/" + ae.getAEid() + "_sub" + "/#";
                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/" + ae.getAEid() + "_sub" + "/json";
                            Log.d(TAG, "ReqTopic[" + MQTT_Req_Topic + "]");//주기적으로 불러오기
                            Log.d(TAG, "ResTopic[" + MQTT_Resp_Topic + "]");
                        } else { // If AE is Exist , GET AEID
                            aeRetrieveRequest aeRetrive = new aeRetrieveRequest();
                            aeRetrive.setReceiver(new IReceived() {
                                public void getResponseBody(final String resmsg) {
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Log.d(TAG, "** AE Retrive ResponseCode[" + resmsg + "]");
                                            MQTT_Req_Topic = "/oneM2M/req/Mobius2/" + ae.getAEid() + "_sub" + "/#";
                                            MQTT_Resp_Topic = "/oneM2M/resp/Mobius2/" + ae.getAEid() + "_sub" + "/json";
                                            Log.d(TAG, "ReqTopic[" + MQTT_Req_Topic + "]");
                                            Log.d(TAG, "ResTopic[" + MQTT_Resp_Topic + "]");
                                        }
                                    });
                                }
                            });
                            aeRetrive.start();
                        }
                    }
                });
            }
        });
        aeCreate.start();
    }
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Log.d(TAG, "MQTT Create");
            MQTT_Create(true);
        } else {
            Log.d(TAG, "MQTT Close");
            MQTT_Create(false);
        }
    }


    /* MQTT Subscription */
    public void MQTT_Create(boolean mtqqStart) {
        if (mtqqStart && mqttClient == null) {
            /* Subscription Resource Create to Yellow Turtle */
            // added by J. Yun, SCH Univ.
            SubscribeResource subcribeResource = new SubscribeResource("temp");
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Creation Response ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();

            // added by J. Yun, SCH Univ.
            subcribeResource = new SubscribeResource("hr");
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Creation Response ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();

            // added by J. Yun, SCH Univ.
            subcribeResource = new SubscribeResource("spo2");
            subcribeResource.setReceiver(new IReceived() {
                public void getResponseBody(final String msg) {
                    handler.post(new Runnable() {
                        public void run() {
                            textViewData.setText("**** Subscription Resource Creation Response ****\r\n\r\n" + msg);
                        }
                    });
                }
            });
            subcribeResource.start();

            /* MQTT Subscribe */
            mqttClient = new MqttAndroidClient(this.getApplicationContext(), "tcp://" + csebase.getHost() + ":" + csebase.getMQTTPort(), MqttClient.generateClientId());
            mqttClient.setCallback(mainMqttCallback);
            try {
                // added by J. Yun, SCH Univ.
                MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
                mqttConnectOptions.setKeepAliveInterval(600);
                mqttConnectOptions.setCleanSession(false);


                IMqttToken token = mqttClient.connect(mqttConnectOptions);
//                IMqttToken token = mqttClient.connect();
                token.setActionCallback(mainIMqttActionListener);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            /* MQTT unSubscribe or Client Close */
            mqttClient.setCallback(null);
            mqttClient.close();
            mqttClient = null;
        }
    }

    /* MQTT Listener */
    private IMqttActionListener mainIMqttActionListener = new IMqttActionListener() {
        @Override
        public void onSuccess(IMqttToken asyncActionToken) {
            Log.d(TAG, "onSuccess");
            String payload = "";
            int mqttQos = 1; /* 0: NO QoS, 1: No Check , 2: Each Check */

            MqttMessage message = new MqttMessage(payload.getBytes());
            try {
                mqttClient.subscribe(MQTT_Req_Topic, mqttQos);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
            Log.d(TAG, "onFailure");
        }
    };

    /* MQTT Broker Message Received */
    private MqttCallback mainMqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.d(TAG, "connectionLost");
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {

            Log.d(TAG, "messageArrived");

            textViewData.setText("");
            textViewData.setText("MQTT data received\r\n\r\n" + message.toString().replaceAll(",", "\n"));
            Log.d(TAG, "Notify ResMessage:" + message.toString());

            // Added by J. Yun, SCH Univ.
//            JSONObject obj = new JSONObject(message.toString());
//            String con = getContainerContentJSON(message.toString());
//            Log.d(TAG, "Received content is " + con);
//            textViewData.setText(con);

            // Added by J. Yun, SCH Univ.
            String cnt = getContainerName(message.toString());
            Log.d(TAG, "Received container name is " + cnt);
            //textViewData.setText(cnt);
            if (cnt.indexOf("temp") != -1) {
                textTemp.setText(getContainerContentJSON(message.toString()));
            }
            //else if (cnt.indexOf("temp2") != -1)
            //textTemp.setText(getContainerContentJSON(message.toString()));
            else if (cnt.indexOf("hr") != -1) {
                if (getContainerContentJSON(message.toString())=="0" ) {
                    Toast.makeText(PatientState.this,"긴급신호 전송 완료! 잠시후 자택으로 구급대가 출동합니다.",Toast.LENGTH_LONG).show();
                    // 서버로 긴급 신호 상태 전송
                    postStatevalue();

                }
                heart_value.setText(getContainerContentJSON(message.toString()));
            }
            else if (cnt.indexOf("spo2") != -1) {
                heart_value.setText(getContainerContentJSON(message.toString()));
            }
            else
                ;

            /* Json Type Response Parsing */
            String retrqi = MqttClientRequestParser.notificationJsonParse(message.toString());
            Log.d(TAG, "RQI["+ retrqi +"]");

            String responseMessage = MqttClientRequest.notificationResponse(retrqi);
            Log.d(TAG, "Recv OK ResMessage ["+responseMessage+"]");

            /* Make json for MQTT Response Message */
            MqttMessage res_message = new MqttMessage(responseMessage.getBytes());

            try {
                mqttClient.publish(MQTT_Resp_Topic, res_message);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.d(TAG, "deliveryComplete");
        }

    };

    // Added by J. Yun, SCH Univ.
    private String getContainerName(String msg) {
        String cnt = "";
        try {
            JSONObject jsonObject = new JSONObject(msg);
            cnt = jsonObject.getJSONObject("pc").
                    getJSONObject("m2m:sgn").getString("sur");
            // Log.d(TAG, "Content is " + cnt);
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject error!");
        }
        return cnt;
    }


    //새로 수정중임,
    // Added by J. Yun, SCH Univ.
    private String getContainerContentJSON(String msg) {
        String con = "";
        //String[] con =new String[2];
        try {
            JSONObject jsonObject = new JSONObject(msg);
            con = jsonObject.getJSONObject("pc").
                    getJSONObject("m2m:sgn").
                    getJSONObject("nev").
                    getJSONObject("rep").
                    getJSONObject("m2m:cin").
                    getString("con");
//            Log.d(TAG, "Content is " + con);
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject error!");
        }
        return con;
    }



    // Added by J. Yun, SCH Univ.
    private String getContainerContentXML(String msg) {
        String con = "";
        try {
            XmlToJson xmlToJson = new XmlToJson.Builder(msg).build();
            JSONObject jsonObject = xmlToJson.toJson();
            con = jsonObject.getJSONObject("m2m:cin").getString("con");
//            Log.d(TAG, "Content is " + con);
        } catch (JSONException e) {
            Log.e(TAG, "JSONObject error!");
        }
        return con;
    }



    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onStop() {
        super.onStop();

    }

    public void OnClickHandler(View view) {//버튼 누르면 가져옴
        RetrieveRequest req = new RetrieveRequest("temp");
        req.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        textTemp.setText(getContainerContentXML(msg));
                    }
                });
            }
        });
        req.start();

        req = new RetrieveRequest("hr");
        req.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        heart_value.setText(getContainerContentXML(msg));
                    }
                });
            }
        });
        req.start();

        req = new RetrieveRequest("spo2");
        req.setReceiver(new IReceived() {
            public void getResponseBody(final String msg) {
                handler.post(new Runnable() {
                    public void run() {
                        oxygen_value.setText(getContainerContentXML(msg));
                    }
                });
            }
        });
        req.start();

    }

    /* Response callback Interface */
    public interface IReceived {
        void getResponseBody(String msg);
    }

    // Retrieve PIR and Sound Sensor, added by J. Yun, SCH Univ. //서련 추가함
    class RetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(RetrieveRequest.class.getName());
        private IReceived receiver;
        //        private String ContainerName = "cnt-co2";
        private String ContainerName = "";


        public RetrieveRequest(String containerName) {
            this.ContainerName = containerName;
        }
        public RetrieveRequest() {}
        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + ContainerName + "/" + "latest";

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid() );
                conn.setRequestProperty("nmtype", "long");
                conn.connect();

                String strResp = "";
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String strLine= "";
                while ((strLine = in.readLine()) != null) {
                    strResp += strLine;
                }

                if ( strResp != "" ) {
                    receiver.getResponseBody(strResp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.WARNING, exp.getMessage());
            }
        }
    }

    /* Request AE Creation */
    class aeCreateRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        String TAG = aeCreateRequest.class.getName();
        private IReceived receiver;
        int responseCode=0;
        public ApplicationEntityObject applicationEntity;
        public void setReceiver(IReceived handler) { this.receiver = handler; }
        public aeCreateRequest(){
            applicationEntity = new ApplicationEntityObject();
            applicationEntity.setResourceName(ae.getappName());
            Log.d(TAG, ae.getappName() + "JJjj");
        }
        @Override
        public void run() {
            try {

                String sb = csebase.getServiceUrl();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml;ty=2");
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-Origin", "S"+ae.getappName());
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-NM", ae.getappName() );

                String reqXml = applicationEntity.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqXml.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqXml.getBytes());
                dos.flush();
                dos.close();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 201) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    Log.d(TAG, "Create Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }

        }
    }
    /* Retrieve AE-ID */
    class aeRetrieveRequest extends Thread {
        private final Logger LOG = Logger.getLogger(aeCreateRequest.class.getName());
        private IReceived receiver;
        int responseCode=0;

        public aeRetrieveRequest() {
        }
        public void setReceiver(IReceived hanlder) {
            this.receiver = hanlder;
        }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl()+"/"+ ae.getappName();
                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", "Sandoroid");
                conn.setRequestProperty("nmtype", "short");
                conn.connect();

                responseCode = conn.getResponseCode();

                BufferedReader in = null;
                String aei = "";
                if (responseCode == 200) {
                    // Get AEID from Response Data
                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String resp = "";
                    String strLine;
                    while ((strLine = in.readLine()) != null) {
                        resp += strLine;
                    }

                    ParseElementXml pxml = new ParseElementXml();
                    aei = pxml.GetElementXml(resp, "aei");
                    ae.setAEid( aei );
                    //Log.d(TAG, "Retrieve Get AEID[" + aei + "]");
                    in.close();
                }
                if (responseCode != 0) {
                    receiver.getResponseBody( Integer.toString(responseCode) );
                }
                conn.disconnect();
            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }

    /* Subscribe Co2 Content Resource */
    class SubscribeResource extends Thread {
        private final Logger LOG = Logger.getLogger(SubscribeResource.class.getName());
        private IReceived receiver;
        //        private String container_name = "cnt-co2"; //change to control container name
        private String container_name; //change to control container name

        public ContentSubscribeObject subscribeInstance;
        public SubscribeResource(String containerName) {
            subscribeInstance = new ContentSubscribeObject();
            subscribeInstance.setUrl(csebase.getHost());
            subscribeInstance.setResourceName(ae.getAEid()+"_rn");
            subscribeInstance.setPath(ae.getAEid()+"_sub");
            subscribeInstance.setOrigin_id(ae.getAEid());

            // added by J. Yun, SCH Univ.
            this.container_name = containerName;
        }

        public void setReceiver(IReceived hanlder) { this.receiver = hanlder; }

        @Override
        public void run() {
            try {
                String sb = csebase.getServiceUrl() + "/" + ServiceAEName + "/" + container_name;

                URL mUrl = new URL(sb);

                HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);

                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/vnd.onem2m-res+xml; ty=23");
                conn.setRequestProperty("locale", "ko");
                conn.setRequestProperty("X-M2M-RI", "12345");
                conn.setRequestProperty("X-M2M-Origin", ae.getAEid());

                String reqmqttContent = subscribeInstance.makeXML();
                conn.setRequestProperty("Content-Length", String.valueOf(reqmqttContent.length()));

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                dos.write(reqmqttContent.getBytes());
                dos.flush();
                dos.close();

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                String resp = "";
                String strLine="";
                while ((strLine = in.readLine()) != null) {
                    resp += strLine;
                }

                if (resp != "") {
                    receiver.getResponseBody(resp);
                }
                conn.disconnect();

            } catch (Exception exp) {
                LOG.log(Level.SEVERE, exp.getMessage());
            }
        }
    }
}