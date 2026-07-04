package com.marketquest.auth_service.event;

public class OtpRequestedEvent {
    private String identifier;
    private String otp;
    private String purpose;

    public OtpRequestedEvent() {
    }

    public OtpRequestedEvent(String identifier, String otp, String purpose) {
        this.identifier = identifier;
        this.otp = otp;
        this.purpose = purpose;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
