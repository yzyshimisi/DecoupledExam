package cn.edu.zjut.backend.util;

import cn.smartjavaai.common.cv.SmartImageFactory;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import cn.smartjavaai.common.entity.face.LivenessResult;
import cn.smartjavaai.common.enums.SimilarityType;
import cn.smartjavaai.face.config.FaceDetConfig;
import cn.smartjavaai.face.config.FaceRecConfig;
import cn.smartjavaai.face.config.LivenessConfig;
import cn.smartjavaai.face.entity.FaceRegisterInfo;
import cn.smartjavaai.face.entity.FaceSearchParams;
import cn.smartjavaai.face.enums.FaceDetModelEnum;
import cn.smartjavaai.face.enums.FaceRecModelEnum;
import cn.smartjavaai.face.enums.LivenessModelEnum;
import cn.smartjavaai.face.factory.FaceDetModelFactory;
import cn.smartjavaai.face.factory.FaceRecModelFactory;
import cn.smartjavaai.face.factory.LivenessModelFactory;
import cn.smartjavaai.face.model.facedect.FaceDetModel;
import cn.smartjavaai.face.model.facerec.FaceRecModel;
import cn.smartjavaai.face.model.liveness.LivenessDetModel;
import cn.smartjavaai.face.vector.config.SQLiteConfig;

import ai.djl.modality.cv.Image;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Random;
import java.util.UUID;

public class FaceRec {

    private final LivenessDetModel livenessDetModel;
    private final FaceRecModel faceRecModel;

    public FaceRec(){
        // 人脸检测模型
        FaceDetConfig faceDetConfig = new FaceDetConfig();
        faceDetConfig.setModelEnum(FaceDetModelEnum.MTCNN); //人脸检测模型
        // 离线模型存储路径
        String resourcePath = "C:\\Users\\31986\\Desktop\\resources";    // 依据情况自己改
        faceDetConfig.setModelPath(resourcePath + "\\FaceModel\\mtcnn");
        FaceDetModel faceDetModel = FaceDetModelFactory.getInstance().getModel(faceDetConfig);

        // 活体识别模型
        LivenessConfig livenessConfig = new LivenessConfig();
        livenessConfig.setModelEnum(LivenessModelEnum.MINI_VISION_MODEL);
        livenessConfig.setDetectModel(faceDetModel);
        livenessConfig.setModelPath(resourcePath + "\\FaceModel\\MiniVision\\2.7_80x80_MiniFASNetV2.onnx");
        livenessConfig.putCustomParam("seModelPath", resourcePath + "\\FaceModel\\MiniVision\\4_0_0_80x80_MiniFASNetV1SE.onnx");
        livenessConfig.setFrameCount(3);
        livenessDetModel = LivenessModelFactory.getInstance().getModel(livenessConfig);

        // 人脸识别模型
        FaceRecConfig faceRecConfig = new FaceRecConfig();
        faceRecConfig.setModelEnum(FaceRecModelEnum.FACENET_MODEL);//人脸识别模型
        faceRecConfig.setModelPath(resourcePath + "\\FaceModel\\FaceNet");
        faceRecConfig.setCropFace(true);
        faceRecConfig.setAlign(true);
        faceRecConfig.setDetectModel(faceDetModel);

        // 配置Milvus数据库
        SQLiteConfig vectorDBConfig = new SQLiteConfig();
        vectorDBConfig.setDbPath( resourcePath + "\\face.db");
        vectorDBConfig.setSimilarityType(SimilarityType.IP);

        faceRecConfig.setVectorDBConfig(vectorDBConfig);
        faceRecModel = FaceRecModelFactory.getInstance().getModel(faceRecConfig);
    }

    // 人脸识别
    public boolean faceRecognition(String file){
        InputStream inputStream = Base64Util.base64ToInputStream(file);
        R<LivenessResult> status = livenessDetModel.detectVideo(inputStream);
        System.out.println(status.getData().toString());
        if(status.getData().getStatus().toString().equals("LIVE")){ // 确定是活体
            try {
                BufferedImage bufferedImage = extractRandomFrame(file);
                Image image = SmartImageFactory.getInstance().fromBufferedImage(bufferedImage);
                R<DetectionResponse> res = faceQuery(image);
                System.out.println(res.toString());
                if(res.getCode().intValue() == 0 && res.getMessage().toString().equals("成功") && res.getData()!=null){
                    return true;
                }else{
                    return false;
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
                return false;
            }
        }else{
            return false;
        }
    }

    // 人脸注册
    public boolean faceRegister(String file){
        try {
            InputStream inputStream = Base64Util.base64ToInputStream(file);
            Image faceImg = SmartImageFactory.getInstance().fromInputStream(inputStream);
            String uuid = UUID.randomUUID().toString().replace("-", "");;

            faceRecModel.loadFaceFeatures();
            if(faceRecModel.isLoadFaceCompleted()){
                FaceRegisterInfo faceRegisterInfo = new FaceRegisterInfo();
                faceRegisterInfo.setId(uuid);
                faceRegisterInfo.setMetadata("NullNull");
                R<String> res = faceRecModel.register(faceRegisterInfo, faceImg);
                System.out.println(res);

                faceRecModel.releaseFaceFeatures();
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    // 人脸查询
    public R<DetectionResponse> faceQuery(Image image){
        FaceSearchParams params = new FaceSearchParams();
        R<DetectionResponse> res = faceRecModel.search(image, params);
        return res;
    }

    // 人脸删除
    public boolean faceDelete(String id){
        try{
            faceRecModel.removeRegister(id);
            return true;
        }catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    // 清空人脸
    public boolean faceClear(){
        try{
            faceRecModel.clearFace();
            return true;
        }catch (Exception e){
            System.err.println(e.getMessage());
            return false;
        }
    }

    // 从mp4流中随机提取一帧
    public static BufferedImage extractRandomFrame(String file) {

        InputStream inputStream = Base64Util.base64ToInputStream(file);

        Path tempPath = null;
        FFmpegFrameGrabber grabber = null;

        try {

            tempPath = Files.createTempFile("video-upload-", ".mp4");

            // 将 InputStream 写入临时文件
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("视频已缓存到临时文件: " + tempPath);

            grabber = new FFmpegFrameGrabber(tempPath.toFile());
            grabber.start();

            long totalFrames = grabber.getLengthInFrames();

            if (totalFrames <= 0) {
                System.out.println("无法获取视频总帧数，默认提取第 3 帧");
                grabber.setVideoFrameNumber(3);
            } else {
                // 生成随机数：范围是 [0, totalFrames)
                Random random = new Random();
                int randomIndex = random.nextInt((int) totalFrames);

                System.out.println("总帧数: " + totalFrames + ", 随机选取第: " + randomIndex + " 帧");

                // 设置位置
                grabber.setVideoFrameNumber(randomIndex);
            }

            // 提取并转换
            Frame frame = grabber.grabImage();
            if (frame != null) {
                return new Java2DFrameConverter().convert(frame);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}
