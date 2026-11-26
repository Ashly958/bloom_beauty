package co.bloombeauty.exception;

/** 404 Not Found: el recurso solicitado no existe o está inactivo. */
public class RecursoNoEncontradoException extends BloomException {

    public RecursoNoEncontradoException(String entidad, Object id) {
        super("RECURSO_NO_ENCONTRADO", entidad + " con identificador " + id + " no existe");
    }
}
