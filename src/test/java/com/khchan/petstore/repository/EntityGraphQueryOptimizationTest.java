package com.khchan.petstore.repository;

import com.khchan.petstore.domain.*;
import com.khchan.petstore.test.DataSourceProxyConfig;
import com.khchan.petstore.test.JpaQueryTrackingRule;
import com.khchan.petstore.test.QueryCounter;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for query optimization with complex entity graphs.
 *
 * Demonstrates:
 * 1. N+1 query problems with lazy loading
 * 2. Solutions using JOIN FETCH
 * 3. Entity Graph usage
 * 4. Batch fetching strategies
 */
@SpringBootTest
@Import(DataSourceProxyConfig.class)
public class EntityGraphQueryOptimizationTest {

    @RegisterExtension
    JpaQueryTrackingRule tracking = new JpaQueryTrackingRule()
            .printQueriesOnFailure(true);

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private BreedRepository breedRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private ClinicRepository clinicRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private VaccinationRepository vaccinationRepository;

    @Autowired
    private EntityManager entityManager;

    private Owner owner1;
    private Owner owner2;
    private Breed goldenRetriever;
    private Breed labrador;
    private Veterinarian drSmith;
    private Long clinicId;

    @BeforeEach
    @Transactional
    public void setUp() {
        // Clean up - delete in order to respect foreign key constraints
        appointmentRepository.deleteAll();
        medicalRecordRepository.deleteAll();
        vaccinationRepository.deleteAll();
        ownerRepository.deleteAll();
        petRepository.deleteAll();
        breedRepository.deleteAll();
        veterinarianRepository.deleteAll();
        clinicRepository.deleteAll();

        entityManager.flush();
        entityManager.clear();

        // Create breeds
        goldenRetriever = breedRepository.save(new Breed("Golden Retriever", "Friendly", Size.LARGE));
        labrador = breedRepository.save(new Breed("Labrador", "Energetic", Size.LARGE));

        // Create clinic and vet
        Address clinicAddress = new Address("123 Vet St", "Boston", "MA", "02101", "USA");
        Clinic clinic = clinicRepository.save(new Clinic("Main Clinic", "555-0100", clinicAddress));
        clinicId = clinic.getId();
        drSmith = new Veterinarian("John", "Smith", "General", "VET123");
        drSmith.setClinic(clinic);
        drSmith = veterinarianRepository.save(drSmith);

        // Create owner 1 with 3 pets
        Address addr1 = new Address("100 Main St", "Boston", "MA", "02101", "USA");
        owner1 = new Owner("Alice", "Johnson", "alice@example.com", "555-1111", addr1);

        for (int i = 1; i <= 3; i++) {
            PetEntity pet = new PetEntity();
            pet.setName("Pet" + i);
            pet.setStatus(Status.AVAILABLE);
            pet.setBreed(goldenRetriever);

            // Add medical records
            MedicalRecord record = new MedicalRecord(
                pet, drSmith, LocalDate.now().minusDays(i),
                "Checkup " + i, "Healthy"
            );
            pet.addMedicalRecord(record);

            // Add vaccination
            Vaccination vacc = new Vaccination(
                pet, "Rabies", LocalDate.now().minusMonths(i),
                LocalDate.now().plusMonths(12 - i), drSmith
            );
            pet.addVaccination(vacc);

            owner1.addPet(pet);
        }

        owner1 = ownerRepository.save(owner1);

        // Create owner 2 with 2 pets
        Address addr2 = new Address("200 Oak Ave", "Cambridge", "MA", "02139", "USA");
        owner2 = new Owner("Bob", "Smith", "bob@example.com", "555-2222", addr2);

        for (int i = 1; i <= 2; i++) {
            PetEntity pet = new PetEntity();
            pet.setName("Dog" + i);
            pet.setStatus(Status.AVAILABLE);
            pet.setBreed(labrador);

            MedicalRecord record = new MedicalRecord(
                pet, drSmith, LocalDate.now().minusDays(i * 5),
                "Annual checkup", "Good health"
            );
            pet.addMedicalRecord(record);

            owner2.addPet(pet);
        }

        owner2 = ownerRepository.save(owner2);

        entityManager.flush();
        entityManager.clear();
        QueryCounter.reset();
    }

    /**
     * Demonstrates N+1 problem when accessing lazy-loaded pets.
     * Without JOIN FETCH, this causes 1 query for owners + N queries for pets.
     */
    @Test
    @Transactional
    public void testNPlusOne_LazyLoadingPets() {
        // Act - Find all owners and access their pets
        List<Owner> owners = ownerRepository.findAll();

        // This triggers N+1: 1 query for owners + 1 query per owner for pets
        for (Owner owner : owners) {
            owner.getPets().size(); // Force lazy load
        }

        // Assert - Should see N+1 pattern
        int selectCount = QueryCounter.getSelectCount();
        assertThat(selectCount).isGreaterThan(2); // 1 for owners + 2 for pets = 3+
    }

    /**
     * Solution to N+1: Use JOIN FETCH to load pets eagerly.
     */
    @Test
    @Transactional
    public void testOptimized_JoinFetchPets() {
        QueryCounter.reset();

        // Act - Use custom query with JOIN FETCH
        Owner owner = ownerRepository.findByIdWithPets(owner1.getId()).orElseThrow();

        // Access pets - should NOT trigger additional query
        assertThat(owner.getPets()).hasSize(3);

        // Assert - Should only be 1 SELECT query
        assertThat(QueryCounter.getSelectCount()).isEqualTo(1);
    }

    /**
     * Demonstrates N+1 when accessing multiple levels of relationships.
     * Accessing owner -> pets -> breed causes multiple queries.
     */
    @Test
    @Transactional
    public void testNPlusOne_MultipleLevels_OwnerPetBreed() {
        QueryCounter.reset();

        // Act
        List<Owner> owners = ownerRepository.findAll();

        for (Owner owner : owners) {
            for (PetEntity pet : owner.getPets()) {
                // Accessing breed triggers additional queries
                if (pet.getBreed() != null) {
                    pet.getBreed().getName();
                }
            }
        }

        // Assert - Multiple SELECTs due to lazy loading
        int selectCount = QueryCounter.getSelectCount();
        assertThat(selectCount).isGreaterThan(3);
    }

    /**
     * Demonstrates N+1 when accessing pet -> medical records.
     */
    @Test
    @Transactional
    public void testNPlusOne_PetToMedicalRecords() {
        QueryCounter.reset();

        // Act - Get all pets and access their medical records
        List<PetEntity> pets = petRepository.findAll();

        for (PetEntity pet : pets) {
            // Each access triggers a new query
            pet.getMedicalRecords().size();
        }

        // Assert - N+1 pattern
        int selectCount = QueryCounter.getSelectCount();
        assertThat(selectCount).isGreaterThan(1); // 1 for pets + N for medical records
    }

    /**
     * Demonstrates that fetching multiple bags (List collections) throws MultipleBagFetchException.
     * This is a Hibernate limitation - cannot fetch multiple Lists in a single query.
     */
    @Test
    @Transactional
    public void testMultipleBagFetchException_WithNestedCollections() {
        QueryCounter.reset();

        // Act & Assert - This query will fail because it tries to fetch multiple bags
        // Owner.pets is a List, and PetEntity.medicalRecords is also a List
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> entityManager.createQuery(
                "SELECT DISTINCT o FROM Owner o " +
                "LEFT JOIN FETCH o.pets p " +
                "LEFT JOIN FETCH p.breed " +
                "LEFT JOIN FETCH p.medicalRecords",
                Owner.class
            ).getResultList()
        )).hasMessageContaining("MultipleBagFetchException");
    }

    /**
     * Test fetching with multiple collections demonstrates the MultipleBagFetchException.
     * Hibernate cannot fetch multiple List collections in a single query.
     * Best practice is to fetch collections in separate queries or use batch fetching.
     */
    @Test
    @Transactional
    public void testCartesianProduct_MultipleCollections_ThrowsException() {
        QueryCounter.reset();

        // This query will fail because both medicalRecords and vaccinations are Lists
        // Hibernate cannot fetch multiple bags simultaneously
        assertThat(org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> entityManager.createQuery(
                "SELECT DISTINCT p FROM PetEntity p " +
                "LEFT JOIN FETCH p.medicalRecords " +
                "LEFT JOIN FETCH p.vaccinations",
                PetEntity.class
            ).getResultList()
        )).hasMessageContaining("MultipleBagFetchException");

        // Note: To avoid this, change collections from List to Set,
        // or fetch collections in separate queries
    }

    /**
     * Best practice: Fetch collections in separate queries to avoid Cartesian product.
     */
    @Test
    @Transactional
    public void testOptimized_SeparateQueriesForCollections() {
        QueryCounter.reset();

        // Query 1: Fetch pets with medical records
        List<PetEntity> pets = entityManager.createQuery(
            "SELECT DISTINCT p FROM PetEntity p LEFT JOIN FETCH p.medicalRecords",
            PetEntity.class
        ).getResultList();

        // Query 2: Fetch vaccinations separately
        if (!pets.isEmpty()) {
            entityManager.createQuery(
                "SELECT DISTINCT p FROM PetEntity p " +
                "LEFT JOIN FETCH p.vaccinations " +
                "WHERE p IN :pets",
                PetEntity.class
            ).setParameter("pets", pets).getResultList();
        }

        // Assert - Should have 2 queries (one for each collection)
        assertThat(QueryCounter.getSelectCount()).isEqualTo(2);

        // Verify medical records are loaded (all pets have medical records)
        for (PetEntity pet : pets) {
            assertThat(pet.getMedicalRecords()).isNotEmpty();
        }

        // Verify at least some pets have vaccinations (owner1's pets have them)
        boolean someHaveVaccinations = pets.stream()
            .anyMatch(pet -> !pet.getVaccinations().isEmpty());
        assertThat(someHaveVaccinations).isTrue();
    }

    /**
     * Test efficient loading of clinic with veterinarians.
     */
    @Test
    @Transactional
    public void testOptimized_ClinicWithVeterinarians() {
        QueryCounter.reset();

        // Use repository method with JOIN FETCH
        Clinic clinic = clinicRepository.findByIdWithVeterinarians(clinicId).orElseThrow();

        // Access veterinarians - should not trigger new query
        assertThat(clinic.getVeterinarians()).hasSize(1);

        // Assert - Only 1 query
        assertThat(QueryCounter.getSelectCount()).isEqualTo(1);
    }

    /**
     * Demonstrate read-only query optimization.
     * Read-only queries can be more efficient as Hibernate doesn't need to track changes.
     */
    @Test
    @Transactional
    public void testReadOnly_QueryPerformance() {
        QueryCounter.reset();

        // Set query as read-only
        List<Owner> owners = entityManager.createQuery(
            "SELECT o FROM Owner o",
            Owner.class
        ).setHint("org.hibernate.readOnly", true).getResultList();

        assertThat(owners).hasSize(2);

        // Read-only entities are not tracked for dirty checking
        // This can improve performance for large read operations
    }

    /**
     * Test proper use of DISTINCT with JOIN FETCH to avoid duplicate results.
     */
    @Test
    @Transactional
    public void testDistinct_WithJoinFetch() {
        // Without DISTINCT, JOIN FETCH can return duplicate parent entities
        List<Owner> ownersWithDuplicates = entityManager.createQuery(
            "SELECT o FROM Owner o LEFT JOIN FETCH o.pets",
            Owner.class
        ).getResultList();

        // With DISTINCT, duplicates are removed
        List<Owner> ownersNoDuplicates = entityManager.createQuery(
            "SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.pets",
            Owner.class
        ).getResultList();

        // Assert - DISTINCT gives correct count
        assertThat(ownersNoDuplicates).hasSize(2);

        // Without DISTINCT, you get one row per pet (if owner has 3 pets, 3 owner rows)
        // Note: Hibernate 6+ uses "distinct" automatically in memory even without keyword
    }

    /**
     * Test pagination with JOIN FETCH.
     * Note: Pagination with JOIN FETCH can be problematic and may trigger warnings.
     */
    @Test
    @Transactional
    public void testPagination_ConsiderationsWithJoinFetch() {
        // This works but Hibernate may warn about in-memory pagination
        List<Owner> firstPage = entityManager.createQuery(
            "SELECT DISTINCT o FROM Owner o LEFT JOIN FETCH o.pets",
            Owner.class
        ).setFirstResult(0).setMaxResults(1).getResultList();

        assertThat(firstPage).hasSize(1);

        // Best practice for pagination with collections:
        // 1. Paginate IDs first
        // 2. Then fetch full entities with JOIN FETCH
        // Or use @BatchSize on collections
    }

    /**
     * Demonstrate efficient deletion with cascade.
     */
    @Test
    @Transactional
    public void testEfficientDeletion_WithCascade() {
        QueryCounter.reset();

        // Delete owner - should cascade to pets and their related entities
        ownerRepository.deleteById(owner1.getId());
        entityManager.flush();

        // Verify deletion
        assertThat(ownerRepository.findById(owner1.getId())).isEmpty();

        // Due to cascade, pets should also be deleted
        List<PetEntity> remainingPets = petRepository.findAll();
        assertThat(remainingPets).hasSize(2); // Only owner2's pets remain
    }
}
