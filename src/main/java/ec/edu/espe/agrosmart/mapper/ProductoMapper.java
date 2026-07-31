package ec.edu.espe.agrosmart.mapper;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;

import java.util.Arrays;
import java.util.List;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static Producto toDominio(ProductoEntity entity) {

        String correosTexto = entity.getCorreosNotificacion();

        List<String> correos;

        if (correosTexto == null || correosTexto.isBlank()) {
            correos = List.of();
        } else {
            correos = Arrays.stream(correosTexto.split(","))
                    .map(String::trim)
                    .filter(correo -> !correo.isEmpty())
                    .toList();
        }

        return new Producto(
                entity.getIdProducto(),
                entity.getNombreProducto(),
                entity.getCategoria(),
                entity.getPrecioUsd(),
                correos
        );
    }
}
