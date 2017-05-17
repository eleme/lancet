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


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import me.ele.lancet.weaver.internal.log.ILogger;


/**
 * Created by gengwanpeng on 16/7/6.
 */
public class FileLoggerImpl extends BaseLogger {

    public static FileLoggerImpl of(String fileName) throws FileNotFoundException {
        PrintWriter pr = new PrintWriter(new FileOutputStream(fileName), true);
        return new FileLoggerImpl(pr);
    }

    private PrintWriter pr;

    private FileLoggerImpl(PrintWriter pr) {
        this.pr = pr;
    }

    @Override
    protected synchronized void write(String prefix, String msg, Throwable t) {
        pr.println(String.format("[%-10s] %s", prefix, msg));
        if (t != null) {
            t.printStackTrace(pr);
        }
    }
}
