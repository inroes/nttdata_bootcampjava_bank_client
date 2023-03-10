package com.bootcamp.java.client.web;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bootcamp.java.client.domain.Client;
import com.bootcamp.java.client.service.ClientService;
import com.bootcamp.java.client.web.mapper.ClientMapper;
import com.bootcamp.java.client.web.model.ClientModel;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/v1/client")
public class ClientController {
	@Value("${spring.application.name}")
	String name;
	
	@Value("${server.port}")
	String port;
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private ClientMapper clientMapper;
	
	
	@Operation(
            summary = "Get a list of clients",
            description = "Get a list of clients registered in the system",
            responses = {
            		@ApiResponse(responseCode = "200",
                    description = "The response for the client request")
            }
    )
	@GetMapping
	public Mono<ResponseEntity<Flux<ClientModel>>> getAll(){
		log.info("getAll executed");
		return Mono.just(ResponseEntity.ok()
			.body(clientService.findAll()
					.map(client -> clientMapper.entityToModel(client))));
	}
	
	
	@Operation(summary = "Funcionalidad de consulta de un cliente por ID")
	@ApiResponses(value= {
		@ApiResponse(responseCode = "200", description = "client found succesully.",
				content = { @Content(mediaType = "application/json",
				schema = @Schema(implementation = Client.class)) }),
		@ApiResponse(responseCode = "400", description = "ID not valid.",
			content = @Content),
		@ApiResponse(responseCode = "404", description = "Client not found.",
			content = @Content)
	})
	@GetMapping("/{id}")
	public Mono<ResponseEntity<ClientModel>> getById(@PathVariable String id){
		log.info("getById executed {}", id);
		Mono<Client> response = clientService.findById(id);
		return response
				.map(client -> clientMapper.entityToModel(client))
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
				
		
		//        .switchIfEmpty(Mono.error(new DataNotFoundException("The data you seek is not here."))); // NO FUNCIONA
		/*
		 return serverRequest.bodyToMono(RequestDTO.class)
                .map((request) -> searchLocations(request.searchFields, request.pageToken))
                .flatMap( t -> ServerResponse
                        .ok()
                        .body(t, ResponseDTO.class)
                )
                .switchIfEmpty(ServerResponse.notFound().build())
                ;
		 * */
	}
	
	@PostMapping
	public Mono<ResponseEntity<ClientModel>> create(@Valid @RequestBody ClientModel request){
		log.info("create executed {}", request);
		return clientService.create(clientMapper.modelToEntity(request))
				.map(client -> clientMapper.entityToModel(client))
				.flatMap(c ->
					Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", name,
							port, "client", c.getId())))
							.body(c)))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PutMapping("/{id}")
	public Mono<ResponseEntity<ClientModel>> updateById(@PathVariable String id, @Valid @RequestBody ClientModel request){
		log.info("updateById executed {}:{}", id, request);
		return clientService.update(id, clientMapper.modelToEntity(request))
				.map(client -> clientMapper.entityToModel(client))
				.flatMap(c ->
				Mono.just(ResponseEntity.created(URI.create(String.format("http://%s:%s/%s/%s", name,
						port, "client", c.getId())))
						.body(c)))
				.defaultIfEmpty(ResponseEntity.badRequest().build());
	}

	@DeleteMapping("/{id}")
	public Mono<ResponseEntity<Void>> deleteById(@PathVariable String id){
		log.info("deleteById executed {}", id);
		return clientService.delete(id)
				.map( r -> ResponseEntity.ok().<Void>build())
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}

	@GetMapping("/{identityDocumentNumber}/{identityDocumentType}")
	public Mono<ResponseEntity<ClientModel>> getByIdentityDocumentNumberAndIdentityDocumentType(@PathVariable String identityDocumentNumber, @PathVariable String identityDocumentType){
		log.info("getById executed {} {}", identityDocumentNumber, identityDocumentType);
		Mono<Client> response = clientService.findTopByIdentityDocumentNumberAndIdentityDocumentType(identityDocumentNumber, identityDocumentType);
		return response
				.map(client -> clientMapper.entityToModel(client))
				.map(ResponseEntity::ok)
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
}
