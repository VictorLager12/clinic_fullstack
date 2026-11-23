package kigali.clinic.rw.service;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import kigali.clinic.rw.domain.Doctor;
import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.domain.Specialization;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.SpecializationRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    @Autowired
    private OfficeRepository officeRepo;

    @Autowired
    private SpecializationRepository specializationRepo;

    public Object create(Doctor doctor) {
        if (doctor.getOffice() != null) {
            Office realOffice = officeRepo.findByOfficeNumber(doctor.getOffice().getOfficeNumber()).orElse(null);
            if (realOffice == null) {
                return "Office with number " + doctor.getOffice().getOfficeNumber() + " does not exist.";
            }
            if (realOffice.getDoctor() != null) {
                return "Office number " + realOffice.getOfficeNumber() + " is already occupied by Dr. " + realOffice.getDoctor().getLastName();
            }
            doctor.setOffice(realOffice);
        }
        return doctorRepo.save(doctor);
    }

    public List<Doctor> findAll() {
        return doctorRepo.findAll();
    }

    public Doctor findById(UUID id) {
        return doctorRepo.findById(id).orElse(null);
    }

    public Object update(UUID id, Doctor updatedDoctor) {
        Doctor existing = doctorRepo.findById(id).orElse(null);
        if (existing == null) {
            return "Doctor not found.";
        }
        existing.setFirstName(updatedDoctor.getFirstName());
        existing.setLastName(updatedDoctor.getLastName());
        existing.setDateOfBirth(updatedDoctor.getDateOfBirth());
        
        if (updatedDoctor.getOffice() != null) {
            Office realOffice = officeRepo.findByOfficeNumber(updatedDoctor.getOffice().getOfficeNumber()).orElse(null);
            if (realOffice == null) {
                return "Office with number " + updatedDoctor.getOffice().getOfficeNumber() + " does not exist.";
            }
            if (realOffice.getDoctor() != null && !realOffice.getDoctor().getId().equals(id)) {
                return "Office number " + realOffice.getOfficeNumber() + " is already occupied by Dr. " + realOffice.getDoctor().getLastName();
            }
            existing.setOffice(realOffice);
        } else {
            existing.setOffice(null);
        }
        return doctorRepo.save(existing);
    }

    public boolean delete(UUID id) {
        if (!doctorRepo.existsById(id)) {
            return false;
        }
        doctorRepo.deleteById(id);
        return true;
    }

    public List<Doctor> bySpecialization(String name) {
        return doctorRepo.findBySpecializationName(name);
    }

    public List<Doctor> withoutOffice() {
        return doctorRepo.findDoctorsWithoutOffice();
    }

    public String addSpecialization(UUID doctorId, UUID specializationId) {
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "The doctor with that id does not exist";
        }
        Specialization specialization = specializationRepo.findById(specializationId).orElse(null);
        if (specialization == null) {
            return "That specialization does not exist";
        }
        if (specialization.getDoctors().contains(doctor)) {
            return "The doctor already has that specialization";
        }
        specialization.getDoctors().add(doctor);
        specializationRepo.save(specialization);
        return "Specialization added to the doctor";
    }

    public String removeSpecialization(UUID doctorId, UUID specializationId) {
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "The doctor with that id does not exist";
        }
        Specialization specialization = specializationRepo.findById(specializationId).orElse(null);
        if (specialization == null) {
            return "That specialization does not exist";
        }
        if (!specialization.getDoctors().contains(doctor)) {
            return "The doctor does not have that specialization";
        }
        specialization.getDoctors().remove(doctor);
        specializationRepo.save(specialization);
        return "Specialization removed from the doctor";
    }

    public String changeOffice(UUID doctorId, int officeNumber) {
        Doctor doctor = doctorRepo.findById(doctorId).orElse(null);
        if (doctor == null) {
            return "The doctor with that id does not exist";
        }
        Office office = officeRepo.findByOfficeNumber(officeNumber).orElse(null);
        if (office == null) {
            return "The office with that office number does not exist";
        }
        if (office.getDoctor() != null && !office.getDoctor().getId().equals(doctorId)) {
            return "That office is taken by other doctor";
        }
        doctor.setOffice(office);
        doctorRepo.save(doctor);
        return "Office changed successfully";
    }

    public List<Doctor> search(String name) {
        return doctorRepo.searchByName(name);
    }

    public List<Object[]> workload(java.sql.Date start, java.sql.Date end) {
        return doctorRepo.workload(start, end);
    }
}