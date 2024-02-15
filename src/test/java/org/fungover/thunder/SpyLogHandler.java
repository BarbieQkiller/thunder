package org.fungover.thunder;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * @author Angela Gustafsson, anggus-1
 */
public class SpyLogHandler extends Handler {
    private LogRecord lastRecord;
    @Override
    public void publish(LogRecord record) {
        lastRecord = record;
    }

    public LogRecord getLastRecord() {
        return lastRecord;
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {

    }
}
