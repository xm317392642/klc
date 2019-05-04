package com.netease.nim.uikit.business.team.activity

import android.os.Bundle
import com.netease.nim.uikit.R
import com.netease.nim.uikit.common.activity.UI

/**
 * 群头像设置页面
 */
class TeamIconActivity: UI() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.team_icon_set)
    }
}