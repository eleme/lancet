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


import java.util.HashMap;

import me.ele.lancet.weaver.internal.log.Impl.SystemLoggerImpl;

/**
 * Created by gengwanpeng on 16/7/6.
 */
public class Log {
    private static ILogger logger = new SystemLoggerImpl();
    private static Level level = Level.INFO;
    public static final String DEFAULT_TAG = "Lancet";

    public static void setLevel(Level l) {
        level = l;
    }

    public static void setImpl(ILogger l) {
        logger = l;
    }

    public static Tag tag(String tag) {
        return new Tag(tag);
    }

    public static void d(String msg) {
        tag(DEFAULT_TAG).d(msg);
    }

    public static void i(String msg) {
        tag(DEFAULT_TAG).i(msg);
    }

    public static void w(String msg) {
        w(msg, null);
    }

    public static void w(String msg, Throwable t) {
        tag(DEFAULT_TAG).w(msg, t);
    }

    public static void e(String msg) {
        e(msg, null);
    }

    public static void e(String msg, Throwable t) {
        tag(DEFAULT_TAG).e(msg, t);
    }

    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }

    public static class Tag {
        private final String tag;

        Tag(String tag) {
            this.tag = tag;
        }

        public Tag d(String msg) {
            if (level.compareTo(Level.DEBUG) <= 0) {
                logger.d(tag, msg);
            }
            return this;
        }

        public Tag i(String msg) {
            if (level.compareTo(Level.INFO) <= 0) {
                logger.i(tag, msg);
            }
            return this;
        }

        public Tag w(String msg) {
            return w(msg, null);
        }

        public Tag w(String msg, Throwable t) {
            if (level.compareTo(Level.WARN) <= 0) {
                logger.w(tag, msg, t);
            }
            return this;
        }

        public Tag e(String msg) {
            return e(msg, null);
        }

        public Tag e(String msg, Throwable t) {
            if (level.compareTo(Level.ERROR) <= 0) {
                logger.e(tag, msg, t);
            }
            return this;
        }
    }
}
