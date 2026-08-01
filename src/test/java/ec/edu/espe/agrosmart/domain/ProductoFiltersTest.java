package ec.edu.espe.agrosmart.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductoFiltersTest {

    @Test
    void isValid_conPrecioPositivoYCorreos_debeRetornarTrue() {
        // Arrange
        Producto producto = new Producto(
                1L,
                "Rosas Premium",
                "Flores",
                new BigDecimal("18.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertTrue(resultado);
    }

    @Test
    void isValid_conPrecioCero_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                2L,
                "Orquideas Promocionales",
                "Flores",
                BigDecimal.ZERO,
                List.of("ofertas@agrosmart.ec")
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void isValid_sinCorreos_debeRetornarFalse() {
        // Arrange
        Producto producto = new Producto(
                3L,
                "Hortensias Azules",
                "Flores",
                new BigDecimal("22.00"),
                List.of()
        );

        // Act
        boolean resultado = ProductoFilters.IS_VALID.test(producto);

        // Assert
        assertFalse(resultado);
    }

    @Test
    void aMayusculas_conProductoValido_debeCrearNuevaInstanciaConNombreEnMayusculas() {
        // Arrange
        Producto productoOriginal = new Producto(
                1L,
                "Rosas Premium",
                "Flores",
                new BigDecimal("18.50"),
                List.of("ventas@agrosmart.ec")
        );

        // Act
        Producto productoTransformado =
                ProductoFilters.A_MAYUSCULAS.apply(productoOriginal);

        // Assert
        assertNotSame(productoOriginal, productoTransformado);
        assertEquals("Rosas Premium", productoOriginal.getNombre());
        assertEquals("ROSAS PREMIUM", productoTransformado.getNombre());
        assertEquals(
                productoOriginal.getCorreosNotificacion(),
                productoTransformado.getCorreosNotificacion()
        );
    }
}
