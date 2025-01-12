package pl.sg.loans.model;

import org.joda.money.Money;

import java.time.LocalDate;

public record Loan(LocalDate start,
                   Money amount,
                   int numberOfInstallments,
                   InstallmentType installmentType,
                   InstallmentFrequency installmentFrequency
) {
}
