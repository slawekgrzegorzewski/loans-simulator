package pl.sg.loans.simulator.overpayment;

import org.joda.money.Money;
import pl.sg.loans.model.Installment;
import pl.sg.loans.model.Loan;
import pl.sg.loans.simulator.OverpaymentProvider;

import java.math.BigDecimal;
import java.util.List;

public class NoOverpayment extends OverpaymentProvider {

    @Override
    public Money getOverpayment(Loan loan, List<Installment> paidInstallments, PendingInstallmentData pendingInstallmentData) {
        return loan.amount().withAmount(BigDecimal.ZERO);
    }
}
