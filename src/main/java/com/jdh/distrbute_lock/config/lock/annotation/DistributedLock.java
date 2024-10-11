package com.jdh.distrbute_lock.config.lock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 분산락을 위한 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    // 분산락 key
    String key();

    // 대기시간
    int waitTime() default 10;

    // 소유시간
    int leaseTime() default 5;

    // TimeUnit
    TimeUnit timeUnit() default TimeUnit.SECONDS;

}
