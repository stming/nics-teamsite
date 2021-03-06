/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nhk.ts.wcms.common;

import org.apache.commons.logging.Log;

/**
 *
 * @author sbhojnag
 */
public class Logger {

    private Log mLogger;

    /**
     * Get the value of mLogger
     *
     * @return the value of mLogger
     */
    private Log getLogger() {
        return mLogger;
    }

    public Logger(Log mLogger) {
        this.mLogger = mLogger;
    }

    public void createLogDebug(String logMessage) {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug(logMessage);
        }
    }

    public void createLogDebug(String logMessage, Throwable e) {
        if (this.getLogger().isDebugEnabled()) {
            this.getLogger().debug(logMessage, e);
        }
    }

    public void createLogWarn(String logMessage, Throwable e) {
        if (this.getLogger().isWarnEnabled()) {
            this.getLogger().warn(logMessage, e);
        }
    }

    public void createLogError(String logMessage, Throwable e) throws RuntimeException {
        if (this.getLogger().isErrorEnabled()) {
            this.getLogger().error(logMessage, e);
        }
        throw new RuntimeException(logMessage, e);
    }

    public void createLogErrorWithoutThrowingException(String logMessage, Throwable e) {
        if (this.getLogger().isErrorEnabled()) {
            this.getLogger().error(logMessage, e);
        }
    }
}
