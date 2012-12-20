package service;

import com.sun.xml.wss.impl.callback.PasswordValidationCallback;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback.DerivedKeyPasswordRequest;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback.PasswordValidationException;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback.PlainTextPasswordRequest;
import com.sun.xml.wss.impl.callback.PasswordValidationCallback.Request;

public class DerivedKeyPasswordValidator extends PasswordValidationCallback.DerivedKeyPasswordValidator {
    
    public DerivedKeyPasswordValidator() {
    }
    
    @Override
    public void setPassword(Request request) {
        if (request instanceof DerivedKeyPasswordRequest) {
            if ("alice".equals(((DerivedKeyPasswordRequest) request).getUsername())) {
                ((DerivedKeyPasswordRequest) request).setPassword("clarinet");
            } else if ("bob".equals(((DerivedKeyPasswordRequest) request).getUsername())) {
                ((DerivedKeyPasswordRequest) request).setPassword("trombone");
            }
        }
    }

    /* Seemingly never called; hardcoded to return false for safety */
    @Override
    public boolean validate(Request request) throws PasswordValidationException {
        return false;
    }
}

