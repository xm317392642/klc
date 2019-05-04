package com.xr.ychat.main.fragment;

import com.xr.ychat.R;
import com.xr.ychat.main.model.MainShareTab;
import com.xr.ychat.team.fragment.TeamListFragment;

/**
 * 群聊
 */
public class ShareTeamFragment extends MainShareTabFragment {


        private TeamListFragment fragment;

        public ShareTeamFragment() {
            setContainerId(MainShareTab.TEAM_CHAT.fragmentId);
        }

        @Override
        protected void onInit() {
            fragment = (TeamListFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.team_fragment);
        }

        @Override
        public void onCurrent() {
            super.onCurrent();
//        if (fragment != null) {
//            fragment.onCurrent();
//        }
        }
    }

