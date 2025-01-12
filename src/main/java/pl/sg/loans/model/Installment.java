package pl.sg.loans.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Installment(InstallmentIndex installmentIndex,
                          BigDecimal remainingCapitalAtPeriodStart,
                          BigDecimal capital,
                          BigDecimal overpayment,
                          BigDecimal interest
) {
    public BigDecimal installment() {
        return capital.add(interest);
    }

    public LocalDate periodStart(LocalDate loanStart, InstallmentFrequency installmentFrequency) {
        return switch (installmentFrequency) {
            case MONTHLY -> loanStart.plusMonths(installmentIndex.index());
            case QUARTERLY -> loanStart.plusMonths(3L * installmentIndex.index());
            case YEARLY -> loanStart.plusYears(installmentIndex.index());
        };
    }

    public LocalDate periodEnd(LocalDate loanStart, InstallmentFrequency installmentFrequency) {
        return switch (installmentFrequency) {
            case MONTHLY -> loanStart.plusMonths(installmentIndex.index() + 1).minusDays(1);
            case QUARTERLY -> loanStart.plusMonths(3L * (installmentIndex.index() + 1)).minusDays(1);
            case YEARLY -> loanStart.plusYears(installmentIndex.index() + 1).minusDays(1);
        };
    }
}
