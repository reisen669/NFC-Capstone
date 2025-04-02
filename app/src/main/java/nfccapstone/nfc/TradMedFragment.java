package nfccapstone.nfc;

import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TradMedFragment extends Fragment {
    private View view;
    private int position;
    private Button btnVideo;
    private de.hdodenhof.circleimageview.CircleImageView imageView;
    private TextView textViewTitle;
    private TextView textViewAltName;
    private TextView textViewDescription;
    private TextView textViewWarning;
    private FragmentManager fragmentManager;
    private TCMTabFragment tcmFrag;
    private String tag = "";
    private TabbedMainActivity activity;
    public static final String debug = "DEBUG_TradMedFragment";

    // Required empty public constructor
    public TradMedFragment(){}

    // create new instance of class
    public static TradMedFragment newInstance(int position) {
        TradMedFragment fragment = new TradMedFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // obtain the resource ID of the video in the bundle added in newInstance static method
        Bundle bundle = getArguments();
        int position = bundle.getInt("position");
        this.position = position;

        tcmFrag = new TCMTabFragment();
        fragmentManager = getFragmentManager();
        Log.d(debug, "trad med fragment created");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // hide bottom navigation and action bars
        activity = (TabbedMainActivity) this.getActivity();
        activity.hideBars();
        activity.mode.setVisibility(View.VISIBLE);

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_trad_med, container, false);

        // identify the components in the view of the fragment
        imageView = view.findViewById(R.id.imageView);
        textViewTitle = view.findViewById(R.id.textViewTitle);
        textViewAltName = view.findViewById(R.id.textViewAltName);
        textViewDescription = view.findViewById(R.id.textViewDescription);
        textViewWarning = view.findViewById(R.id.textViewWarning);
        btnVideo = view.findViewById(R.id.btnVideo);

        // change orientation of the activity to portrait
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // make video playing mode visible
        activity.mode.setVisibility(View.VISIBLE);

        // set on click listener on button to play the video of TCM which details are displayed in the page
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(debug, "Play video on external display? " + activity.switchMode.isChecked());
                // play video on external display
                if(activity.switchMode.isChecked()){
                    Log.d(debug, "Play video on external display");
                    if (activity.connected) {
                        String number = Integer.toString(position + 1);
                        Log.d(debug, "number: " + number);
                        activity.sendValueUsingBle(number);
                        activity.controlViaBle();
                    }else{
                        Toast.makeText(activity, "Not connected to external display", Toast.LENGTH_SHORT).show();
                    }
                } // play video on smartphone
                else {
                    switch (position) {
                        case 0:
                            tag = "A1";
                            break;
                        case 1:
                            tag = "A2";
                            break;
                        case 2:
                            tag = "A3";
                            break;
                        default:
                            tag = "00";
                            break;
                    }
                    if (!tag.equals("00")) {
                        // make video playing mode visible
                        activity.mode.setVisibility(View.GONE);

                        fragmentManager.beginTransaction()
                                .replace(R.id.fragContainer, ViewVideoFragment.newInstance(TCMTabFragment.tradMeds[position].getVideo()), tag)
                                .addToBackStack(tag).commit();
                        fragmentManager.executePendingTransactions();
                    } else {
                        Log.d(debug, "tag = 00");
                    }
                }
            }
        });

        insertValues();

        Log.d(debug, "TradMedFragment view created");
        return view;
    }

    // assign the information of the respective traditional medicine
    private void insertValues(){
        imageView.setImageResource(TCMTabFragment.tradMeds[position].getImage());
        textViewTitle.setText(TCMTabFragment.tradMeds[position].getTitle());
        textViewAltName.setText(TCMTabFragment.tradMeds[position].getAltName());
        textViewDescription.setText(TCMTabFragment.tradMeds[position].getDetail());
        textViewWarning.setText(TCMTabFragment.tradMeds[position].getWarning());
    }
}
