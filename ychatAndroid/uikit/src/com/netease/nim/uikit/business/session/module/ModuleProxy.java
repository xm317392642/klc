package com.netease.nim.uikit.business.session.module;

import com.netease.nim.uikit.common.UnclaimedEnvelope;
import com.netease.nimlib.sdk.msg.model.IMMessage;

import java.io.Serializable;

/**
 * 会话窗口提供给子模块的代理接口。
 */
public interface ModuleProxy extends Serializable {
    // 发送消息
    boolean sendMessage(IMMessage msg);

    // 消息输入区展开时候的处理
    void onInputPanelExpand();

    // 应当收起输入区
    void shouldCollapseInputPanel();

    // 是否正在录音
    boolean isLongClickEnabled();

    void onItemFooterClick(IMMessage message);

    void onUnclaimedEnvelopeClick(UnclaimedEnvelope message);
}
