package tools.unrealbot.aj.wephone;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity{
    private String myTag= "AJ";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MySDKManager MySdkInstance = new MySDKManager(this);
        MySdkInstance.CreateClientConfig();
        Log.i(myTag, "Created Client Config");
        MySdkInstance.createUserConfiguration("80813","123456", "lab.com",false,"135.60.51.130",5060);
        MySdkInstance.registerUser();

    }
}
