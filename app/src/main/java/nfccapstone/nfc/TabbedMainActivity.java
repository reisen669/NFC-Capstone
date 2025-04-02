package nfccapstone.nfc;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.BottomNavigationView;
import android.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import java.util.Arrays;
import java.util.UUID;

public class TabbedMainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    private NfcAdapter nfcAdapter;
    private android.app.FragmentManager fragmentManager;
    private ViewVideoFragment viewVideoFrag;
    private TCMTabFragment tcmFrag;
    private HomeTabFragment homeFrag;
    private GuideFragment guideFrag;
    private ControlFragment controlFrag;
    public SwitchCompat switchMode;
    public BottomNavigationView bottomNavi;
    private Fragment currentBackStackFragment;
    public CardView mode;
    private boolean fromBackground = false;
    private boolean nfc = false;
    private SharedPreferences sharedPreferences;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice device;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic charCapstone;
    public boolean connected = false;

    private static final String debug = "DEBUG_TabbedMain";
    public static final String SHARED_PREF = "sharedPreferences";
    // the corresponding BLE MAC Address
    private static final String ADDRESS = "B8:27:EB:BE:29:7A";
    private static final String CAPSTONE_SERVICE_UUID = "13333333-3333-3333-3333-333333333337";
    private static final String CAPSTONE_CHARACTERISTICS_UUID = "13333333-3333-3333-3333-333333330001";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(debug, "test");

        // link the respective interface
        setContentView(R.layout.activity_tabbed_main);

        // link xml components to allow manipulation in java
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // identify the SwitchCompat in the view of the fragment
        switchMode = findViewById(R.id.switchExternal);
        setSwitchMode();
        switchMode.setOnCheckedChangeListener(this);

        mode = findViewById(R.id.mode);

        bottomNavi = findViewById(R.id.bottomNavi);
        bottomNavi.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        bottomNavi.setOnNavigationItemSelectedListener(navListener);

        // Get default NFC adapter for Android device
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        fragmentManager = getFragmentManager();
        tcmFrag = new TCMTabFragment();
        homeFrag = new HomeTabFragment();
        controlFrag = new ControlFragment();
        guideFrag = new GuideFragment();

        // getDefaultAdapter() returns null if NFC is not supported
        if(nfcAdapter == null){
            Toast.makeText(this,"NFC Not Supported",Toast.LENGTH_SHORT).show();
            Log.d(debug, "NFC not supported by phone");
        }

        if(savedInstanceState == null) {
            Log.d(debug, "Newly created");

            bottomNavi.setSelectedItemId(R.id.nav_home);
            Log.d(debug, "stack num: " + fragmentManager.getBackStackEntryCount());
        }
        Log.d(debug, "Tabbed main activity created");

        // to handle NFC data detected from background
        if(getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Log.d(debug, "from background dispatch");
            fromBackground = true;
            bluetoothSetup();
            readIntent(getIntent());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_option, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // action bar will automatically handle clicks on top toolbar
        int id = item.getItemId();

        if (id == R.id.action_guide) {
            Log.d(debug, "How to use is pressed");
            // change content of main activity to another fragment
            fragmentManager.beginTransaction().replace(R.id.fragContainer, guideFrag, "guide").addToBackStack("guide").commit();
            fragmentManager.executePendingTransactions();
            hideBars();
            Log.d(debug, "How to use is pressed");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        // occurs only when the activity is in the foreground
        super.onResume();

        if(nfcAdapter != null) {
            // if NFC supported and enabled, start an intent to start itself
            if (nfcAdapter.isEnabled()) {
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                        new Intent(this, getClass()), 0);

                // filter such that only NDEF data of type "plain text" are taken
                IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
                ndef.addCategory(Intent.CATEGORY_DEFAULT);
                try {
                    ndef.addDataType("text/plain");
                } catch (IntentFilter.MalformedMimeTypeException e) {
                    e.printStackTrace();
                    Log.d(debug, "Error in adding intent data: " + e);
                }

                // declare an array of filters and initialise
                IntentFilter[] filters = {ndef};

                // NFC foreground dispatch - activity will read NDEF data when it is active in foreground
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, null);
                Log.d(debug, "nfc foreground mode enabled");
            }
            // if NFC supported but disabled, output message
            else {
                Log.d(debug, "NFC not enabled on phone");
                Toast.makeText(this,"NFC Disabled",Toast.LENGTH_SHORT).show();
            }
        }
        // if NFC not supported
        else{
            Log.d(debug, "NFC not supported by phone");
            Toast.makeText(this,"NFC Not Supported",Toast.LENGTH_SHORT).show();
        }

        // to set the selected tab on bottom navigation bar to previous state after clicking NFC on the bar
        if (bottomNavi.getSelectedItemId() == R.id.nav_nfc){
            currentBackStackFragment = getFragmentManager().findFragmentByTag("home");
            bottomNavi.setSelectedItemId(R.id.nav_home);
            fragmentManager.popBackStackImmediate("home", 0);
            Log.d(debug, "home");
        }

        // hide support action and notification bars when: video is playing/ at the page of a specific traditional medicine/
        // at the user guide page, otherwise, show those bars
        if(fragmentManager.findFragmentByTag("A1") != null ||
                fragmentManager.findFragmentByTag("A2") != null || fragmentManager.findFragmentByTag("A3") != null ||
                fragmentManager.findFragmentByTag("guide") != null){
            hideBars();
        }
        else{
            if(fragmentManager.findFragmentByTag("viewholder") != null){
                mode.setVisibility(View.VISIBLE);
            }
            else {
                showBars();
            }
        }

        // if came back to app from selecting nfc tab, home fragment will be displayed and
        // the fragment back stack will be cleared
        if(nfc){
            nfc = false;
            bottomNavi.setSelectedItemId(R.id.nav_home);
            fragmentManager.getBackStackEntryAt(1);
        }
    }

    // show bottom navigation bar and action bar
    public void showBars(){
        bottomNavi.setVisibility(View.VISIBLE);
        mode.setVisibility(View.VISIBLE);
        getSupportActionBar().show();
    }

    // hide bottom navigation bar and action bar
    public void hideBars(){
        bottomNavi.setVisibility(View.GONE);
        mode.setVisibility(View.GONE);
        getSupportActionBar().hide();
    }

    @Override
    public void onBackPressed() {
        Log.d(debug, "before back, Stack no: " + fragmentManager.getBackStackEntryCount());
        Log.d(debug, "viewholder: " + fragmentManager.findFragmentByTag("viewholder") +
                "\tmain: " + fragmentManager.findFragmentByTag("main") +
                "\thome: " + fragmentManager.findFragmentByTag("home"));

        // exit from app if app was launched from background NFC data detected
        if(fromBackground){
            Log.d(debug, "close app");
            finish();
            moveTaskToBack(true);
            while(fragmentManager.getBackStackEntryCount() > 0){
                fragmentManager.popBackStackImmediate();
            }
        }else {
            // if app was not launched from background NFC data detected
            // if video of a TCM was played, return to its detail page and configure the page accordingly
            if (fragmentManager.findFragmentByTag("A1") != null || fragmentManager.findFragmentByTag("A2") != null
                    || fragmentManager.findFragmentByTag("A3") != null) {
                fragmentManager.popBackStackImmediate("viewholder", 0);
            } else {
                if (fragmentManager.findFragmentByTag("viewholder") != null) {
                    currentBackStackFragment = getFragmentManager().findFragmentByTag("viewholder");
                    // make video playing mode visible
                    mode.setVisibility(View.VISIBLE);
                    if (currentBackStackFragment instanceof TradMedFragment) {
                        // pop and push stacks accordingly to allow easy navigation to required page
                        while (fragmentManager.getBackStackEntryCount() > 2) {
                            fragmentManager.popBackStackImmediate();
                        }
                        fragmentManager.popBackStackImmediate("main", 0);
                        showBars();
                        // set the selected item on bottom navigation bar to TCM
                        bottomNavi.setSelectedItemId(R.id.nav_tcm);
                    }
                } else {
                    // if user guide page was displayed, return to previous page and show bottom navigation bar
                    if (fragmentManager.findFragmentByTag("guide") != null) {
                        currentBackStackFragment = getFragmentManager().findFragmentByTag("guide");
                        if (currentBackStackFragment instanceof GuideFragment) {
                            showBars();
                            super.onBackPressed();
                        }
                    } else {
                        // if list of TCM page was displayed, close app
                        if (fragmentManager.findFragmentByTag("main") != null) {
                            currentBackStackFragment = getFragmentManager().findFragmentByTag("main");
                            if (currentBackStackFragment instanceof TCMTabFragment) {
                                moveTaskToBack(true);
                            }
                        } else {
                            // if home page was displayed, close app
                            if (fragmentManager.findFragmentByTag("home") != null) {
                                currentBackStackFragment = getFragmentManager().findFragmentByTag("home");
                                if (currentBackStackFragment instanceof HomeTabFragment) {
                                    moveTaskToBack(true);
                                }
                            } else {
                                // if control page was displayed, close app
                                if (fragmentManager.findFragmentByTag("control") != null) {
                                    currentBackStackFragment = getFragmentManager().findFragmentByTag("control");
                                    if (currentBackStackFragment instanceof ControlFragment) {
                                        moveTaskToBack(true);
                                    }
                                }else {
                                    super.onBackPressed();
                                }
                            }
                        }
                    }
                }
            }
        }
        Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());

        // change orientation of the activity to portrait, forward or reverse depending on choice of user
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public void onPause(){
        if (nfcAdapter != null) {
            // stop listening to NDEF discovered intent
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // NFC card detected
        if (intent.hasExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)) {
            readIntent(intent);
        }
    }

    private void readIntent(Intent intent) {
        // callback to process the data from the scanned NFC tag
        Parcelable[] messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        NdefMessage message = null;
        for (int i = 0; i < messages.length; i++) {
            message = (NdefMessage) messages[i];
        }

        NdefRecord[] records = message.getRecords();
        for (int j = 0; j < records.length; j++) {
            NdefRecord record = records[j];

            // Obtain the text from NDEF Record Type T
            if (new String(record.getType()).equals("T")) {
                byte[] original = record.getPayload();

                // bypass/ remove first 3 bytes which are not used
                byte[] value = Arrays.copyOfRange(original, 3, original.length);
                String payload = new String(value);

                // debug in log
                Log.d(debug, "INITIAL TAG: " + fragmentManager.findFragmentByTag("A1") + "\t" + fragmentManager.findFragmentByTag("A2")
                        + "\t" + fragmentManager.findFragmentByTag("A3"));
                Log.d(debug, "CONTENT: " + payload);
                Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());

                if (payload.equals("1")) {
                    // play video of female ginseng on external display
                    if(switchMode.isChecked()){
                        Log.d(debug, "Female ginseng - external display");
                        if (connected){
                            sendValueUsingBle("1");
                            controlViaBle();
                        }else{
                            Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                        }
                    } // play video of female ginseng on smartphone
                    else {
                        A1Action();
                    }
                }
                else {
                    if (payload.equals("2")) {
                        // play video of fennel on external display
                        if(switchMode.isChecked()){
                            Log.d(debug, "Fennel - external display");
                            if (connected){
                                sendValueUsingBle("2");
                                controlViaBle();
                            }else{
                                Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                            }
                        } // play video of fennel on smartphone
                        else {
                            A2Action();
                        }
                    }
                    else {
                        if (payload.equals("3")) {
                            // play video of pot marigold on external display
                            if(switchMode.isChecked()){
                                Log.d(debug, "Pot marigold - external display");
                                if (connected){
                                    sendValueUsingBle("3");
                                    controlViaBle();
                                }else{
                                    Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                                }
                            } // play video of pot marigold on smartphone
                            else {
                                A3Action();
                            }
                        }
                        else{
                            if (payload.equals("4")) {
                                // pause on external display
                                if(switchMode.isChecked()){
                                    Log.d(debug, "Pause - external display");
                                    if (connected){
                                        sendValueUsingBle("4");
                                        controlViaBle();
                                    }else{
                                        Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                                    }
                                } // pause on smartphone
                                else {
                                    pauseAction();
                                }
                            } else {
                                if (payload.equals("5")) {
                                    if(switchMode.isChecked()){
                                        Log.d(debug, "Volume up - external display");
                                        if (connected){
                                            sendValueUsingBle("5");
                                            controlViaBle();
                                        }else{
                                            Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                                        }
                                    } // increase volume on smartphone
                                    else {
                                        volumeUpAction();
                                    }
                                } else {
                                    if (payload.equals("6")) {
                                        if(switchMode.isChecked()){
                                            Log.d(debug, "Volume down - external display");
                                            if (connected){
                                                sendValueUsingBle("6");
                                                controlViaBle();
                                            }else{
                                                Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                                            }
                                        } // decrease volume on smartphone
                                        else {
                                            volumeDownAction();
                                        }
                                    } else {
                                        if (payload.equals("7")) {
                                            if(switchMode.isChecked()){
                                                Log.d(debug, "Fast forward - external display");
                                                if (connected){
                                                    sendValueUsingBle("7");
                                                    controlViaBle();
                                                }else{
                                                    Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                                                }
                                            } // fast forward on smartphone
                                            else {
                                                fastForwardAction();
                                            }
                                        } else {
                                            if (payload.equals("8")) {
                                                if(switchMode.isChecked()){
                                                    Log.d(debug, "Rewind - external display");
                                                    if (connected){
                                                        sendValueUsingBle("8");
                                                        controlViaBle();
                                                    }else{
                                                        Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                                                    }
                                                } // rewind on smartphone
                                                else {
                                                    rewindAction();
                                                }
                                            } else{
                                                if (payload.equals("9")) {
                                                    if(switchMode.isChecked()){
                                                        Log.d(debug, "Stop - external display");
                                                        if (connected){
                                                            sendValueUsingBle("9");
                                                            controlViaBle();
                                                        }else{
                                                            Toast.makeText(TabbedMainActivity.this, "Not connected to external display", Toast.LENGTH_SHORT).show();
                                                        }
                                                    } // stop on smartphone
                                                    else {
                                                        stopAction();
                                                    }
                                                } else {
                                                    // output error on toast if content of NDEF data does not match any of the above
                                                    Toast.makeText(this, "ERROR!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // send command to RPi
    public void sendValueUsingBle(String number){
        byte command = (byte) Integer.parseInt(number);
        Log.d(debug, "charCapstone: " + charCapstone);

        // wait for setup to be done to allow a single tap to play video on external display
        while (charCapstone == null){}
            charCapstone.setValue(new byte[]{command});
            bluetoothGatt.writeCharacteristic(charCapstone);
            Log.d(debug, "Sending to RPi: " + number);
    }

    // bottom navigation view listener
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener(){
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item){
                    // show corresponding page when that icon is selected on bottom navigation view
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            fragmentManager.beginTransaction().replace(R.id.fragContainer, homeFrag, "home").addToBackStack("home").commit();
                            Log.d(debug, "home fragment selected");
                            break;
                        case R.id.nav_tcm:
                            fragmentManager.beginTransaction().replace(R.id.fragContainer, tcmFrag, "main").addToBackStack("main").commit();
                            Log.d(debug, "tcm fragment selected");
                            break;
                        case R.id.nav_control:
                            fragmentManager.beginTransaction().replace(R.id.fragContainer, controlFrag, "control").addToBackStack("control").commit();
                            Log.d(debug, "control fragment selected");
                            break;
                        case R.id.nav_nfc:
                            Log.d(debug, "nfc selected");
                            // go to smartphone's NFC setting page
                            startNfcSettingsActivity();
                            break;
                    }
                    // return the selected item
                    return true;
                }
    };

    // update video viewing mode upon switch state change
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // no other app can change the shared prefs
        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        Log.d(debug, "Checked? " + isChecked);

        editor.clear();
        editor.putBoolean("EXTERNAL_DISPLAY_MODE", isChecked);
        editor.apply();

        // disconnect ble connection to RPi if chose to play on smartphone
        if(!isChecked) {
            if (connected) {
                bluetoothGatt.disconnect();
                connected = false;
            }
        }else{
            // if chose to play on external display, set up bluetooth settings
            bluetoothSetup();
        }
    }

    // set up (default) switch mode
    public void setSwitchMode(){
        sharedPreferences = getSharedPreferences(SHARED_PREF, MODE_PRIVATE);
        Boolean temp = sharedPreferences.getBoolean("EXTERNAL_DISPLAY_MODE", false);
        switchMode.setChecked(temp);
        Log.d(debug, "default: " + temp);
    }

    // to stop currently playing video
    private void stopAction(){
        // if video is being played
        if((fragmentManager.findFragmentByTag("A1") != null) || (fragmentManager.findFragmentByTag("A2") != null)
                || (fragmentManager.findFragmentByTag("A3") != null)) {
            focusVideo();
            Toast.makeText(this, "Stopping video...", Toast.LENGTH_SHORT).show();
            viewVideoFrag.stopVideo();

            // change orientation of the activity to portrait, forward or reverse depending on choice of user
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else{
            videoNotPlaying();
        }
    }

    // to pause or resume currently playing video
    private void pauseAction(){
        if ((fragmentManager.findFragmentByTag("A1") != null) || (fragmentManager.findFragmentByTag("A2") != null)
                || (fragmentManager.findFragmentByTag("A3") != null)) {
            Log.d(debug, "Video to be paused or resumed");
            focusVideo();
            Toast.makeText(this, "Pause", Toast.LENGTH_SHORT).show();
            viewVideoFrag.pauseVideo();
        }
        else{
            videoNotPlaying();
        }
    }

    // to fast forward currently playing video
    private void fastForwardAction(){
        if ((fragmentManager.findFragmentByTag("A1") != null) || (fragmentManager.findFragmentByTag("A2") != null)
                || (fragmentManager.findFragmentByTag("A3") != null)) {
            Log.d(debug, "Video to be fast forwarded");
            focusVideo();
            Toast.makeText(this, "Fast forward", Toast.LENGTH_SHORT).show();
            viewVideoFrag.fastForwardVideo();
        }
        else{
            videoNotPlaying();
        }
    }

    // to rewind currently playing video
    private void rewindAction(){
        if ((fragmentManager.findFragmentByTag("A1") != null) || (fragmentManager.findFragmentByTag("A2") != null)
                || (fragmentManager.findFragmentByTag("A3") != null)) {
            Log.d(debug, "Video to be rewound");
            focusVideo();
            viewVideoFrag.rewindVideo();
            Toast.makeText(this, "Rewind", Toast.LENGTH_SHORT).show();
        }
        else{
            videoNotPlaying();
        }
    }

    // if no video is being played, output toast to inform user
    private void videoNotPlaying(){
        Toast.makeText(this, "No video is playing", Toast.LENGTH_SHORT).show();
        if(fromBackground){
            finish();
            moveTaskToBack(true);
            while(fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            }
        }
    }

    // to play video of female ginseng
    private void A1Action() {
        // refer to the related fragment
        viewVideoFrag = (ViewVideoFragment) fragmentManager.findFragmentByTag("A1");
        Log.d(debug, "TAG: " + fragmentManager.findFragmentByTag("A1") + "\t" + fragmentManager.findFragmentByTag("A2")
                + "\t" + fragmentManager.findFragmentByTag("A3"));

        // pop currently playing video from back stack if video is played from background NFC data detected
        if (fromBackground) {
            while (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            }
            // add active fragment to play the corresponding video
            fragmentManager.beginTransaction().add(R.id.fragContainer, ViewVideoFragment.newInstance(R.raw.female_ginseng), "A1")
                    .addToBackStack("A1").commit();
            fragmentManager.executePendingTransactions();
            Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());
        } else {
            // pop back stack
            while (fragmentManager.getBackStackEntryCount() > 2) {
                Log.d(debug, "pop back stack");
                fragmentManager.popBackStackImmediate();
            }

            // push back stack to allow specific back navigation
            Log.d(debug, "stacking up");
            fragmentManager.beginTransaction().replace(R.id.fragContainer, tcmFrag, "main")
                    .addToBackStack("main").commit();
            bottomNavi.setSelectedItemId(R.id.nav_tcm);
            fragmentManager.beginTransaction().replace(R.id.fragContainer, TradMedFragment.newInstance(0), "viewholder")
                    .addToBackStack("viewholder").commit();

        // replace active fragment to play the corresponding video
        fragmentManager.beginTransaction().replace(R.id.fragContainer, ViewVideoFragment.newInstance(R.raw.female_ginseng), "A1")
                .addToBackStack("A1").commit();
        fragmentManager.executePendingTransactions();
        Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());
    }
        // output toast on screen
        Toast.makeText(this, "Female Ginseng", Toast.LENGTH_SHORT).show();
        Log.d(debug, "A1 video is played");
    }

    // to play video of fennel
    private void A2Action(){
        // refer to the related fragment
        viewVideoFrag = (ViewVideoFragment) fragmentManager.findFragmentByTag("A2");
        Log.d(debug, "TAG: " + fragmentManager.findFragmentByTag("A1") + "\t" + fragmentManager.findFragmentByTag("A2")
                + "\t" + fragmentManager.findFragmentByTag("A3"));

        // pop currently playing video from back stack if video is played from background NFC data detected
        if(fromBackground) {
            while (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            }
            // add active fragment to play the corresponding video
            fragmentManager.beginTransaction().add(R.id.fragContainer, ViewVideoFragment.newInstance(R.raw.fennel), "A2")
                    .addToBackStack("A2").commit();
            fragmentManager.executePendingTransactions();
            Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());
        }
        else {
            // pop back stack
            while (fragmentManager.getBackStackEntryCount() > 1) {
                Log.d(debug, "pop back stack");
                fragmentManager.popBackStackImmediate();
            }

            // push back stack to allow specific back navigation
            Log.d(debug, "stacking up");
            fragmentManager.beginTransaction().replace(R.id.fragContainer, tcmFrag, "main")
                    .addToBackStack("main").commit();
            bottomNavi.setSelectedItemId(R.id.nav_tcm);
            fragmentManager.beginTransaction().replace(R.id.fragContainer, TradMedFragment.newInstance(1), "viewholder")
                    .addToBackStack("viewholder").commit();

            // replace active fragment to play the corresponding video
            fragmentManager.beginTransaction().replace(R.id.fragContainer, ViewVideoFragment.newInstance(R.raw.fennel), "A2")
                    .addToBackStack("A2").commit();
            fragmentManager.executePendingTransactions();
            Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());
        }

        Toast.makeText(this, "Fennel", Toast.LENGTH_SHORT).show();
        Log.d(debug, "A2 video is played");
    }

    // to play video of pot marigold
    private void A3Action() {
        // refer to the related fragment
        viewVideoFrag = (ViewVideoFragment) fragmentManager.findFragmentByTag("A3");
        Log.d(debug, "TAG: " + fragmentManager.findFragmentByTag("A1") + "\t" + fragmentManager.findFragmentByTag("A2")
                + "\t" + fragmentManager.findFragmentByTag("A3"));

        // pop currently playing video from back stack if video is played from background NFC data detected
        if(fromBackground) {
            while(fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            }
            // add active fragment to play the corresponding video
            fragmentManager.beginTransaction().add(R.id.fragContainer, ViewVideoFragment.newInstance(R.raw.pot_marigold), "A3")
                    .addToBackStack("A3").commit();
            fragmentManager.executePendingTransactions();
            Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());
        }
        else {
            // pop back stack
            while (fragmentManager.getBackStackEntryCount() > 2) {
                Log.d(debug, "pop back stack");
                fragmentManager.popBackStackImmediate();
            }

            // push back stack to allow specific back navigation
            Log.d(debug, "stacking up");
            fragmentManager.beginTransaction().replace(R.id.fragContainer, tcmFrag, "main")
                    .addToBackStack("main").commit();
            bottomNavi.setSelectedItemId(R.id.nav_tcm);
            fragmentManager.beginTransaction().replace(R.id.fragContainer, TradMedFragment.newInstance(2), "viewholder")
                    .addToBackStack("viewholder").commit();

            // replace active fragment to play the corresponding video
            fragmentManager.beginTransaction().replace(R.id.fragContainer, ViewVideoFragment.newInstance(R.raw.pot_marigold), "A3")
                    .addToBackStack("A3").commit();
            fragmentManager.executePendingTransactions();
            Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());
        }

        // output toast on screen
        Toast.makeText(this, "Pot Marigold", Toast.LENGTH_SHORT).show();
        Log.d(debug, "A3 video is played");
    }

    // to increase volume of currently playing video
    private void volumeUpAction(){
        if ((fragmentManager.findFragmentByTag("A1") != null) || (fragmentManager.findFragmentByTag("A2") != null)
                || (fragmentManager.findFragmentByTag("A3") != null)) {
            Log.d(debug, "Increase volume");
            focusVideo();
            Toast.makeText(this, "Volume up", Toast.LENGTH_SHORT).show();
            viewVideoFrag.volUpVideo();
        }
        else{
            videoNotPlaying();
        }
    }

    // to decrease volume of currently playing video
    private void volumeDownAction(){
        if ((fragmentManager.findFragmentByTag("A1") != null) || (fragmentManager.findFragmentByTag("A2") != null)
                || (fragmentManager.findFragmentByTag("A3") != null)) {
            Log.d(debug, "Decrease volume");
            focusVideo();
            Toast.makeText(this, "Volume down", Toast.LENGTH_SHORT).show();
            viewVideoFrag.volDownVideo();
        }
        else{
            videoNotPlaying();
        }
    }

    public void controlViaBle(){
        while (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStackImmediate();
        }
        // add active fragment to play the corresponding video
        fragmentManager.beginTransaction().replace(R.id.fragContainer, controlFrag, "control")
                .addToBackStack("control").commit();
        bottomNavi.setSelectedItemId(R.id.nav_control);
        showBars();
        fragmentManager.executePendingTransactions();
        Log.d(debug, "Stack no: " + fragmentManager.getBackStackEntryCount());
        Log.d(debug, "Playing on external display");
    }

    // direct focus/control to video to be played/is being played
    private void focusVideo(){
        if(fragmentManager.findFragmentByTag("A1") != null){
            viewVideoFrag = (ViewVideoFragment) fragmentManager.findFragmentByTag("A1");
            Log.d(debug, "locked at A1");
        }
        else{
            if(fragmentManager.findFragmentByTag("A2") != null){
                viewVideoFrag = (ViewVideoFragment) fragmentManager.findFragmentByTag("A2");
                Log.d(debug, "locked at A2");
            }
            else {
                if (fragmentManager.findFragmentByTag("A3") != null) {
                    viewVideoFrag = (ViewVideoFragment) fragmentManager.findFragmentByTag("A3");
                    Log.d(debug, "locked at A3");
                }
            }
        }
    }

    public void bluetoothSetup(){
        if(switchMode.isChecked()) {
            // try to set up bluetooth
            try {
                bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                Log.d(debug, "set up bluetooth");
            } catch (Exception e) {
                Log.d(debug, "Error: " + e);
            }

            // connect to RPi
            device = mBluetoothAdapter.getRemoteDevice(ADDRESS);

            // set up general attribute profile
            bluetoothGatt = device.connectGatt(this, true, new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    super.onConnectionStateChange(gatt, status, newState);

                    Log.d(debug, "state: " + newState);
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        gatt.discoverServices();
                        Log.d(debug, "Connected");
                        connected = true;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TabbedMainActivity.this, "Connected to display", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        connected = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(TabbedMainActivity.this, "Disconnected from display", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                    // get services of RPi
                    super.onServicesDiscovered(gatt, status);
                    charCapstone = gatt.getService(UUID.fromString(CAPSTONE_SERVICE_UUID))
                            .getCharacteristic(UUID.fromString(CAPSTONE_CHARACTERISTICS_UUID));
                    bluetoothGatt = gatt;
                    Log.d(debug, "gatt: " + bluetoothGatt);
                    connected = true;
                }
            });

            if(fromBackground) {
                if (mBluetoothAdapter.isEnabled()) {
                    Log.d(debug, "display on external screen from background");

                    Toast.makeText(this, "Connected to display", Toast.LENGTH_SHORT).show();
                }
            }
            fromBackground = false;
        }
    }

    // go to NFC settings of phone
    protected void startNfcSettingsActivity() {
        nfc = true;
        startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
    }
}