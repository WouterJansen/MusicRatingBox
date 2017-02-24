package be.uantwerpen.ds.iot.musicratingbox;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MusicRating extends AppCompatActivity {

    TextView songTitleTextView;
    MqttAndroidClient mqttAndroidClient;
    final String serverUri = "tcp://test.mosquitto.org:1883";
    String clientId = MqttClient.generateClientId();
    final String subscriptionTopic = "songInformation";
    final String publishTopic = "songVote";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_rating);
        songTitleTextView = (TextView)findViewById(R.id.songTitleTextView);

        mqttAndroidClient = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        try {
            IMqttToken token = mqttAndroidClient.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void onClickGreen(View v) {
        Toast.makeText(MusicRating.this, "You have upvoted this song!", Toast.LENGTH_SHORT).show();
        songTitleTextView.setText("Upvote");
        publishMessage("upvoted");
    }

    public void onClickRed(View v) {
        Toast.makeText(MusicRating.this, "You have downvoted this song!", Toast.LENGTH_SHORT).show();
        songTitleTextView.setText("Downvote");
        publishMessage("downvoted");
    }

    public void subscribeToTopic(){
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    songTitleTextView.setText("song changed");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
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
            e.printStackTrace();
        }
    }
}
