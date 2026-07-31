package ec.edu.espe.agrosmart;

import ec.edu.espe.agrosmart.entity.ProductoEntity;
import ec.edu.espe.agrosmart.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.List;

@SpringBootApplication
public class AgrosmartApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgrosmartApplication.class, args);
    }

    @Bean
    CommandLineRunner sembrarProductos(ProductoRepository repository) {
        return args -> {

            if (repository.count() == 0) {

                List<ProductoEntity> productos = List.of(

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
                                new BigDecimal("0.00"),
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

                repository.saveAll(productos);
            }
        };
    }
}
