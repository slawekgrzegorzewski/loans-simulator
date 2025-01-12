package pl.sg.loans.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.exceptions.DgsEntityNotFoundException;
import org.jetbrains.annotations.NotNull;
import pl.sg.graphql.schema.types.*;
import pl.sg.loans.components.LoansService;
import pl.sg.loans.model.InstallmentFrequency;
import pl.sg.loans.model.InstallmentType;
import pl.sg.loans.simulator.LoanRepaymentSimulator;

import java.util.List;
import java.util.Map;

@DgsComponent
public class LoansDataFetcher {

    private final LoanRepaymentSimulator loanRepaymentSimulator;
    private final LoansService loansService;

    public LoansDataFetcher(LoanRepaymentSimulator loanRepaymentSimulator, LoansService loansService) {
        this.loanRepaymentSimulator = loanRepaymentSimulator;
        this.loansService = loansService;
    }

    @DgsQuery
    public List<Loan> loans() {
        return loansService.getLoans().stream()
                .map(this::map)
                .toList();
    }

    @DgsQuery
    public LoanSimulationResult regularSimulation(@InputArgument LoanSimulationInput input) {
        pl.sg.loans.entities.Loan loan = loansService.find(input.getId()).orElseThrow(DgsEntityNotFoundException::new);
        List<Installment> installments = loanRepaymentSimulator.simulateRepayment(mapToModel(loan), Map.of(), loan.getRate())
                .stream()
                .map(installment -> map(loan, installment))
                .toList();
        return LoanSimulationResult.newBuilder().installments(installments).build();
    }

    @DgsMutation
    public LoanCreationResult createLoan(@InputArgument LoanCreationInput input) {
        return LoanCreationResult.newBuilder()
                .id(
                        loansService.createLoan(
                                input.getStart(),
                                input.getAmount(),
                                input.getRate(),
                                input.getNumberOfInstallments(),
                                map(input.getInstallmentType()),
                                map(input.getInstallmentFrequency())
                        ).getPublicId()
                ).build();
    }

    private Loan map(pl.sg.loans.entities.Loan loan) {
        return Loan.newBuilder()
                .id(loan.getPublicId())
                .start(loan.getStart())
                .amount(loan.getAmount())
                .rate(loan.getRate())
                .numberOfInstallments(loan.getNumberOfInstallments())
                .installmentType(map(loan.getInstallmentType()))
                .installmentFrequency(map(loan.getInstallmentFrequency()))
                .build();
    }

    private pl.sg.loans.model.Loan mapToModel(pl.sg.loans.entities.Loan loan) {
        return new pl.sg.loans.model.Loan(
                loan.getStart(),
                loan.getAmount(),
                loan.getNumberOfInstallments(),
                loan.getInstallmentType(),
                loan.getInstallmentFrequency()
        );
    }

    private Installment map(pl.sg.loans.entities.Loan loan, pl.sg.loans.model.Installment installment) {
        return Installment.newBuilder()
                .installmentIndex(installment.installmentIndex().index())
                .amount(installment.installment())
                .periodStart(installment.periodStart(loan.getStart(), loan.getInstallmentFrequency()))
                .periodEnd(installment.periodEnd(loan.getStart(), loan.getInstallmentFrequency()))
                .remainingCapitalAtPeriodStart(installment.remainingCapitalAtPeriodStart())
                .capital(installment.capital())
                .overpayment(installment.overpayment())
                .interest(installment.interest())
                .build();
    }

    private InstallmentType map(pl.sg.graphql.schema.types.InstallmentType installmentType) {
        return switch (installmentType) {
            case DECLINING -> InstallmentType.DECLINING;
            case FIXED -> InstallmentType.FIXED;
        };
    }

    private pl.sg.graphql.schema.types.InstallmentType map(InstallmentType installmentType) {
        return switch (installmentType) {
            case DECLINING -> pl.sg.graphql.schema.types.InstallmentType.DECLINING;
            case FIXED -> pl.sg.graphql.schema.types.InstallmentType.FIXED;
        };
    }

    private InstallmentFrequency map(pl.sg.graphql.schema.types.InstallmentFrequency installmentFrequency) {
        return switch (installmentFrequency) {
            case YEARLY -> InstallmentFrequency.YEARLY;
            case QUARTERLY -> InstallmentFrequency.QUARTERLY;
            case MONTHLY -> InstallmentFrequency.MONTHLY;
        };
    }

    private pl.sg.graphql.schema.types.InstallmentFrequency map(InstallmentFrequency installmentFrequency) {
        return switch (installmentFrequency) {
            case YEARLY -> pl.sg.graphql.schema.types.InstallmentFrequency.YEARLY;
            case QUARTERLY -> pl.sg.graphql.schema.types.InstallmentFrequency.QUARTERLY;
            case MONTHLY -> pl.sg.graphql.schema.types.InstallmentFrequency.MONTHLY;
        };
    }
}
