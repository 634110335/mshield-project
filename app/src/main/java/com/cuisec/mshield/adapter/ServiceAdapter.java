package com.cuisec.mshield.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.cuisec.mshield.R;
import com.cuisec.mshield.activity.WebViewActivity;
import com.cuisec.mshield.bean.ServiceInfo;
import com.cuisec.mshield.config.Constants;

import java.util.ArrayList;

public class ServiceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<ServiceInfo.DomainListBean> mDomainListBeans;
    public ServiceAdapter(Context context, ArrayList<ServiceInfo.DomainListBean> domainListBeans) {
        mContext = context;
        mDomainListBeans = domainListBeans;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_service,null);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int i) {
            ViewHolder bindHolder = (ViewHolder) viewHolder;
            bindHolder.serviceName.setText(mDomainListBeans.get(i).getTitle());
        Glide.with(mContext).load(stringToBitmap(mDomainListBeans.get(i).getImg())).into(bindHolder.serviceImage);
        bindHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WebViewActivity.class);
                intent.putExtra(Constants.WEB_URL,mDomainListBeans.get(i).getUrl());
                mContext.startActivity(intent);
            }
        });
    }

    public Bitmap stringToBitmap(String string) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray = Base64.decode(string, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }
    @Override
    public int getItemCount() {
        return mDomainListBeans.size();
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView serviceImage;
        private TextView serviceName;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceImage = itemView.findViewById(R.id.service_image);
            serviceName = itemView.findViewById(R.id.service_name);
        }
    }

}
