package ec.edu.espe.agrosmart.service;

import ec.edu.espe.agrosmart.domain.Producto;
import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.exception.ProductoNoEncontradoException;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

class ProductoServiceTest {

    @Test
    void obtenerProductosComercializables_conTresValidosYDosInvalidos_debeEmitirTres() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);

        Mockito.when(repository.findAll())
                .thenReturn(datosDePrueba());

        ProductoService service = new ProductoService(repository);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextCount(3)
                .verifyComplete();
    }

    @Test
    void obtenerProductosComercializables_conTodosInvalidos_debeEmitirProductoGenerico() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);

        List<ProductoEntity> productosInvalidos = List.of(
                new ProductoEntity(
                        "Producto sin precio",
                        BigDecimal.ZERO,
                        100,
                        "Flores",
                        "ventas@agrosmart.ec"
                ),
                new ProductoEntity(
                        "Producto sin correos",
                        new BigDecimal("15.00"),
                        100,
                        "Flores",
                        ""
                )
        );

        Mockito.when(repository.findAll())
                .thenReturn(productosInvalidos);

        ProductoService service = new ProductoService(repository);

        // Act
        Flux<Producto> flujo = service.obtenerProductosComercializables();

        // Assert
        StepVerifier.create(flujo)
                .expectNextMatches(producto ->
                        producto.getId().equals(0L)
                                && producto.getNombre().equals("PRODUCTO GENERICO")
                )
                .verifyComplete();
    }

    @Test
    void buscarPorId_conIdInexistente_debeTerminarConProductoNoEncontradoException() {
        // Arrange
        ProductoRepository repository = Mockito.mock(ProductoRepository.class);
        Long idInexistente = 9999L;

        Mockito.when(repository.findById(idInexistente))
                .thenReturn(Optional.empty());

        ProductoService service = new ProductoService(repository);

        // Act
        Mono<Producto> resultado = service.buscarPorId(idInexistente);

        // Assert
        StepVerifier.create(resultado)
                .expectError(ProductoNoEncontradoException.class)
                .verify();
    }

    private List<ProductoEntity> datosDePrueba() {
        return List.of(
                new ProductoEntity(
                        "Rosas Premium",
                        new BigDecimal("18.50"),
                        400,
                        "Flores",
                        "ventas@agrosmart.ec"
                ),
                new ProductoEntity(
                        "Gypsophila Exportacion",
                        new BigDecimal("12.75"),
                        280,
                        "Flores",
                        "pedidos@agrosmart.ec"
                ),
                new ProductoEntity(
                        "Claveles Selectos",
                        new BigDecimal("9.90"),
                        500,
                        "Flores",
                        "comercial@agrosmart.ec,exportaciones@agrosmart.ec"
                ),
                new ProductoEntity(
                        "Orquideas Promocionales",
                        BigDecimal.ZERO,
                        80,
                        "Flores",
                        "ofertas@agrosmart.ec"
                ),
                new ProductoEntity(
                        "Hortensias Azules",
                        new BigDecimal("22.00"),
                        120,
                        "Flores",
                        ""
                )
        );
    }
}