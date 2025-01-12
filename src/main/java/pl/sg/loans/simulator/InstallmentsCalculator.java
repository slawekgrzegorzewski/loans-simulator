package pl.sg.loans.simulator;

import org.jetbrains.annotations.NotNull;
import org.joda.money.Money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InstallmentsCalculator {

    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public static @NotNull Money fixedCapitalForDecliningLoan(Money loanAmount, int numberOfInstallments) {
        return loanAmount.dividedBy(numberOfInstallments, ROUNDING_MODE);
    }

    public static @NotNull Money fixedInstallment(
            int numberOfPeriodsInYear,
            BigDecimal nextInstallmentRate,
            Money capitalLeft,
            int numberOfInstallments) {
        // https://pl.wikipedia.org/wiki/Raty_r%C3%B3wne
        Money N = capitalLeft;
        BigDecimal r = nextInstallmentRate;
        BigDecimal k = BigDecimal.valueOf(numberOfPeriodsInYear);
        int n = numberOfInstallments;

        BigDecimal factor = r.divide(
                k.multiply(
                        BigDecimal.ONE.subtract(
                                k.divide(k.add(r), 10, ROUNDING_MODE).pow(n))),
                10, ROUNDING_MODE);

        return N.multipliedBy(factor, ROUNDING_MODE);
    }
}
