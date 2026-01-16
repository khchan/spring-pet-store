package com.khchan.petstore.repository;

import com.khchan.petstore.domain.*;
import com.khchan.petstore.test.DataSourceProxyConfig;
import com.khchan.petstore.test.JpaQueryTrackingRule;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest
@Import(DataSourceProxyConfig.class)
@Transactional
public class DataSourceProxyEntityCrudTest {

    @RegisterExtension
    JpaQueryTrackingRule queryTracking = new JpaQueryTrackingRule()
        .printQueriesOnFailure(true);

    @Autowired
    private BreedRepository breedRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private PetInsuranceRepository petInsuranceRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void insertBreed_shouldTrackInsertQuery() {
        queryTracking.reset();

        Breed breed = new Breed("Test Breed", "Insert coverage", Size.MEDIUM);
        breedRepository.save(breed);
        entityManager.flush();

        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updateBreed_shouldTrackUpdateQuery() {
        Breed breed = breedRepository.save(new Breed("Update Breed", "Before", Size.SMALL));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        Breed managed = breedRepository.findById(breed.getId()).orElseThrow();
        managed.setDescription("After");
        breedRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deleteBreed_shouldTrackDeleteQuery() {
        Breed breed = breedRepository.save(new Breed("Delete Breed", "Cleanup", Size.LARGE));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        breedRepository.deleteById(breed.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(1);
    }

    @Test
    public void insertClinic_shouldTrackInsertQuery() {
        queryTracking.reset();

        Clinic clinic = new Clinic(
            "Insert Clinic",
            "555-0100",
            new Address("10 Main St", "Boston", "MA", "02110", "USA")
        );
        clinicRepository.save(clinic);
        entityManager.flush();

        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updateClinic_shouldTrackUpdateQuery() {
        Clinic clinic = clinicRepository.save(new Clinic(
            "Update Clinic",
            "555-0101",
            new Address("11 Main St", "Boston", "MA", "02110", "USA")
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        Clinic managed = clinicRepository.findById(clinic.getId()).orElseThrow();
        managed.setPhone("555-0102");
        clinicRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deleteClinic_shouldTrackDeleteQuery() {
        Clinic clinic = clinicRepository.save(new Clinic(
            "Delete Clinic",
            "555-0103",
            new Address("12 Main St", "Boston", "MA", "02110", "USA")
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        clinicRepository.deleteById(clinic.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(1);
    }

    @Test
    public void insertVeterinarian_shouldTrackInsertQuery() {
        queryTracking.reset();

        Veterinarian vet = new Veterinarian("Alex", "Kim", "General", "LIC-INSERT");
        veterinarianRepository.save(vet);
        entityManager.flush();

        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updateVeterinarian_shouldTrackUpdateQuery() {
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Alex",
            "Kim",
            "General",
            "LIC-UPDATE"
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        Veterinarian managed = veterinarianRepository.findById(vet.getId()).orElseThrow();
        managed.setSpecialty("Surgery");
        veterinarianRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deleteVeterinarian_shouldTrackDeleteQuery() {
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Alex",
            "Kim",
            "General",
            "LIC-DELETE"
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        veterinarianRepository.deleteById(vet.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(1);
    }

    @Test
    public void insertOwner_shouldTrackInsertQuery() {
        queryTracking.reset();

        Owner owner = new Owner(
            "Taylor",
            "Lee",
            "taylor.lee@example.com",
            "555-0200",
            new Address("20 Oak St", "Cambridge", "MA", "02139", "USA")
        );
        ownerRepository.save(owner);
        entityManager.flush();

        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updateOwner_shouldTrackUpdateQuery() {
        Owner owner = ownerRepository.save(new Owner(
            "Taylor",
            "Lee",
            "taylor.lee.update@example.com",
            "555-0201",
            new Address("21 Oak St", "Cambridge", "MA", "02139", "USA")
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        Owner managed = ownerRepository.findById(owner.getId()).orElseThrow();
        managed.setPhone("555-0202");
        ownerRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deleteOwner_shouldTrackDeleteQuery() {
        Owner owner = ownerRepository.save(new Owner(
            "Taylor",
            "Lee",
            "taylor.lee.delete@example.com",
            "555-0203",
            new Address("22 Oak St", "Cambridge", "MA", "02139", "USA")
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        ownerRepository.deleteById(owner.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(1);
    }

    @Test
    public void insertAppointment_shouldTrackInsertQuery() {
        PetEntity pet = petRepository.save(buildPet("Appt Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Jordan",
            "Parker",
            "Dentistry",
            "LIC-APPT-INSERT"
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        PetEntity managedPet = petRepository.findById(pet.getId()).orElseThrow();
        Veterinarian managedVet = veterinarianRepository.findById(vet.getId()).orElseThrow();
        Appointment appointment = new Appointment(
            managedPet,
            managedVet,
            LocalDateTime.now().plusDays(1),
            "Checkup",
            AppointmentStatus.SCHEDULED
        );
        appointmentRepository.save(appointment);
        entityManager.flush();

        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updateAppointment_shouldTrackUpdateQuery() {
        PetEntity pet = petRepository.save(buildPet("Appt Update Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Jordan",
            "Parker",
            "Dentistry",
            "LIC-APPT-UPDATE"
        ));
        Appointment appointment = appointmentRepository.save(new Appointment(
            pet,
            vet,
            LocalDateTime.now().plusDays(2),
            "Follow-up",
            AppointmentStatus.SCHEDULED
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        Appointment managed = appointmentRepository.findById(appointment.getId()).orElseThrow();
        managed.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deleteAppointment_shouldTrackDeleteQuery() {
        PetEntity pet = petRepository.save(buildPet("Appt Delete Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Jordan",
            "Parker",
            "Dentistry",
            "LIC-APPT-DELETE"
        ));
        Appointment appointment = appointmentRepository.save(new Appointment(
            pet,
            vet,
            LocalDateTime.now().plusDays(3),
            "Cleanup",
            AppointmentStatus.SCHEDULED
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        appointmentRepository.deleteById(appointment.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(1);
    }

    @Test
    public void insertMedicalRecord_shouldTrackInsertQuery() {
        PetEntity pet = petRepository.save(buildPet("Record Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Sam",
            "Rivera",
            "General",
            "LIC-REC-INSERT"
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        PetEntity managedPet = petRepository.findById(pet.getId()).orElseThrow();
        Veterinarian managedVet = veterinarianRepository.findById(vet.getId()).orElseThrow();
        MedicalRecord record = new MedicalRecord(
            managedPet,
            managedVet,
            LocalDate.now(),
            "Routine",
            "All good"
        );
        medicalRecordRepository.save(record);
        entityManager.flush();

        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updateMedicalRecord_shouldTrackUpdateQuery() {
        PetEntity pet = petRepository.save(buildPet("Record Update Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Sam",
            "Rivera",
            "General",
            "LIC-REC-UPDATE"
        ));
        MedicalRecord record = medicalRecordRepository.save(new MedicalRecord(
            pet,
            vet,
            LocalDate.now().minusDays(2),
            "Initial",
            "Monitor"
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        MedicalRecord managed = medicalRecordRepository.findById(record.getId()).orElseThrow();
        managed.setTreatment("Resolved");
        medicalRecordRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deleteMedicalRecord_shouldTrackDeleteQuery() {
        PetEntity pet = petRepository.save(buildPet("Record Delete Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Sam",
            "Rivera",
            "General",
            "LIC-REC-DELETE"
        ));
        MedicalRecord record = medicalRecordRepository.save(new MedicalRecord(
            pet,
            vet,
            LocalDate.now().minusDays(1),
            "Delete",
            "Remove"
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        medicalRecordRepository.deleteById(record.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(1);
    }

    @Test
    public void insertVaccination_shouldTrackInsertQuery() {
        PetEntity pet = petRepository.save(buildPet("Vaccination Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Casey",
            "Nguyen",
            "Immunology",
            "LIC-VACC-INSERT"
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        PetEntity managedPet = petRepository.findById(pet.getId()).orElseThrow();
        Veterinarian managedVet = veterinarianRepository.findById(vet.getId()).orElseThrow();
        Vaccination vaccination = new Vaccination(
            managedPet,
            "Rabies",
            LocalDate.now().minusDays(1),
            LocalDate.now().plusMonths(12),
            managedVet
        );
        vaccinationRepository.save(vaccination);
        entityManager.flush();

        queryTracking.assertInsertCount(1);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updateVaccination_shouldTrackUpdateQuery() {
        PetEntity pet = petRepository.save(buildPet("Vaccination Update Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Casey",
            "Nguyen",
            "Immunology",
            "LIC-VACC-UPDATE"
        ));
        Vaccination vaccination = vaccinationRepository.save(new Vaccination(
            pet,
            "Distemper",
            LocalDate.now().minusDays(10),
            LocalDate.now().plusMonths(6),
            vet
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        Vaccination managed = vaccinationRepository.findById(vaccination.getId()).orElseThrow();
        managed.setNotes("Updated notes");
        vaccinationRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deleteVaccination_shouldTrackDeleteQuery() {
        PetEntity pet = petRepository.save(buildPet("Vaccination Delete Pet"));
        Veterinarian vet = veterinarianRepository.save(new Veterinarian(
            "Casey",
            "Nguyen",
            "Immunology",
            "LIC-VACC-DELETE"
        ));
        Vaccination vaccination = vaccinationRepository.save(new Vaccination(
            pet,
            "Bordetella",
            LocalDate.now().minusDays(5),
            LocalDate.now().plusMonths(9),
            vet
        ));
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        vaccinationRepository.deleteById(vaccination.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(1);
    }

    @Test
    public void insertPetInsurance_shouldTrackCascadeInsertQueries() {
        queryTracking.reset();

        PetEntity pet = buildPet("Insurance Pet");
        PetInsurance insurance = new PetInsurance(
            "POL-INSERT-1",
            "CarePlus",
            new BigDecimal("8000.00"),
            new BigDecimal("55.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            InsuranceStatus.ACTIVE
        );
        pet.setInsurance(insurance);
        petRepository.save(pet);
        entityManager.flush();

        queryTracking.assertInsertCount(2);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void updatePetInsurance_shouldTrackUpdateQuery() {
        PetEntity pet = buildPet("Insurance Update Pet");
        PetInsurance insurance = new PetInsurance(
            "POL-UPDATE-1",
            "CarePlus",
            new BigDecimal("9000.00"),
            new BigDecimal("65.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            InsuranceStatus.ACTIVE
        );
        pet.setInsurance(insurance);
        petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        PetInsurance managed = petInsuranceRepository.findByPolicyNumber("POL-UPDATE-1").orElseThrow();
        managed.setStatus(InsuranceStatus.SUSPENDED);
        petInsuranceRepository.save(managed);
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(1);
        queryTracking.assertDeleteCount(0);
    }

    @Test
    public void deletePetInsurance_shouldTrackCascadeDeleteQueries() {
        PetEntity pet = buildPet("Insurance Delete Pet");
        PetInsurance insurance = new PetInsurance(
            "POL-DELETE-1",
            "CarePlus",
            new BigDecimal("7000.00"),
            new BigDecimal("45.00"),
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            InsuranceStatus.ACTIVE
        );
        pet.setInsurance(insurance);
        petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        queryTracking.reset();

        petRepository.deleteById(pet.getId());
        entityManager.flush();

        queryTracking.assertInsertCount(0);
        queryTracking.assertUpdateCount(0);
        queryTracking.assertDeleteCount(4);
    }

    private PetEntity buildPet(String name) {
        PetEntity pet = new PetEntity();
        pet.setName(name);
        pet.setStatus(Status.AVAILABLE);
        return pet;
    }
}
