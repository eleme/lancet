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

import org.gradle.api.logging.LogLevel;

import java.io.PrintStream;

/**
 * Created by gengwanpeng on 16/7/6.
 */
public class SystemOutputImpl extends BaseLogger {

    @Override
    protected void write(LogLevel level, String prefix, String msg, Throwable t) {
        PrintStream ps = System.out;
        if (level == LogLevel.WARN || level == LogLevel.ERROR) {
            ps = System.err;
        }
        ps.println((String.format("%s [%-10s] %s", level.name(), prefix, msg)));
        if (t != null) {
            ps.println(level.name() + " " + stackToString(t));
        }
    }
}
