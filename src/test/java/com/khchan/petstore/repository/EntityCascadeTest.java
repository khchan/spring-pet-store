package com.khchan.petstore.repository;

import com.khchan.petstore.domain.*;
import com.khchan.petstore.test.JpaQueryTrackingRule;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for JPA cascade operations demonstrating best practices.
 *
 * This test suite covers:
 * 1. CASCADE.PERSIST - automatic persistence of child entities
 * 2. CASCADE.MERGE - automatic merging of detached entities
 * 3. CASCADE.REMOVE - automatic deletion of child entities
 * 4. Orphan Removal - automatic deletion when removing from collection
 * 5. Bidirectional relationship management
 * 6. Query optimization with JOIN FETCH
 */
@SpringBootTest
@ExtendWith({SpringExtension.class, JpaQueryTrackingRule.class})
public class EntityCascadeTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private BreedRepository breedRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private PetInsuranceRepository petInsuranceRepository;

    @Autowired
    private EntityManager entityManager;

    private Breed goldenRetriever;
    private Clinic mainClinic;
    private Veterinarian drSmith;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up all data
        appointmentRepository.deleteAll();
        medicalRecordRepository.deleteAll();
        vaccinationRepository.deleteAll();
        petInsuranceRepository.deleteAll();
        ownerRepository.deleteAll();
        petRepository.deleteAll();
        veterinarianRepository.deleteAll();
        clinicRepository.deleteAll();
        breedRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();

        // Create reference data that will be reused
        goldenRetriever = new Breed("Golden Retriever", "Friendly and intelligent", Size.LARGE);
        breedRepository.save(goldenRetriever);

        Address clinicAddress = new Address("123 Vet St", "Boston", "MA", "02101", "USA");
        mainClinic = new Clinic("Main Street Veterinary Clinic", "555-0100", clinicAddress);
        clinicRepository.save(mainClinic);

        drSmith = new Veterinarian("John", "Smith", "General Practice", "VET12345");
        drSmith.setClinic(mainClinic);
        veterinarianRepository.save(drSmith);

        entityManager.flush();
        entityManager.clear();
    }

    /**
     * Test CASCADE.PERSIST: When an owner is saved, their pets should be automatically persisted.
     * This is controlled by CASCADE.ALL on Owner.pets.
     */
    @Test
    @Transactional
    public void testCascadePersist_OwnerWithPets() {
        // Arrange
        Address ownerAddress = new Address("456 Oak Ave", "Cambridge", "MA", "02139", "USA");
        Owner owner = new Owner("Alice", "Johnson", "alice@example.com", "555-1234", ownerAddress);

        PetEntity pet1 = new PetEntity();
        pet1.setName("Max");
        pet1.setStatus(Status.AVAILABLE);
        pet1.setBreed(goldenRetriever);

        PetEntity pet2 = new PetEntity();
        pet2.setName("Bella");
        pet2.setStatus(Status.AVAILABLE);
        pet2.setBreed(goldenRetriever);

        owner.addPet(pet1);
        owner.addPet(pet2);

        // Act - Save only the owner, pets should cascade
        Owner savedOwner = ownerRepository.save(owner);
        entityManager.flush();

        // Assert
        assertThat(savedOwner.getId()).isNotNull();
        assertThat(savedOwner.getPets()).hasSize(2);
        assertThat(savedOwner.getPets().get(0).getId()).isNotNull();
        assertThat(savedOwner.getPets().get(1).getId()).isNotNull();

        // Verify pets were actually persisted
        assertThat(petRepository.count()).isEqualTo(2);
    }

    /**
     * Test CASCADE.PERSIST with OneToOne: Pet -> Insurance
     * When a pet with insurance is saved, the insurance should be automatically persisted.
     */
    @Test
    @Transactional
    public void testCascadePersist_PetWithInsurance() {
        // Arrange
        PetEntity pet = new PetEntity();
        pet.setName("Rocky");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        PetInsurance insurance = new PetInsurance(
            "POL-12345",
            "PetCare Insurance",
            new BigDecimal("5000.00"),
            new BigDecimal("49.99"),
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            InsuranceStatus.ACTIVE
        );

        pet.setInsurance(insurance);

        // Act - Save only the pet, insurance should cascade
        PetEntity savedPet = petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        // Assert
        PetEntity foundPet = petRepository.findById(savedPet.getId()).orElseThrow();
        assertThat(foundPet.getInsurance()).isNotNull();
        assertThat(foundPet.getInsurance().getId()).isNotNull();
        assertThat(foundPet.getInsurance().getPolicyNumber()).isEqualTo("POL-12345");

        // Verify insurance was actually persisted
        assertThat(petInsuranceRepository.count()).isEqualTo(1);
    }

    /**
     * Test CASCADE.ALL with multiple child collections.
     * When a pet is saved with medical records and vaccinations, both should cascade.
     */
    @Test
    @Transactional
    public void testCascadePersist_PetWithMedicalRecordsAndVaccinations() {
        // Arrange
        PetEntity pet = new PetEntity();
        pet.setName("Luna");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        MedicalRecord record1 = new MedicalRecord(
            pet, drSmith, LocalDate.now().minusMonths(2),
            "Annual checkup", "All clear, healthy"
        );

        MedicalRecord record2 = new MedicalRecord(
            pet, drSmith, LocalDate.now().minusMonths(1),
            "Minor infection", "Prescribed antibiotics"
        );

        pet.addMedicalRecord(record1);
        pet.addMedicalRecord(record2);

        Vaccination vacc1 = new Vaccination(
            pet, "Rabies", LocalDate.now().minusMonths(6),
            LocalDate.now().plusMonths(6), drSmith
        );

        Vaccination vacc2 = new Vaccination(
            pet, "Distemper", LocalDate.now().minusMonths(6),
            LocalDate.now().plusYears(1), drSmith
        );

        pet.addVaccination(vacc1);
        pet.addVaccination(vacc2);

        // Act
        PetEntity savedPet = petRepository.save(pet);
        entityManager.flush();

        // Assert
        assertThat(savedPet.getMedicalRecords()).hasSize(2);
        assertThat(savedPet.getVaccinations()).hasSize(2);
        assertThat(savedPet.getMedicalRecords().get(0).getId()).isNotNull();
        assertThat(savedPet.getVaccinations().get(0).getId()).isNotNull();

        // Verify actual persistence
        assertThat(medicalRecordRepository.count()).isEqualTo(2);
        assertThat(vaccinationRepository.count()).isEqualTo(2);
    }

    /**
     * Test ORPHAN REMOVAL: When a vaccination is removed from a pet's collection,
     * it should be automatically deleted from the database.
     */
    @Test
    @Transactional
    public void testOrphanRemoval_RemoveVaccinationFromPet() {
        // Arrange - Create pet with vaccinations
        PetEntity pet = new PetEntity();
        pet.setName("Charlie");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        Vaccination vacc1 = new Vaccination(
            pet, "Rabies", LocalDate.now().minusYears(1),
            LocalDate.now(), drSmith
        );

        Vaccination vacc2 = new Vaccination(
            pet, "Distemper", LocalDate.now().minusMonths(6),
            LocalDate.now().plusMonths(6), drSmith
        );

        pet.addVaccination(vacc1);
        pet.addVaccination(vacc2);

        PetEntity savedPet = petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        Long petId = savedPet.getId();
        assertThat(vaccinationRepository.count()).isEqualTo(2);

        // Act - Remove one vaccination (orphan removal should delete it)
        PetEntity foundPet = petRepository.findById(petId).orElseThrow();
        Vaccination toRemove = foundPet.getVaccinations().get(0);
        foundPet.removeVaccination(toRemove);

        petRepository.save(foundPet);
        entityManager.flush();

        // Assert - Should only have 1 vaccination left in DB
        assertThat(vaccinationRepository.count()).isEqualTo(1);

        PetEntity reloadedPet = petRepository.findById(petId).orElseThrow();
        assertThat(reloadedPet.getVaccinations()).hasSize(1);
    }

    /**
     * Test ORPHAN REMOVAL: When a pet is removed from owner's collection,
     * it should be automatically deleted from the database.
     */
    @Test
    @Transactional
    public void testOrphanRemoval_RemovePetFromOwner() {
        // Arrange
        Address ownerAddress = new Address("789 Elm St", "Somerville", "MA", "02143", "USA");
        Owner owner = new Owner("Bob", "Smith", "bob@example.com", "555-5678", ownerAddress);

        PetEntity pet1 = new PetEntity();
        pet1.setName("Fluffy");
        pet1.setStatus(Status.AVAILABLE);

        PetEntity pet2 = new PetEntity();
        pet2.setName("Spot");
        pet2.setStatus(Status.AVAILABLE);

        owner.addPet(pet1);
        owner.addPet(pet2);

        Owner savedOwner = ownerRepository.save(owner);
        entityManager.flush();
        entityManager.clear();

        assertThat(petRepository.count()).isEqualTo(2);

        // Act - Remove one pet from owner (orphan removal should delete it)
        Owner foundOwner = ownerRepository.findById(savedOwner.getId()).orElseThrow();
        PetEntity toRemove = foundOwner.getPets().get(0);
        foundOwner.removePet(toRemove);

        ownerRepository.save(foundOwner);
        entityManager.flush();

        // Assert
        assertThat(petRepository.count()).isEqualTo(1);

        Owner reloadedOwner = ownerRepository.findById(savedOwner.getId()).orElseThrow();
        assertThat(reloadedOwner.getPets()).hasSize(1);
    }

    /**
     * Test CASCADE.REMOVE: When an owner is deleted, all their pets should be automatically deleted.
     * And because Pet cascades to MedicalRecords and Vaccinations, those should also be deleted.
     */
    @Test
    @Transactional
    public void testCascadeRemove_DeleteOwnerDeletesPetsAndRelated() {
        // Arrange - Create owner with pet, medical records, and vaccinations
        Address ownerAddress = new Address("321 Pine St", "Boston", "MA", "02115", "USA");
        Owner owner = new Owner("Carol", "Davis", "carol@example.com", "555-9999", ownerAddress);

        PetEntity pet = new PetEntity();
        pet.setName("Duke");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        MedicalRecord record = new MedicalRecord(
            pet, drSmith, LocalDate.now(),
            "Checkup", "Healthy"
        );
        pet.addMedicalRecord(record);

        Vaccination vacc = new Vaccination(
            pet, "Rabies", LocalDate.now(),
            LocalDate.now().plusYears(1), drSmith
        );
        pet.addVaccination(vacc);

        owner.addPet(pet);

        Owner savedOwner = ownerRepository.save(owner);
        entityManager.flush();
        entityManager.clear();

        Long ownerId = savedOwner.getId();

        // Verify initial state
        assertThat(ownerRepository.count()).isEqualTo(1);
        assertThat(petRepository.count()).isEqualTo(1);
        assertThat(medicalRecordRepository.count()).isEqualTo(1);
        assertThat(vaccinationRepository.count()).isEqualTo(1);

        // Act - Delete the owner
        ownerRepository.deleteById(ownerId);
        entityManager.flush();

        // Assert - Everything should be deleted due to cascading
        assertThat(ownerRepository.count()).isEqualTo(0);
        assertThat(petRepository.count()).isEqualTo(0);
        assertThat(medicalRecordRepository.count()).isEqualTo(0);
        assertThat(vaccinationRepository.count()).isEqualTo(0);
    }

    /**
     * Test CASCADE.REMOVE with OneToOne: When a pet is deleted, its insurance should be deleted.
     */
    @Test
    @Transactional
    public void testCascadeRemove_DeletePetDeletesInsurance() {
        // Arrange
        PetEntity pet = new PetEntity();
        pet.setName("Buddy");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        PetInsurance insurance = new PetInsurance(
            "POL-99999",
            "PetHealth Co",
            new BigDecimal("3000.00"),
            new BigDecimal("29.99"),
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            InsuranceStatus.ACTIVE
        );

        pet.setInsurance(insurance);

        PetEntity savedPet = petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        Long petId = savedPet.getId();

        // Verify initial state
        assertThat(petRepository.count()).isEqualTo(1);
        assertThat(petInsuranceRepository.count()).isEqualTo(1);

        // Act - Delete the pet
        petRepository.deleteById(petId);
        entityManager.flush();

        // Assert - Insurance should also be deleted
        assertThat(petRepository.count()).isEqualTo(0);
        assertThat(petInsuranceRepository.count()).isEqualTo(0);
    }

    /**
     * Test NO CASCADE on ManyToOne: Deleting a pet should NOT delete the breed.
     * Breed is a shared reference entity.
     */
    @Test
    @Transactional
    public void testNoCascade_DeletePetDoesNotDeleteBreed() {
        // Arrange
        PetEntity pet = new PetEntity();
        pet.setName("Rex");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        PetEntity savedPet = petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        Long petId = savedPet.getId();
        Long breedId = goldenRetriever.getId();

        // Act - Delete the pet
        petRepository.deleteById(petId);
        entityManager.flush();

        // Assert - Breed should still exist
        assertThat(petRepository.count()).isEqualTo(0);
        assertThat(breedRepository.findById(breedId)).isPresent();
    }

    /**
     * Test CASCADE.PERSIST and MERGE only (not REMOVE):
     * When a clinic is deleted, veterinarians should NOT be deleted.
     */
    @Test
    @Transactional
    public void testPartialCascade_DeleteClinicDoesNotDeleteVeterinarians() {
        // Arrange
        Address clinicAddress = new Address("999 Medical Dr", "Cambridge", "MA", "02138", "USA");
        Clinic clinic = new Clinic("East Side Clinic", "555-2000", clinicAddress);

        Veterinarian vet = new Veterinarian("Jane", "Doe", "Surgery", "VET67890");
        clinic.addVeterinarian(vet);

        Clinic savedClinic = clinicRepository.save(clinic);
        entityManager.flush();
        entityManager.clear();

        Long clinicId = savedClinic.getId();

        // Verify initial state
        assertThat(clinicRepository.count()).isEqualTo(2); // mainClinic + this one
        assertThat(veterinarianRepository.count()).isEqualTo(2); // drSmith + vet

        // Act - Delete the clinic
        clinicRepository.deleteById(clinicId);
        entityManager.flush();

        // Assert - Veterinarian should still exist (no CASCADE.REMOVE)
        assertThat(clinicRepository.count()).isEqualTo(1);
        assertThat(veterinarianRepository.count()).isEqualTo(2);

        // Verify vet has null clinic now
        Veterinarian foundVet = veterinarianRepository.findByLicenseNumber("VET67890").orElseThrow();
        assertThat(foundVet.getClinic()).isNull();
    }

    /**
     * Test CASCADE.PERSIST and MERGE on Appointments.
     * Appointments should be saved when pet is saved, but not deleted when pet is deleted.
     */
    @Test
    @Transactional
    public void testAppointmentCascade_PersistButNotRemove() {
        // Arrange
        PetEntity pet = new PetEntity();
        pet.setName("Milo");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        Appointment appointment = new Appointment(
            pet, drSmith, LocalDateTime.now().plusDays(7),
            "Annual checkup", AppointmentStatus.SCHEDULED
        );

        pet.addAppointment(appointment);

        // Act - Save pet with appointment
        PetEntity savedPet = petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        Long petId = savedPet.getId();

        // Assert - Appointment was persisted
        assertThat(appointmentRepository.count()).isEqualTo(1);

        // Now delete the pet
        petRepository.deleteById(petId);
        entityManager.flush();

        // Appointment should still exist (no CASCADE.REMOVE)
        assertThat(appointmentRepository.count()).isEqualTo(1);

        // But the appointment's pet reference will be broken (foreign key constraint)
        // In a real scenario, you'd want to handle this with ON DELETE SET NULL or similar
    }

    /**
     * Test BIDIRECTIONAL relationship management.
     * Verify that helper methods properly maintain both sides of the relationship.
     */
    @Test
    @Transactional
    public void testBidirectionalRelationship_PetAndOwner() {
        // Arrange
        Address ownerAddress = new Address("555 Maple Ave", "Boston", "MA", "02120", "USA");
        Owner owner = new Owner("David", "Wilson", "david@example.com", "555-7777", ownerAddress);

        PetEntity pet = new PetEntity();
        pet.setName("Coco");
        pet.setStatus(Status.AVAILABLE);

        // Act - Use helper method to establish bidirectional relationship
        owner.addPet(pet);

        // Assert - Both sides should be set
        assertThat(pet.getOwner()).isEqualTo(owner);
        assertThat(owner.getPets()).contains(pet);

        // Save and verify persistence
        Owner savedOwner = ownerRepository.save(owner);
        entityManager.flush();
        entityManager.clear();

        // Reload and verify both sides still connected
        Owner foundOwner = ownerRepository.findById(savedOwner.getId()).orElseThrow();
        PetEntity foundPet = foundOwner.getPets().get(0);
        assertThat(foundPet.getOwner().getId()).isEqualTo(foundOwner.getId());
    }

    /**
     * Test complex cascade scenario: Owner -> Pet -> (MedicalRecords, Vaccinations, Insurance)
     * Everything should cascade from the owner.
     */
    @Test
    @Transactional
    public void testComplexCascade_OwnerToPetToAllRelatedEntities() {
        // Arrange - Build entire object graph
        Address ownerAddress = new Address("777 Cedar Ln", "Brookline", "MA", "02445", "USA");
        Owner owner = new Owner("Emma", "Brown", "emma@example.com", "555-3333", ownerAddress);

        PetEntity pet = new PetEntity();
        pet.setName("Oscar");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        // Add insurance
        PetInsurance insurance = new PetInsurance(
            "POL-11111",
            "BestPet Insurance",
            new BigDecimal("10000.00"),
            new BigDecimal("79.99"),
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            InsuranceStatus.ACTIVE
        );
        pet.setInsurance(insurance);

        // Add medical records
        MedicalRecord record1 = new MedicalRecord(
            pet, drSmith, LocalDate.now().minusMonths(3),
            "Initial exam", "Healthy"
        );
        MedicalRecord record2 = new MedicalRecord(
            pet, drSmith, LocalDate.now().minusMonths(1),
            "Follow-up", "Still healthy"
        );
        pet.addMedicalRecord(record1);
        pet.addMedicalRecord(record2);

        // Add vaccinations
        Vaccination vacc1 = new Vaccination(
            pet, "Rabies", LocalDate.now().minusMonths(2),
            LocalDate.now().plusMonths(10), drSmith
        );
        Vaccination vacc2 = new Vaccination(
            pet, "Bordetella", LocalDate.now().minusMonths(1),
            LocalDate.now().plusMonths(11), drSmith
        );
        pet.addVaccination(vacc1);
        pet.addVaccination(vacc2);

        owner.addPet(pet);

        // Act - Save only the owner, everything should cascade
        Owner savedOwner = ownerRepository.save(owner);
        entityManager.flush();

        // Assert - All entities should be persisted
        assertThat(savedOwner.getId()).isNotNull();
        assertThat(savedOwner.getPets()).hasSize(1);

        PetEntity savedPet = savedOwner.getPets().get(0);
        assertThat(savedPet.getId()).isNotNull();
        assertThat(savedPet.getInsurance().getId()).isNotNull();
        assertThat(savedPet.getMedicalRecords()).hasSize(2);
        assertThat(savedPet.getVaccinations()).hasSize(2);

        // Verify in database
        assertThat(petRepository.count()).isEqualTo(1);
        assertThat(petInsuranceRepository.count()).isEqualTo(1);
        assertThat(medicalRecordRepository.count()).isEqualTo(2);
        assertThat(vaccinationRepository.count()).isEqualTo(2);
    }

    /**
     * Test that removing insurance from pet deletes it (orphan removal).
     */
    @Test
    @Transactional
    public void testOrphanRemoval_RemoveInsuranceFromPet() {
        // Arrange
        PetEntity pet = new PetEntity();
        pet.setName("Daisy");
        pet.setStatus(Status.AVAILABLE);
        pet.setBreed(goldenRetriever);

        PetInsurance insurance = new PetInsurance(
            "POL-22222",
            "SafePet Insurance",
            new BigDecimal("7500.00"),
            new BigDecimal("59.99"),
            LocalDate.now(),
            LocalDate.now().plusYears(1),
            InsuranceStatus.ACTIVE
        );
        pet.setInsurance(insurance);

        PetEntity savedPet = petRepository.save(pet);
        entityManager.flush();
        entityManager.clear();

        Long petId = savedPet.getId();

        // Verify initial state
        assertThat(petInsuranceRepository.count()).isEqualTo(1);

        // Act - Remove insurance from pet
        PetEntity foundPet = petRepository.findById(petId).orElseThrow();
        foundPet.setInsurance(null);

        petRepository.save(foundPet);
        entityManager.flush();

        // Assert - Insurance should be deleted
        assertThat(petInsuranceRepository.count()).isEqualTo(0);
    }
}
