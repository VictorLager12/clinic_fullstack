package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.service.SpecializationService;

@RestController
@RequestMapping("/api/specializations")
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @PostMapping
    public ResponseEntity<Specialization> create(@RequestBody Specialization specialization) {
        Specialization saved = specializationService.create(specialization);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Specialization>> findAll() {
        return ResponseEntity.ok(specializationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Specialization> findById(@PathVariable UUID id) {
        Specialization found = specializationService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Specialization> update(@PathVariable UUID id, @RequestBody Specialization specialization) {
        Specialization updated = specializationService.update(id, specialization);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = specializationService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{specializationId}/doctors/{doctorId}")
    public ResponseEntity<?> assignDoctor(@PathVariable UUID specializationId, @PathVariable UUID doctorId) {
        Object result = specializationService.assignDoctor(specializationId, doctorId);
        if (result instanceof String) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/unused")
    public ResponseEntity<List<Specialization>> unused() {
        return ResponseEntity.ok(specializationService.unused());
    }
}