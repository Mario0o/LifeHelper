package com.yc.logging.logger;

import android.os.Process;
import androidx.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.Log;

import com.yc.logging.executor.BinaryExecutor;
import com.yc.logging.constant.Level;
import com.yc.logging.executor.LogbackExecutor;
import com.yc.logging.config.LoggerConfig;
import com.yc.logging.config.LoggerContext;
import com.yc.logging.log.BinaryLog;
import com.yc.logging.log.FormatLog;
import com.yc.logging.log.LongLog;
import com.yc.toolutils.AppStringUtils;
import com.yc.toolutils.ObjectsUtils;

import java.util.Date;
import java.util.Map;


@RestrictTo(RestrictTo.Scope.LIBRARY)
public class InternalLogger extends AbstractLogger {

    private final String mBuffer;

    //private int m FileLogLevel;
    //private int m LogcatLogLevel;
    private LoggerConfig mConfig;

    public InternalLogger(final String name, String buffer, LoggerConfig config) {
        super(name);
        mConfig = config;
        this.mBuffer = buffer;
    }

    @Override
    public boolean isTraceEnabled() {
        return Level.TRACE.level >= mConfig.getFileLogLevel().level || Level.TRACE.level >= mConfig
                .getLogcatLogLevel().level;
    }

    @Override
    public boolean isDebugEnabled() {
        return Level.DEBUG.level >= mConfig.getFileLogLevel().level || Level.TRACE.level >= mConfig
                .getLogcatLogLevel().level;
    }

    @Override
    public boolean isInfoEnabled() {
        return Level.INFO.level >= mConfig.getFileLogLevel().level || Level.TRACE.level >= mConfig
                .getLogcatLogLevel().level;
    }

    @Override
    public boolean isWarnEnabled() {
        return Level.WARN.level >= mConfig.getFileLogLevel().level || Level.TRACE.level >= mConfig
                .getLogcatLogLevel().level;
    }

    @Override
    public boolean isErrorEnabled() {
        return Level.ERROR.level >= mConfig.getFileLogLevel().level || Level.TRACE.level >= mConfig
                .getLogcatLogLevel().level;
    }

    @Override
    public void write(byte[] bytes) {
        ObjectsUtils.requireNonNull(bytes);
        if (bytes.length == 0) {
            return;
        }
        byte[] clone = bytes.clone();
        BinaryExecutor.getInstance(mBuffer).enqueue(new BinaryLog(clone));
    }

    @Override
    public void trace(String msg, Throwable t) {
        println(Level.TRACE, msg, t);
    }

    @Override
    public void debug(String msg, Throwable t) {
        println(Level.DEBUG, msg, t);
    }

    @Override
    public void info(String msg, Throwable t) {
        println(Level.INFO, msg, t);
    }

    @Override
    public void warn(String msg, Throwable t) {
        println(Level.WARN, msg, t);
    }

    @Override
    public void error(String msg, Throwable t) {
        println(Level.WARN, msg, t);
    }


    @Override
    public void trace(String msg, Object... args) {
        printf(Level.TRACE, msg, args);
    }

    @Override
    public void debug(String msg, Object... args) {
        printf(Level.DEBUG, msg, args);
    }

    @Override
    public void info(String msg, Object... args) {
        printf(Level.INFO, msg, args);
    }

    @Override
    public void warn(String msg, Object... args) {
        printf(Level.WARN, msg, args);
    }

    @Override
    public void error(String msg, Object... args) {
        printf(Level.ERROR, msg, args);
    }

    @Override
    public void traceEvent(String event, Map<?, ?> map) {
        println(Level.TRACE, event, mapCopy(map));
    }

    @Override
    public void debugEvent(String event, Map<?, ?> map) {
        println(Level.DEBUG, event, mapCopy(map));
    }

    @Override
    public void infoEvent(String event, Map<?, ?> map) {
        println(Level.INFO, event, mapCopy(map));
    }

    @Override
    public void warnEvent(String event, Map<?, ?> map) {
        println(Level.WARN, event, mapCopy(map));
    }

    @Override
    public void errorEvent(String event, Map<?, ?> map) {
        println(Level.ERROR, event, mapCopy(map));
    }

    @Override
    public void traceEvent(String event, Object... args) {
        println(Level.TRACE, event, toMap(args));
    }

    @Override
    public void debugEvent(String event, Object... args) {
        println(Level.DEBUG, event, toMap(args));
    }

    @Override
    public void infoEvent(String event, Object... args) {
        println(Level.INFO, event, toMap(args));
    }

    @Override
    public void warnEvent(String event, Object... args) {
        println(Level.WARN, event, toMap(args));
    }

    @Override
    public void errorEvent(String event, Object... args) {
        println(Level.ERROR, event, toMap(args));
    }

    private void printf(Level level, String msg, Object... args) {
        if (!LoggerContext.getDefault().isInitial()) {
            return;
        }
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (level.level < mConfig.getFileLogLevel().level && level.level < mConfig.getLogcatLogLevel().level) {
            return;
        }
        LongLog longLog = new LongLog.Builder()
                .setLogLevel(level)
                .setArgs(args)
                .setMsg(msg)
                .setDate(new Date())
                .setTag(mName)
                .setTid(Process.myTid())
                .setTnm(Thread.currentThread().getName())
                .build();

        LogbackExecutor.getInstance(mBuffer).enqueue(longLog);
    }

    /**
     * An overloaded version of  println(final Level level, final String msg,Throwable throwable)
     *
     * @param level
     * @param msg
     */
    private void println(final Level level, final String msg) {
        println(level, msg, (Throwable) null);
    }

    /**
     * Println log message with formatForFileName: datetime [threadname] level tag - message
     *
     * @param level
     * @param msg
     */
    private void println(final Level level, String msg, Throwable throwable) {
        if (!LoggerContext.getDefault().isInitial()) {
            return;
        }
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        if (level.level < mConfig.getFileLogLevel().level && level.level < mConfig.getLogcatLogLevel().level) {
            return;
        }
        if (throwable != null) {
            msg += "\n" + Log.getStackTraceString(throwable);
        }
        LongLog longLog = new LongLog.Builder()
                .setLogLevel(level)
                .setDate(new Date())
                .setTag(mName)
                .setMsg(msg)
                .setTid(Process.myTid())
                .setTnm(AppStringUtils.ellipsize(Thread.currentThread().getName(), 20, 4))
                .build();
        LogbackExecutor.getInstance(mBuffer).enqueue(longLog);
    }

    @Override
    public void println(String msg) {
        if (TextUtils.isEmpty(msg)) {
            return;
        }
        LongLog log = new LongLog.Builder()
                .setTag("logging")
                .setLogLevel(Level.INFO)
                .setMsg(msg)
                .setFormat(false)
                .build();
        LogbackExecutor.getInstance(mBuffer).enqueue(log);
    }

    private void println(final Level level, final String event, final Map<?, ?> map) {
        if (level.level < mConfig.getFileLogLevel().level && level.level < mConfig.getLogcatLogLevel().level) {
            return;
        }
        LogbackExecutor.getInstance(mBuffer).enqueue(new FormatLog(this, level, event, map));
    }

}
