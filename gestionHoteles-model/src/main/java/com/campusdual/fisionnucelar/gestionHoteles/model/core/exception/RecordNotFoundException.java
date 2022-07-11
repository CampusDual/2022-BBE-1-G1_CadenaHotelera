package com.campusdual.fisionnucelar.gestionHoteles.model.core.exception;

public class RecordNotFoundException extends RuntimeException {
    public RecordNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
