package com.stasha.info.flex.parsers;

/**
 *
 * @author stasha
 */
public class ParseOptionsException extends RuntimeException {

    private String option;

    public ParseOptionsException(String option, String message) {
        super(message);
        this.option = option;
    }

    public String getOption() {
        return option;
    }

}
