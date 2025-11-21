package br.com.financetrackhub;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StartUp {

	public static void main(String[] args) {
		// Carrega variáveis do arquivo .env antes de iniciar a aplicação
		Dotenv dotenv = Dotenv.configure()
				.ignoreIfMissing() // Não falha se o arquivo .env não existir
				.load();
		
		// Define as variáveis de ambiente do sistema a partir do .env
		dotenv.entries().forEach(entry -> {
			if (System.getProperty(entry.getKey()) == null) {
				System.setProperty(entry.getKey(), entry.getValue());
			}
		});
		
		SpringApplication.run(StartUp.class, args);
	}

}
