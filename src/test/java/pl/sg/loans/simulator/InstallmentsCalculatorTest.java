package pl.sg.loans.simulator;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

class InstallmentsCalculatorTest {

    private static final CurrencyUnit PLN = CurrencyUnit.of("PLN");

    @ParameterizedTest
    @MethodSource("fixedCapitalLoan")
    void testFixedCapitalLoan(Money loanAmount, int numberOfInstallments, Money expectedInstallment) {
        Money fixedInstallment = InstallmentsCalculator.fixedCapitalForDecliningLoan(
                loanAmount,
                numberOfInstallments);
        Assertions.assertEquals(expectedInstallment, fixedInstallment);
    }

    public static Stream<Arguments> fixedCapitalLoan() {
        return Stream.of(
                Arguments.of(Money.of(PLN, new BigDecimal("420000.00")), 6, Money.of(PLN, new BigDecimal("70000.00"))),
                Arguments.of(Money.of(PLN, new BigDecimal("1000000.00")), 6, Money.of(PLN, new BigDecimal("166666.67")))
        );
    }

    @ParameterizedTest
    @MethodSource("loansData")
    void testFixedLoan(int numberOfPeriodsInYear, BigDecimal rate, Money loanAmount, int numberOfInstallments, Money expectedInstallment) {
        Money fixedInstallment = InstallmentsCalculator.fixedInstallment(
                numberOfPeriodsInYear,
                rate,
                loanAmount,
                numberOfInstallments);
        Assertions.assertEquals(expectedInstallment, fixedInstallment);
    }

    public static Stream<Arguments> loansData() {
        return Stream.of(
                Arguments.of(2, new BigDecimal("0.05"), Money.of(PLN, new BigDecimal("420000.00")), 6, Money.of(PLN, new BigDecimal("76250.99"))),
                Arguments.of(12, new BigDecimal("0.10"), Money.of(PLN, new BigDecimal("1000000.00")), 6, Money.of(PLN, new BigDecimal("171561.39")))
        );
    }
}