package ec.edu.espe.agrosmart.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class PublicidadServiceTest {

    @Test
    void generarPublicidad_cuandoProveedorResponde_debeEmitirTextoGenerado() {
        // Arrange
        AgroSmartAIService aiService =
                Mockito.mock(AgroSmartAIService.class);

        String publicidadEsperada =
                "Rosas premium para floristerías que buscan elegancia.";

        Mockito.when(
                aiService.generarPublicidad(
                        "Rosas Premium",
                        "floristerías premium"
                )
        ).thenReturn(publicidadEsperada);

        PublicidadService service =
                new PublicidadService(aiService);

        // Act
        Mono<String> resultado = service.generarPublicidad(
                "Rosas Premium",
                "floristerías premium"
        );

        // Assert
        StepVerifier.create(resultado)
                .expectNext(publicidadEsperada)
                .verifyComplete();
    }

    @Test
    void generarPublicidad_cuandoProveedorFalla_debeEmitirMensajeDeRespaldo() {
        // Arrange
        AgroSmartAIService aiService =
                Mockito.mock(AgroSmartAIService.class);

        Mockito.when(
                aiService.generarPublicidad(
                        Mockito.anyString(),
                        Mockito.anyString()
                )
        ).thenThrow(
                new RuntimeException("429 Too Many Requests")
        );

        PublicidadService service =
                new PublicidadService(aiService);

        // Act
        Mono<String> resultado = service.generarPublicidad(
                "Rosas Premium",
                "floristerías premium"
        );

        // Assert
        StepVerifier.create(resultado)
                .expectNextMatches(texto ->
                        texto.contains("Publicidad no disponible")
                                && texto.contains("RuntimeException")
                )
                .verifyComplete();
    }
}
