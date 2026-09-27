package com.example.springboot.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

@RestController
@CrossOrigin
@RequestMapping("/file")
public class FileController {

    @Value("${ip:localhost}")
    String ip;

    @Value("${server.port}")
    String port;

    private static final String ROOT_PATH = resolveRootPath();

    private static String resolveRootPath() {
        File workingDirectory = new File(System.getProperty("user.dir"));
        File filesDirectory = new File(workingDirectory, "files");
        if (filesDirectory.exists() || !"springboot".equalsIgnoreCase(workingDirectory.getName())) {
            return filesDirectory.getAbsolutePath();
        }

        File projectFilesDirectory = new File(workingDirectory.getParentFile(), "files");
        return projectFilesDirectory.getAbsolutePath();
    }

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("400", "アップロードファイルが空です");
        }
        String originalFilename = file.getOriginalFilename();
        String extName = FileUtil.extName(originalFilename);
        if (!FileUtil.exist(ROOT_PATH)) {
            FileUtil.mkdir(ROOT_PATH);
        }
        // 优化：采用时间戳+UUID生成唯一文件名，防止特殊字符注入与文件冲突
        String safeFileName = System.currentTimeMillis() + "_" + IdUtil.fastSimpleUUID() + (StrUtil.isNotBlank(extName) ? ("." + extName) : "");
        File saveFile = new File(ROOT_PATH + File.separator + safeFileName);
        file.transferTo(saveFile);
        String url = "http://" + ip + ":" + port + "/file/download/" + safeFileName;
        return Result.success(url);
    }

    /**
     * 下载文件
     */
    @AuthAccess
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) throws IOException {
        // 优化：路径穿越攻击防御，禁止包含 .. 或非法跳出 ROOT_PATH 目录
        if (StrUtil.isBlank(fileName) || fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        File targetFile = new File(ROOT_PATH, fileName);
        String canonicalBasePath = new File(ROOT_PATH).getCanonicalPath();
        String canonicalTargetPath = targetFile.getCanonicalPath();
        if (!canonicalTargetPath.startsWith(canonicalBasePath) || !targetFile.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.addHeader("Content-Disposition", "inline;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        byte[] bytes = FileUtil.readBytes(targetFile);
        ServletOutputStream outputStream = response.getOutputStream();
        try {
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 富文本上传图片
     */
    @PostMapping("/editor/upload")
    public Dict editorUpload(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Dict.create().set("errno", 1).set("message", "アップロードファイルが空です");
        }
        String originalFilename = file.getOriginalFilename();
        String extName = FileUtil.extName(originalFilename);
        if (!FileUtil.exist(ROOT_PATH)) {
            FileUtil.mkdir(ROOT_PATH);
        }
        String safeFileName = System.currentTimeMillis() + "_" + IdUtil.fastSimpleUUID() + (StrUtil.isNotBlank(extName) ? ("." + extName) : "");
        File saveFile = new File(ROOT_PATH + File.separator + safeFileName);
        file.transferTo(saveFile);
        String url = "http://" + ip + ":" + port + "/file/download/" + safeFileName;
        return Dict.create().set("errno", 0).set("data", CollUtil.newArrayList(Dict.create().set("url", url)));
    }
}
