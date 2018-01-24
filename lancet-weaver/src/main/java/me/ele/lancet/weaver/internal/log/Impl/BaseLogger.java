package me.ele.lancet.weaver.internal.log.Impl;

import org.gradle.api.logging.LogLevel;

import java.io.PrintWriter;
import java.io.StringWriter;

import me.ele.lancet.weaver.internal.log.ILogger;

/**
 * Created by gengwanpeng on 17/5/17.
 */
public abstract class BaseLogger implements ILogger {

    @Override
    public void d(String tag, String msg) {
        write(LogLevel.DEBUG, tag, msg, null);
    }

    @Override
    public void i(String tag, String msg) {
        write(LogLevel.INFO, tag, msg, null);
    }

    @Override
    public void w(String tag, String msg) {
        w(tag, msg, null);
    }

    @Override
    public void w(String tag, String msg, Throwable t) {
        write(LogLevel.WARN, tag, msg, t);
    }

    @Override
    public void e(String tag, String msg) {
        e(tag, msg, null);
    }

    @Override
    public void e(String tag, String msg, Throwable t) {
        write(LogLevel.ERROR, tag, msg, t);
    }

    protected abstract void write(LogLevel level, String prefix, String msg, Throwable t);

    static String stackToString(Throwable t) {
        StringWriter sw = new StringWriter(128);
        PrintWriter ps = new PrintWriter(sw);
        t.printStackTrace(ps);
        ps.flush();
        return sw.toString();
    }
}
