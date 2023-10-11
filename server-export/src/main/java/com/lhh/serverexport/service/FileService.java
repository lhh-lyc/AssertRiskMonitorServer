package com.lhh.serverexport.service;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.exception.EmException;
import com.lhh.serverbase.dto.FileInfoDTO;
import com.lhh.serverbase.utils.FileUtils;
import com.lhh.serverbase.utils.UuidUtils;
import com.lhh.serverexport.utils.MinioUtils;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * minIO文件服务
 *
 * @author lyc
 */
@Slf4j
@Service("fileService")
public class FileService {

    @Autowired
    MinioUtils minioUtils;

//    @Autowired
//    private SysFilesDao sysFilesDao;

    /**
     * 文件上传
     *
     * @param bucketName  文件桶名称
     * @param inputStream 文件流
     * @param orgName     源文件名称
     * @param folder      文件路径
     * @return FileInfoDTO
     * @throws EmException
     */
    public FileInfoDTO uploadFile(String bucketName, InputStream inputStream, String orgName, String folder) throws EmException {
        FileInfoDTO uploadReturnDTO = new FileInfoDTO();
        // 原文件名称赋值
        uploadReturnDTO.setFileOrgName(orgName);
        try {
            if (!minioUtils.bucketExists(bucketName)) {
                minioUtils.createBucket(bucketName);
            }
            // bucketName赋值
            uploadReturnDTO.setBucketName(bucketName);
            // 文件类型赋值
            String fileSuffix = FileUtils.GetExtName(orgName);
            uploadReturnDTO.setFileType(fileSuffix);
            // 生成新文件名称
            StringBuilder targetNameBuilder = new StringBuilder();
            targetNameBuilder.append(folder);
            targetNameBuilder.append(Const.STR_SLASH);
            // uuid新文件名称
            targetNameBuilder.append(orgName);
//            targetNameBuilder.append(Const.STR_SPOT);
//            targetNameBuilder.append(fileSuffix);
            // 新文件名称赋值
            String newFileName = targetNameBuilder.toString();
            uploadReturnDTO.setFileName(newFileName);
            // 生产url bucketName+folder+filename
            targetNameBuilder.setLength(0);
            targetNameBuilder.append(bucketName);
            targetNameBuilder.append(Const.STR_SLASH);
            targetNameBuilder.append(newFileName);
            uploadReturnDTO.setFileUrl(newFileName);
            // 上传文件
            minioUtils.uploadFile(bucketName, newFileName, inputStream, URLEncoder.encode(orgName, Const.STR_UTF8));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new EmException(e.getMessage());
        }
        return uploadReturnDTO;
    }
    public String download(String bucketName, String targetName) {
        String url = Const.STR_EMPTY;
        try {
            url = minioUtils.temporaryDownloadURL(bucketName, targetName, 1000);
        } catch (InvalidPortException e) {
            e.printStackTrace();
        } catch (InvalidEndpointException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidExpiresRangeException e) {
            e.printStackTrace();
        } catch (NoResponseException e) {
            e.printStackTrace();
        } catch (InvalidBucketNameException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (ErrorResponseException e) {
            e.printStackTrace();
        } catch (InvalidResponseException e) {
            e.printStackTrace();
        } catch (InsufficientDataException e) {
            e.printStackTrace();
        } catch (InternalException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * 文件详细内容
     *
     * @param bucketName 文件桶名称
     * @param targetName 文件名称
     * @return
     * @throws EmException
     */
    public FileInfoDTO fileDetail(String bucketName, String targetName) throws EmException {
        try {
            if (!minioUtils.bucketExists(bucketName)) {
                throw new EmException("文件桶名称不存在");
            }
            return minioUtils.fileDetail(bucketName, targetName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmException(e.getMessage());
        }
    }

    /**
     * 删除文件(多个)
     *
     * @param fileList   文件集
     * @param bucketName 文件桶名称
     * @throws EmException
     */
    public void deleteFile(List<FileInfoDTO> fileList, String bucketName) {
        if (fileList != null || fileList.size() > 0) {
            if (fileList.size() == Const.INTEGER_1) {
//                sysFilesDao.deleteById(fileList.get(Const.INT_0).getFileId());
                doDeleteFile(fileList.get(Const.INTEGER_0).getFileName(), bucketName);
                return;
            }
//            sysFilesDao.deleteBatchIds(fileList.stream().map(e -> e.getFileId()).collect(Collectors.toList()));
            doDeleteFile(fileList.stream().map(e -> e.getFileName()).collect(Collectors.toList()), bucketName);
        }
    }

    /**
     * 执行删除文件
     *
     * @param bucketName     文件桶名称
     * @param targetNameList 文件名称List
     * @throws EmException
     */
    public void doDeleteFile(List<String> targetNameList, String bucketName) throws EmException {
        try {
            if (!minioUtils.bucketExists(bucketName)) {
                log.error("文件桶名称不存在");
                throw new EmException("文件桶名称不存在");
            }
            Iterable<Result<DeleteError>> results = minioUtils.deleteFiles(bucketName, targetNameList);
            for (Result<DeleteError> result : results) {
            }
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new EmException(e.getMessage());
        }
    }

    /**
     * 执行删除文件
     *
     * @param targetName 目标文件
     * @param bucketName 文件桶名称
     */
    private void doDeleteFile(String targetName, String bucketName) {
        try {
            if (!minioUtils.bucketExists(bucketName)) {
                log.error("文件桶名称不存在");
                throw new EmException("文件桶名称不存在");
            }
            minioUtils.deleteFile(bucketName, targetName);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new EmException(e.getMessage());
        }
    }

}
