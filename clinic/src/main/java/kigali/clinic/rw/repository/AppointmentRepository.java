package kigali.clinic.rw.repository;

import java.util.List;
import java.util.UUID;
import java.sql.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinic.rw.domain.Appointment;
import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.domain.Patient;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateAsc(UUID doctorId, AppointmentStatus status);
    
    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, Date appointmentDate, AppointmentStatus status);

    @Query("""
        SELECT a FROM Appointment a
        JOIN a.doctor d
        JOIN d.specializations s
        WHERE s.name = :specializationName
        """)
    List<Appointment> findByDoctorSpecialization(@Param("specializationName") String specializationName);

    Page<Appointment> findByDoctorId(UUID doctorId, Pageable pageable);
    
    @Query("SELECT DISTINCT a.patient FROM Appointment a WHERE a.doctor.id = :doctorId")
List<Patient> findPatientsOfDoctor(@Param("doctorId") UUID doctorId);

    @Query("SELECT a.patient FROM Appointment a GROUP BY a.patient HAVING COUNT(a) >= :min ORDER BY COUNT(a) DESC")
    List<Patient> findFrequentPatients(@Param("min") long min);
    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(Date start, Date end);
    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
List<Object[]> countAppointmentsByStatus();
@Query("""
    SELECT o.name, o.officeNumber, COUNT(a)
    FROM Appointment a
    JOIN a.doctor d
    JOIN d.office o
    GROUP BY o.name, o.officeNumber
    ORDER BY COUNT(a) DESC
    """)
List<Object[]> busiestOfficeRaw();
@Modifying
@Query("""
    UPDATE Appointment a
    SET a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED
    WHERE a.doctor.id = :doctorId
      AND a.appointmentDate = :date
      AND a.status <> kigali.clinic.rw.domain.AppointmentStatus.COMPLETED
    """)
int cancelDoctorDay(@Param("doctorId") UUID doctorId, @Param("date") Date date);
@Modifying
@Query("DELETE FROM Appointment a WHERE a.status = kigali.clinic.rw.domain.AppointmentStatus.CANCELLED AND a.appointmentDate < :date")
int deleteCancelledBefore(@Param("date") Date date);

    

    List<Appointment> findByDoctorIdAndAppointmentDateAndStatusNot(UUID doctorId, Date date, AppointmentStatus status);

    List<Appointment> findByPatientSsNumberAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(String ssNumber, Date today, List<AppointmentStatus> statuses);

    long countByStatus(AppointmentStatus status);

    long countByAppointmentDate(Date date);
}
