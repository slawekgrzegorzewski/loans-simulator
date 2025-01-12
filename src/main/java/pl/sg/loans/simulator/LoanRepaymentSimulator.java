package pl.sg.loans.simulator;

import org.jetbrains.annotations.NotNull;
import org.joda.money.Money;
import org.springframework.stereotype.Component;
import pl.sg.loans.model.Installment;
import pl.sg.loans.model.InstallmentIndex;
import pl.sg.loans.model.Loan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LoanRepaymentSimulator {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public List<Installment> simulateRepayment(Loan loan, Map<InstallmentIndex, Money> overpayments, BigDecimal rate) {
        return switch (loan.installmentType()) {
            case DECLINING -> simulateDecliningLoan(loan, overpayments, rate);
            case FIXED -> simulateFixedLoan(loan, overpayments, rate);
        };
    }

    private @NotNull List<Installment> simulateDecliningLoan(Loan loan, Map<InstallmentIndex, Money> overpayments, BigDecimal rate) {
        List<Installment> installments = new ArrayList<>();
        Money capitalLeft = loan.amount();
        Money capital = calculateDecliningLoanCapital(loan.amount(), loan.numberOfInstallments());
        while (!capitalLeft.isZero()) {
            InstallmentIndex installmentIndex = installments.isEmpty() ? InstallmentIndex.of(0) : installments.getLast().installmentIndex().next();
            BigDecimal periodRate = rate.divide(BigDecimal.valueOf(loan.installmentFrequency().numberOfPeriodsInYear()), 10, ROUNDING_MODE);
            Money periodInterest = capitalLeft.multipliedBy(periodRate, ROUNDING_MODE);
            Money overpayment = overpayments.getOrDefault(
                    installmentIndex,
                    Money.of(loan.amount().getCurrencyUnit(), BigDecimal.ZERO));
            if (installmentIndex.number() == loan.numberOfInstallments()) {
                capital = capitalLeft;
            }
            installments.add(new Installment(installmentIndex, capitalLeft.getAmount(), capital.getAmount(), overpayment.getAmount(), periodInterest.getAmount()));
            capitalLeft = capitalLeft.minus(capital.getAmount()).minus(overpayment);
            if (!overpayment.isZero()) {
                capital = calculateDecliningLoanCapital(capitalLeft, loan.numberOfInstallments() - installments.size());
            }
        }
        return installments;
    }

    private @NotNull Money calculateDecliningLoanCapital(Money loanAmount, int numberOfInstallments) {
        return loanAmount.dividedBy(numberOfInstallments, ROUNDING_MODE);
    }

    private @NotNull List<Installment> simulateFixedLoan(Loan loan, Map<InstallmentIndex, Money> overpayments, BigDecimal rate) {
        List<Installment> installments = new ArrayList<>();
        Money capitalLeft = loan.amount();
        BigDecimal numberOfPeriodsInYear = BigDecimal.valueOf(loan.installmentFrequency().numberOfPeriodsInYear());
        BigDecimal periodRate = rate.divide(numberOfPeriodsInYear, 10, ROUNDING_MODE);
        Money installmentAmount = calculateFixedLoanCapital(numberOfPeriodsInYear, rate, capitalLeft, loan.numberOfInstallments());
        while (!capitalLeft.isZero()) {
            InstallmentIndex installmentIndex = installments.isEmpty() ? InstallmentIndex.of(0) : installments.getLast().installmentIndex().next();
            Money interest = capitalLeft.multipliedBy(periodRate, ROUNDING_MODE);
            Money capital = installmentAmount.minus(interest);
            Money overpayment = overpayments.getOrDefault(
                    installmentIndex,
                    Money.of(loan.amount().getCurrencyUnit(), BigDecimal.ZERO));

            if (installmentIndex.number() == loan.numberOfInstallments()) {
                capital = capitalLeft;
            }

            installments.add(new Installment(installmentIndex, capitalLeft.getAmount(), capital.getAmount(), overpayment.getAmount(), interest.getAmount()));
            capitalLeft = capitalLeft.minus(capital.getAmount()).minus(overpayment);
            if (!overpayment.isZero()) {
                installmentAmount = calculateFixedLoanCapital(
                        numberOfPeriodsInYear,
                        rate,
                        capitalLeft,
                        loan.numberOfInstallments() - installments.size()
                );
            }
        }
        return installments;
    }

    private @NotNull Money calculateFixedLoanCapital(BigDecimal numberOfPeriodsInYear, BigDecimal nextInstallmentRate, Money capitalLeft, int numberOfInstallments) {
        BigDecimal factor = nextInstallmentRate.divide(
                numberOfPeriodsInYear.multiply(
                        BigDecimal.ONE.subtract(
                                numberOfPeriodsInYear.divide(
                                                numberOfPeriodsInYear.add(nextInstallmentRate), 10, ROUNDING_MODE)
                                        .pow(numberOfInstallments))), 10, ROUNDING_MODE);
        return capitalLeft.multipliedBy(factor, ROUNDING_MODE);
    }
}
