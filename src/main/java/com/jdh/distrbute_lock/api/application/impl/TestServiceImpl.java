package com.jdh.distrbute_lock.api.application.impl;

import com.jdh.distrbute_lock.api.application.TestService;
import com.jdh.distrbute_lock.config.lock.annotation.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class TestServiceImpl implements TestService {

    // 테스트용 변수
    public int num = 1;

    // random
    private final Random random = new Random();

    @Override
    @DistributedLock(key = "#key", waitTime = 60, leaseTime = 10)
    public void test(String key) {
        // 0부터 500 사이의 랜덤한 정수 생성 (0 포함, 500 미포함)
        int randomNumber = random.nextInt(501);

        try {
            // num 출력
            log.info("test::{}", num);

            // 0.5초 이내 랜덤 대기
            Thread.sleep(randomNumber);

            // num + 1
            num++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
