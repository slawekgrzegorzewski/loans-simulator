package pl.sg.loans.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CompositeType;
import org.hibernate.annotations.RowId;
import org.joda.money.Money;
import pl.sg.loans.database.MoneyType;
import pl.sg.loans.model.InstallmentFrequency;
import pl.sg.loans.model.InstallmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity(name = "loans")
public class Loan {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private UUID publicId = UUID.randomUUID();

    LocalDate start;

    @AttributeOverride(name = "amount", column = @Column(name = "amount", columnDefinition = "numeric(19,4)"))
    @AttributeOverride(name = "currency", column = @Column(name = "currency", updatable = false))
    @CompositeType(MoneyType.class)
    Money amount;

    @Column(columnDefinition = "numeric(19,10)")
    BigDecimal rate;

    int numberOfInstallments;

    @Enumerated(EnumType.STRING)
    InstallmentType installmentType;

    @Enumerated(EnumType.STRING)
    InstallmentFrequency installmentFrequency;

    public long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public LocalDate getStart() {
        return start;
    }

    public void setStart(LocalDate start) {
        this.start = start;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public int getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public void setNumberOfInstallments(int numberOfInstallments) {
        this.numberOfInstallments = numberOfInstallments;
    }

    public InstallmentType getInstallmentType() {
        return installmentType;
    }

    public void setInstallmentType(InstallmentType installmentType) {
        this.installmentType = installmentType;
    }

    public InstallmentFrequency getInstallmentFrequency() {
        return installmentFrequency;
    }

    public void setInstallmentFrequency(InstallmentFrequency installmentFrequency) {
        this.installmentFrequency = installmentFrequency;
    }
}
