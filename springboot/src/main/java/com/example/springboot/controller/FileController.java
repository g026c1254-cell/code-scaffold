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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.file.Files;

/**
 * 文件接口
 */
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
        // 1. 优先检查当前目录下的 files 文件夹
        File currentFiles = new File(workingDirectory, "files");
        if (currentFiles.exists()) {
            return currentFiles.getAbsolutePath();
        }
        // 2. 若在子工程目录（如 springboot）下启动，检查父级工程的 files 文件夹
        if (workingDirectory.getParentFile() != null) {
            File parentFiles = new File(workingDirectory.getParentFile(), "files");
            if (parentFiles.exists()) {
                return parentFiles.getAbsolutePath();
            }
        }
        // 3. 若均不存在，在 springboot 子目录启动时默认采用父工程 files 目录
        if ("springboot".equalsIgnoreCase(workingDirectory.getName()) && workingDirectory.getParentFile() != null) {
            return new File(workingDirectory.getParentFile(), "files").getAbsolutePath();
        }
        return currentFiles.getAbsolutePath();
    }

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

        // 设置准确的 Content-Type，确保浏览器能正确渲染各类图片格式（webp、png、jpg、gif、svg等）
        String mimeType = null;
        try {
            mimeType = Files.probeContentType(targetFile.toPath());
        } catch (Exception ignored) {}
        if (StrUtil.isBlank(mimeType)) {
            mimeType = URLConnection.guessContentTypeFromName(fileName);
        }
        if (StrUtil.isBlank(mimeType)) {
            String ext = FileUtil.extName(fileName);
            if ("webp".equalsIgnoreCase(ext)) {
                mimeType = "image/webp";
            } else if ("png".equalsIgnoreCase(ext)) {
                mimeType = "image/png";
            } else if ("jpg".equalsIgnoreCase(ext) || "jpeg".equalsIgnoreCase(ext)) {
                mimeType = "image/jpeg";
            } else if ("gif".equalsIgnoreCase(ext)) {
                mimeType = "image/gif";
            } else if ("svg".equalsIgnoreCase(ext)) {
                mimeType = "image/svg+xml";
            }
        }
        if (StrUtil.isNotBlank(mimeType)) {
            response.setContentType(mimeType);
        }

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
     * 富文本上传图片（适配 WangEditor v5 标准数据格式）
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
        // 同时提供 object 与 url，完美适配各类前端编辑器解析
        return Dict.create().set("errno", 0)
                .set("data", Dict.create().set("url", url).set("alt", originalFilename).set("href", url))
                .set("url", url);
    }
}
