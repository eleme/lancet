package me.ele.lancet.weaver.internal.exception;

import java.io.IOException;

/**
 * Created by gengwanpeng on 17/5/4.
 */
public class LoadClassException extends RuntimeException{
    public LoadClassException(String message, Throwable cause) {
        super(message, cause);
    }
}
