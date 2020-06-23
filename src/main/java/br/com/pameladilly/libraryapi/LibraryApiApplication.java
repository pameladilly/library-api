package br.com.pameladilly.libraryapi;

import br.com.pameladilly.libraryapi.service.EmailService;
import ch.qos.logback.core.net.SyslogOutputStream;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class LibraryApiApplication {

	@Autowired
	private EmailService emailService;

	@Bean
	public ModelMapper modelMapper(){
		return new ModelMapper();
	}
/*
	@Bean
	public CommandLineRunner runner(){
		return args -> {
			List<String> emails = Arrays.asList("library-api-fd1aa5@inbox.mailtrap.io");
			emailService.sendMails("Testando serviço de emails", emails);
			System.out.println("EMAILS ENVIADOS");
		};
	}
*/
	public static void main(String[] args) {
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
