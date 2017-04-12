
package me.ele.lancet.plugin

import com.google.common.base.Strings
import me.ele.lancet.weaver.internal.log.Log

class LancetExtension {
    private Log.Level level = Log.Level.INFO
    private String fileName = null

    void logLevel(Log.Level level) {
        Objects.requireNonNull(level, "Log.Level is null")
        this.level = level
    }

    void logLevel(int level) {
        this.level = Log.Level.values()[level]
    }

    void logLevel(String logStr) {
        logLevel strToLog(logStr)
    }

    private static Log.Level strToLog(String logStr) {
        logStr = logStr.toLowerCase()
        if (logStr.equals('d') || logStr.equals('debug')) {
            Log.Level.DEBUG
        } else if (logStr.equals('i') || logStr.equals('info')) {
            Log.Level.INFO
        } else if (logStr.equals('w') || logStr.equals('warn')) {
            Log.Level.WARN
        }else if(logStr.equals('e') || logStr.equals('error')){
            Log.Level.ERROR
        }else{
            throw new IllegalArgumentException('wrong log string: ' + logStr)
        }
    }

    void useFileLog(String fileName) {
        if (Strings.isNullOrEmpty(fileName)) {
            throw new IllegalArgumentException("File name is illegal: " + fileName)
        }
        this.fileName = fileName
    }

    public Log.Level getLogLevel() {
        level
    }

    public String getFileName() {
        fileName
    }
}