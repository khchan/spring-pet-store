package com.khchan.petstore.service;

import com.khchan.petstore.domain.Address;
import com.khchan.petstore.domain.InsuranceStatus;
import com.khchan.petstore.domain.Owner;
import com.khchan.petstore.domain.PetEntity;
import com.khchan.petstore.domain.Status;
import com.khchan.petstore.repository.PetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class PetCareOrchestrationService {

    private final OwnerService ownerService;
    private final PetManagementService petManagementService;
    private final PetInsuranceService petInsuranceService;
    private final AppointmentService appointmentService;
    private final VeterinarianService veterinarianService;
    private final PetRepository petRepository;
    private final TransactionTemplate requiresNewTemplate;

    @Autowired
    public PetCareOrchestrationService(OwnerService ownerService,
                                       PetManagementService petManagementService,
                                       PetInsuranceService petInsuranceService,
                                       AppointmentService appointmentService,
                                       VeterinarianService veterinarianService,
                                       PetRepository petRepository,
                                       PlatformTransactionManager transactionManager) {
        this.ownerService = ownerService;
        this.petManagementService = petManagementService;
        this.petInsuranceService = petInsuranceService;
        this.appointmentService = appointmentService;
        this.veterinarianService = veterinarianService;
        this.petRepository = petRepository;
        this.requiresNewTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional
    public PetEntity onboardOwnerWithPetAndInsurance(String firstName,
                                                     String lastName,
                                                     String email,
                                                     String phone,
                                                     Address address,
                                                     String petName,
                                                     Long breedId,
                                                     String policyNumber,
                                                     String provider,
                                                     BigDecimal coverageAmount,
                                                     BigDecimal monthlyPremium,
                                                     LocalDate startDate,
                                                     LocalDate endDate) {
        Owner owner = ownerService.registerOwner(firstName, lastName, email, phone, address);
        PetEntity pet = petManagementService.createPet(petName, Status.AVAILABLE, breedId, owner.getId());
        petInsuranceService.enrollPet(
            pet.getId(),
            policyNumber,
            provider,
            coverageAmount,
            monthlyPremium,
            startDate,
            endDate
        );
        return petRepository.findById(pet.getId()).orElseThrow();
    }

    @Transactional
    public void scheduleAppointmentAndUpdateVetSpecialty(Long petId,
                                                         Long veterinarianId,
                                                         LocalDateTime appointmentTime,
                                                         String reason,
                                                         String newSpecialty,
                                                         boolean failAfterUpdate) {
        appointmentService.scheduleAppointment(petId, veterinarianId, appointmentTime, reason);

        requiresNewTemplate.execute(status -> {
            veterinarianService.updateSpecialty(veterinarianId, newSpecialty);
            return null;
        });

        if (failAfterUpdate) {
            throw new IllegalStateException("Forcing rollback after nested update");
        }
    }

    @Transactional
    public void updateOwnerAndPetStatus(Long ownerId,
                                        Long petId,
                                        String newPhone,
                                        Status newStatus,
                                        boolean failAfterUpdate) {
        ownerService.updateContact(ownerId, null, newPhone);
        petManagementService.updateStatus(petId, newStatus);

        if (failAfterUpdate) {
            throw new IllegalStateException("Forcing rollback after updates");
        }
    }

    @Transactional
    public void lapseInsuranceAndScheduleCheckup(Long petId,
                                                 Long insuranceId,
                                                 Long veterinarianId,
                                                 LocalDateTime appointmentTime) {
        petInsuranceService.updateStatus(insuranceId, InsuranceStatus.EXPIRED);
        appointmentService.scheduleAppointment(petId, veterinarianId, appointmentTime, "Policy lapse checkup");
    }
}
