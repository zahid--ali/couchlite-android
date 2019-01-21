package lelab.couchdb.model;


public class Message {
    private String id;
    private String msgTxt;
    private String conversationId;
    private String senderId;
    private String messageType;
    private String messageStatus;
    private String mediaUrl;
    private String mediaMimeType;
    private Boolean isStarred;
    private String mediaSize;
    private String media_name;
    private String latitude;
    private String longitude;
    private String receivedAt;
    private String createdAt;
    private String deletedAt;

    public Message() {
    }

    public Message(String id, String msgTxt, String conversationId, String senderId, String messageType, String messageStatus, String mediaUrl, String mediaMimeType, Boolean isStarred, String mediaSize, String media_name, String latitude, String longitude, String receivedAt, String createdAt, String deletedAt) {
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

    public String getMsgTxt() {
        return msgTxt;
    }

    public void setMsgTxt(String msgTxt) {
        this.msgTxt = msgTxt;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
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

    public String getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(String mediaSize) {
        this.mediaSize = mediaSize;
    }

    public String getMedia_name() {
        return media_name;
    }

    public void setMedia_name(String media_name) {
        this.media_name = media_name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
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
