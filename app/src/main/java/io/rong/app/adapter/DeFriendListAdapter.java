package io.rong.app.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.app.R;
import io.rong.app.model.Friend;
import io.rong.app.model.FriendSectionIndexer;
import io.rong.app.ui.DePinnedHeaderAdapter;
import io.rong.app.utils.PinyinFilterList;
import me.add1.resource.Resource;
import io.rong.imkit.widget.AsyncImageView ;

@SuppressLint("UseSparseArrays")
public class DeFriendListAdapter extends DePinnedHeaderAdapter<Friend> implements Filterable {

    private static String TAG =DeFriendListAdapter.class.getSimpleName();
    private LayoutInflater mInflater;
    private FriendFilter mFilter;
    private ArrayList<View> mViewList;

    public DeFriendListAdapter(Context context, List<Friend> friends) {
        super(context);
        setAdapterData(friends);

        mViewList = new ArrayList<View>();

        if (context != null)
            mInflater = LayoutInflater.from(context);

    }

    public void setAdapterData(List<Friend> friends) {

        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();

        List<List<Friend>> result = new ArrayList<List<Friend>>();
        int key = 0;

        for (Friend friend : friends) {
            key = friend.getSearchKey();

            if (hashMap.containsKey(key)) {
                int position = (Integer) hashMap.get(key);
                if (position <= result.size() - 1) {
                    result.get(position).add(friend);
                }
            } else {
                result.add(new ArrayList<Friend>());
                int length = result.size() - 1;
                result.get(length).add(friend);
                hashMap.put(key, length);
            }
        }

        updateCollection(result);
        mFilter = new FriendFilter(friends);
    }

    @Override
    protected View newView(Context context, int partition, List<Friend> data, int position, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.de_item_friendlist,null);
        ViewHolder holder = new ViewHolder();
        newSetTag(view, holder, position, data);
        view.setTag(holder);
        return view;
    }

    @Override
    protected void bindView(View v, int partition, List<Friend> data, int position) {

        ViewHolder holder = (ViewHolder) v.getTag();
        TextView name = holder.name;
        AsyncImageView photo = holder.photo;
        CheckBox choice = holder.choice;
        Friend friend = data.get(position);
        name.setText(friend.getNickname());

        Resource res =new Resource( friend.getPortrait());

        photo.setResource(res);

        photo.setTag(position);

        holder.friend = friend;

    }

    @Override
    protected View newHeaderView(Context context, int partition, List<Friend> data, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.de_item_friend_index, null);
        view.setTag(view.findViewById(R.id.index));
        return view;
    }

    @Override
    protected void bindHeaderView(View view, int partition, List<Friend> data) {
        Object objTag = view.getTag();
        if (objTag != null) {
            ((TextView) objTag).setText(String.valueOf(data.get(0).getSearchKey()));

        }
    }


    @Override
    protected View newView(Context context, int position, ViewGroup group) {
        return null;
    }

    @Override
    protected void bindView(View v, int position, Friend data) {

    }

    class PinnedHeaderCache {
        TextView titleView;
        ColorStateList textColor;
        Drawable background;
    }

    @Override
    protected SectionIndexer updateIndexer(Partition<Friend>[] data) {
        return new FriendSectionIndexer(data);
    }

    @Override
    public void configurePinnedHeader(View header, int position, int alpha) {
        PinnedHeaderCache cache = (PinnedHeaderCache) header.getTag();

        if (cache == null) {
            cache = new PinnedHeaderCache();
            cache.titleView = (TextView) header.findViewById(R.id.index);
            cache.textColor = cache.titleView.getTextColors();
            cache.background = header.getBackground();
            header.setTag(cache);
        }

        int section = getSectionForPosition(position);

        if (section != -1) {
            String title = (String) getSectionIndexer().getSections()[section];
            cache.titleView.setText(title);

            if (alpha == 255) {
                cache.titleView.setTextColor(cache.textColor);
            } else {
                int textColor = cache.textColor.getDefaultColor();
                cache.titleView.setTextColor(Color.argb(alpha, Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
            }
        }

    }

    public static class ViewHolder {
        public TextView name;
        public AsyncImageView photo;
        public String userId;
        public Friend friend;
        public CheckBox choice;
    }

    protected void newSetTag(View view, ViewHolder holder, int position, List<Friend> data) {

        AsyncImageView photo = (AsyncImageView) view.findViewById(R.id.de_ui_friend_icon);

        if (mViewList != null && !mViewList.contains(view)) {
            mViewList.add(view);
        }

        holder.name = (TextView) view.findViewById(R.id.de_ui_friend_name);
        holder.choice = (CheckBox) view.findViewById(R.id.de_ui_friend_checkbox);
        holder.photo = photo;
    }

    public void destroy() {

        if (mViewList != null) {
            mViewList.clear();
            mViewList = null;
        }
    }

    class FriendFilter extends PinyinFilterList<Friend> {

        public FriendFilter(List<Friend> dataList) {
            super(dataList);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            List<Friend> friends = (List<Friend>) results.values;

            if (friends == null) {
                friends = new ArrayList<Friend>();
            }

            HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();

            List<List<Friend>> result = new ArrayList<List<Friend>>();
            int key = 0;
            // int index = -1;
            for (Friend friend : friends) {
                key = friend.getSearchKey();

                if (hashMap.containsKey(key)) {
                    int position = (Integer) hashMap.get(key);
                    if (position <= result.size() - 1) {
                        result.get(position).add(friend);
                    }
                } else {
                    result.add(new ArrayList<Friend>());
                    int length = result.size() - 1;
                    result.get(length).add(friend);
                    hashMap.put(key, length);
                }

            }

            updateCollection(result);

            if (result.size() > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }

    public interface OnFilterFinished {
        void onFilterFinished();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public void onItemClick(String friendId, CheckBox checkBox) {
    }

}
