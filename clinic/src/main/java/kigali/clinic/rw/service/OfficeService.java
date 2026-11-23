package kigali.clinic.rw.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.Office;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.OfficeRepository;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository offRepo;

    @Autowired
    private AppointmentRepository appointmentRepo;

    public String saveOffice(Office office) {
        Optional<Office> duplicate = offRepo.findByOfficeNumber(office.getOfficeNumber());
        if (duplicate.isPresent()) {
            return "Office with that number " + office.getOfficeNumber() + " already exists";
        }
        offRepo.save(office);
        return "Office is saved successfully";
    }

    public List<Office> findAll() {
        return offRepo.findAll();
    }

    public Office findById(UUID id) {
        return offRepo.findById(id).orElse(null);
    }

    public String updateOffice(UUID id, Office updatedOffice) {
        Optional<Office> existingOpt = offRepo.findById(id);
        if (existingOpt.isEmpty()) {
            return "Office not found";
        }
        Optional<Office> duplicate = offRepo.findByOfficeNumber(updatedOffice.getOfficeNumber());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            return "Office with that number " + updatedOffice.getOfficeNumber() + " already exists";
        }
        Office existing = existingOpt.get();
        existing.setName(updatedOffice.getName());
        existing.setOfficeNumber(updatedOffice.getOfficeNumber());
        offRepo.save(existing);
        return "Office updated successfully";
    }

    public boolean delete(UUID id) {
        if (!offRepo.existsById(id)) {
            return false;
        }
        offRepo.deleteById(id);
        return true;
    }

    public String busiestOffice() {
        List<Object[]> results = appointmentRepo.busiestOfficeRaw();
        if (results.isEmpty()) {
            return "No appointments yet";
        }
        Object[] top = results.get(0);
        return "Office " + top[0] + " (#" + top[1] + ") has " + top[2] + " appointments";
    }

    public List<Office> freeOffices() {
        return offRepo.findFreeOffices();
    }

}
