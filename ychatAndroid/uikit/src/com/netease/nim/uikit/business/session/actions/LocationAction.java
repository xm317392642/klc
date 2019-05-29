package com.netease.nim.uikit.business.session.actions;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.location.LocationProvider;
import com.netease.nim.uikit.business.team.helper.TeamHelper;
import com.netease.nim.uikit.common.util.YchatToastUtils;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class LocationAction extends BaseAction {

    public LocationAction() {
        super(R.drawable.nim_message_plus_location_selector, R.string.input_panel_location);
    }

    @Override
    public void onClick() {
        if (getContainer().sessionType == SessionTypeEnum.Team) {
            if (TeamHelper.isTeamMember(getContainer().account, NimUIKit.getAccount())) {
                if (NimUIKitImpl.getLocationProvider() != null) {
                    NimUIKitImpl.getLocationProvider().requestLocation(getActivity(), new LocationProvider.Callback() {
                        @Override
                        public void onSuccess(double longitude, double latitude, String address) {
                            IMMessage message = MessageBuilder.createLocationMessage(getAccount(), getSessionType(), latitude, longitude, address);
                            sendMessage(message);
                        }
                    });
                }
            } else {
                YchatToastUtils.showShort("你已不在本群，无法进行下一步操作");
            }
        } else {
            if (NimUIKitImpl.getLocationProvider() != null) {
                NimUIKitImpl.getLocationProvider().requestLocation(getActivity(), new LocationProvider.Callback() {
                    @Override
                    public void onSuccess(double longitude, double latitude, String address) {
                        IMMessage message = MessageBuilder.createLocationMessage(getAccount(), getSessionType(), latitude, longitude, address);
                        sendMessage(message);
                    }
                });
            }
        }
    }
}
