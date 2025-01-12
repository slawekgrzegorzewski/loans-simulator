package pl.sg.loans.simulator;

import org.jetbrains.annotations.NotNull;
import org.joda.money.Money;
import org.springframework.stereotype.Component;
import pl.sg.loans.model.Installment;
import pl.sg.loans.model.InstallmentIndex;
import pl.sg.loans.model.Loan;
import pl.sg.loans.simulator.overpayment.PendingInstallmentData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static pl.sg.loans.simulator.InstallmentsCalculator.fixedCapitalForDecliningLoan;
import static pl.sg.loans.simulator.InstallmentsCalculator.fixedInstallment;

@Component
public class LoanRepaymentSimulator {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public List<Installment> simulate(Loan loan, OverpaymentProvider overpaymentProvider, BigDecimal rate) {
        return switch (loan.installmentType()) {
            case DECLINING -> simulateDecliningLoan(loan, overpaymentProvider, rate);
            case FIXED -> simulateFixedLoan(loan, overpaymentProvider, rate);
        };
    }

    private @NotNull List<Installment> simulateDecliningLoan(
            Loan loan,
            OverpaymentProvider overpaymentProvider,
            BigDecimal rate) {
        List<Installment> installments = new ArrayList<>();
        Money capitalLeft = loan.amount();
        BigDecimal periodRate = rate.divide(BigDecimal.valueOf(loan.installmentFrequency().numberOfPeriodsInYear()), 10, ROUNDING_MODE);
        Money fixedCapital = fixedCapitalForDecliningLoan(loan.amount(), loan.numberOfInstallments());
        while (!capitalLeft.isZero()) {
            InstallmentIndex installmentIndex = nextInstallmentIndex(installments);
            Money interest = capitalLeft.multipliedBy(periodRate, ROUNDING_MODE);

            if (installmentIndex.number() == loan.numberOfInstallments()) {
                installments.add(createLastInstallment(installmentIndex, capitalLeft, interest));
                capitalLeft = capitalLeft.withAmount(BigDecimal.ZERO);
            } else {
                Money overpayment = overpaymentProvider.getOverpayment(
                        loan,
                        installments,
                        new PendingInstallmentData(installmentIndex, fixedCapital, interest));
                installments.add(new Installment(
                        installmentIndex,
                        capitalLeft.getAmount(),
                        fixedCapital.getAmount(),
                        overpayment.getAmount(),
                        interest.getAmount()));
                capitalLeft = capitalLeft.minus(fixedCapital.getAmount()).minus(overpayment);
                if (!overpayment.isZero()) {
                    fixedCapital = fixedCapitalForDecliningLoan(capitalLeft, loan.numberOfInstallments() - installments.size());
                }
            }
        }
        return installments;
    }

    private @NotNull List<Installment> simulateFixedLoan(
            Loan loan,
            OverpaymentProvider overpaymentProvider,
            BigDecimal rate) {
        List<Installment> installments = new ArrayList<>();
        Money capitalLeft = loan.amount();
        int numberOfPeriodsInYear = loan.installmentFrequency().numberOfPeriodsInYear();
        BigDecimal periodRate = rate.divide(BigDecimal.valueOf(numberOfPeriodsInYear), 10, ROUNDING_MODE);
        Money fixedInstallment = fixedInstallment(numberOfPeriodsInYear, rate, capitalLeft, loan.numberOfInstallments());
        while (!capitalLeft.isZero()) {
            InstallmentIndex installmentIndex = nextInstallmentIndex(installments);
            Money interest = capitalLeft.multipliedBy(periodRate, ROUNDING_MODE);
            Money capital = fixedInstallment.minus(interest);

            if (installmentIndex.number() == loan.numberOfInstallments()) {
                installments.add(createLastInstallment(installmentIndex, capitalLeft, interest));
                capitalLeft = capitalLeft.withAmount(BigDecimal.ZERO);
            } else {
                Money overpayment = overpaymentProvider.getOverpayment(
                        loan,
                        installments,
                        new PendingInstallmentData(installmentIndex, capital, interest));
                installments.add(new Installment(
                        installmentIndex,
                        capitalLeft.getAmount(),
                        capital.getAmount(),
                        overpayment.getAmount(),
                        interest.getAmount()));
                capitalLeft = capitalLeft.minus(capital.getAmount()).minus(overpayment);
                if (!overpayment.isZero()) {
                    fixedInstallment = fixedInstallment(
                            numberOfPeriodsInYear,
                            rate,
                            capitalLeft,
                            loan.numberOfInstallments() - installments.size()
                    );
                }
            }
        }
        return installments;
    }

    private InstallmentIndex nextInstallmentIndex(List<Installment> installments) {
        return installments.isEmpty() ? InstallmentIndex.of(0) : installments.getLast().installmentIndex().next();
    }

    private Installment createLastInstallment(InstallmentIndex installmentIndex, Money capitalLeft, Money interest) {
        return new Installment(
                installmentIndex,
                capitalLeft.getAmount(),
                capitalLeft.getAmount(),
                BigDecimal.ZERO.setScale(capitalLeft.getScale(), ROUNDING_MODE),
                interest.getAmount());
    }
}
