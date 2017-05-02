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
public class SystemOutputImpl implements ILogger {

    private final Logger logger = Logging.getLogger("lancet");

    @Override
    public void d(String tag,String msg) {
        write("D "+tag, msg, null);
    }

    @Override
    public void i(String tag,String msg) {
        write("I "+tag, msg, null);
    }

    @Override
    public void w(String tag,String msg) {
        w(tag,msg, null);
    }

    @Override
    public void w(String tag,String msg, Throwable t) {
        write("W "+tag, msg, t);
    }

    @Override
    public void e(String tag,String msg) {
        e(tag,msg, null);
    }

    @Override
    public void e(String tag,String msg, Throwable t) {
        write("E "+tag, msg, t);
    }

    synchronized protected void write(String prefix, String msg, Throwable t) {
        System.out.println((String.format("[%-10s] %s", prefix, msg)));
        if (t != null) {
            System.out.println((stackToString(t)));
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
