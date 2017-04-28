package tools.unrealbot.aj.wephone;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity{
    private String myTag= "AJ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //REGISTER A BROADCAST RECEIVER
        MyReceiver myrcr = new MyReceiver();
        IntentFilter itf = new IntentFilter();
        itf.addAction("fromSDKManager");
        MySDKManager MySdkInstance = new MySDKManager(this);
        MySdkInstance.CreateClientConfig();
        Log.i(myTag, "Created Client Config");
        MySdkInstance.createUserConfiguration("80813","123456", "lab.com",false,"135.60.51.130",5060);
        MySdkInstance.registerUser();



    }

    //Implement a Broadcast Receiver to Receive the Register Event
    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.i(myTag, getClass().getCanonicalName()+ ": Got event: "+message);
            //IF LOOP TO TAKE VARIOUS ACTION ON WHATEVER BE THE MESSAGE


        }
    }
}
