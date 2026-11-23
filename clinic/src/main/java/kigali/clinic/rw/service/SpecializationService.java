package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepo;

    public Specialization create(Specialization specialization) {
        return specializationRepo.save(specialization);
    }

    public List<Specialization> findAll() {
        return specializationRepo.findAll();
    }

    public Specialization findById(UUID id) {
        return specializationRepo.findById(id).orElse(null);
    }

    public Specialization update(UUID id, Specialization updated) {
        Specialization existing = specializationRepo.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        existing.setName(updated.getName());
        return specializationRepo.save(existing);
    }

    public boolean delete(UUID id) {
        if (!specializationRepo.existsById(id)) {
            return false;
        }
        specializationRepo.deleteById(id);
        return true;
    }
    
@Autowired
private DoctorRepository doctorRepo;


public Specialization assignDoctor(UUID specializationId, UUID doctorId) {
    Specialization spec = specializationRepo.findById(specializationId)
        .orElseThrow(() -> new IllegalArgumentException("Specialization not found."));
    Doctor doctor = doctorRepo.findById(doctorId)
        .orElseThrow(() -> new IllegalArgumentException("Doctor not found."));

    if (!spec.getDoctors().contains(doctor)) {
        spec.getDoctors().add(doctor);
    }
    return specializationRepo.save(spec);
}
public List<Specialization> unused() {
    return specializationRepo.findUnusedSpecializations();
}
}