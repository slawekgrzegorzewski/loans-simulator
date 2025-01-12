package pl.sg.loans.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;

class InstallmentTest {
    @ParameterizedTest
    @CsvSource(textBlock = """
            0,1000.00,4000.00,2024-01-01,MONTHLY,5000.00,2024-01-01,2024-01-31
            1,2000.00,4000.00,2024-01-01,MONTHLY,6000.00,2024-02-01,2024-02-29
            2,1000.00,4000.00,2024-01-01,QUARTERLY,5000.00,2024-07-01,2024-09-30
            3,2000.00,4000.00,2024-01-01,QUARTERLY,6000.00,2024-10-01,2024-12-31
            4,1000.00,4000.00,2024-01-01,YEARLY,5000.00,2028-01-01,2028-12-31
            5,2000.00,4000.00,2024-01-01,YEARLY,6000.00,2029-01-01,2029-12-31
            """)
    void testAdditionalGetters(
            int installmentIndex, BigDecimal capital, BigDecimal interest, LocalDate loanStart, InstallmentFrequency installmentFrequency,
            BigDecimal expectedInstallment, LocalDate expectedPeriodStart, LocalDate expectedPeriodEnd) {
        Installment installment = new Installment(
                InstallmentIndex.of(installmentIndex),
                null,
                capital,
                null,
                interest
        );
        Assertions.assertEquals(expectedInstallment, installment.installment());
        Assertions.assertEquals(expectedPeriodStart, installment.periodStart(loanStart, installmentFrequency));
        Assertions.assertEquals(expectedPeriodEnd, installment.periodEnd(loanStart, installmentFrequency));
    }
}