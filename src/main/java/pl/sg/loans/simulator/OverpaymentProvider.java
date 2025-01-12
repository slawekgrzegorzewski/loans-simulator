package pl.sg.loans.simulator;

import org.joda.money.Money;
import pl.sg.loans.model.Installment;
import pl.sg.loans.model.Loan;
import pl.sg.loans.simulator.overpayment.PendingInstallmentData;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

public abstract class OverpaymentProvider {

    /**
     * Provide overpayment amount for pending installment - it will always be non-negative and aligned with loan
     * which means that applying this overpayment will never result with a negative loan balance
     * @param loan - a loan to pay
     * @param paidInstallments - already paid installments prior to pending installment
     * @param pendingInstallmentData - an installment overpayment will be provided for
     * @return an overpayment value
     */
    public abstract Money getOverpayment(Loan loan, List<Installment> paidInstallments, PendingInstallmentData pendingInstallmentData);

    protected Money alignWithLoan(Loan loan, List<Installment> paidInstallments, PendingInstallmentData pendingInstallmentData, Money overpayment) {
        overpayment = assurePositiveOrZero(overpayment);
        Money capitalLeft = calculateCapitalLeft(loan, paidInstallments, pendingInstallmentData);
        Money capitalLeftAfterOverpayment = capitalLeft.minus(overpayment);
        overpayment = capitalLeftAfterOverpayment.isNegative()
                ? capitalLeft
                : overpayment;
        return assurePositiveOrZero(overpayment);
    }

    private Money assurePositiveOrZero(Money overpayment) {
        return overpayment.isNegative()
                ? overpayment.withAmount(BigDecimal.ZERO)
                : overpayment;
    }

    private Money calculateCapitalLeft(Loan loan, List<Installment> paidInstallments, PendingInstallmentData pendingInstallmentData) {
        BigDecimal alreadyPaid = paidInstallments.stream()
                .flatMap(installment -> Stream.of(installment.capital(), installment.overpayment()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return loan.amount()
                .minus(alreadyPaid)
                .minus(pendingInstallmentData.capital());
    }
}