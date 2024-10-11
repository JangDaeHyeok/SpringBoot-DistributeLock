package com.jdh.distrbute_lock.config.lock.aop;

import com.jdh.distrbute_lock.config.lock.annotation.DistributedLock;
import com.jdh.distrbute_lock.config.lock.exception.DistributedLockException;
import com.jdh.distrbute_lock.config.lock.transaction.RequireNewTransactionAspect;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    private final RequireNewTransactionAspect requireNewTransactionAspect;

    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    /**
     * `@DistributedLock` 어노테이션이 선언된 메소드를 포인트컷으로 설정
     *
     * @param distributedLock 분산락 처리를 위한 어노테이션
     */
    @Pointcut("@annotation(distributedLock)")
    public void pointCut(DistributedLock distributedLock) {
    }

    /**
     * 분산 락을 사용하여 메소드를 감싸는 Around 어드바이스
     *
     * @param pjp ProceedingJoinPoint, 원래의 메소드를 나타냄
     * @param distributedLock 분산락 어노테이션
     * @return 메소드 실행 결과
     * @throws Throwable 예외 처리
     */
    @Around(value = "pointCut(distributedLock)", argNames = "pjp,distributedLock")
    public Object around(ProceedingJoinPoint pjp, DistributedLock distributedLock) throws Throwable {
        // 메소드 정보
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();

        // 분산락 key
        String key = REDISSON_LOCK_PREFIX + distributedLock.key();

        // 분산락 시도
        RLock rLock = redissonClient.getLock(key);
        try {
            // 락 획득 시도
            boolean available = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                // 락 획득 실패 시 예외처리
                log.error("lock not available [method:{}] [key:{}]", method, key);
                throw new DistributedLockException(); // custom exception
            }

            // 분산락을 획득하면 새로운 트랜잭션을 시작하여 비즈니스 로직 실행
            return requireNewTransactionAspect.proceed(pjp);
        } catch (InterruptedException e) {
            // 락 획득 중 인터럽트 발생 시 처리
            log.error("lock interrupted [method:{}] [key:{}]", method, key, e);
            Thread.currentThread().interrupt(); // 현재 스레드의 인터럽트 상태 복구
            throw new DistributedLockException(); // custom exception
        } finally {
            // 분산락 해제
            if (rLock.isLocked() && rLock.isHeldByCurrentThread()) { // 락이 현재 스레드에 의해 획득되었는지 확인
                rLock.unlock(); // 락 해제
            }
        }
    }

}
