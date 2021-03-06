package io.rong.app.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.rong.app.DemoContext;
import io.rong.app.R;
import io.rong.app.adapter.DeBlackMultiChoiceAdapter;
import io.rong.app.adapter.DeFriendListAdapter;
import io.rong.app.model.Friend;
import io.rong.app.ui.DePinnedHeaderListView;
import io.rong.app.ui.DeSwitchGroup;
import io.rong.app.ui.DeSwitchItemView;
import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.UserInfo;

/**
 * Created by Bob on 2015/3/26.
 */
@SuppressWarnings("ALL")
public class DeBlackListFragment extends Fragment implements DeSwitchGroup.ItemHander, View.OnClickListener, TextWatcher, DeFriendListAdapter.OnFilterFinished, AdapterView.OnItemClickListener {

    private static final String TAG = DeBlackListFragment.class.getSimpleName();
    protected DeBlackMultiChoiceAdapter mAdapter;
    private DePinnedHeaderListView mListView;
    private DeSwitchGroup mSwitchGroup;
    /**
     * 好友list
     */
    protected List<Friend> mFriendsList;
    protected List<UserInfo> mUserInfoList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.de_list_address, null);

        mListView = (DePinnedHeaderListView) view.findViewById(R.id.de_ui_friend_list);
        mSwitchGroup = (DeSwitchGroup) view.findViewById(R.id.de_ui_friend_message);

        mListView.setPinnedHeaderView(LayoutInflater.from(this.getActivity()).inflate(R.layout.de_item_friend_index,
                mListView, false));

        mListView.setFastScrollEnabled(false);

        mListView.setOnItemClickListener(this);
        mSwitchGroup.setItemHander(this);

        mListView.setHeaderDividersEnabled(false);
        mListView.setFooterDividersEnabled(false);

        return view;
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

//        ArrayList<UserInfo> userInfos = null;

        //获取好友列表
        if (RongIM.getInstance().getRongClient() != null) {
            RongIM.getInstance().getRongClient().getBlacklist(new RongIMClient.GetBlacklistCallback() {
                @Override
                public void onSuccess(String[] userIds) {
                    mUserInfoList = DemoContext.getInstance().getUserInfoByIds(userIds);

                    mFriendsList = new ArrayList<Friend>();

                    if (mUserInfoList != null) {
                        for (UserInfo userInfo : mUserInfoList) {
                            Friend friend = new Friend();
                            friend.setNickname(userInfo.getName());
                            friend.setPortrait(userInfo.getPortraitUri() + "");
                            friend.setUserId(userInfo.getUserId());
                            mFriendsList.add(friend);
                        }
                    }
                    mFriendsList = sortFriends(mFriendsList);
                    mAdapter = new DeBlackMultiChoiceAdapter(getActivity(), mFriendsList);
                    mListView.setAdapter(mAdapter);
                    fillData();

                }

                @Override
                public void onError(RongIMClient.ErrorCode errorCode) {
                }
            });
        }


        super.onViewCreated(view, savedInstanceState);
    }


    private final void fillData() {

//        mAdapter.removeAll();
        mAdapter.setAdapterData(mFriendsList);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {

        if (v instanceof DeSwitchItemView) {
            CharSequence tag = ((DeSwitchItemView) v).getText();

            if (mAdapter != null && mAdapter.getSectionIndexer() != null) {
                Object[] sections = mAdapter.getSectionIndexer().getSections();
                int size = sections.length;

                for (int i = 0; i < size; i++) {
                    if (tag.equals(sections[i])) {
                        int index = mAdapter.getPositionForSection(i);
                        mListView.setSelection(index + mListView.getHeaderViewsCount());
                        break;
                    }
                }
            }
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object tagObj = view.getTag();
        if (tagObj != null && tagObj instanceof DeBlackMultiChoiceAdapter.ViewHolder) {
            DeBlackMultiChoiceAdapter.ViewHolder viewHolder = (DeBlackMultiChoiceAdapter.ViewHolder) tagObj;
            mAdapter.onItemClick(viewHolder.friend.getUserId());
            return;
        }

    }

    @Override
    public void onDestroyView() {
        if (mAdapter != null) {
            mAdapter.destroy();
            mAdapter = null;
        }
        super.onDestroyView();
    }

    /**
     * 好友数据排序
     *
     * @param friends 好友 List
     * @return 排序后的好友 List
     */
    private ArrayList<Friend> sortFriends(List<Friend> friends) {

        String[] searchLetters = getResources().getStringArray(R.array.de_search_letters);

        HashMap<String, ArrayList<Friend>> userMap = new HashMap<String, ArrayList<Friend>>();

        ArrayList<Friend> friendsArrayList = new ArrayList<Friend>();

        for (Friend friend : friends) {

            String letter = new String(new char[]{friend.getSearchKey()});

            if (userMap.containsKey(letter)) {
                ArrayList<Friend> friendList = userMap.get(letter);
                friendList.add(friend);
            } else {
                ArrayList<Friend> friendList = new ArrayList<Friend>();
                friendList.add(friend);
                userMap.put(letter, friendList);
            }

        }

        for (int i = 0; i < searchLetters.length; i++) {
            String letter = searchLetters[i];
            ArrayList<Friend> fArrayList = userMap.get(letter);
            if (fArrayList != null) {
                friendsArrayList.addAll(fArrayList);
            }
        }

        return friendsArrayList;
    }

    @Override
    public void onFilterFinished() {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }


}
