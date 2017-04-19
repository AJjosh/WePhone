package tools.unrealbot.aj.wephone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import com.avaya.clientservices.call.Call;
import com.avaya.clientservices.call.CallEndReason;
import com.avaya.clientservices.call.CallException;
import com.avaya.clientservices.call.CallListener;
import com.avaya.clientservices.call.CallService;
import com.avaya.clientservices.call.CallServiceListener;

import com.avaya.clientservices.call.VideoChannel;
import com.avaya.clientservices.client.Client;
import com.avaya.clientservices.client.ClientConfiguration;
import com.avaya.clientservices.client.ClientListener;
import com.avaya.clientservices.client.CreateUserCompletionHandler;
import com.avaya.clientservices.client.UserCreatedException;
import com.avaya.clientservices.common.ConnectionPolicy;
import com.avaya.clientservices.common.SignalingServer;
import com.avaya.clientservices.credentials.Challenge;
import com.avaya.clientservices.credentials.CredentialCompletionHandler;
import com.avaya.clientservices.credentials.CredentialProvider;
import com.avaya.clientservices.credentials.UserCredential;
import com.avaya.clientservices.media.capture.VideoCamera;
import com.avaya.clientservices.media.capture.VideoCaptureController;
import com.avaya.clientservices.provider.sip.SIPUserConfiguration;
import com.avaya.clientservices.user.User;
import com.avaya.clientservices.user.UserConfiguration;
import com.avaya.clientservices.user.UserRegistrationListener;

import java.io.File;
import java.util.List;



public class SDKManager implements UserRegistrationListener, ClientListener, CredentialProvider, CallServiceListener, CallListener {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private static final String CLIENTSDK_TEST_APP_PREFS = "com.avaya.android.prefs";
    private static final String MESSAGE_RECEIVER = "messageReceiver";
    private static final String TOAST_TAG = "toastMessage";

    private static final String CALL_EVENT_RINGING = "onCallRemoteAlerting";
    private static final String CALL_EVENT_ESTABLISHED = "onCallEstablished";
    private static final String CALL_EVENT_ENDED = "onCallEnded";


    private static final String CALL_EVENT_CAPABILITIES_CHANGED = "onCallCapabilitiesChanged";
    private static final String CALL_EVENT_REMOTE_ADDRESS_CHANGED = "onCallRemoteAddressChanged";
    private static final String CALL_EVENT_REDIRECTED = "onCallRedirected";
    private static final String CALL_EVENT_TAG = "callEvent";
    private static final String CALL_EVENTS_RECEIVER = "callEventsReceiver";
    private static final String CALL_EVENT_DENIED = "onCallDenied";
    private static final String CALL_EVENT_IGNORED = "onCallIgnored";
    private static final String CALL_EVENT_VIDEO_CHANNELS_UPDATED = "onCallVideoChannelsUpdated";

    private static final String LOGIN_RECEIVER = "loginReceiver";
    private static final String LOGIN_TAG = "loginStatus";


    // Singleton instance of SDKManager
    private static volatile SDKManager instance;
    private static volatile VideoCaptureController videoCaptureController;
    public static final String CALL_ID = "callId";

    private final Activity activity;

    private SharedPreferences settings;
    private AlertDialog incomingCallDialog;
    private UserConfiguration userConfiguration;
    private Client mClient;
    private User mUser;
    private boolean isUserLoggedIn = false;
    private static Call inComingCall;

    private static final String address =  "sm-113.collaboratory.avaya.com";
    private static final int port = 5061;
    private static final String domain = "collaboratory.avaya.com";
    private static final boolean useTls = true;
    private static final String extension = "2304";
    private static final String password = "123456";

    TextView status;

    private SDKManager(Activity activity) {
        this.activity = activity;
        //status = (TextView) activity.findViewById(R.id.registrationStatus);
    }



    public static SDKManager getInstance(Activity activity) {
        if (instance == null) {
            synchronized (SDKManager.class) {
                if (instance == null) {
                    instance = new SDKManager(activity);
                }
            }
        }
        return instance;
    }

    // Configure and create mClient
    // TODO Exercise 1.1 - Creating and configuring a Client
    public void setupClientConfiguration(Application application) {
        // Create client configuration
        // follow the Technical Articles "Initializing the SDK" at http://www.devconnectprogram.com developer HUB

        String productName = activity.getResources().getString(R.string.productName);
        String productVersion = activity.getResources().getString(R.string.productVersion);
        String buildNumber = activity.getResources().getString(R.string.buildNumber);
        String vendorName = activity.getResources().getString(R.string.vendorName);
        String dataDirectory = activity.getResources().getString(R.string.dataDirectory);

        // Configuring a Client
        ClientConfiguration clientConfiguration = new ClientConfiguration(dataDirectory, productName,
                productVersion, Build.MODEL, Build.VERSION.RELEASE, buildNumber, vendorName);

        // Set user agent name
        clientConfiguration.setUserAgentName(productName + '(' + Build.MODEL + ')');

        // Create Client
        mClient = new Client(clientConfiguration, application, this);

    }

    /////////////////////////////////CREATE A NEW METHOD FOR NEW USER

    /*
    public void setupUserConfigurationWithParams(String address, int port, String domain, boolean useTLS, String extension, int password) {
        // Creating and configuring a User
        // follow the Technical Articles "Initializing the SDK" at http://www.devconnectprogram.com developer HUB
        Log.d("ADDRESS FROM NEW METHOD", address);
        Log.d("PORT FROM NEW METHOD", String.valueOf(port) );
        Log.d("DOMAIN FROM NEW METHOD", domain);
        Log.d("EXT FROM NEW METHOD", String.valueOf(extension));

        // Create SIP UserConfiguration
        userConfiguration = new UserConfiguration();
        SIPUserConfiguration sipConfig = userConfiguration.getSIPUserConfiguration();

        // Set SIP service enabled and configure userID and domain
        sipConfig.setEnabled(true);
        sipConfig.setUserId(extension);
        sipConfig.setDomain(domain);

        // Configure Session Manager connection details
        SignalingServer.TransportType transportType =
                useTls ? SignalingServer.TransportType.TLS : SignalingServer.TransportType.TCP;

        SignalingServer sipSignalingServer = new SignalingServer(transportType, address, port,
                SignalingServer.FailbackPolicy.AUTOMATIC);

        //encapsulate the server configurations in a single ConnectionPolicy object:
        sipConfig.setConnectionPolicy(new ConnectionPolicy(sipSignalingServer));

        // Set CredentialProvider
        // Passwords are requested by and communicated to the Client SDK using the CredentialProvider interface:
        sipConfig.setCredentialProvider(this);

        // And finally save your configuration back to your user object:
        userConfiguration.setSIPUserConfiguration(sipConfig);

    }
    */
    ///////////////////////////////////FINISH THE NEW METHOD

    // Configure and create mUser
    //TODO Exercise 1.2 - Configuring a User
    public void setupUserConfiguration() {
        // Creating and configuring a User
        // follow the Technical Articles "Initializing the SDK" at http://www.devconnectprogram.com developer HUB
        Log.d("ADDRESS...............", address);
        Log.d("PORT..................", String.valueOf(port) );
        Log.d("DOMAIN................", domain);
        Log.d("USE_TLS...............", String.valueOf(useTls));
        Log.d("EXTENSION.............", extension);

        // Create SIP UserConfiguration
        userConfiguration = new UserConfiguration();
        SIPUserConfiguration sipConfig = userConfiguration.getSIPUserConfiguration();

        // Set SIP service enabled and configure userID and domain
        sipConfig.setEnabled(true);
        sipConfig.setUserId(extension);
        sipConfig.setDomain(domain);

        // Configure Session Manager connection details
        SignalingServer.TransportType transportType =
                useTls ? SignalingServer.TransportType.TLS : SignalingServer.TransportType.TCP;

        SignalingServer sipSignalingServer = new SignalingServer(transportType, address, port,
                SignalingServer.FailbackPolicy.AUTOMATIC);

        //encapsulate the server configurations in a single ConnectionPolicy object:
        sipConfig.setConnectionPolicy(new ConnectionPolicy(sipSignalingServer));

        // Set CredentialProvider
        // Passwords are requested by and communicated to the Client SDK using the CredentialProvider interface:
        sipConfig.setCredentialProvider(this);

        // And finally save your configuration back to your user object:
        userConfiguration.setSIPUserConfiguration(sipConfig);

    }
    //TODO Exercise 1.3 - Creation of the User object
    public void register() {
        // Creation of the User object
        // follow the Technical Articles "Initializing the SDK" at http://www.devconnectprogram.com developer HUB
        Log.d(LOG_TAG, "Register user");

        if (mUser != null) {
            // Login if user already exist
            mUser.start();
        } else {
            // Create user if not created yet
            mClient.createUser(userConfiguration, new CreateUserCompletionHandler() {
                @Override
                public void onSuccess(User user) {
                    Log.d(LOG_TAG, "createUser onSuccess");
                    // Initialize class member mUser if we created user successfully
                    mUser = user;
                    Log.d(LOG_TAG, "User Id = " + mUser.getUserId());
                    //status log for the 2026v training mini App exercise
                  //  status.append("\n User Id = "+ mUser.getUserId());

                    // Adds a new registration listener to the User instance.
                    mUser.addRegistrationListener(SDKManager.this);

                    // TODO Exercise 4 define a CallServiceListener for incoming calls
                    //Get the service object to use for basic calls.
                    CallService callService = mUser.getCallService();

                    if (callService != null) {
                        Log.d(LOG_TAG, "CallService is ready to use");
                        // Subscribe to CallService events for incoming call handling
                        callService.addListener(getInstance(activity));
                    }

                    // Starts registration with the communications servers.
                    // Feedback is provided via the UserRegistrationListener callbacks.
                    mUser.start();

                }

                @Override
                public void onError(UserCreatedException e) {
                    Log.e(LOG_TAG, "createUser onError " + e.getFailureReason());

                    //status log for the 2026v training mini App exercise
                    status.append("\n createUser onError ");
                }
            });
        }
    }

    public User getUser() {
        return mUser;
    }

    public void shutdownClient() {
        Log.d(LOG_TAG, "Shutdown client");

        //Remove call service listener as we are not going to receive calls anymore
        if (mUser != null) {
            CallService callService = mUser.getCallService();
            if (callService != null) {
                callService.removeListener(getInstance(activity));
            }
            mUser.stop();
        }

        // gracefulShutdown true will try to disconnect the user from servers
        if (mClient != null) {
            mClient.shutdown(true);
        }
    }

    public void delete(boolean loginStatus) {
        Log.d(LOG_TAG, "Delete user");
        if (mUser != null) {
            Log.d(LOG_TAG, "User exist. Deleting...");
            mClient.removeUser(mUser, loginStatus);
            mUser = null;
        }
    }

    public boolean isUserLoggedIn() {
        return isUserLoggedIn;
    }

   /*
   * UserRegistrationListener section
   */
    @Override
    public void onUserRegistrationInProgress(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserRegistrationInProgress");
    }

    // onUserRegistrationSuccessful is called when signaling server respond that provided user
    // credentials are fine and user successfully registered on the server.
    @Override
    public void onUserRegistrationSuccessful(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserRegistrationSuccessful");
        isUserLoggedIn = true;

        //status log for the 2026v training mini App exercise
//        status.append("\nSuccessfully logged in: "+extension);

        // Send broadcast to notify SettingsCallServiceFragments that login label may changed
        activity.sendBroadcast(new Intent(LOGIN_RECEIVER).putExtra(LOGIN_TAG, isUserLoggedIn));
        // Send broadcast to notify MainActivity to show message to the user
        activity.sendBroadcast(new Intent(MESSAGE_RECEIVER).putExtra(TOAST_TAG,
                "Successfully logged in: "
                        + userConfiguration.getSIPUserConfiguration().getUserId()));
        // msg - user registration successful
    }

    @Override
    public void onUserRegistrationFailed(User user, SignalingServer signalingServer, Exception e) {
        Log.d(LOG_TAG, "onUserRegistrationFailed " + e.toString());
        isUserLoggedIn = false;

        //status log for the 2026v training mini App exercise
        status.append("\nFailed to login");

        // Send broadcast to notify SettingsCallServiceFragments that login label may changed
        activity.sendBroadcast(new Intent(LOGIN_RECEIVER).putExtra(LOGIN_TAG, isUserLoggedIn));
        // Send broadcast to notify MainActivity to show message to the user
        activity.sendBroadcast(new Intent(MESSAGE_RECEIVER).putExtra(TOAST_TAG, "Failed to login: "
                + e.getLocalizedMessage()));
    }

    @Override
    public void onUserAllRegistrationsSuccessful(User user) {
        Log.d(LOG_TAG, "onUserAllRegistrationsSuccessful");
    }

    @Override
    public void onUserAllRegistrationsFailed(User user, boolean b) {
        Log.d(LOG_TAG, "onUserRegistrationFailed ");
    }

    @Override
    public void onUserUnregistrationInProgress(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserUnregistrationInProgress");
    }

    @Override
    public void onUserUnregistrationSuccessful(User user, SignalingServer signalingServer) {
        Log.d(LOG_TAG, "onUserUnregistrationSuccessful");
    }

    @Override
    public void onUserUnregistrationFailed(User user, SignalingServer signalingServer, Exception e) {
        Log.d(LOG_TAG, "onUserUnregistrationFailed " + e.toString());
    }

    // onUserUnregistrationComplete is called when server respond that user successfully
    // unregistered.
    @Override
    public void onUserUnregistrationComplete(User user) {
        Log.d(LOG_TAG, "onUserUnregistrationComplete");
        isUserLoggedIn = false;

        // Send broadcast to notify SettingsCallServiceFragments that login label may changed
        activity.sendBroadcast(new Intent(LOGIN_RECEIVER).putExtra(LOGIN_TAG, isUserLoggedIn));
        // Send broadcast to notify MainActivity to show message to the user
        activity.sendBroadcast(new Intent(MESSAGE_RECEIVER).putExtra(TOAST_TAG,
                "Successfully logged off: "
                        + userConfiguration.getSIPUserConfiguration().getUserId()));
    }

        /*
        * ClientListener section
        */

    @Override
    public void onClientShutdown(Client client) {
        Log.d(LOG_TAG, "onClientShutdown");
    }


    @Override
    public void onClientUserCreated(Client client, User user) {
        Log.d(LOG_TAG, "onClientUserCreated");
        //status log for the 2026v training mini App exercise
//        status.append("\nClient created version:"+client.getVersion());
    }

    // onClientUserRemoved executed when Client.removeUser() is called and successfully completed
    @Override
    public void onClientUserRemoved(Client client, User user) {
        Log.d(LOG_TAG, "onClientUserRemoved");
        // User was deleted due to settings update. Let's create new user with updated
        // configuration.
        setupUserConfiguration();
    }

    /*
     * CallServiceListener listener section
     */
    // TODO Exercise 4 Create a dialog to accept a call
    @Override
    public void onIncomingCallReceived(CallService callService, Call call) {
        Log.d(LOG_TAG, "onIncomingCall");
        // Dismiss active dialog if any
        if (incomingCallDialog != null) {
            incomingCallDialog.dismiss();
        }
        inComingCall = call;

        // Initialize incoming call dialog
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.incoming_call);
        alert.setMessage(call.getRemoteNumber() + " \"" + call.getRemoteDisplayName() + '\"');

        alert.setMessage(call.getRemoteNumber() + " \"" + call.getRemoteDisplayName() + '\"');
        alert.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                inComingCall.accept();
            }
        });

        alert.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(LOG_TAG, "Ignore incoming call");
                inComingCall.ignore();


            }
        });

        incomingCallDialog = alert.create();
        incomingCallDialog.show();
    }


    @Override
    public void onCallCreated(CallService callService, Call call) {
        Log.d(LOG_TAG, "onCallCreated");
    }

    @Override
    public void onIncomingCallUndelivered(CallService callService, Call call) {
        Log.d(LOG_TAG, "onIncomingCallUndelivered");
    }

    @Override
    public void onCallRemoved(CallService callService, Call call) {
        Log.d(LOG_TAG, "onCallRemoved");
        // Hide incoming call dialog
        if (incomingCallDialog != null) {
            incomingCallDialog.dismiss();
        }
        // Unsubscribe from call state events
        call.removeListener(this);
    }

    @Override
    public void onCallServiceCapabilityChanged(CallService callService) {
        Log.d(LOG_TAG, "onCallServiceCapabilityChanged");
    }

    @Override
    public void onActiveCallChanged(CallService callService, Call call) {}

    public int getCallId(){
        // Create call
        CallService callService = mUser.getCallService();
        Call call = callService.getActiveCall();
        int callId = call.getCallId();
        return callId;
    }

    // TODO Exercise 2 To start the call

    public void startCall(String calledParty) {

        // Create call
        CallService callService = mUser.getCallService();
        Call call = callService.createCall();

        // Set far-end's number
        call.setRemoteAddress(calledParty);

        // Subscribe to call state events
        call.addListener(this);
        Log.d(LOG_TAG, "Outgoing call started");

        // Start the outgoing call
        call.start();
    }

    // TODO Exercise 3 To end active call
    public void endCall() {

        // Create call
        CallService callService = mUser.getCallService();
        Call call = callService.getActiveCall();
        call.end();
    }

    /*
     * Credential provider listener section
     */
    // onAuthenticationChallenge executed when we call User.start(). It is passing login
    // credentials to signaling server.
    @Override
    public void onAuthenticationChallenge(Challenge challenge,
                                          CredentialCompletionHandler credentialCompletionHandler) {
        Log.d(LOG_TAG, "UserCredentialProvider.onAuthenticationChallenge : Challenge = "
                + challenge);

        // Getting login information from settings
        //String extension = settings.getString(EXTENSION, "");

        // Note: Although this sample application manages passwords as clear text this application
        // is intended as a learning tool to help users become familiar with the Avaya SDK.
        // Managing passwords as clear text is not illustrative of a secure process to protect
        // passwords in an enterprise quality application.

        //String password = settings.getString(PASSWORD, "");
        //String domain = settings.getString(DOMAIN, "");

        // Login with saved credentials
        UserCredential userCredential = new UserCredential(extension, password, domain);
        credentialCompletionHandler.onCredentialProvided(userCredential);
    }


    @Override
    public void onCredentialAccepted(Challenge challenge) {

    }

    @Override
    public void onAuthenticationChallengeCancelled(Challenge challenge) {

    }

    @Override
    public void onCallStarted(Call call) {

    }

    @Override
    public void onCallRemoteAlerting(Call call, boolean b) {

    }

    @Override
    public void onCallRedirected(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_REDIRECTED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_REDIRECTED));
    }

    @Override
    public void onCallQueued(Call call) {

    }

    @Override
    public void onCallEstablished(Call call) {

    }

    @Override
    public void onCallRemoteAddressChanged(Call call, String s, String s1) {
        Log.d(LOG_TAG, CALL_EVENT_REMOTE_ADDRESS_CHANGED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_REMOTE_ADDRESS_CHANGED));

    }

    @Override
    public void onCallHeld(Call call) {}

    @Override
    public void onCallUnheld(Call call) {}

    @Override
    public void onCallHeldRemotely(Call call) {}

    @Override
    public void onCallUnheldRemotely(Call call) {}

    @Override
    public void onCallJoined(Call call) { }

    @Override
    public void onCallEnded(Call call, CallEndReason callEndReason) {
        Log.d(LOG_TAG, CALL_EVENT_ENDED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_ENDED));
    }

    @Override
    public void onCallFailed(Call call, CallException e) {}

    @Override
    public void onCallDenied(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_DENIED);
    }

    @Override
    public void onCallIgnored(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_IGNORED);
    }

    @Override
    public void onCallAudioMuteStatusChanged(Call call, boolean b) {
    }

    @Override
    public void onCallVideoChannelsUpdated(Call call, List<VideoChannel> list) {
        Log.d(LOG_TAG, CALL_EVENT_VIDEO_CHANNELS_UPDATED);
    }

    @Override
    public void onCallIncomingVideoAddRequestReceived(Call call) {
    }

    @Override
    public void onCallIncomingVideoAddRequestAccepted(Call call, VideoChannel videoChannel) {

    }

    @Override
    public void onCallIncomingVideoAddRequestDenied(Call call) {
    }

    @Override
    public void onCallIncomingVideoAddRequestTimedOut(Call call) {

    }

    @Override
    public void onCallConferenceStatusChanged(Call call, boolean b) {

    }

    @Override
    public void onCallCapabilitiesChanged(Call call) {
        Log.d(LOG_TAG, CALL_EVENT_CAPABILITIES_CHANGED);
        activity.sendBroadcast(new Intent(CALL_EVENTS_RECEIVER)
                .putExtra(CALL_EVENT_TAG, CALL_EVENT_CAPABILITIES_CHANGED));
    }

    @Override
    public void onCallServiceAvailable(Call call) {

    }

    @Override
    public void onCallServiceUnavailable(Call call) {

    }

    @Override
    public void onCallParticipantMatchedContactsChanged(Call call) {

    }

    @Override
    public void onCallDigitCollectionPlayDialTone(Call call) {

    }

    @Override
    public void onCallDigitCollectionCompleted(Call call) {

    }

}
