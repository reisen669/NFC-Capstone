package nfccapstone.nfc;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class ControlFragment extends Fragment implements View.OnClickListener{
    private View view;
    private Button btnVid1;
    private Button btnVid2;
    private Button btnVid3;
    private ImageButton btnPause;
    private ImageButton btnFastForward;
    private ImageButton btnRewind;
    private ImageButton btnVolUp;
    private ImageButton btnVolDown;
    private ImageButton btnStop;
    private TabbedMainActivity activity;

    public static final String debug = "DEBUG_ControlFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // change orientation of the activity to portrait
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // inflate the view from a XML Layout file
        view = inflater.inflate(R.layout.fragment_control, container, false);

        activity = (TabbedMainActivity) this.getActivity();

        // identify the buttons in the view of the fragment
        btnVid1 = view.findViewById(R.id.btnVid1);
        btnVid2 = view.findViewById(R.id.btnVid2);
        btnVid3 = view.findViewById(R.id.btnVid3);
        btnPause = view.findViewById(R.id.btnPause);
        btnFastForward = view.findViewById(R.id.btnFastForward);
        btnRewind = view.findViewById(R.id.btnRewind);
        btnVolUp = view.findViewById(R.id.btnVolUp);
        btnVolDown = view.findViewById(R.id.btnVolDown);
        btnStop = view.findViewById(R.id.btnStop);

        btnVid1.setOnClickListener(this);
        btnVid2.setOnClickListener(this);
        btnVid3.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnFastForward.setOnClickListener(this);
        btnRewind.setOnClickListener(this);
        btnVolUp.setOnClickListener(this);
        btnVolDown.setOnClickListener(this);
        btnStop.setOnClickListener(this);

        Log.d(debug, "Control fragment created");
        return view;
    }

    @Override
    public void onClick(View v) {
        // send corresponding command to RPi upon button click
        switch(v.getId()){
            case R.id.btnVid1:
                if (activity.connected){
                    activity.sendValueUsingBle("1");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnVid2:
                if (activity.connected){
                    activity.sendValueUsingBle("2");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnVid3:
                if (activity.connected){
                    activity.sendValueUsingBle("3");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnPause:
                if (activity.connected){
                    activity.sendValueUsingBle("4");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnVolUp:
                if (activity.connected){
                    activity.sendValueUsingBle("5");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnVolDown:
                if (activity.connected){
                    activity.sendValueUsingBle("6");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnFastForward:
                if (activity.connected){
                    activity.sendValueUsingBle("7");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnRewind:
                if (activity.connected){
                    activity.sendValueUsingBle("8");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.btnStop:
                if (activity.connected){
                    activity.sendValueUsingBle("9");
                }else{
                    Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }
}
