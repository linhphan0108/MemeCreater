package uit.linh.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.ArrayList;

import uit.linh.adapters.ColorAdapter;
import uit.linh.providers.Colors;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link uit.linh.ui.EditTextFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link uit.linh.ui.EditTextFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditTextFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener{

    private static final String TAG = "EditTextFragment";
    private static final String ARG_FONT_SIZE = "font size";

    private OnFragmentInteractionListener mListener;
    private GridView gridViewColors;
    private ColorAdapter colorAdapter;
    private ArrayList<Colors.Color> colors;
    private int currItemColorChecked = -1;
    private Button btnFonts;
    private SeekBar sbFontSize;
    private int fontSize;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EditTextFragment.
     */
    public static EditTextFragment newInstance(int fontSize) {
        EditTextFragment fragment = new EditTextFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_FONT_SIZE, fontSize);
        fragment.setArguments(args);
        return fragment;
    }

    public EditTextFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fontSize = getArguments().getInt(ARG_FONT_SIZE, 0);
        }

        colors = new Colors().getColors();
        colorAdapter = new ColorAdapter(getActivity(), R.layout.color_item, colors);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_edit_text, container, false);
        gridViewColors = (GridView) view.findViewById(R.id.gridView);
        btnFonts = (Button) view.findViewById(R.id.btn_fonts);
        sbFontSize = (SeekBar) view.findViewById(R.id.sb_font_size);

        gridViewColors.setAdapter(colorAdapter);
        gridViewColors.setOnItemClickListener(this);
        btnFonts.setOnClickListener(this);
        sbFontSize.setMax(MemeCreator.MAX_FONT_SIZE);
        sbFontSize.setProgress(fontSize);
        sbFontSize.setOnSeekBarChangeListener(this);


        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Toast.makeText(getActivity(), "Clicked", Toast.LENGTH_SHORT).show();
        colors.get(i).setChecked(true);
        if(currItemColorChecked != -1){
            colors.get(currItemColorChecked).setChecked(false);
        }
        currItemColorChecked = i;
        colorAdapter.notifyDataSetChanged();

        mListener.setTextColor(Color.parseColor(colors.get(i).getColor()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_fonts:
                Intent intent = new Intent(getActivity(), FontPicker.class);
                getActivity().startActivityForResult(intent, MemeCreator.REQ_CODE_OPEN_FONT_PICKER);
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case MemeCreator.REQ_CODE_OPEN_FONT_PICKER:
//                Log.d(TAG, "slected");
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        mListener.setFontSize(seekBar.getProgress());
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void setFontSize(int fontSize);
        public void setTextColor(int color);
    }

}
