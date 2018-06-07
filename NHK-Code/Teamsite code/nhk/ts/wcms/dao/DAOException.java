/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package nhk.ts.wcms.dao;

/**
 *
 * @author sbhojnag
 */
public class DAOException extends RuntimeException{
    public DAOException() {
        super();
    }
    public DAOException(String message) {
        super(message);

    }
    public DAOException(Throwable cause) {
        super(cause);

    }
    public DAOException(String message, Throwable cause) {
        super(message, cause);

    }
}