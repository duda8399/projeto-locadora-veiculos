package ifmg.edu.projeto_locadora_veiculos.services.exceptions;

public class DatabaseException extends RuntimeException {
    public DatabaseException() {}

    public DatabaseException(String message) {
        super(message);
    }
}
