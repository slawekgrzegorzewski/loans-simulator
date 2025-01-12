package pl.sg.loans.simulator.overpayment;

import org.joda.money.Money;
import pl.sg.loans.model.Installment;
import pl.sg.loans.model.InstallmentIndex;
import pl.sg.loans.model.Loan;
import pl.sg.loans.simulator.OverpaymentProvider;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class CustomOverpayment extends OverpaymentProvider {

    private final Map<InstallmentIndex, Money> overpaymentsSchedule;

    public CustomOverpayment(Map<InstallmentIndex, Money> overpaymentsSchedule) {
        this.overpaymentsSchedule = overpaymentsSchedule;
    }

    @Override
    public Money getOverpayment(Loan loan, List<Installment> paidInstallments, PendingInstallmentData pendingInstallmentData) {
        return alignWithLoan(
                loan,
                paidInstallments,
                pendingInstallmentData,
                overpaymentsSchedule.getOrDefault(
                        pendingInstallmentData.installmentIndex(),
                        loan.amount().withAmount(BigDecimal.ZERO)
                )
        );
    }
}
