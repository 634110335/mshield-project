package com.cuisec.mshield.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cuisec.mshield.R;

public class QRCodeFragment extends Fragment {
    private static QRCodeFragment sQRCodeFragment;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_service, null);
        return inflate;
    }
    public static QRCodeFragment getInstance(){
        if (sQRCodeFragment == null){
            sQRCodeFragment = new QRCodeFragment();
        }
        return sQRCodeFragment;
    }
}
