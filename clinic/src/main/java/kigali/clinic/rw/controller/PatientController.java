package kigali.clinic.rw.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.service.PatientService;
import kigali.clinic.rw.service.AppointmentService;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Patient patient) {
        Object result = patientService.create(patient);
        if (result instanceof String) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Patient>> findAll() {
        return ResponseEntity.ok(patientService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Patient> findById(@PathVariable UUID id) {
        Patient found = patientService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Patient patient) {
        Object result = patientService.update(id, patient);
        if (result instanceof String) {
            if (result.equals("Patient not found")) {
                return ResponseEntity.notFound().build();
            }
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = patientService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-last-name")
    public ResponseEntity<List<Patient>> byLastName(@RequestParam String lastName) {
        return ResponseEntity.ok(patientService.findByLastName(lastName));
    }

    @GetMapping("/of-doctor/{doctorId}")
    public ResponseEntity<?> ofDoctor(@PathVariable UUID doctorId) {
        Optional<List<Patient>> result = patientService.patientsOfDoctor(doctorId);
        if (result.isEmpty()) {
            return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result.get());
    }

    @GetMapping("/frequent")
    public ResponseEntity<List<Patient>> frequent(@RequestParam long min) {
        return ResponseEntity.ok(patientService.frequentPatients(min));
    }

    @GetMapping("/{ssNumber}/upcoming")
    public ResponseEntity<?> upcoming(@PathVariable String ssNumber) {
        Optional<List<Appointment>> result = appointmentService.upcomingForPatient(ssNumber);
        if (result.isEmpty()) {
            return new ResponseEntity<>("That patient with that ssnumber does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result.get());
    }
}