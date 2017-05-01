package tools.unrealbot.aj.wephone;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity{
    private String myTag= "AJ";
    // Define Strings to collect user Details
    private String hostname;
    private String extNo;
    private String password;
    private String domain;
    private boolean useTls;
    private MySDKManager MySdkInstance;
    private MyReceiver myrcr;
    private IntentFilter itf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //REGISTER A BROADCAST RECEIVER
        myrcr = new MyReceiver();
        itf = new IntentFilter();
        itf.addAction("fromSDKManager");
        LocalBroadcastManager.getInstance(this).registerReceiver(myrcr,itf);


        //Code to get the inputs when user presses the Login Button
        Button loginR = (Button)findViewById(R.id.login);
        loginR.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Trying to Register", Toast.LENGTH_SHORT).show();
                getInputs();
            }
        });



    }

    @Override
    protected void onResume() {
        Log.i(myTag, getClass().getCanonicalName()+" : Registering the receivers again on Activity Resume");
        LocalBroadcastManager.getInstance(this).registerReceiver(myrcr,itf);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(myTag, getClass().getCanonicalName()+" : Unregistering the receivers on Activity Pause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myrcr);
        super.onPause();
    }

    //METHOD TO START A NEW SCREEN

    private void startMenuScreen(){
        Intent myIntent = new Intent(this, CallMenu.class);
        startActivity(myIntent);
    }

    // Method to get the User Inputs so we can register
    private void getInputs(){
        EditText hostnameR = (EditText)findViewById(R.id.hostname);
        EditText extNoR = (EditText)findViewById(R.id.extNo);
        EditText passwordR = (EditText)findViewById(R.id.password);
        EditText domainR = (EditText)findViewById(R.id.domain);
        Switch useTlsR = (Switch) findViewById(R.id.tlsSwitch);
        hostname=hostnameR.getText().toString();
        extNo=extNoR.getText().toString();
        password=passwordR.getText().toString();
        domain=domainR.getText().toString();
        //useTls=useTlsR.g
        MySdkInstance = new MySDKManager(this);
        MySdkInstance.CreateClientConfig();
        Log.i(myTag, "Created Client Config");

        //Using the inputs that were entered try to register the user

        MySdkInstance.createUserConfiguration("80813","123456", "lab.com",false,"135.60.51.130",5060);
        //MySdkInstance.createUserConfiguration(extNo,password,domain,false,hostname,5060);
        MySdkInstance.registerUser();
    }

    //Implement a Broadcast Receiver to Receive the Register Event
    private class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            Log.i(myTag, getClass().getCanonicalName()+ ": Got event: "+message);

            //IF LOOP TO TAKE VARIOUS ACTION ON WHATEVER BE THE MESSAGE
            if(message.equals("onUserRegistrationSuccessful")){
                //Show a new activity that will show make Call Screen etc
                startMenuScreen();
            }
            else if(message.equals("onUserRegistrationFailed")){
                //Do not change Screen, but show a popup showing that something wrong
                startMenuScreen();
            }


        }
    }
}
