package kigali.clinic.rw.service;

import java.util.List;
import java.sql.Date;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private PrescriptionRepository prescriptionRepo;

    public Object create(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return "Doctor ID is required to book an appointment.";
        }
        Optional<Doctor> realDoctorOpt = doctorRepo.findById(appointment.getDoctor().getId());
        if (realDoctorOpt.isEmpty()) {
            return "Doctor with the provided ID does not exist.";
        }

        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            return "Patient ID is required to book an appointment.";
        }
        Optional<Patient> realPatientOpt = patientRepo.findById(appointment.getPatient().getId());
        if (realPatientOpt.isEmpty()) {
            return "Patient with the provided ID does not exist.";
        }

        appointment.setDoctor(realDoctorOpt.get());
        appointment.setPatient(realPatientOpt.get());

        if (appointment.getStatus() == null) {
            appointment.setStatus(AppointmentStatus.SCHEDULED);
        }

        return appointmentRepo.save(appointment);
    }

    public List<Appointment> findAll() {
        return appointmentRepo.findAll();
    }

    public Appointment findById(UUID id) {
        return appointmentRepo.findById(id).orElse(null);
    }

    public Object update(UUID id, Appointment updatedData) {
        Optional<Appointment> existingOpt = appointmentRepo.findById(id);
        if (existingOpt.isEmpty()) {
            return "Appointment not found.";
        }
        Appointment existing = existingOpt.get();

        existing.setAppointmentDate(updatedData.getAppointmentDate());
        existing.setReason(updatedData.getReason());

        if (updatedData.getDoctor() != null && updatedData.getDoctor().getId() != null) {
            Optional<Doctor> realDoctorOpt = doctorRepo.findById(updatedData.getDoctor().getId());
            if (realDoctorOpt.isEmpty()) {
                return "New Doctor ID does not exist.";
            }
            existing.setDoctor(realDoctorOpt.get());
        }

        if (updatedData.getPatient() != null && updatedData.getPatient().getId() != null) {
            Optional<Patient> realPatientOpt = patientRepo.findById(updatedData.getPatient().getId());
            if (realPatientOpt.isEmpty()) {
                return "New Patient ID does not exist.";
            }
            existing.setPatient(realPatientOpt.get());
        }

        return appointmentRepo.save(existing);
    }

    public String delete(UUID id) {
        if (!appointmentRepo.existsById(id)) {
            return "The appointment with that id does not exist";
        }
        if (prescriptionRepo.existsByAppointmentId(id)) {
            return "That appointment has prescriptions, delete them first";
        }
        appointmentRepo.deleteById(id);
        return "Appointment deleted successfully";
    }

    public List<Appointment> byStatus(AppointmentStatus status) {
        return appointmentRepo.findByStatusOrderByAppointmentDateAsc(status);
    }

    public List<Appointment> between(Date start, Date end) {
        return appointmentRepo.findByAppointmentDateBetweenOrderByAppointmentDateAsc(start, end);
    }

    public String saveAppointment(Appointment appointment) {
        if (appointment.getDoctor() == null || appointment.getDoctor().getId() == null) {
            return "Doctor ID is required to book an appointment";
        }
        Optional<Doctor> doctorOpt = doctorRepo.findById(appointment.getDoctor().getId());
        if (doctorOpt.isEmpty()) {
            return "Doctor with the provided ID does not exist";
        }
        if (appointment.getPatient() == null || appointment.getPatient().getId() == null) {
            return "Patient ID is required to book an appointment";
        }
        Optional<Patient> patientOpt = patientRepo.findById(appointment.getPatient().getId());
        if (patientOpt.isEmpty()) {
            return "Patient with the provided ID does not exist";
        }
        if (appointment.getAppointmentDate() == null) {
            return "Appointment date is required";
        }
        if (appointment.getAppointmentDate().toLocalDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        Doctor doctor = doctorOpt.get();
        Patient patient = patientOpt.get();
        boolean alreadyBooked = appointmentRepo.existsByDoctorIdAndAppointmentDateAndStatusNot(
            doctor.getId(), appointment.getAppointmentDate(), AppointmentStatus.CANCELLED);
        if (alreadyBooked) {
            return "Doctor is already booked on that date";
        }
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(appointment);
        return "Appointment saved successfully";
    }

    public String changeStatus(UUID id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepo.findById(id).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        }
        AppointmentStatus current = appointment.getStatus();
        boolean allowed = false;
        if (current == AppointmentStatus.SCHEDULED
                && (newStatus == AppointmentStatus.CONFIRMED || newStatus == AppointmentStatus.CANCELLED)) {
            allowed = true;
        }
        if (current == AppointmentStatus.CONFIRMED
                && (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.CANCELLED)) {
            allowed = true;
        }
        if (!allowed) {
            return "Cannot change status from " + current + " to " + newStatus;
        }
        appointment.setStatus(newStatus);
        appointmentRepo.save(appointment);
        return "Appointment status updated successfully";
    }

    public String reschedule(UUID id, LocalDate newDate) {
        Appointment appointment = appointmentRepo.findById(id).orElse(null);
        if (appointment == null) {
            return "The appointment with that id does not exist";
        }
        AppointmentStatus current = appointment.getStatus();
        if (current == AppointmentStatus.COMPLETED || current == AppointmentStatus.CANCELLED) {
            return "A " + current + " appointment cannot be rescheduled";
        }
        if (newDate.isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        Date sqlNewDate = Date.valueOf(newDate);
        boolean alreadyBooked = appointmentRepo.existsByDoctorIdAndAppointmentDateAndStatusNot(
            appointment.getDoctor().getId(), sqlNewDate, AppointmentStatus.CANCELLED);
        if (alreadyBooked) {
            return "Doctor is already booked on that date";
        }
        appointment.setAppointmentDate(sqlNewDate);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepo.save(appointment);
        return "Appointment rescheduled successfully";
    }

    public List<Object[]> statsByStatus() {
        return appointmentRepo.countAppointmentsByStatus();
    }

    @Transactional
    public String cancelDoctorDay(UUID doctorId, LocalDate date) {
        int count = appointmentRepo.cancelDoctorDay(doctorId, Date.valueOf(date));
        return count + " appointments cancelled";
    }

    public Page<Appointment> page(Pageable pageable) {
        return appointmentRepo.findAll(pageable);
    }

    @Transactional
    public String deleteCancelledBefore(LocalDate date) {
        int count = appointmentRepo.deleteCancelledBefore(Date.valueOf(date));
        return count + " appointments deleted";
    }

    public Optional<List<Appointment>> agenda(UUID doctorId, LocalDate date) {
        if (!doctorRepo.existsById(doctorId)) {
            return Optional.empty();
        }
        return Optional.of(appointmentRepo.findByDoctorIdAndAppointmentDateAndStatusNot(
            doctorId, Date.valueOf(date), AppointmentStatus.CANCELLED));
    }

    public Optional<List<Appointment>> upcomingForPatient(String ssNumber) {
        Optional<Patient> patientOpt = patientRepo.findBySsNumber(ssNumber);
        if (patientOpt.isEmpty()) {
            return Optional.empty();
        }
        List<AppointmentStatus> statuses = List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED);
        Date today = Date.valueOf(LocalDate.now());
        return Optional.of(appointmentRepo.findByPatientSsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
            ssNumber, today, statuses));
    }

}
