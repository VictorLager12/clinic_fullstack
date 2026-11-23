package kigali.clinic.rw.controller;

import java.time.LocalDate;
import java.sql.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestBody Appointment appointment) {
        String result = appointmentService.saveAppointment(appointment);
        if (result.equals("Appointment saved successfully")) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        }
        if (result.equals("Doctor is already booked on that date")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @GetMapping
    public ResponseEntity<List<Appointment>> findAll() {
        return ResponseEntity.ok(appointmentService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> findById(@PathVariable UUID id) {
        Appointment found = appointmentService.findById(id);
        if (found == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(found);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Appointment appointment) {
        Object result = appointmentService.update(id, appointment);
        if (result instanceof String) {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String result = appointmentService.delete(id);
        if (result.equals("Appointment deleted successfully")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("does not exist")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.CONFLICT);
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<Appointment>> byStatus(@RequestParam AppointmentStatus status) {
        return ResponseEntity.ok(appointmentService.byStatus(status));
    }

    @GetMapping("/between")
    public ResponseEntity<List<Appointment>> between(@RequestParam String start, @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);
        List<Appointment> result = appointmentService.between(Date.valueOf(startDate), Date.valueOf(endDate));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats/by-status")
    public ResponseEntity<List<Object[]>> statsByStatus() {
        return ResponseEntity.ok(appointmentService.statsByStatus());
    }

    @PatchMapping("/cancel-day")
    public ResponseEntity<String> cancelDay(@RequestParam UUID doctorId, @RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return ResponseEntity.ok(appointmentService.cancelDoctorDay(doctorId, parsedDate));
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Appointment>> page(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sort) {
        String[] sortParams = sort.split(",");
        Sort sortObj = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return ResponseEntity.ok(appointmentService.page(pageable));
    }

    @DeleteMapping("/cancelled-before")
    public ResponseEntity<String> cancelledBefore(@RequestParam String date) {
        LocalDate parsedDate = LocalDate.parse(date);
        return ResponseEntity.ok(appointmentService.deleteCancelledBefore(parsedDate));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<String> changeStatus(@PathVariable UUID id, @RequestParam AppointmentStatus status) {
        String result = appointmentService.changeStatus(id, status);
        if (result.equals("Appointment status updated successfully")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("does not exist")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<String> reschedule(@PathVariable UUID id, @RequestParam String newDate) {
        String result = appointmentService.reschedule(id, LocalDate.parse(newDate));
        if (result.equals("Appointment rescheduled successfully")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        }
        if (result.contains("does not exist")) {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
        if (result.equals("Doctor is already booked on that date")) {
            return new ResponseEntity<>(result, HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
    }

}
