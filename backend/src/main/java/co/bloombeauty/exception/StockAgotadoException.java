package co.bloombeauty.exception;

/** 409 "Stock agotado" (HU-01 E-2 / RNF-02). */
public class StockAgotadoException extends ConflictoException {

    public StockAgotadoException(String producto, int disponible, int solicitado) {
        super("STOCK_AGOTADO",
              "Stock agotado para '" + producto + "': disponible " + disponible + ", solicitado " + solicitado);
    }
}
