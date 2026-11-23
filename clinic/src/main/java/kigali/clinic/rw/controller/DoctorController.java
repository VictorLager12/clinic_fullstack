package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.service.AppointmentService;
import kigali.clinic.rw.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Doctor doctor) {
        Object result = doctorService.create(doctor);
        if (result instanceof String) {
            String message = (String) result;
            if (message.contains("occupied")) {
                return new ResponseEntity<>(message, HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> findAll() {
        return ResponseEntity.ok(doctorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> findById(@PathVariable UUID id) {
        Doctor found = doctorService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Doctor doctor) {
        Object result = doctorService.update(id, doctor);
        if (result instanceof String) {
            String message = (String) result;
            if (message.contains("occupied")) {
                return new ResponseEntity<>(message, HttpStatus.CONFLICT);
            }
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        boolean deleted = doctorService.delete(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-specialization")
    public ResponseEntity<List<Doctor>> bySpecialization(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.bySpecialization(name));
    }

    @GetMapping("/without-office")
    public ResponseEntity<List<Doctor>> withoutOffice() {
        return ResponseEntity.ok(doctorService.withoutOffice());
    }

    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> addSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        String result = doctorService.addSpecialization(doctorId, specializationId);
        if (result.equals("Specialization added to the doctor")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("does not exist")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }

    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> removeSpecialization(@PathVariable UUID doctorId, @PathVariable UUID specializationId) {
        String result = doctorService.removeSpecialization(doctorId, specializationId);
        if (result.equals("Specialization removed from the doctor")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("does not exist")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }

    @PatchMapping("/{doctorId}/office")
    public ResponseEntity<String> changeOffice(@PathVariable UUID doctorId, @RequestParam int officeNumber) {
        String result = doctorService.changeOffice(doctorId, officeNumber);
        if (result.equals("Office changed successfully")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("does not exist")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> search(@RequestParam String name) {
        return ResponseEntity.ok(doctorService.search(name));
    }

    @GetMapping("/{doctorId}/agenda")
    public ResponseEntity<?> agenda(@PathVariable UUID doctorId, @RequestParam String date) {
        Optional<List<Appointment>> result = appointmentService.agenda(doctorId, LocalDate.parse(date));
        if (result.isEmpty()) {
            return new ResponseEntity<>("The doctor with that id does not exist", HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(result.get());
    }

    @GetMapping("/workload")
    public ResponseEntity<List<Object[]>> workload(@RequestParam String start, @RequestParam String end) {
        java.sql.Date startDate = java.sql.Date.valueOf(LocalDate.parse(start));
        java.sql.Date endDate = java.sql.Date.valueOf(LocalDate.parse(end));
        return ResponseEntity.ok(doctorService.workload(startDate, endDate));
    }
}