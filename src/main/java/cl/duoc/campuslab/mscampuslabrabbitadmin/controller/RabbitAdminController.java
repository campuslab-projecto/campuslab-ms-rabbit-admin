package cl.duoc.campuslab.mscampuslabrabbitadmin.controller;

import cl.duoc.campuslab.mscampuslabrabbitadmin.dto.BindingRequest;
import cl.duoc.campuslab.mscampuslabrabbitadmin.dto.ExchangeRequest;
import cl.duoc.campuslab.mscampuslabrabbitadmin.dto.QueueRequest;
import cl.duoc.campuslab.mscampuslabrabbitadmin.service.RabbitAdminService;
import cl.duoc.campuslab.mscampuslabrabbitadmin.service.RabbitManagementReadService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rabbit")
public class RabbitAdminController {

    private final RabbitAdminService rabbitAdminService;
    private final RabbitManagementReadService rabbitManagementReadService;

    public RabbitAdminController(
            RabbitAdminService rabbitAdminService,
            RabbitManagementReadService rabbitManagementReadService
    ) {
        this.rabbitAdminService = rabbitAdminService;
        this.rabbitManagementReadService = rabbitManagementReadService;
    }

    @GetMapping(value = "/queues", produces = "application/json")
    public ResponseEntity<String> listQueues() {
        return ResponseEntity.ok(rabbitManagementReadService.listQueues());
    }

    @GetMapping(value = "/exchanges", produces = "application/json")
    public ResponseEntity<String> listExchanges() {
        return ResponseEntity.ok(rabbitManagementReadService.listExchanges());
    }

    @GetMapping(value = "/bindings", produces = "application/json")
    public ResponseEntity<String> listBindings() {
        return ResponseEntity.ok(rabbitManagementReadService.listBindings());
    }

    @PostMapping("/queues")
    public ResponseEntity<String> createQueue(
            @Valid @RequestBody QueueRequest request
    ) {
        rabbitAdminService.createQueue(request);

        return ResponseEntity.ok(
                "Cola creada correctamente"
        );
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<String> deleteQueue(
            @PathVariable String name
    ) {
        boolean deleted =
                rabbitAdminService.deleteQueue(name);

        if (deleted) {
            return ResponseEntity.ok(
                    "Cola eliminada correctamente"
            );
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<String> createExchange(
            @Valid @RequestBody ExchangeRequest request
    ) {
        rabbitAdminService.createExchange(request);

        return ResponseEntity.ok(
                "Exchange creado correctamente"
        );
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<String> deleteExchange(
            @PathVariable String name
    ) {
        boolean deleted =
                rabbitAdminService.deleteExchange(name);

        if (deleted) {
            return ResponseEntity.ok(
                    "Exchange eliminado correctamente"
            );
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<String> createBinding(
            @Valid @RequestBody BindingRequest request
    ) {
        rabbitAdminService.createBinding(request);

        return ResponseEntity.ok(
                "Binding creado correctamente"
        );
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<String> deleteBinding(
            @Valid @RequestBody BindingRequest request
    ) {
        rabbitAdminService.deleteBinding(request);

        return ResponseEntity.ok(
                "Binding eliminado correctamente"
        );
    }
}