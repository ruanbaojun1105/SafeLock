package com.hwx.safelock.safelock;

/**
 * Created by wanjian on 2017/2/15.
 */

final class QuitCockroachException extends RuntimeException {
    public QuitCockroachException(String message) {
        super(message);
    }
}