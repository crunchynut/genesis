package com.uk.genesis.model;

/**
 * Exception used to indicate that a GenesisObject has failed validation.
 *
 * @author paul.jones
 * @author hussein.badakhchani
 */
public class GenesisObjectValidationException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 683473989795806548L;

    public GenesisObjectValidationException(final String message) {
        super(message);
    }

    public GenesisObjectValidationException(final String message,
                                            final Throwable cause) {
        super(message, cause);
    }
}
