package cn.edu.zjut.backend.dto;

public class FaceImageRequest {
    private String faceImageUrl;

    public FaceImageRequest() {}

    public FaceImageRequest(String faceImageUrl) {
        this.faceImageUrl = faceImageUrl;
    }

    public String getFaceImageUrl() {
        return faceImageUrl;
    }

    public void setFaceImageUrl(String faceImageUrl) {
        this.faceImageUrl = faceImageUrl;
    }
}