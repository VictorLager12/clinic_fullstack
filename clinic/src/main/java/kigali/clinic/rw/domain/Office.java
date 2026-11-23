package kigali.clinic.rw.domain;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity 
@Table (name="office")
public class Office {
    
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    
    @NotBlank (message = "Office name is required")
    @Column (name="name")
    private String name;

    
    @Min (value = 1, message = "Office number must be a positive number")
    @Column (name="office_number", unique = true)
    private int officeNumber;

    
    @OneToOne (mappedBy = "office")
    @JsonIgnore
    private Doctor doctor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOfficeNumber() {
        return officeNumber;
    }

    public void setOfficeNumber(int officeNumber) {
        this.officeNumber = officeNumber;
    }

    public UUID getId() {
        return id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

}