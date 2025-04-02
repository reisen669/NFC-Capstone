package nfccapstone.nfc;

import android.app.FragmentManager;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TCMTabFragment extends Fragment{
    private RecyclerView recyclerView;
    private View view;
    private MyAdapter adapter;
    private FragmentManager fragmentManager;

    public static final String debug = "DEBUG_MainFragment";

    // hard-corded data/model for each trad med object
    public static TradMed[] tradMeds = {
            new TradMed("Female Ginseng", R.drawable.dong_quai_after, R.raw.female_ginseng, R.string.A1_altname, R.string.A1_detail, R.string.A1_warning),
            new TradMed("Fennel", R.drawable.fennel_after, R.raw.fennel, R.string.A2_altname, R.string.A2_detail, R.string.A2_warning),
            new TradMed("Pot Marigold", R.drawable.pot_marigold_after, R.raw.pot_marigold, R.string.A3_altname, R.string.A3_detail, R.string.A3_warning)
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // change orientation of the activity to portrait
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // inflate the view from a XML Layout file
        view = inflater.inflate(R.layout.tab_fragment_tcm, container, false);

        // identify the RecyclerView in the view of the fragment
        recyclerView = view.findViewById(R.id.recyclerView);
        // set the RecyclerView to show a single column vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        fragmentManager = getFragmentManager();

        //  instantiate our custom RecyclerView.Adapter to obtain the data/model, generate the view for each item in the list
        adapter = new MyAdapter();
        recyclerView.setAdapter(adapter);

        Log.d(debug, "TCM tab fragment created");
        return view;
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        // Custom RecyclerView.ViewHolder class to store the UI Widget to be used in each row in the RecyclerView
        public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private TextView textViewTitle;
            private ImageView imageView;

            // Constructor of MyViewHolder to identify the UI Widget in the view of each row and set the listener for clicking a row
            public MyViewHolder(View itemView) {
                super(itemView);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                imageView = itemView.findViewById(R.id.imageView);
                itemView.setOnClickListener(this);
            }

            // Clicking a row will replace the TCMTabFragment with TCMFragment
            // newInstance method allows the resource ID of the video to be played to be sent to the ViewVideoFragment
            @Override
            public void onClick(View view) {
                Log.d(debug, "Item clicked");

                // change content of main activity to another fragment
                fragmentManager.beginTransaction().replace(R.id.fragContainer, TradMedFragment.newInstance(getAdapterPosition()), "viewholder")
                        .addToBackStack("viewholder").commit();
                fragmentManager.executePendingTransactions();
            }
        }

        // Generate the view for each row and store the view in MyViewHolder
        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.row, parent, false);
            MyViewHolder holder = new MyViewHolder(view);

            Log.d(debug, "MyViewHolder created");
            return holder;
        }

        // Setup the content of UI widgets in each row via the ViewHolder
        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Log.d(debug, "Setting up view holder");
            holder.textViewTitle.setText(tradMeds[position].getTitle());
            holder.imageView.setImageResource(tradMeds[position].getImage());
            Log.d(debug, "ViewHolder interface set up");
        }

        // Obtain the size of the data/model to determine the length of the recyclerview
        @Override
        public int getItemCount() {
            Log.d(debug, "No. of items: " + tradMeds.length);
            return tradMeds.length;
        }
    }
}