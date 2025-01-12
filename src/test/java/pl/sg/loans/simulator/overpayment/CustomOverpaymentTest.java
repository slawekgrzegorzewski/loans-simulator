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
import java.util.Map;
import java.util.stream.Stream;

class CustomOverpaymentTest {

    private static final CurrencyUnit PLN = CurrencyUnit.of("PLN");
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;


    private static final Money ZERO = Money.of(PLN, BigDecimal.ZERO, RoundingMode.HALF_EVEN);
    private static final Money HUNDRED = Money.of(PLN, BigDecimal.valueOf(100), RoundingMode.HALF_EVEN);
    private static final Money TWO_HUNDRED = Money.of(PLN, BigDecimal.valueOf(200), RoundingMode.HALF_EVEN);
    private static final Money THREE_HUNDRED = Money.of(PLN, BigDecimal.valueOf(300), ROUNDING_MODE);
    private static final Money FOUR_HUNDRED = Money.of(PLN, BigDecimal.valueOf(400), ROUNDING_MODE);
    private static final Money FIVE_HUNDRED = Money.of(PLN, BigDecimal.valueOf(500), ROUNDING_MODE);
    private static final Money TWO_THOUSAND = Money.of(PLN, BigDecimal.valueOf(2000), ROUNDING_MODE);

    @ParameterizedTest
    @MethodSource("customOverpayment")
    void testCustomOverpayment(Loan loan,
                               List<Installment> installments,
                               PendingInstallmentData pendingInstallmentData,
                               Map<InstallmentIndex, Money> overpaymentsSchedule,
                               Money expectedOverpayment) {
        Money overpayment = new CustomOverpayment(overpaymentsSchedule).getOverpayment(loan, installments, pendingInstallmentData);
        Assertions.assertEquals(expectedOverpayment, overpayment);
    }

    public static Stream<Arguments> customOverpayment() {
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
                        Map.of(InstallmentIndex.of(1), HUNDRED),
                        ZERO
                ),
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
                                FOUR_HUNDRED,
                                THREE_HUNDRED
                        ),
                        Map.of(InstallmentIndex.of(1), HUNDRED),
                        HUNDRED),
                Arguments.of(
                        loan,
                        List.of(new Installment(
                                InstallmentIndex.of(0),
                                TWO_THOUSAND.getAmount(),
                                Money.of(PLN, BigDecimal.valueOf(1800), ROUNDING_MODE).getAmount(),
                                TWO_HUNDRED.getAmount(),
                                THREE_HUNDRED.getAmount()
                        )),
                        new PendingInstallmentData(
                                InstallmentIndex.of(1),
                                Money.of(PLN, BigDecimal.valueOf(700), ROUNDING_MODE),
                                THREE_HUNDRED
                        ),
                        Map.of(InstallmentIndex.of(1), HUNDRED),
                        ZERO)
        );
    }
}