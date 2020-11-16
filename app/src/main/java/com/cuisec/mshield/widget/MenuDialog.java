package com.cuisec.mshield.widget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.cuisec.mshield.R;

public class MenuDialog {
    private Context mContext;
    private String mCancleContent;
    private String[] mItems;
    private Boolean mCancleAbleOutSide;
    private PopupWindow mPopupWindow;

    /**
     * @param mContext           context
     * @param mCancleContent     取消按钮文字
     * @param mItems             菜单数组
     * @param mCancleAbleOutSide 是否让点击外部取消
     */
    public MenuDialog(Context mContext, String mCancleContent, String[] mItems, Boolean mCancleAbleOutSide) {
        super();
        this.mContext = mContext;
        this.mCancleContent = mCancleContent;
        this.mItems = mItems;
        this.mCancleAbleOutSide = mCancleAbleOutSide;
        showMyBottomPop();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("InflateParams")
    public void showMyBottomPop() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.layout_menu_dialog, null);
        ListView list = view.findViewById(R.id.pop_list);
        TextView cancle = view.findViewById(R.id.cancle_tv);
        cancle.setText(mCancleContent);
        list.setAdapter(new MyItemAdapter());
        mPopupWindow = new PopupWindow(view, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        // 点击外面popupWindow消失
        mPopupWindow.setOutsideTouchable(mCancleAbleOutSide);
        if (mCancleAbleOutSide) {
            mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        }
        // 设置popWindow的显示和消失动画
        mPopupWindow.setAnimationStyle(R.style.menu_window_anim_style);
        // 在底部显示
        mPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        backgroundAlpha(0.5f);
        cancle.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mMenuClickListener != null) {
                    disMissPop();
                    mMenuClickListener.onCancelClick(mCancleContent);
                }
            }
        });
        mPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
    }

    class MyItemAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            int conut = 0;
            if (mItems != null && mItems.length > 0) {
                conut = mItems.length;
            }
            return conut;
        }

        @Override
        public Object getItem(int position) {
            String item = "暂无内容";
            if (mItems != null && mItems.length > 0) {
                item = mItems[position];
            }
            return item;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_menu_item, parent, false);
                holder.item_tv = convertView.findViewById(R.id.item_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.item_tv.setText(getItem(position).toString());
            if (getCount() == 1) {
                convertView.setBackgroundResource(R.drawable.menu_cancel);
            } else {
                if (position == 0) {
                    convertView.setBackgroundResource(R.drawable.menu_item_first_bg);
                } else if (position == (mItems.length - 1)) {
                    convertView.setBackgroundResource(R.drawable.menu_item_last_bg);
                } else {
                    convertView.setBackgroundResource(R.drawable.menu_item_middle_bg);
                }
            }


            convertView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mMenuClickListener != null) {
                        disMissPop();
                        mMenuClickListener.onItemClick(position, getItem(position).toString());
                    }
                }
            });

            return convertView;
        }

        class ViewHolder {
            TextView item_tv;
        }

    }


    private MenuClickListener mMenuClickListener;

    public void setOnMyPopClickListener(MenuClickListener menuClickListener) {
        this.mMenuClickListener = menuClickListener;
    }

    public interface MenuClickListener {
        void onItemClick(int index, String content);

        void onCancelClick(String content);
    }

    private void disMissPop() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }


    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha 透明度
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity) mContext).getWindow().getAttributes();
        lp.alpha = bgAlpha; // 0.0-1.0
        ((Activity) mContext).getWindow().setAttributes(lp);
    }
}
