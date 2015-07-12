package uit.linh.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * A simple {@link android.app.Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link uit.linh.ui.GroupControlFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link uit.linh.ui.GroupControlFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GroupControlFragment extends android.support.v4.app.Fragment implements View.OnClickListener {

    private static final String TAG = "GroupControlFragment";
    private static final String SHARE_REFERENCE_FILE = "meme_creater";
    private static final String CAPTION_PREFERENCE = "caption";
    private OnFragmentInteractionListener mListener;
    Button btnFragmentEditText, btnClear, btnNewCaption;
    EditText edtCaption;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment GroupControl.
     */
    public static GroupControlFragment newInstance() {
        GroupControlFragment fragment = new GroupControlFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupControlFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_control, container, false);
        btnFragmentEditText = (Button) view.findViewById(R.id.btn_frament_edit_text);
        btnClear = (Button) view.findViewById(R.id.btn_clear);
        btnNewCaption = (Button) view.findViewById(R.id.btn_new_caption);
        edtCaption = (EditText) view.findViewById(R.id.edt_caption);


        btnFragmentEditText.setOnClickListener(this);
        btnNewCaption.setOnClickListener(this);
        btnClear.setOnClickListener(this);
        edtCaption.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                mListener.setCaption(edtCaption.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Log.d(TAG, "onResume");
        SharedPreferences preferences = getActivity().getSharedPreferences(SHARE_REFERENCE_FILE, Context.MODE_PRIVATE);
        String caption = preferences.getString(CAPTION_PREFERENCE, MemeCreator.defaultCaption);
        if (caption.equals("")){
            edtCaption.setText(MemeCreator.defaultCaption);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
//        Log.d(TAG, "onPause");
        SharedPreferences preferences = getActivity().getSharedPreferences(SHARE_REFERENCE_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CAPTION_PREFERENCE, edtCaption.getText().toString());
        editor.apply();
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
    public void onClick(View view) {
        if (mListener.isButtonEnabled()){
            switch (view.getId()){
                case R.id.btn_frament_edit_text:
                    mListener.replaceFragment();
                    break;

                case R.id.btn_clear:
                    mListener.clear();
                    edtCaption.setText(MemeCreator.defaultCaption);
                    break;
                case R.id.btn_new_caption:
                    mListener.createNewCaption();
                    edtCaption.setText(MemeCreator.defaultCaption);
                    break;

            }
        }
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
        public void replaceFragment();
        public boolean isButtonEnabled();
        public void setCaption(String caption);
        public void createNewCaption();
        public void clear();
    }

}
