package lelab.couchdb.model;


public class Message {
    private String id;
    private int conversationId;
    private int senderId;
    private int messageType;
    private String msgTxt;
    private String messageStatus;
    private String mediaUrl;
    private String mediaMimeType;
    private Boolean isStarred;
    private double mediaSize;
    private String media_name;
    private double latitude;
    private double longitude;
    private String receivedAt;
    private String createdAt;
    private String deletedAt;

    public Message() {
    }

    public Message(String id, String msgTxt, int conversationId, int senderId, int messageType, String messageStatus, String mediaUrl, String mediaMimeType, Boolean isStarred, double mediaSize, String media_name, double latitude, double longitude, String receivedAt, String createdAt, String deletedAt) {
        this.id = id;
        this.msgTxt = msgTxt;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.messageType = messageType;
        this.messageStatus = messageStatus;
        this.mediaUrl = mediaUrl;
        this.mediaMimeType = mediaMimeType;
        this.isStarred = isStarred;
        this.mediaSize = mediaSize;
        this.media_name = media_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.receivedAt = receivedAt;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getConversationId() {
        return conversationId;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getMsgTxt() {
        return msgTxt;
    }

    public void setMsgTxt(String msgTxt) {
        this.msgTxt = msgTxt;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaMimeType() {
        return mediaMimeType;
    }

    public void setMediaMimeType(String mediaMimeType) {
        this.mediaMimeType = mediaMimeType;
    }

    public Boolean getStarred() {
        return isStarred;
    }

    public void setStarred(Boolean starred) {
        isStarred = starred;
    }

    public double getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(double mediaSize) {
        this.mediaSize = mediaSize;
    }

    public String getMedia_name() {
        return media_name;
    }

    public void setMedia_name(String media_name) {
        this.media_name = media_name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(String receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(String deletedAt) {
        this.deletedAt = deletedAt;
    }
}
