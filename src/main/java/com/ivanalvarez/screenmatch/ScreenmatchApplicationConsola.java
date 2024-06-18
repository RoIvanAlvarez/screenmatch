//package com.ivanalvarez.screenmatch;
//
//import com.ivanalvarez.screenmatch.principal.Principal;
//import com.ivanalvarez.screenmatch.repository.SerieRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//
//@SpringBootApplication
//public class ScreenmatchApplicationConsola implements CommandLineRunner {
//	@Autowired
//	private SerieRepository repository;
//	public static void main(String[] args){
//
//		SpringApplication.run(ScreenmatchApplicationConsola.class, args);
//	}
//
//	@Override
//	public void run(String... args) throws Exception {
//		Principal principal = new Principal(repository);
//		principal.muestraMenu();
//
//	}
//}
