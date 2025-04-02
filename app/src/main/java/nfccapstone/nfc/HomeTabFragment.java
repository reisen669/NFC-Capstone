package nfccapstone.nfc;

import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HomeTabFragment extends Fragment {
    private View view;
    private TabbedMainActivity main;

    private static final String debug = "DEBUG_HomeTab";

    public HomeTabFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // change orientation of the activity to portrait, forward or reverse depending on choice of user
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // inflate the view from a XML Layout file
        view = inflater.inflate(R.layout.tab_fragment_home, container, false);

        // Inflate the layout for this fragment
        Log.d(debug, "Home tab fragment created");
        return view;
    }
}
