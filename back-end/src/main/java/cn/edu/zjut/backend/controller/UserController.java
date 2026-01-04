package cn.edu.zjut.backend.controller;

import cn.edu.zjut.backend.po.User;
import cn.edu.zjut.backend.service.UserService;
import cn.edu.zjut.backend.util.FaceRec;
import cn.edu.zjut.backend.util.Jwt;
import cn.edu.zjut.backend.util.Response;
import cn.edu.zjut.backend.util.LoginLogger;
import cn.edu.zjut.backend.util.UserContext;
import cn.smartjavaai.common.entity.DetectionResponse;
import cn.smartjavaai.common.entity.R;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import cn.edu.zjut.backend.dto.*;
import cn.edu.zjut.backend.annotation.LogRecord;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.util.Map;


@Controller
@RequestMapping("/api")
public class UserController {
    
    @Autowired
    @Qualifier("userServ")
    private UserService userService;
    
    @Autowired
    private LoginLogger loginLogger;

    /**
     * 用户注册
     */
    @RequestMapping(value = "/user/register", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "用户认证", action = "用户注册", targetType = "用户", logType = LogRecord.LogType.SECURITY)
    public Response<String> register(@RequestBody User user) {
        // 检查手机号格式
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String phonePattern = "^1[3-9]\\d{9}$";
            if (!user.getPhone().matches(phonePattern)) {
                return Response.error("手机号格式不正确");
            }
        }
        
        // 检查密码是否为空
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Response.error("密码不能为空！");
        }
        
        if (userService.register(user)) {
            return Response.success("注册成功");
        } else {
            // 区分不同错误原因
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return Response.error("密码不能为空！");
            }
            return Response.error("注册失败，用户名可能已存在或手机号格式不正确");
        }
    }

    /**
     * 用户登录
     */
    @RequestMapping(value = "/user/login", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "用户认证", action = "用户登录", targetType = "用户", logType = LogRecord.LogType.LOGIN)
    public Response<String> login(@RequestBody LoginRequest loginRequest) {
        String token = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (token != null && !token.isEmpty()) {
            // 登录成功，将用户信息保存到session中
            String[] parts = token.split("\\.");
            if (parts.length == 3) {
                // 解析JWT token获取用户信息
                try {
                    String payload = new String(java.util.Base64.getDecoder().decode(parts[1]));
                    com.google.gson.JsonObject jsonObject = new com.google.gson.Gson().fromJson(payload, com.google.gson.JsonObject.class);
                    Long userId = jsonObject.get("id").getAsLong();
                    String username = jsonObject.get("username").getAsString();
                    Integer userType = jsonObject.get("userType").getAsInt();
                    
                    // 创建用户对象并保存到session
                    User user = new User();
                    user.setUserId(userId);
                    user.setUsername(username);
                    user.setUserType(userType);

//                   session.setAttribute("currentUser", user);
                    
                    // 设置UserContext，供AOP记录日志使用
                    Claims claims = new Jwt().validateJwt(token);
                    if (claims != null) {
                        UserContext.setClaims(claims);
                    }

                } catch (Exception e) {
                    // 如果解析失败，仍然记录日志但使用基本信息
                    e.printStackTrace();
                }
            }
            return Response.success(token);
        } else {
// 登录失败，记录登录日志
//            loginLogger.logLoginFailure(
//                loginRequest.getUsername(),
//                request.getRemoteAddr(),
//                request.getHeader("User-Agent"),
//                "用户名或密码错误，或账户已被禁用"
//            );
            loginLogger.logLoginFailure("用户名或密码错误，或账户已被禁用");
            return Response.error("用户名或密码错误，或账户已被禁用");
        }
    }

    /**
     * 用户登录（人脸）
     */
    @RequestMapping(value = "/user/login-face", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "用户认证", action = "人脸登录", targetType = "用户", logType = LogRecord.LogType.LOGIN)
    public Response<String> loginFace(@RequestBody Map<String, Object> loginRequest) {
//        FaceRec faceRec = FaceRec.getInstance();
        FaceRec faceRec = new FaceRec();

        String videoBase64 = loginRequest.get("video").toString();
        if(videoBase64==null || videoBase64.isEmpty()){
            return Response.error("参数禁止为空");
        }

        R<DetectionResponse> res = faceRec.faceRecognition(videoBase64);

        if(res != null && res.getCode() == 0 && res.getMessage().equals("成功") && res.getData()!=null){

            Gson gson = new Gson();
            String metadata = res.getData().getDetectionInfoList().get(0).getFaceInfo().getFaceSearchResults().get(0).getMetadata();
            Map Metadata = gson.fromJson(metadata, Map.class);

            Jwt jwt = new Jwt();

            Long id = ((Number) Metadata.get("id")).longValue();
            String username = (String) Metadata.get("username");
            Integer userType = ((Number) Metadata.get("userType")).intValue();

            String token = jwt.generateJwtToken(id, username, userType);

            return Response.success(token);
        }else{
            return Response.error("人脸登录失败！");
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @RequestMapping(value = "/user/current", method = RequestMethod.GET)
    @ResponseBody
    public Response<User> getCurrentUser(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims != null) {
            Long userId = ((Number) claims.get("id")).longValue();
            User user = userService.getUserById(userId);
            if (user != null) {
                // 清除密码信息再返回
                user.setPassword(null);
                return Response.success(user);
            }
        }
        return Response.error("用户未登录");
    }

    /**
     * 查询用户列表（可根据用户类型筛选）
     * 只有管理员有权查询用户列表
     */
    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "查询用户列表", targetType = "用户", logType = LogRecord.LogType.OPERATION)
    public Response<List<User>> getUsers(@RequestParam(value = "userType", required = false) Integer userType, HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Integer currentUserType = (Integer) claims.get("userType");
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 只有管理员（userType=0）可以查询用户列表
        if (currentUserType != 0) {
            return Response.error("权限不足，只有管理员可以查询用户列表");
        }
        
        List<User> users = userService.getUsers(userType);
        return Response.success(users);
    }

    /**
     * 管理员注册教师用户
     */
    @RequestMapping(value = "/admin/teacher/register", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "管理员注册教师用户", targetType = "用户", logType = LogRecord.LogType.OPERATION)
    public Response<String> adminRegisterTeacher(@RequestBody User user, HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Integer currentUserType = (Integer) claims.get("userType");
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 只有管理员可以注册教师用户
        if (currentUserType != 0) {
            return Response.error("权限不足，只有管理员可以注册教师用户");
        }
        
        // 强制设置用户类型为教师
        user.setUserType(1); // 教师账号
        
        // 检查手机号格式
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String phonePattern = "^1[3-9]\\d{9}$";
            if (!user.getPhone().matches(phonePattern)) {
                return Response.error("手机号格式不正确");
            }
        }
        
        // 检查密码是否为空
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return Response.error("密码不能为空！");
        }
        
        if (userService.adminRegister(user)) {
            return Response.success("教师用户注册成功");
        } else {
            // 区分不同错误原因
            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return Response.error("密码不能为空！");
            }
            return Response.error("注册失败，用户名可能已存在或手机号格式不正确");
        }
    }

    /**
     * 管理员更新其他用户信息
     */
    @RequestMapping(value = "/admin/user", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "管理员更新用户信息", targetType = "用户", logType = LogRecord.LogType.OPERATION)
    public Response<String> adminUpdateUser(@RequestBody User user, HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Integer currentUserType = (Integer) claims.get("userType");
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 只有管理员可以更新其他用户信息
        if (currentUserType != 0) {
            return Response.error("权限不足，只有管理员可以更新其他用户信息");
        }
        
        // 管理员不能通过此接口修改自己的信息
        if (currentUserId.equals(user.getUserId())) {
            return Response.error("管理员不能通过此接口修改自己的信息，请使用个人信息修改接口");
        }
        
        // 验证手机号格式（如果提供了手机号）
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            String phonePattern = "^1[3-9]\\d{9}$";
            if (!user.getPhone().matches(phonePattern)) {
                return Response.error("手机号格式不正确");
            }
        }
        
        if (userService.updateUser(user)) {
            return Response.success("用户信息更新成功");
        } else {
            return Response.error("用户信息更新失败，用户名可能已存在");
        }
    }

    /**
    * 上传用户人脸图像
    */
    @RequestMapping(value = "/user/face-image/upload", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "上传用户人脸图像", targetType = "用户", logType = LogRecord.LogType.OPERATION)
    public Response<String> uploadUserFaceImage(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {

        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }

        Long currentUserId = ((Number) claims.get("id")).longValue();

        // 检查是否有文件上传
        if (file.isEmpty()) {
            return Response.error("请选择要上传的文件");
        }

        String fileBase64 = "";
        try{
            byte[] fileBytes = file.getBytes();
            fileBase64 = Base64.getEncoder().encodeToString(fileBytes);
        }catch (IOException e){
            e.printStackTrace();
            return Response.error("文件处理失败");
        }

        FaceRec faceRec = FaceRec.getInstance();

        if(faceRec.faceRegister(fileBase64)){
            return Response.success("人脸图像上传成功");
        }else{
            return Response.error("人脸图像上传失败");
        }
    }

    /**
     * 上传用户头像
     */
    @RequestMapping(value = "/user/avatar/upload", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> uploadUserAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }

        Long currentUserId = ((Number) claims.get("id")).longValue();

        // 检查是否有文件上传
        if (file.isEmpty()) {
            return Response.error("请选择要上传的头像文件");
        }

        // 检查文件类型是否为图片
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Response.error("请选择图片文件（jpg、png、gif等）");
        }

        try {
            // 确保目录存在
            String uploadDir = "C:\\Users\\31986\\Desktop\\resources\\uploads\\avatars";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFileName = "avatar_" + currentUserId + "_" + System.currentTimeMillis() + fileExtension;

            // 保存文件到指定目录
            String filePath = uploadDir + "/" + newFileName;
            file.transferTo(new File(filePath));

            // 生成访问URL
            String fileUrl = "/uploads/avatars/" + newFileName;

            // 更新用户的头像URL
            User userToUpdate = new User();
            userToUpdate.setUserId(currentUserId);
            userToUpdate.setAvatarUrl(fileUrl);

            if (userService.updateUser(userToUpdate)) {
                return Response.success(fileUrl);
            } else {
                // 如果更新数据库失败，删除已上传的文件
                File uploadedFile = new File(filePath);
                if (uploadedFile.exists()) {
                    uploadedFile.delete();
                }
                return Response.error("头像上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.error("头像上传失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @RequestMapping(value = "/user", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "更新用户信息", targetType = "用户", logType = LogRecord.LogType.OPERATION)
    public Response<String> updateUser(@RequestBody UpdateUserInfoRequest request, HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 构建要更新的用户对象
        User userToUpdate = new User();
        userToUpdate.setUserId(currentUserId); // 从token获取用户ID
        userToUpdate.setUsername(request.getUsername());
        userToUpdate.setRealName(request.getRealName());
        userToUpdate.setAvatarUrl(request.getAvatarUrl());
        userToUpdate.setPhone(request.getPhone());
        
        // 验证手机号格式（如果提供了手机号）
        if (userToUpdate.getPhone() != null && !userToUpdate.getPhone().isEmpty()) {
            String phonePattern = "^1[3-9]\\d{9}$";
            if (!userToUpdate.getPhone().matches(phonePattern)) {
                return Response.error("手机号格式不正确");
            }
        }
        
        if (userService.updateUser(userToUpdate)) {
            // 更新后返回最新的用户信息
            User updatedUser = userService.getUserById(currentUserId);
            updatedUser.setPassword(null); // 清除密码信息
            return Response.success("用户信息更新成功");
        } else {
            return Response.error("用户信息更新失败，用户名可能已存在");
        }
    }

    /**
     * 更新用户人脸图像URL
     */
    @RequestMapping(value = "/user/face-image", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "更新用户人脸图像", targetType = "用户", logType = LogRecord.LogType.OPERATION)
    public Response<String> updateUserFaceImage(@RequestBody FaceImageRequest request, HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Long currentUserId = ((Number) claims.get("id")).longValue();

        // 构建要更新的用户对象
        User userToUpdate = new User();
        userToUpdate.setUserId(currentUserId);
        userToUpdate.setFaceImg(request.getFaceImageUrl());

        if (userService.updateUser(userToUpdate)) {
            return Response.success("人脸图像更新成功");
        } else {
            return Response.error("人脸图像更新失败");
        }
    }

    /**
     * 删除用户
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.DELETE)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "删除用户", targetType = "用户", logType = LogRecord.LogType.SECURITY)
    public Response<String> deleteUser(@PathVariable("userId") Long userId, HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Integer currentUserType = (Integer) claims.get("userType");
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 只有管理员可以删除用户
        if (currentUserType != 0) {
            return Response.error("权限不足，只有管理员可以删除用户");
        }
        
        // 不能删除自己
        if (currentUserId.equals(userId)) {
            return Response.error("不能删除自己的账户");
        }
        
        if (userService.deleteUser(userId)) {
            return Response.success("用户删除成功");
        } else {
            return Response.error("用户删除失败");
        }
    }

    /**
     * 修改用户状态（启用/禁用）
     */
    @RequestMapping(value = "/user/status", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "修改用户状态", targetType = "用户", logType = LogRecord.LogType.SECURITY)
    public Response<String> updateUserStatus(@RequestBody UpdateUserStatusRequest request, HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Integer currentUserType = (Integer) claims.get("userType");
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 只有管理员可以修改用户状态
        if (currentUserType != 0) {
            return Response.error("权限不足，只有管理员可以修改用户状态");
        }
        
        // 不能修改自己的状态
        if (currentUserId.equals(request.getUserId())) {
            return Response.error("不能修改自己的账户状态");
        }
        
        if (userService.updateUserStatus(request.getUserId(), request.getStatus())) {
            return Response.success("用户状态更新成功");
        } else {
            return Response.error("用户状态更新失败");
        }
    }

    /**
     * 修改密码
     */
    @RequestMapping(value = "/user/password", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "修改密码", targetType = "用户", logType = LogRecord.LogType.SECURITY)
    public Response<String> changePassword(@RequestBody ChangePasswordRequest request, HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 检查新密码是否为空
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            return Response.error("密码不能为空！");
        }
        
        if (userService.changePassword(currentUserId, request.getOldPassword(), request.getNewPassword())) {
            return Response.success("密码修改成功");
        } else {
            // 区分不同的错误原因
            if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
                return Response.error("密码不能为空！");
            }
            return Response.error("原密码错误，密码修改失败");
        }
    }

    /**
     * 管理员重置用户密码
     */
    @RequestMapping(value = "/admin/user/password/reset", method = RequestMethod.PUT)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "管理员重置用户密码", targetType = "用户", logType = LogRecord.LogType.SECURITY)
    public Response<String> adminResetPassword(@RequestBody ResetPasswordRequest request, HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Integer currentUserType = (Integer) claims.get("userType");
        Long currentUserId = ((Number) claims.get("id")).longValue();
        
        // 只有管理员可以重置其他用户密码
        if (currentUserType != 0) {
            return Response.error("权限不足，只有管理员可以重置其他用户密码");
        }
        
        if (userService.resetPassword(request.getUserId())) {
            return Response.success("用户密码重置成功");
        } else {
            return Response.error("用户密码重置失败");
        }
    }
    
    /**
     * 根据用户ID获取用户信息（仅管理员可访问其他用户信息）
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public Response<User> getUserById(@PathVariable("userId") Long userId, HttpServletRequest request) {
        // 取消权限限制，所有用户都可以访问
        User user = userService.getUserById(userId);
        if (user != null) {
            // 清除密码信息再返回
            user.setPassword(null);
            return Response.success(user);
        } else {
            return Response.error("用户不存在");
        }
    }
    
    /**
     * 获取当前用户头像
     */
    @RequestMapping(value = "/user/avatar", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getUserAvatar(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Long currentUserId = ((Number) claims.get("id")).longValue();
        User user = userService.getUserById(currentUserId);
        if (user != null) {
            return Response.success(user.getAvatarUrl());
        } else {
            return Response.error("用户不存在");
        }
    }
    
    /**
     * 获取当前用户人脸基准照片
     */
    @RequestMapping(value = "/user/face-image", method = RequestMethod.GET)
    @ResponseBody
    public Response<String> getUserFaceImage(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Long currentUserId = ((Number) claims.get("id")).longValue();
        User user = userService.getUserById(currentUserId);
        if (user != null) {
            return Response.success(user.getFaceImg());
        } else {
            return Response.error("用户不存在");
        }
    }
    
    /**
     * 管理员模拟用户登录
     */
    @RequestMapping(value = "/user/simulate", method = RequestMethod.POST)
    @ResponseBody
    @LogRecord(module = "用户管理", action = "管理员模拟用户登录", targetType = "用户", logType = LogRecord.LogType.SECURITY)
    public Response<User> simulateLogin(@RequestBody SimulateLoginRequest request, HttpServletRequest httpRequest) {
        Claims claims = (Claims) httpRequest.getAttribute("claims");
        if (claims == null) {
            return Response.error("请先登录");
        }
        
        Long currentUserId = ((Number) claims.get("id")).longValue();
        Integer currentUserType = (Integer) claims.get("userType");
        
        User simulateUser = userService.simulateLogin(currentUserId, request.getSimulateUserType());
        if (simulateUser != null) {
            // 返回模拟用户的JWT Token
            Jwt jwtUtil = new Jwt();
            String token = jwtUtil.generateJwtToken(simulateUser.getUserId(), simulateUser.getUsername(), simulateUser.getUserType(), simulateUser.getFaceImg());
            simulateUser.setPassword(null); // 清除密码信息
            return Response.success(simulateUser);
        } else {
            return Response.error("模拟登录失败，无权限或用户不存在");
        }
    }
}