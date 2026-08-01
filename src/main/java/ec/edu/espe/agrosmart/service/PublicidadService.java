package ec.edu.espe.agrosmart.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class PublicidadService {

    private final AgroSmartAIService aiService;

    public PublicidadService(AgroSmartAIService aiService) {
        this.aiService = aiService;
    }

    public Mono<String> generarPublicidad(String producto, String audiencia) {

        // La llamada de LangChain4j realiza una petición HTTP bloqueante.
        // fromCallable difiere su ejecución hasta que exista una suscripción.
        return Mono.fromCallable(
                        () -> aiService.generarPublicidad(producto, audiencia)
                )

                // La llamada al proveedor se ejecuta fuera del event loop de Netty.
                .subscribeOn(Schedulers.boundedElastic())

                // Evita que el flujo espere indefinidamente si el proveedor
                // no responde dentro del tiempo establecido.
                .timeout(Duration.ofSeconds(30))

                // Si ocurre un timeout, error de red o límite de cuota,
                // se emite un texto de respaldo en lugar de propagar el error.
                .onErrorResume(error -> Mono.just(
                        "Publicidad no disponible en este momento ("
                                + error.getClass().getSimpleName()
                                + ")"
                ));
    }
}
