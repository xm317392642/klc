package com.netease.nim.uikit.common;

import java.util.ArrayList;

public class Mahjong {
    private String cell;
    private int cost_type;
    private String des;
    private String game_name;
    private String game_type;
    private int gid;
    private int plat_flag;
    private ArrayList<MahjongBean> player;
    private String qun_name;
    private int tid;
    private long time;
    
    public int getPlat_flag() {
        return plat_flag;
    }

    public void setPlat_flag(int plat_flag) {
        this.plat_flag = plat_flag;
    }

    public String getGame_name() {
        return game_name;
    }

    public void setGame_name(String game_name) {
        this.game_name = game_name;
    }

    public String getGame_type() {
        return game_type;
    }

    public void setGame_type(String game_type) {
        this.game_type = game_type;
    }

    public String getCell() {
        return cell;
    }

    public void setCell(String cell) {
        this.cell = cell;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public int getCost_type() {
        return cost_type;
    }

    public void setCost_type(int cost_type) {
        this.cost_type = cost_type;
    }

    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    public String getQun_name() {
        return qun_name;
    }

    public void setQun_name(String qun_name) {
        this.qun_name = qun_name;
    }

    public int getGid() {
        return gid;
    }

    public void setGid(int gid) {
        this.gid = gid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public ArrayList<MahjongBean> getPlayer() {
        return player;
    }

    public void setPlayer(ArrayList<MahjongBean> player) {
        this.player = player;
    }
}
