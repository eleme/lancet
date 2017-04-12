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
package me.ele.lancet.weaver.internal.log;


import me.ele.lancet.weaver.internal.log.Impl.SystemLoggerImpl;

/**
 * Created by gengwanpeng on 16/7/6.
 */
public class Log {
    private static ILogger logger = new SystemLoggerImpl();
    private static Level level = Level.INFO;

    public static void setLevel(Level l) {
        level = l;
    }

    public static void setImpl(ILogger l) {
        logger = l;
    }

    public static void d(String msg) {
        if (level.compareTo(Level.DEBUG) <= 0) {
            logger.d(msg);
        }
    }

    public static void i(String msg) {
        if (level.compareTo(Level.INFO) <= 0) {
            logger.i(msg);
        }
    }

    public static void w(String msg) {
        w(msg, null);
    }

    public static void w(String msg, Throwable t) {
        if (level.compareTo(Level.WARN) <= 0) {
            logger.w(msg, t);
        }
    }

    public static void e(String msg) {
        e(msg, null);
    }

    public static void e(String msg, Throwable t) {
        if (level.compareTo(Level.ERROR) <= 0) {
            logger.e(msg, t);
        }
    }

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }
}
