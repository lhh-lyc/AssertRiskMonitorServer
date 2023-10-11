package com.lhh.serverexport.utils;

import com.lhh.serverbase.common.constant.Const;
import com.lhh.serverbase.common.constant.JwtConst;
import com.lhh.serverbase.dto.FileInfoDTO;
import com.lhh.serverbase.utils.FileUtils;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException;
import org.apache.commons.math3.exception.InsufficientDataException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MinioUtils {

    @Value("${my-config.minio.endpoint}")
    private String endpoint;

    @Value("${my-config.minio.accesskey}")
    private String accessKey;

    @Value("${my-config.minio.secretkey}")
    private String secretKey;

    /**
     * 获取minio客户端
     *
     * @return
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     */
    private MinioClient getClient() throws InvalidPortException, InvalidEndpointException {
        MinioClient minioClient = new MinioClient(endpoint, accessKey, secretKey);
        minioClient.setTimeout(JwtConst.MINIO_EXPIRE_TIME, JwtConst.MINIO_EXPIRE_TIME, JwtConst.MINIO_EXPIRE_TIME);
        return minioClient;
    }

    /**
     * 判断bucket是否存在
     *
     * @param bucketName 文件桶
     * @return
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InsufficientDataException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws XmlPullParserException
     * @throws ErrorResponseException
     */
    public boolean bucketExists(String bucketName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException, io.minio.errors.InsufficientDataException, io.minio.errors.InternalException {
        return getClient().bucketExists(bucketName);
    }

    /**
     * 上传文件
     *
     * @param bucketName     文件桶
     * @param targetName     文件名称
     * @param stream         上传文件流
     * @param sourceFileName 源文件名称
     * @return
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws XmlPullParserException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     */
    public void uploadFile(String bucketName, String targetName, InputStream stream, String sourceFileName) throws InvalidPortException, InvalidEndpointException, IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, NoResponseException, InvalidBucketNameException, ErrorResponseException, InvalidResponseException, InternalException, InsufficientDataException, io.minio.errors.InternalException, io.minio.errors.InsufficientDataException, io.minio.errors.InvalidArgumentException {
        HashMap<String, String> map = new HashMap<>();
        map.put("sourceFileName", sourceFileName);
        MinioClient client = getClient();
        client.putObject(bucketName, targetName, stream, null, map, null, null);
    }

    /**
     * 删除文件
     *
     * @param bucketName 文件桶
     * @param targetName 文件名称
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InsufficientDataException
     * @throws InvalidArgumentException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws XmlPullParserException
     * @throws ErrorResponseException
     */
    public void deleteFile(String bucketName, String targetName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InternalException, InsufficientDataException, io.minio.errors.InternalException, io.minio.errors.InsufficientDataException, io.minio.errors.InvalidArgumentException {
        getClient().removeObject(bucketName, targetName);
    }

    /**
     * 批量删除
     *
     * @param bucketName  文件桶名称
     * @param targetNames 文件列表
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InsufficientDataException
     * @throws InvalidArgumentException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws XmlPullParserException
     * @throws ErrorResponseException
     */
    public Iterable<Result<DeleteError>> deleteFiles(String bucketName, List<String> targetNames) {
        Iterable<Result<DeleteError>> results = null;
        try {
            results = getClient().removeObjects(bucketName, targetNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 文件详细
     *
     * @param bucketName
     * @param targetName
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws XmlPullParserException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidArgumentException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     */
    public FileInfoDTO fileDetail(String bucketName, String targetName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InternalException, InsufficientDataException, io.minio.errors.InternalException, io.minio.errors.InsufficientDataException, io.minio.errors.InvalidArgumentException {
        ObjectStat objectStat = getClient().statObject(bucketName, targetName);
        FileInfoDTO fileInfoDTO = new FileInfoDTO();
        fileInfoDTO.setBucketName(objectStat.bucketName());
        fileInfoDTO.setFileName(objectStat.name());
        fileInfoDTO.setFileSize(FileUtils.FileSizeConvert(objectStat.length(), Const.STR_M));
        List<String> sourcefilenames = objectStat.httpHeaders().get("x-amz-meta-sourcefilename");
        if (sourcefilenames != null && sourcefilenames.size() > 0) {
            String sourcefilename = sourcefilenames.get(0);
            fileInfoDTO.setFileOrgName(URLDecoder.decode(sourcefilename, Const.STR_UTF8)

            );
        }
        return fileInfoDTO;
    }

    /**
     * 临时下载文件链接
     *
     * @param bucketName 文件桶
     * @param targetName 文件名
     * @param expires    过期时间 单位秒
     * @return
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InsufficientDataException
     * @throws InvalidExpiresRangeException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws XmlPullParserException
     * @throws ErrorResponseException
     */
    public String temporaryDownloadURL(String bucketName, String targetName, Integer expires) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidExpiresRangeException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException, io.minio.errors.InsufficientDataException, io.minio.errors.InternalException {
        return temporaryDownloadURL(bucketName, targetName, expires, null);
    }

    /**
     * 临时下载文件链接
     *
     * @param bucketName    文件桶
     * @param targetName    文件名
     * @param expires       过期时间 单位秒
     * @param requestHeader 请求头部
     * @return
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InsufficientDataException
     * @throws InvalidExpiresRangeException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws XmlPullParserException
     * @throws ErrorResponseException
     */
    public String temporaryDownloadURL(String bucketName, String targetName, Integer expires, Map<String, String> requestHeader) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidExpiresRangeException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException, io.minio.errors.InsufficientDataException, io.minio.errors.InternalException {
        return getClient().presignedGetObject(bucketName, targetName, expires, requestHeader);
    }

    /**
     * 创建bucket
     *
     * @param bucketName 文件桶名称
     * @throws InvalidPortException
     * @throws InvalidEndpointException
     * @throws IOException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws XmlPullParserException
     * @throws InternalException
     * @throws RegionConflictException
     */
    public void createBucket(String bucketName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, ErrorResponseException, NoResponseException, InvalidBucketNameException, XmlPullParserException, RegionConflictException, InvalidResponseException, InsufficientDataException, InternalException, io.minio.errors.InsufficientDataException, io.minio.errors.InternalException {
        getClient().makeBucket(bucketName);
    }

}
