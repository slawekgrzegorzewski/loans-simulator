package pl.sg.loans.simulator.overpayment;

import org.joda.money.Money;
import pl.sg.loans.model.Installment;
import pl.sg.loans.model.Loan;
import pl.sg.loans.simulator.OverpaymentProvider;

import java.util.List;

public class ConstantBudgetOverpayment extends OverpaymentProvider {

    private final Money budget;

    public ConstantBudgetOverpayment(Money budget) {
        this.budget = budget;
    }

    @Override
    public Money getOverpayment(Loan loan, List<Installment> paidInstallments, PendingInstallmentData pendingInstallmentData) {
        return alignWithLoan(
                loan,
                 paidInstallments,
                pendingInstallmentData,
                budget.minus(pendingInstallmentData.capital()).minus(pendingInstallmentData.interest()));
    }
}
