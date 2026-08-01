package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductoTest {

    @Test
    void getters_conValoresDelConstructor_debenRetornarLosMismosValores() {
        // Arrange
        List<String> correos = List.of(
                "ventas@agrosmart.ec",
                "exportaciones@agrosmart.ec"
        );

        Producto producto = new Producto(
                1L,
                "Rosas Premium",
                "Flores",
                new BigDecimal("18.50"),
                correos
        );

        // Act
        Long idObtenido = producto.getId();
        String nombreObtenido = producto.getNombre();
        String categoriaObtenida = producto.getCategoria();
        BigDecimal precioObtenido = producto.getPrecioUsd();
        List<String> correosObtenidos = producto.getCorreosNotificacion();

        // Assert
        assertEquals(1L, idObtenido);
        assertEquals("Rosas Premium", nombreObtenido);
        assertEquals("Flores", categoriaObtenida);
        assertEquals(new BigDecimal("18.50"), precioObtenido);
        assertEquals(correos, correosObtenidos);
    }

    @Test
    void constructor_alModificarListaOriginal_noDebeAfectarAlProducto() {
        // Arrange
        List<String> correosOriginales = new ArrayList<>();
        correosOriginales.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Rosas Premium",
                "Flores",
                new BigDecimal("18.50"),
                correosOriginales
        );

        // Act
        correosOriginales.add("intruso@correo.com");

        // Assert
        assertEquals(1, producto.getCorreosNotificacion().size());
        assertEquals(
                "ventas@agrosmart.ec",
                producto.getCorreosNotificacion().getFirst()
        );
    }

    @Test
    void getCorreosNotificacion_alIntentarModificarResultado_debeProtegerEstadoInterno() {
        // Arrange
        List<String> correosOriginales = new ArrayList<>();
        correosOriginales.add("ventas@agrosmart.ec");

        Producto producto = new Producto(
                1L,
                "Rosas Premium",
                "Flores",
                new BigDecimal("18.50"),
                correosOriginales
        );

        // Act
        List<String> correosDevueltos = producto.getCorreosNotificacion();

        // Assert
        assertNotSame(correosOriginales, correosDevueltos);

        assertThrows(
                UnsupportedOperationException.class,
                () -> correosDevueltos.add("intruso@correo.com")
        );

        assertEquals(1, producto.getCorreosNotificacion().size());
    }
}
