package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Patient;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    @Autowired
    private DoctorRepository doctorRepo;

    public Patient create(Patient patient) {
        if (patient.getSsNumber() != null) {
            Optional<Patient> duplicate = patientRepo.findBySsNumber(patient.getSsNumber());
            if (duplicate.isPresent()) {
                throw new IllegalStateException("A patient with ssNumber " + patient.getSsNumber() + " already exists");
            }
        }
        return patientRepo.save(patient);
    }

    public List<Patient> findAll() {
        return patientRepo.findAll();
    }

    public Patient findById(UUID id) {
        return patientRepo.findById(id).orElse(null);
    }

    public Patient update(UUID id, Patient updated) {
        Patient existing = patientRepo.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        if (updated.getSsNumber() != null) {
            Optional<Patient> duplicate = patientRepo.findBySsNumber(updated.getSsNumber());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
                throw new IllegalStateException("A patient with ssNumber " + updated.getSsNumber() + " already exists");
            }
        }
        existing.setFirstName(updated.getFirstName());
        existing.setLastName(updated.getLastName());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setSsNumber(updated.getSsNumber());
        return patientRepo.save(existing);
    }

    public boolean delete(UUID id) {
        if (!patientRepo.existsById(id)) {
            return false;
        }
        patientRepo.deleteById(id);
        return true;
    }

    public List<Patient> findByLastName(String lastName) {
        return patientRepo.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    public Optional<List<Patient>> patientsOfDoctor(UUID doctorId) {
        if (!doctorRepo.existsById(doctorId)) {
            return Optional.empty();
        }
        return Optional.of(appointmentRepo.findPatientsOfDoctor(doctorId));
    }

    public List<Patient> frequentPatients(long min) {
        return appointmentRepo.findFrequentPatients(min);
    }

}
