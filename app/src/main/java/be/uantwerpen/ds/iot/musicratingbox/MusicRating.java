package be.uantwerpen.ds.iot.musicratingbox;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MusicRating extends AppCompatActivity implements MqttCallback {

    FloatingActionButton greenButton;
    FloatingActionButton redButton;
    TextView songTitleTextView;
    TextView songArtistTextView;
    TextView songAlbumTextView;
    TextView reconnectTextView;
    Button reconnectButton;
    final String serverUri = "tcp://broker.hivemq.com:1883";
    String clientId = MqttClient.generateClientId();
    final String subscriptionTopic = "songInformation";
    final String publishTopic = "songVote";
    MqttAndroidClient mqttAndroidClient;
    String songID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_rating);
        greenButton = (FloatingActionButton)findViewById(R.id.greenButton);
        redButton = (FloatingActionButton)findViewById(R.id.redButton);
        reconnectButton = (Button)findViewById(R.id.reconnectButton);
        reconnectTextView = (TextView)findViewById(R.id.reconnectTextView);
        disableButtons();
        songTitleTextView = (TextView)findViewById(R.id.songTitleTextView);
        songArtistTextView = (TextView)findViewById(R.id.songArtistTextView);
        songAlbumTextView = (TextView)findViewById(R.id.songAlbumTextView);
        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);
        mqttAndroidClient.setCallback(MusicRating.this);
        connectMQTT();
    }

    public void connectMQTT(){
        try {
            IMqttToken token = mqttAndroidClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    enableText();
                    enableButtons();
                    reconnectButton.setVisibility(View.INVISIBLE);
                    reconnectTextView.setVisibility(View.INVISIBLE);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MusicRating.this, "ERROR: Cannot connect", Toast.LENGTH_SHORT).show();
                    reconnectButton.setVisibility(View.VISIBLE);
                    reconnectTextView.setVisibility(View.VISIBLE);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void reconnectMQTT(View v){
        connectMQTT();
    }

    public void onClickGreen(View v) {
        Toast.makeText(MusicRating.this, "You have upvoted this song!", Toast.LENGTH_SHORT).show();
        publishMessage(songID + "1");
        timeoutButtons(2000);
    }

    public void onClickRed(View v) {
        Toast.makeText(MusicRating.this, "You have downvoted this song!", Toast.LENGTH_SHORT).show();
        publishMessage(songID + "0");
        timeoutButtons(2000);

    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MusicRating.this, "ERROR: Cannot connect(subscription)", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException ex){
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    public void publishMessage(String publishMessage){

        try {
            MqttMessage message = new MqttMessage();
            message.setPayload(publishMessage.getBytes());
            mqttAndroidClient.publish(publishTopic, message);
        } catch (MqttException e) {
            System.err.println("Error Publishing: " + e.getMessage());
            Toast.makeText(MusicRating.this, "ERROR: Cannot connect(publish)", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        enableButtons();
        setSongInformation(mqttMessage);
    }

    private void setSongInformation(MqttMessage mqttMessage){
        String[] songParts = mqttMessage.toString().split("#%");
        songTitleTextView.setText(songParts[1]);
        if(songParts[2] != ""){
            songArtistTextView.setText(songParts[2]);
        }else{
            songArtistTextView.setText("");
        }
        if(songParts[3] != ""){
            if(songParts[4] != ""){
                songAlbumTextView.setText(songParts[3] + "(" + songParts[4] + ")");
            }else{
                songAlbumTextView.setText(songParts[3]);
            }

        }else {
            songAlbumTextView.setText("");
        }
        songID = songParts[0];
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
    public void timeoutButtons(int timeout){
        disableButtons();
        greenButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                enableButtons();
            }
        }, timeout);
    }

    public void enableText(){
        songTitleTextView.setVisibility(View.VISIBLE);
        songArtistTextView.setVisibility(View.VISIBLE);
        songAlbumTextView.setVisibility(View.VISIBLE);
    }

    public void disableButtons(){
        greenButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        redButton.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
        greenButton.setEnabled(false);
        redButton.setEnabled(false);
    }

    public void enableButtons(){
        greenButton.setVisibility(View.VISIBLE);
        redButton.setVisibility(View.VISIBLE);
        greenButton.setEnabled(true);
        redButton.setEnabled(true);
        greenButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(36,163,8)));
        redButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(163,8,8)));
    }
}
