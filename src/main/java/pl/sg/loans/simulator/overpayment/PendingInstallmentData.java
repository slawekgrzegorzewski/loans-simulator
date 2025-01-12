package pl.sg.loans.simulator.overpayment;

import org.joda.money.Money;
import pl.sg.loans.model.InstallmentIndex;

public record PendingInstallmentData(InstallmentIndex installmentIndex, Money capital, Money interest) {
}