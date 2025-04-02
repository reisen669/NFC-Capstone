package nfccapstone.nfc;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GuideFragment extends Fragment {
    private View view;

    private static final String debug = "DEBUG_GuideFragment";
    public GuideFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // change orientation of the activity to portrait, forward or reverse depending on choice of user
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_guide, container, false);

        Log.d(debug, "Guide fragment view created");
        return view;
    }
}
