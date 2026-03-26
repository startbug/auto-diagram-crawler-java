package com.exportbot.crawler.delivery.plugins;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectRequest;
import com.exportbot.crawler.delivery.DeliveryPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;

public class AliyunOssPlugin implements DeliveryPlugin {

    private static final Logger logger = LoggerFactory.getLogger(AliyunOssPlugin.class);

    private OSS ossClient;
    private String bucket;
    private String prefix;
    private String endpoint;

    @Override
    public String getName() {
        return "aliyun-oss";
    }

    @Override
    public void initialize(Map<String, Object> options) {
        String region = getString(options, "region");
        String accessKeyId = getString(options, "accessKeyId");
        String accessKeySecret = getString(options, "accessKeySecret");
        this.bucket = getString(options, "bucket");
        this.prefix = getString(options, "prefix", "exports/{{date}}/");
        this.endpoint = "https://oss-" + region + ".aliyuncs.com";

        this.ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        logger.info("Initialized Aliyun OSS client for bucket: {}", bucket);
    }

    @Override
    public DeliveryResult deliver(List<String> files, DeliveryMetadata metadata) {
        try {
            String datePrefix = prefix.replace("{{date}}", metadata.timestamp());
            StringBuilder urls = new StringBuilder();

            for (String filePath : files) {
                File file = new File(filePath);
                if (!file.exists()) continue;

                String objectKey = datePrefix + file.getName();
                PutObjectRequest request = new PutObjectRequest(bucket, objectKey, file);
                ossClient.putObject(request);

                String url = "https://" + bucket + "." + endpoint.replace("https://", "") + "/" + objectKey;
                urls.append(url).append("\n");
                logger.info("Uploaded to OSS: {}", objectKey);
            }

            return new DeliveryResult(true, getName(), "Files uploaded successfully", urls.toString());
        } catch (Exception e) {
            logger.error("Failed to upload to OSS", e);
            return new DeliveryResult(false, getName(), e.getMessage(), null);
        }
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        Object value = map.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
