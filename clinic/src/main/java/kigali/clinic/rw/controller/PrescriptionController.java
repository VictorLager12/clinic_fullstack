package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Prescription;
import kigali.clinic.rw.service.PrescriptionService;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestParam UUID appointmentId, @RequestBody Prescription prescription) {
        String result = prescriptionService.save(appointmentId, prescription);
        if (result.equals("Prescription saved successfully")) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
        if (result.contains("does not exist")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> byAppointment(@PathVariable UUID appointmentId) {
        Optional<List<Prescription>> result = prescriptionService.findByAppointment(appointmentId);
        if (result.isEmpty()) {
            return new ResponseEntity<>("The appointment with that id does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result.get());
    }

    @GetMapping("/patient/{ssNumber}")
    public ResponseEntity<List<Prescription>> byPatient(@PathVariable String ssNumber) {
        return ResponseEntity.ok(prescriptionService.findByPatientSsNumber(ssNumber));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(@PathVariable UUID id, @RequestBody Prescription prescription) {
        String result = prescriptionService.update(id, prescription);
        if (result.equals("Prescription updated successfully")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String result = prescriptionService.delete(id);
        if (result.equals("Prescription deleted successfully")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/stats/top-medicines")
    public ResponseEntity<List<Object[]>> topMedicines() {
        return ResponseEntity.ok(prescriptionService.topMedicines());
    }

}
