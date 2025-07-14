package ifmg.edu.projeto_locadora_veiculos.services.exceptions;

public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound() {
        super();
    }

    public ResourceNotFound(String msg) {
        super(msg);
    }
}
