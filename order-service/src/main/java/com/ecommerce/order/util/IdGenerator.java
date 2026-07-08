package com.ecommerce.order.util;

import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {
    private static final long START_EPOCH = 1704067200000L; // 2024-01-01T00:00:00Z
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);

    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    private static final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);

    private static final long workerId;
    private static final long datacenterId;
    private static final AtomicLong lastTimestamp = new AtomicLong(-1L);
    private static final AtomicLong sequence = new AtomicLong(0L);

    static {
        long tempWorkerId = 0;
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            if (e != null && e.hasMoreElements()) {
                NetworkInterface ni = e.nextElement();
                byte[] mac = ni.getHardwareAddress();
                if (mac != null && mac.length >= 2) {
                    tempWorkerId = ((mac[mac.length - 1] & 0xFF) | ((mac[mac.length - 2] & 0xFF) << 8)) & MAX_WORKER_ID;
                }
            }
        } catch (Exception ex) {
            // Ignore
        }
        if (tempWorkerId == 0) {
            tempWorkerId = new SecureRandom().nextInt((int) MAX_WORKER_ID + 1);
        }
        workerId = tempWorkerId;
        datacenterId = new SecureRandom().nextInt((int) MAX_DATACENTER_ID + 1);
    }

    public static synchronized long nextId() {
        long timestamp = timeGen();
        long last = lastTimestamp.get();

        if (timestamp < last) {
            timestamp = tilNextMillis(last);
        }

        if (last == timestamp) {
            long seq = sequence.incrementAndGet() & SEQUENCE_MASK;
            if (seq == 0) {
                timestamp = tilNextMillis(last);
                sequence.set(0L);
            }
        } else {
            sequence.set(0L);
        }

        lastTimestamp.set(timestamp);

        return ((timestamp - START_EPOCH) << TIMESTAMP_LEFT_SHIFT)
                | (datacenterId << DATACENTER_ID_SHIFT)
                | (workerId << WORKER_ID_SHIFT)
                | sequence.get();
    }

    private static long tilNextMillis(long lastTimestampVal) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestampVal) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    private static long timeGen() {
        return System.currentTimeMillis();
    }
}
