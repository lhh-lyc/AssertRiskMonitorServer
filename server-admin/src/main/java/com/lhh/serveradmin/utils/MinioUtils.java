package com.lhh.serveradmin.utils;

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
    public boolean bucketExists(String bucketName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException {
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
     * @throws InvalidArgumentException
     * @throws InternalException
     * @throws NoResponseException
     * @throws InvalidBucketNameException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     */
    public void uploadFile(String bucketName, String targetName, InputStream stream, String sourceFileName) throws InvalidPortException, InvalidEndpointException, IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, com.sun.javaws.exceptions.InvalidArgumentException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, org.apache.commons.math3.exception.InsufficientDataException, ErrorResponseException, InvalidResponseException, InternalException, InsufficientDataException, InvalidArgumentException {
        HashMap<String, String> map = new HashMap<>();
        map.put("sourceFileName", sourceFileName);
        MinioClient client = getClient();
        client.putObject(bucketName, targetName, stream, null, map, null, null);
    }

    /**
     * 获取文件访问链接
     *
     * @param bucketName 文件桶
     * @param targetName 文件名称
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
    public String getFileUrl(String bucketName, String targetName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException {
        return getClient().getObjectUrl(bucketName, targetName);
    }


    /**
     * 获取文件流
     *
     * @param bucketName 文件桶
     * @param targetName 文件名称
     * @return
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
    public InputStream getFile(String bucketName, String targetName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, com.sun.javaws.exceptions.InvalidArgumentException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InternalException, InsufficientDataException, InvalidArgumentException {
        return getClient().getObject(bucketName, targetName);
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
    public void deleteFile(String bucketName, String targetName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, com.sun.javaws.exceptions.InvalidArgumentException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InternalException, InsufficientDataException, InvalidArgumentException {
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
    public Iterable<Result<DeleteError>> deleteFiles(String bucketName, List<String> targetNames) throws InvalidPortException, InvalidEndpointException {
        Iterable<Result<DeleteError>> results = null;
        try {
            results = getClient().removeObjects(bucketName, targetNames);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 复制文件
     *
     * @param bucketName     现文件所在桶
     * @param targetName     现文件名称
     * @param destBucketName 复制目标文件所在桶
     * @param destTargetName 复制目标文件名称
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
    public void copyFile(String bucketName, String targetName, String destBucketName, String destTargetName) throws InvalidPortException, InvalidEndpointException, IOException, XmlPullParserException, NoSuchAlgorithmException, InvalidKeyException, com.sun.javaws.exceptions.InvalidArgumentException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, org.apache.commons.math3.exception.InsufficientDataException, ErrorResponseException, InvalidResponseException, InternalException, InsufficientDataException, InvalidArgumentException {
        getClient().copyObject(bucketName, targetName, destBucketName, destTargetName);
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
    public FileInfoDTO fileDetail(String bucketName, String targetName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, com.sun.javaws.exceptions.InvalidArgumentException, InternalException, InsufficientDataException, InvalidArgumentException {
        ObjectStat objectStat = getClient().statObject(bucketName, targetName);
        FileInfoDTO fileInfoDTO = new FileInfoDTO();
        fileInfoDTO.setBucketName(objectStat.bucketName());
        fileInfoDTO.setFileName(objectStat.name());
        fileInfoDTO.setFileSize(FileUtils.FileSizeConvert(objectStat.length(), Const.STR_K));
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
    public String temporaryDownloadURL(String bucketName, String targetName, Integer expires) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, InvalidExpiresRangeException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException {
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
    public String temporaryDownloadURL(String bucketName, String targetName, Integer expires, Map<String, String> requestHeader) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, InvalidExpiresRangeException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException {
        return getClient().presignedGetObject(bucketName, targetName, expires, requestHeader);
    }

    /**
     * 临时上传文件链接 注:生成的链接需要使用 HTTP PUT方式
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
    public String temporaryUploadURL(String bucketName, String targetName, Integer expires) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, InvalidExpiresRangeException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException {
        return getClient().presignedPutObject(bucketName, targetName, expires);
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
    public void createBucket(String bucketName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, ErrorResponseException, NoResponseException, InvalidBucketNameException, XmlPullParserException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, RegionConflictException, InvalidResponseException, InsufficientDataException, InternalException {
        getClient().makeBucket(bucketName);
    }

    /**
     * 现有的bucket列表
     *
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
    public List<Bucket> bucketList() throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException {
        return getClient().listBuckets();
    }

    /**
     * 删除bucket
     *
     * @param bucketName 文件桶名称
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
    public void deleteBucket(String bucketName) throws InvalidPortException, InvalidEndpointException, IOException, InvalidKeyException, NoSuchAlgorithmException, org.apache.commons.math3.exception.InsufficientDataException, jdk.nashorn.internal.runtime.regexp.joni.exception.InternalException, NoResponseException, InvalidBucketNameException, XmlPullParserException, ErrorResponseException, InvalidResponseException, InsufficientDataException, InternalException {
        getClient().removeBucket(bucketName);
    }
}
