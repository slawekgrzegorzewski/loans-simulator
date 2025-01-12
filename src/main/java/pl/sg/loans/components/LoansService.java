package pl.sg.loans.components;

import org.joda.money.Money;
import org.springframework.stereotype.Component;
import pl.sg.loans.entities.Loan;
import pl.sg.loans.model.InstallmentFrequency;
import pl.sg.loans.model.InstallmentType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LoansService {

    private final LoanRepository loanRepository;

    public LoansService(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public Optional<Loan> find(UUID publicId) {
        return loanRepository.findByPublicId(publicId);
    }

    public List<Loan> getLoans() {
        return loanRepository.findAll();
    }

    public Loan createLoan(LocalDate start,
                           Money amount,
                           BigDecimal rate,
                           int numberOfInstallments,
                           InstallmentType installmentType,
                           InstallmentFrequency installmentFrequency) {
        Loan loan = new Loan();
        loan.setStart(start);
        loan.setAmount(amount);
        loan.setRate(rate);
        loan.setNumberOfInstallments(numberOfInstallments);
        loan.setInstallmentType(installmentType);
        loan.setInstallmentType(installmentType);
        loan.setInstallmentFrequency(installmentFrequency);
        return loanRepository.save(loan);
    }
}
