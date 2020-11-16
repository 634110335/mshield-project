package com.cuisec.mshield.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.home.NotifiActivity;
import com.cuisec.mshield.activity.ipass.IpassNoticaActivity;
import com.cuisec.mshield.utils.T;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpdateFragment extends Fragment {
    private static UpdateFragment sUpdateFragment;
    @BindView(R.id.not_notica)
    TextView mNotNotica;
    @BindView(R.id.rl_tender_notice)
    RelativeLayout mRlTenderNotice;
    @BindView(R.id.rl_ipass_notice)
    RelativeLayout mRlIpassNotice;
    @BindView(R.id.rl_system_notice)
    RelativeLayout mRlSystemNotice;
    @BindView(R.id.rl_else_notice)
    RelativeLayout mRlElseNotice;
    @BindView(R.id.ipass_notifica)
    TextView mIpassNotifica;
    @BindView(R.id.ipass_count)
    TextView mIpassCount;
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_code, null);
        EventBus.getDefault().register(this);
        ButterKnife.bind(this, inflate);

        initView();
        return inflate;
    }

    private void initView() {
    }

    public static UpdateFragment getInstance() {
        if (sUpdateFragment == null) {
            sUpdateFragment = new UpdateFragment();
        }
        return sUpdateFragment;
    }

    @Subscribe
    public void onEvent(ReturnPayResult result) {
        //接收以及处理数据
        switch (result.getFlag()) {
            case 2:
                //mNotNotica.setText("新增" + result + "条通知");
                mNotNotica.setText("新增" + result.getStatus() + "条通知");
                break;
            case 3:
                mIpassCount.setText("新增" + result.getStatus() + "条通知");
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @OnClick({R.id.rl_tender_notice, R.id.rl_ipass_notice, R.id.rl_system_notice, R.id.rl_else_notice})
    public void onClick(View v) {
        switch (v.getId()) {
            default:
                break;
            case R.id.rl_tender_notice:
                if (mNotNotica.getText().equals("暂无招标讯息")) {
                    T.showShort("暂无招标讯息");
                    return;
                }
                startActivity(new Intent(getActivity(), NotifiActivity.class));
                mNotNotica.setText("暂无招标讯息");
                break;
            case R.id.rl_ipass_notice:
                //跳转ipass通知代码
                /*boolean isBind = (boolean) SPUtils.get(getActivity(), "isbindipass", false);
                if (!isBind){
                    T.showShort("未绑定iPASS业务通知");
                    return;
                }*/
               /* if (!mIpassCount.getText().toString().contains("新增通知")){
                    T.showShort("暂无新的通知");
                    return;
                }*/
                mIpassCount.setText("暂无iPASS业务通知");
                startActivity(new Intent(getActivity(), IpassNoticaActivity.class));
                break;
            case R.id.rl_system_notice:
            case R.id.rl_else_notice:
                T.showShort("功能正在完善中,敬请期待");
                break;
        }
    }


}
