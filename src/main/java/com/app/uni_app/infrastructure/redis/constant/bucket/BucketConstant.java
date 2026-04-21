package com.app.uni_app.infrastructure.redis.constant.bucket;

public class BucketConstant {

  public record BucketSign(BucketThreadType bucketThreadType, String UUID) {}

    public enum BucketThreadType{
        READ_THREAD ,
       WRITE_THREAD ;
    }


}
