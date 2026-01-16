package com.khchan.petstore.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents an insurance policy for a pet.
 * Demonstrates bidirectional OneToOne relationship with cascade from Pet side.
 */
@Entity
@Table(name = "pet_insurance")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetInsurance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Bidirectional OneToOne - this is the inverse side (mappedBy).
     * The Pet entity owns the relationship.
     */
    @OneToOne(mappedBy = "insurance", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PetEntity pet;

    private String policyNumber;
    private String provider;
    private BigDecimal coverageAmount;
    private BigDecimal monthlyPremium;
    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private InsuranceStatus status;

    public PetInsurance(String policyNumber, String provider, BigDecimal coverageAmount, BigDecimal monthlyPremium,
                       LocalDate startDate, LocalDate endDate, InsuranceStatus status) {
        this.policyNumber = policyNumber;
        this.provider = provider;
        this.coverageAmount = coverageAmount;
        this.monthlyPremium = monthlyPremium;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public void setPet(PetEntity pet) {
        this.pet = pet;
        if (pet != null && pet.getInsurance() != this) {
            pet.setInsurance(this);
        }
    }
}
