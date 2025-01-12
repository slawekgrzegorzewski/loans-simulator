package pl.sg.loans.simulator;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pl.sg.loans.model.*;
import pl.sg.loans.simulator.overpayment.CustomOverpayment;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class LoanRepaymentSimulatorTest {
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2024-12-01T00:00:00.00Z"), ZoneId.of("UTC"));
    private static final BigDecimal LOAN_AMOUNT = new BigDecimal("1000000.00");
    private static final BigDecimal LOAN_RATE = new BigDecimal("0.10");
    private static final int NUMBER_OF_INSTALLMENTS = 6;
    private static final CurrencyUnit PLN = CurrencyUnit.of("PLN");

    private static Map<String, List<Installment>> expectedInstallments;

    @BeforeAll
    static void setUp() throws URISyntaxException, IOException {
        URL url = LoanRepaymentSimulatorTest.class.getClassLoader().getResource("installments/loanRepaymentScheduleTest.csv");
        assert url != null;
        Path path = Paths.get(url.toURI());
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        expectedInstallments = lines.stream().skip(1)
                .filter(line -> !line.isBlank())
                .map(line -> line.split(";"))
                .collect(Collectors.groupingBy(
                        fields -> fields[0],
                        Collectors.mapping(
                                fields -> new Installment(
                                        InstallmentIndex.parse(fields[1]),
                                        new BigDecimal(fields[2]),
                                        new BigDecimal(fields[3]),
                                        new BigDecimal(fields[4]),
                                        new BigDecimal(fields[5])),
                                Collectors.toList()
                        )
                ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void testSimulation(String label, Loan loan, Map<InstallmentIndex, Money> overpayments) {
        LoanRepaymentSimulator loanRepaymentSimulator = new LoanRepaymentSimulator();
        List<Installment> installments = loanRepaymentSimulator.simulate(loan, new CustomOverpayment(overpayments), LOAN_RATE);
        Assertions.assertEquals(expectedInstallments.get(label), installments);
        Assertions.assertEquals(
                LOAN_AMOUNT,
                installments.stream().map(installment -> installment.capital().add(installment.overpayment())).reduce(BigDecimal.ZERO, BigDecimal::add)
        );
    }

    public static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of(
                        "declining, monthly, no overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.MONTHLY
                        ),
                        Map.of()
                ),
                Arguments.of(
                        "declining, monthly, overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.MONTHLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(3), Money.of(PLN, BigDecimal.valueOf(3000))
                        )
                ),
                Arguments.of(
                        "declining, monthly, overpayments early finish",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.MONTHLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(2), Money.of(PLN, new BigDecimal("450000.00")),
                                InstallmentIndex.of(3), Money.of(PLN, new BigDecimal("32833.33"))
                        )
                ),
                Arguments.of(
                        "declining, quarterly, no overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.QUARTERLY
                        ),
                        Map.of()
                ),
                Arguments.of(
                        "declining, quarterly, overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.QUARTERLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(3), Money.of(PLN, BigDecimal.valueOf(3000))
                        )
                ),
                Arguments.of(
                        "declining, quarterly, overpayments early finish",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.QUARTERLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(2), Money.of(PLN, new BigDecimal("499250.00"))
                        )
                ),
                Arguments.of(
                        "declining, yearly, no overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.YEARLY
                        ),
                        Map.of()
                ),
                Arguments.of(
                        "declining, yearly, overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.YEARLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(3), Money.of(PLN, BigDecimal.valueOf(3000))
                        )
                ),
                Arguments.of(
                        "declining, yearly, overpayments early finish",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.DECLINING,
                                InstallmentFrequency.YEARLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(2), Money.of(PLN, new BigDecimal("499250.00"))
                        )
                ),
                Arguments.of(
                        "fixed, monthly, no overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.MONTHLY
                        ),
                        Map.of()
                ),
                Arguments.of(
                        "fixed, monthly, overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.MONTHLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(3), Money.of(PLN, BigDecimal.valueOf(3000))
                        )
                ),
                Arguments.of(
                        "fixed, monthly, overpayments early finish",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.MONTHLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(2), Money.of(PLN, new BigDecimal("505470.68"))
                        )
                ),
                Arguments.of(
                        "fixed, quarterly, no overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.QUARTERLY
                        ),
                        Map.of()
                ),
                Arguments.of(
                        "fixed, quarterly, overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.QUARTERLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(3), Money.of(PLN, BigDecimal.valueOf(3000))
                        )
                ),
                Arguments.of(
                        "fixed, quarterly, overpayments early finish",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.QUARTERLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(2), Money.of(PLN, new BigDecimal("517751.82"))
                        )
                ),
                Arguments.of(
                        "fixed, yearly, no overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.YEARLY
                        ),
                        Map.of()
                ),
                Arguments.of(
                        "fixed, yearly, overpayments",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.YEARLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(3), Money.of(PLN, BigDecimal.valueOf(3000))
                        )
                ),
                Arguments.of(
                        "fixed, yearly, overpayments early finish",
                        new Loan(
                                LocalDate.now(CLOCK),
                                Money.of(PLN, LOAN_AMOUNT),
                                NUMBER_OF_INSTALLMENTS,
                                InstallmentType.FIXED,
                                InstallmentFrequency.YEARLY
                        ),
                        Map.of(
                                InstallmentIndex.of(1), Money.of(PLN, BigDecimal.valueOf(1000)),
                                InstallmentIndex.of(2), Money.of(PLN, new BigDecimal("570215.04"))
                        )
                )
        );
    }
}