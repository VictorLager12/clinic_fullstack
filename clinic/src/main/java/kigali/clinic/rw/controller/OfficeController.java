package kigali.clinic.rw.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.service.OfficeService;

@RestController 
@RequestMapping (value="/api/offices")
public class OfficeController {

    @Autowired 
    private OfficeService offServe;

    @PostMapping(value = "/save")  
    public ResponseEntity<?> saveOffice(@Valid @RequestBody Office office){
        String returnedMessage = offServe.saveOffice(office);
        if (returnedMessage.equals("Office is saved successfully")) {
            return new ResponseEntity<>(returnedMessage, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(returnedMessage, HttpStatus.CONFLICT);
        }
    }

    @GetMapping
    public ResponseEntity<List<Office>> findAll() {
        return ResponseEntity.ok(offServe.findAll()); 
    }

    @GetMapping("/{id}")
    public ResponseEntity<Office> findById(@PathVariable UUID id) {
        Office found = offServe.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable UUID id, @Valid @RequestBody Office office) {
        String result = offServe.updateOffice(id, office);
        if (result.equals("Office not found")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        } else if (result.contains("already exists")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = offServe.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/busiest")
    public ResponseEntity<String> busiest() {
        return ResponseEntity.ok(offServe.busiestOffice());
    }

    @GetMapping("/free")
    public ResponseEntity<java.util.List<kigali.clinic.rw.domain.Office>> free() {
        return ResponseEntity.ok(offServe.freeOffices());
    }

}