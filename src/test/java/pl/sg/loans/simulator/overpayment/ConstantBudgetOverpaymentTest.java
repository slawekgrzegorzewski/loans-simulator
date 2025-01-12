package pl.sg.loans.simulator.overpayment;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.sg.loans.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

class ConstantBudgetOverpaymentTest {

    private static final CurrencyUnit PLN = CurrencyUnit.of("PLN");
    private static final Money BUDGET = Money.of(PLN, BigDecimal.valueOf(1000));
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    private static final Money HUNDRED = Money.of(PLN, BigDecimal.valueOf(100), RoundingMode.HALF_EVEN);
    private static final Money TWO_HUNDRED = Money.of(PLN, BigDecimal.valueOf(200), RoundingMode.HALF_EVEN);
    private static final Money THREE_HUNDRED = Money.of(PLN, BigDecimal.valueOf(300), ROUNDING_MODE);
    private static final Money FIVE_HUNDRED = Money.of(PLN, BigDecimal.valueOf(500), ROUNDING_MODE);
    private static final Money TWO_THOUSAND = Money.of(PLN, BigDecimal.valueOf(2000), ROUNDING_MODE);

    @ParameterizedTest
    @MethodSource("constantBudgetOverpayment")
    void testConstantBudgetOverpayment(Loan loan, List<Installment> installments, PendingInstallmentData pendingInstallmentData, Money expectedOverpayment) {
        Money overpayment = new ConstantBudgetOverpayment(BUDGET).getOverpayment(loan, installments, pendingInstallmentData);
        Assertions.assertEquals(expectedOverpayment, overpayment);
    }

    public static Stream<Arguments> constantBudgetOverpayment() {
        Loan loan = new Loan(
                LocalDate.of(2024, 12, 1),
                TWO_THOUSAND,
                12,
                InstallmentType.FIXED,
                InstallmentFrequency.MONTHLY
        );
        return Stream.of(
                Arguments.of(
                        loan,
                        List.of(),
                        new PendingInstallmentData(
                                InstallmentIndex.of(0),
                                FIVE_HUNDRED,
                                THREE_HUNDRED
                        ),
                        //1000-500-300
                        TWO_HUNDRED),
                Arguments.of(
                        loan,
                        List.of(new Installment(
                                InstallmentIndex.of(0),
                                TWO_THOUSAND.getAmount(),
                                Money.of(PLN, BigDecimal.valueOf(1200), ROUNDING_MODE).getAmount(),
                                TWO_HUNDRED.getAmount(),
                                THREE_HUNDRED.getAmount()
                        )),
                        new PendingInstallmentData(
                                InstallmentIndex.of(1),
                                FIVE_HUNDRED,
                                THREE_HUNDRED
                        ),
                        //1000-500-200=200 but remaining loan is 2000-1200-200-500=1900
                        HUNDRED),
                Arguments.of(
                        loan,
                        List.of(new Installment(
                                InstallmentIndex.of(0),
                                TWO_THOUSAND.getAmount(),
                                Money.of(PLN, BigDecimal.valueOf(1200), ROUNDING_MODE).getAmount(),
                                TWO_HUNDRED.getAmount(),
                                THREE_HUNDRED.getAmount()
                        )),
                        new PendingInstallmentData(
                                InstallmentIndex.of(1),
                                Money.of(PLN, BigDecimal.valueOf(600), ROUNDING_MODE),
                                THREE_HUNDRED
                        ),
                        //1000-600-300=100 but remaining loan is 2000-1200-200-600=0
                        Money.of(PLN, BigDecimal.valueOf(0), RoundingMode.HALF_EVEN)),
                Arguments.of(
                        loan,
                        List.of(new Installment(
                                InstallmentIndex.of(0),
                                TWO_THOUSAND.getAmount(),
                                Money.of(PLN, BigDecimal.valueOf(800), ROUNDING_MODE).getAmount(),
                                TWO_HUNDRED.getAmount(),
                                THREE_HUNDRED.getAmount()
                        )),
                        new PendingInstallmentData(
                                InstallmentIndex.of(1),
                                Money.of(PLN, BigDecimal.valueOf(700), ROUNDING_MODE),
                                THREE_HUNDRED
                        ),
                        //1000-700-300=0
                        Money.of(PLN, BigDecimal.valueOf(0), RoundingMode.HALF_EVEN))
        );
    }
}