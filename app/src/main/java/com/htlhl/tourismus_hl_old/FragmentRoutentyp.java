package com.htlhl.tourismus_hl_old;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

public class FragmentRoutentyp extends Fragment implements View.OnClickListener{

    Button btnWander, btnLauf;
    RelativeLayout layoutRad, layoutWander, layoutLauf;
    static boolean flagRad=false, flagWander=false, flagLauf=false;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routentyp, container, false);
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        layoutRad = (RelativeLayout) getActivity().findViewById(R.id.LayoutRadwege);
        layoutLauf = (RelativeLayout) getActivity().findViewById(R.id.LayoutKellergassenlauf);
        layoutWander = (RelativeLayout) getActivity().findViewById(R.id.LayoutWanderwege);
        layoutRad.setOnClickListener(this);
        layoutLauf.setOnClickListener(this);
        layoutWander.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.LayoutRadwege :
                flagRad = true;
                flagWander = false;
                flagLauf = false;
                Intent intent = new Intent(getActivity(), RoutenHaupt.class);
                startActivity(intent);
                break;
            case R.id.LayoutKellergassenlauf:
                flagLauf = true;
                flagRad = false;
                flagWander = false;
                Intent intent3 = new Intent(getActivity(), RoutenHaupt.class);
                startActivity(intent3);
                break;
            case R.id.LayoutWanderwege:
                flagWander = true;
                flagRad = false;
                flagLauf = false;
                Intent intent2 = new Intent(getActivity(), RoutenHaupt.class);
                startActivity(intent2);
                //Toast.makeText(getActivity(), "Diese Funktion ist noch nicht vollständig ausgearbeitet", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
