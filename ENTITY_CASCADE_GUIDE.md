# Entity Cascade Guide

This document describes the complex entity graph implemented in the Spring Pet Store application and demonstrates JPA cascade best practices.

## Entity Relationship Overview

```
Owner (1) ----< (*) Pet (*) >---- (1) Breed
  |                   |
  |                   +----< (*) MedicalRecord (*) >---- (1) Veterinarian
  |                   |                                        |
  |                   +----< (*) Vaccination   (*) >-----------+
  |                   |                                        |
  |                   +----< (*) Appointment  (*) >-----------+
  |                   |                                        |
  |                   +---- (1) PetInsurance                   |
  |                                                            |
  +- Address (Embedded)                                Clinic (1) ----< (*)
                                                         |
                                                      Address (Embedded)
```

## Cascade Configurations

### 1. Owner → Pet (CASCADE.ALL + orphanRemoval)

```java
@OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
private List<PetEntity> pets;
```

**Behavior:**
- Saving an owner automatically saves all pets
- Updating an owner automatically updates all pets
- Deleting an owner automatically deletes all pets
- Removing a pet from owner's collection deletes the pet from database

**Use Case:** Strong ownership - pets belong exclusively to one owner

### 2. Pet → MedicalRecord (CASCADE.ALL + orphanRemoval)

```java
@OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
private List<MedicalRecord> medicalRecords;
```

**Behavior:**
- Medical records are owned by the pet
- Deleting a pet deletes all medical records
- Removing a record from collection deletes it from database

**Use Case:** Lifecycle dependency - medical records have no meaning without the pet

### 3. Pet → Vaccination (CASCADE.ALL + orphanRemoval)

```java
@OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Vaccination> vaccinations;
```

**Behavior:** Same as medical records

### 4. Pet → Insurance (CASCADE.ALL + orphanRemoval, OneToOne)

```java
@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
private PetInsurance insurance;
```

**Behavior:**
- Insurance policy is owned by the pet
- Deleting a pet deletes its insurance
- Setting insurance to null deletes it from database

**Use Case:** Exclusive relationship - one insurance policy per pet

### 5. Pet → Appointment (CASCADE.PERSIST, MERGE only)

```java
@OneToMany(mappedBy = "pet", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Appointment> appointments;
```

**Behavior:**
- New appointments are saved with the pet
- Updates to appointments cascade
- Deleting a pet does NOT delete appointments (they involve veterinarians too)

**Use Case:** Shared ownership - appointments are important history

### 6. Clinic → Veterinarian (CASCADE.PERSIST, MERGE only)

```java
@OneToMany(mappedBy = "clinic", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
private List<Veterinarian> veterinarians;
```

**Behavior:**
- Deleting a clinic does NOT delete veterinarians
- Veterinarians should be reassigned to another clinic

**Use Case:** Independent lifecycle - veterinarians exist independently

### 7. Pet → Breed (NO cascade)

```java
@ManyToOne(fetch = FetchType.LAZY)
private Breed breed;
```

**Behavior:**
- Breed is a shared reference entity
- Deleting a pet does NOT delete the breed
- Multiple pets can reference the same breed

**Use Case:** Reference data - breeds are shared across many pets

## Orphan Removal vs CASCADE.REMOVE

### Orphan Removal
- Triggered when removing an entity from a collection
- Only works with OneToOne and OneToMany
- Example: `owner.getPets().remove(pet)` → pet is deleted

### CASCADE.REMOVE
- Triggered when explicitly deleting the parent
- Example: `ownerRepository.delete(owner)` → all pets deleted

### Both Together (CASCADE.ALL + orphanRemoval)
- Provides complete lifecycle management
- Child is deleted when:
  1. Parent is deleted (CASCADE.REMOVE)
  2. Child is removed from collection (orphanRemoval)

## Testing Cascade Operations

### EntityCascadeTest
Comprehensive tests covering:
- **CASCADE.PERSIST**: Auto-save child entities
- **CASCADE.MERGE**: Auto-update detached entities
- **CASCADE.REMOVE**: Auto-delete on parent deletion
- **Orphan Removal**: Auto-delete when removed from collection
- **Bidirectional Management**: Helper methods maintain both sides
- **Complex Cascades**: Multi-level cascade operations

### EntityGraphQueryOptimizationTest
Performance tests covering:
- **N+1 Query Detection**: Identifying lazy loading problems
- **JOIN FETCH Solutions**: Efficient data loading
- **Cartesian Product Issues**: Multiple collection fetching
- **Query Optimization**: Best practices for complex graphs

## Best Practices Demonstrated

### 1. Use Helper Methods for Bidirectional Relationships

```java
public void addPet(PetEntity pet) {
    pets.add(pet);
    pet.setOwner(this);  // Maintain both sides
}

public void removePet(PetEntity pet) {
    pets.remove(pet);
    pet.setOwner(null);  // Clean up both sides
}
```

### 2. Lazy Loading by Default

```java
@ManyToOne(fetch = FetchType.LAZY)
private Owner owner;
```

Prevents loading entire object graph unnecessarily.

### 3. Use @ToString.Exclude on Lazy Relationships

```java
@ManyToOne(fetch = FetchType.LAZY)
@ToString.Exclude
private Owner owner;
```

Prevents lazy initialization exceptions when logging.

### 4. Initialize Collections

```java
@OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
private List<MedicalRecord> medicalRecords = new ArrayList<>();
```

Prevents NullPointerException when adding items.

### 5. Choose Cascade Types Carefully

- **CASCADE.ALL + orphanRemoval**: Strong parent-child ownership
- **CASCADE.PERSIST, MERGE**: Shared responsibility
- **No Cascade**: Independent lifecycle (reference data)

### 6. Use JOIN FETCH for Query Optimization

```java
@Query("SELECT o FROM Owner o LEFT JOIN FETCH o.pets WHERE o.id = :id")
Optional<Owner> findByIdWithPets(Long id);
```

Avoids N+1 queries by loading related entities in one query.

### 7. Separate Queries for Multiple Collections

Instead of:
```java
// Can cause Cartesian product
SELECT p FROM Pet p
  LEFT JOIN FETCH p.medicalRecords
  LEFT JOIN FETCH p.vaccinations
```

Do:
```java
// Query 1: Load pets with medical records
SELECT p FROM Pet p LEFT JOIN FETCH p.medicalRecords

// Query 2: Load vaccinations
SELECT p FROM Pet p LEFT JOIN FETCH p.vaccinations WHERE p IN :pets
```

## Entity Summary

| Entity | Cascade From | Cascade To | Orphan Removal |
|--------|--------------|------------|----------------|
| Owner | - | Pet | Yes |
| Pet | Owner | MedicalRecord, Vaccination, Insurance | Yes |
| Pet | - | Appointment | No |
| Breed | - | - | - |
| Clinic | - | Veterinarian | No |
| Veterinarian | Clinic | - | No |
| MedicalRecord | Pet | - | Yes |
| Vaccination | Pet | - | Yes |
| PetInsurance | Pet | - | Yes |
| Appointment | Pet | - | No |

## Running Tests

```bash
# Run cascade tests
mvn test -Dtest=EntityCascadeTest

# Run query optimization tests
mvn test -Dtest=EntityGraphQueryOptimizationTest

# Run all tests
mvn test
```

## Key Learnings

1. **Cascade is powerful but dangerous** - Use carefully to avoid unintended deletions
2. **Orphan removal is even more dangerous** - Only use for true parent-child relationships
3. **Lazy loading prevents performance issues** - But requires careful query design
4. **JOIN FETCH solves N+1 problems** - But can cause Cartesian products with multiple collections
5. **Helper methods ensure consistency** - Always maintain both sides of bidirectional relationships
6. **Reference data should never cascade** - Breeds, categories, etc. are shared
7. **Transaction boundaries matter** - Cascade operations occur within the same transaction
