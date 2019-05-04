package com.xr.ychat.redpacket;

import java.io.Serializable;

public class EnvelopeBean implements Serializable {
    private String envelopesID;
    private String envelopeMessage;
    private String envelopeName;
    private float envelopeAmount;
    private int envelopeType;

    public String getEnvelopesID() {
        return envelopesID;
    }

    public void setEnvelopesID(String envelopesID) {
        this.envelopesID = envelopesID;
    }

    public String getEnvelopeMessage() {
        return envelopeMessage;
    }

    public void setEnvelopeMessage(String envelopeMessage) {
        this.envelopeMessage = envelopeMessage;
    }

    public String getEnvelopeName() {
        return envelopeName;
    }

    public void setEnvelopeName(String envelopeName) {
        this.envelopeName = envelopeName;
    }

    public float getEnvelopeAmount() {
        return envelopeAmount;
    }

    public void setEnvelopeAmount(float envelopeAmount) {
        this.envelopeAmount = envelopeAmount;
    }

    public int getEnvelopeType() {
        return envelopeType;
    }

    public void setEnvelopeType(int envelopeType) {
        this.envelopeType = envelopeType;
    }
}
