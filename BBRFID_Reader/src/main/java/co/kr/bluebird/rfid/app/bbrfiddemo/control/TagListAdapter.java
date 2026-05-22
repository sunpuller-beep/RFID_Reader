/*
 * Copyright (C) 2015 - 2025 Bluebird Inc, All rights reserved.
 * 
 * http://www.bluebirdcorp.com/
 * 
 */

package co.kr.bluebird.rfid.app.bbrfiddemo.control;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.concurrent.CopyOnWriteArrayList;
import co.kr.bluebird.rfid.app.bbrfiddemo.R;
import co.kr.bluebird.rfid.app.bbrfiddemo.Constants;//[20250402]Add Bulk encoding

public class TagListAdapter extends BaseAdapter {

    private static final String TAG = TagListAdapter.class.getSimpleName();
    
    private static final boolean D = false;
    
    private static final int MAX_LIST_COUNT = 50000;
    
    private int mListCycleCount = 0;
       
    private CopyOnWriteArrayList<ListItem> mItemList;
    
    private CopyOnWriteArrayList<String> mTagList;
    
    private Context mContext;
    
    private class ItemHolder {
        
        public ImageView mImage;
        
        public TextView mUpText;
        
        public TextView mDownText;
        
        public TextView mDupText;
        public TextView mEPCDecodeText;
    }

    public TagListAdapter(Context ctx) {
        super();
        if (D) Log.d(TAG, "TagListAdapter");
        mContext = ctx;
        mItemList = new CopyOnWriteArrayList<>();
        mTagList = new CopyOnWriteArrayList<>();
    }
    
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getCount");
        return mItemList.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getItem");
        return mItemList.get(arg0);
    }

    public int getItemDupCount(int arg0) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getItem");
        return mItemList.get(arg0).mDupCount;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getItemId");
        return arg0;
    }

    @Override
    public View getView(int arg0, View arg1, ViewGroup arg2) {
        // TODO Auto-generated method stub
        if (D) Log.d(TAG, "getView");
        ItemHolder holder;
        if (arg1 == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            arg1 = inflater.inflate(R.layout.list_item, null);
            
            holder = new ItemHolder();
            holder.mImage = (ImageView) arg1.findViewById(R.id.mImage);
            holder.mUpText = (TextView) arg1.findViewById(R.id.mTagText);
            holder.mDownText = (TextView) arg1.findViewById(R.id.mRssiText);
            holder.mDupText = (TextView) arg1.findViewById(R.id.mDupText);
            holder.mEPCDecodeText = (TextView) arg1.findViewById(R.id.mEPCDecodeText);
            arg1.setTag(holder);
        } else {
            holder = (ItemHolder) arg1.getTag();
        }
        ListItem item = mItemList.get(arg0);
     
        if (item.mIv != -1) {
            holder.mImage.setVisibility(View.VISIBLE);
            holder.mImage.setImageDrawable(mContext.getDrawable(item.mIv));
        } else {
            holder.mImage.setVisibility(View.GONE);
        }
        //holder.mDownText.setVisibility(View.GONE);
        holder.mUpText.setText(item.mUt);
        if(item.mDt.contains(";")) {
            holder.mDownText.setText(item.mDt.split(";")[0]);
        } else {
        holder.mDownText.setText(item.mDt);
        }
        if(TextUtils.isEmpty(item.mEpcDecode)) {
            if(TextUtils.isEmpty(item.mPha) && TextUtils.isEmpty(item.mFrequency)) {
                holder.mEPCDecodeText.setVisibility(View.GONE);
            } else {
                holder.mEPCDecodeText.setVisibility(View.VISIBLE);
                holder.mEPCDecodeText.setText(item.mPha + " / " + item.mFrequency);
            }
        } else {
            holder.mEPCDecodeText.setVisibility(View.VISIBLE);
            holder.mEPCDecodeText.setText(item.mEpcDecode);
        }
        if (item.mDupCount > 1) {
            holder.mDupText.setText(mContext.getResources().getString(R.string.dup_str) + item.mDupCount);
        }
        return arg1;
    }
	
    //<-[20250402]Add Bulk encoding
    public Object getTagList() {
        if (D) Log.d(TAG, "getTagList");
        return mTagList;
    }
    
    public void addItem(int img, String upText, String downText, String pha, String frequency, String epcDecode, int isRead, String time,
                        boolean hasPC, boolean filter) {
        if (D) Log.d(TAG, "addItem " + filter);
        if (filter) {
            if (mTagList.contains(upText)) {
                if (D) Log.d(TAG, "count++ " + filter);
                int idx = mTagList.indexOf(upText);
                mItemList.get(idx).mDupCount = (mItemList.get(idx).mDupCount) + 1;
                this.notifyDataSetInvalidated();
                return;
            }

            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();
                notifyDataSetChanged();
                mListCycleCount++;
            }

            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText;
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            item.mTimeStamp = time;//[20251210]Add RSSI,TIMESTAMP
            mTagList.add(upText);
            mItemList.add(item);
            notifyDataSetChanged();
        } else {
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();
                notifyDataSetChanged();
                mListCycleCount++;
            }
            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText;
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            item.mTimeStamp = time;//[20251210]Add RSSI,TIMESTAMP
            mItemList.add(item);
            notifyDataSetChanged();
        }
    }
    //[20250402]Add Bulk encoding->

    public void addItem(int img, String upText, String downText, boolean hasPC, boolean filter) {
        addItem(img, upText, downText, "", "", "", hasPC, filter);
    }

    public void addItem(int img, String upText, String downText, String pha, String frequency, String epcDecode, boolean hasPC, boolean filter) {
        if (D) Log.d(TAG, "addItem " + filter);
        if (filter) {
            if (mTagList.contains(upText)) {
                if (D) Log.d(TAG, "count++ " + filter);
                int idx = mTagList.indexOf(upText);
                mItemList.get(idx).mDupCount = (mItemList.get(idx).mDupCount) + 1;
                this.notifyDataSetInvalidated();
                return;
            }
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();
                notifyDataSetChanged();
                mListCycleCount++;
            }
            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText;// + Long.toString(mItemList.size() + 1);
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            mTagList.add(upText);
            mItemList.add(item);
            notifyDataSetChanged();
        } else {
            if (mItemList.size() == MAX_LIST_COUNT) {
                mTagList.clear();
                mItemList.clear();
                notifyDataSetChanged();
                mListCycleCount++;
            }
            ListItem item = new ListItem();
            item.mIv = img;
            item.mUt = upText;// + Long.toString(mItemList.size() + 1);
            item.mDt = downText;
            item.mHasPc = hasPC;
            item.mDupCount = 1;
            item.mPha = pha;
            item.mFrequency = frequency;
            item.mEpcDecode = epcDecode;
            mItemList.add(item);
            notifyDataSetChanged();
        }
    }

    public void removeAllItem() {
        if (D) Log.d(TAG, "removeAllItem");
        mItemList.clear();
        mTagList.clear();
        mListCycleCount = 0;
        notifyDataSetChanged();
    }
    
    public int getTotalCount() {
        if (D) Log.d(TAG, "getTotalCount");
        return (mListCycleCount * MAX_LIST_COUNT) + mItemList.size();
    }
}