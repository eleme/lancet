//
//                            _ooOoo_  
//                           o8888888o  
//                           88" . "88  
//                           (| -_- |)  
//                            O\ = /O  
//                        ____/`---'\____  
//                      .   ' \\| |// `.  
//                       / \\||| : |||// \  
//                     / _||||| -:- |||||- \  
//                       | | \\\ - /// | |  
//                     | \_| ''\---/'' | |  
//                      \ .-\__ `-` ___/-. /  
//                   ___`. .' /--.--\ `. . __  
//                ."" '< `.___\_<|>_/___.' >'"".  
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |  
//                 \ \ `-. \_ __\ /__ _/ .-` / /  
//         ======`-.____`-.___\_____/___.-`____.-'======  
//                            `=---='  
//  
//         .............................................
package me.ele.lancet.weaver.internal.log.Impl;

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.PrintWriter;
import java.io.StringWriter;

import me.ele.lancet.weaver.internal.log.ILogger;

/**
 * Created by gengwanpeng on 16/7/6.
 */
public class SystemLoggerImpl implements ILogger {

    private final Logger logger = Logging.getLogger("fragarach");

    @Override
    public void d(String msg) {
        write("fragarach.debug", msg, null);
    }

    @Override
    public void i(String msg) {
        write("fragarach.info", msg, null);
    }

    @Override
    public void w(String msg) {
        w(msg, null);
    }

    @Override
    public void w(String msg, Throwable t) {
        write("fragarach.warn", msg, t);
    }

    @Override
    public void e(String msg) {
        e(msg, null);
    }

    @Override
    public void e(String msg, Throwable t) {
        write("fragarach.error", msg, t);
    }

    synchronized protected void write(String prefix, String msg, Throwable t) {
        logger.info(String.format("[%-10s] %s", prefix, msg));
        if (t != null) {
            logger.info(stackToString(t));
        }
    }

    private static String stackToString(Throwable t) {
        StringWriter sw = new StringWriter(128);
        PrintWriter ps = new PrintWriter(sw);
        t.printStackTrace(ps);
        ps.flush();
        return sw.toString();
    }
}
