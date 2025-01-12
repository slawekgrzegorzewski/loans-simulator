package pl.sg.loans.graphql;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import com.netflix.graphql.dgs.exceptions.DgsEntityNotFoundException;
import org.joda.money.Money;
import pl.sg.graphql.schema.types.*;
import pl.sg.loans.components.LoanMapper;
import pl.sg.loans.components.LoansService;
import pl.sg.loans.model.InstallmentIndex;
import pl.sg.loans.simulator.LoanRepaymentSimulator;
import pl.sg.loans.simulator.OverpaymentProvider;
import pl.sg.loans.simulator.overpayment.ConstantBudgetOverpayment;
import pl.sg.loans.simulator.overpayment.CustomOverpayment;
import pl.sg.loans.simulator.overpayment.NoOverpayment;

import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@DgsComponent
public class LoansDataFetcher {

    private final LoanMapper loanMapper;
    private final LoanRepaymentSimulator loanRepaymentSimulator;
    private final LoansService loansService;

    public LoansDataFetcher(LoanMapper loanMapper, LoanRepaymentSimulator loanRepaymentSimulator, LoansService loansService) {
        this.loanMapper = loanMapper;
        this.loanRepaymentSimulator = loanRepaymentSimulator;
        this.loansService = loansService;
    }

    @DgsQuery
    public List<Loan> loans() {
        return loansService.getLoans().stream()
                .map(loanMapper::fromEntityToGraphqlModel)
                .toList();
    }

    @DgsQuery
    public LoanSimulationResult regularSimulation(@InputArgument SimpleLoanSimulationInput input) {
        return simulate(
                input.getLoanId(),
                _ -> new NoOverpayment());
    }

    @DgsQuery
    public LoanSimulationResult constantBudgetSimulation(@InputArgument ConstantBudgetLoanSimulationInput input) {
        return simulate(
                input.getLoanId(),
                loan -> new ConstantBudgetOverpayment(Money.of(loan.getAmount().getCurrencyUnit(), input.getConstantBudget(), RoundingMode.HALF_EVEN)));
    }

    @DgsQuery
    public LoanSimulationResult customOverpaymentSimulation(@InputArgument CustomOverpaymentLoanSimulationInput input) {
        return simulate(
                input.getLoanId(),
                loan -> new CustomOverpayment(
                        input.getOverpaymentsSchedule()
                                .stream()
                                .collect(Collectors.toMap(
                                        overpaymentPerInstallment -> InstallmentIndex.of(overpaymentPerInstallment.getInstallmentIndex()),
                                        overpaymentPerInstallment -> Money.of(loan.getAmount().getCurrencyUnit(), overpaymentPerInstallment.getOverpayment(), RoundingMode.HALF_EVEN)
                                ))));
    }

    private LoanSimulationResult simulate(UUID loanId, Function<pl.sg.loans.entities.Loan, OverpaymentProvider> overpaymentProvider) {
        pl.sg.loans.entities.Loan loan = loansService.find(loanId).orElseThrow(DgsEntityNotFoundException::new);
        List<Installment> installments = loanRepaymentSimulator.simulate(
                        loanMapper.fromEntityToModel(loan),
                        overpaymentProvider.apply(loan),
                        loan.getRate())
                .stream()
                .map(installment -> loanMapper.fromEntityToGraphqlModel(installment, loan.getStart(), loan.getInstallmentFrequency()))
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
                                loanMapper.fromGraphqlModelToModel(input.getInstallmentType()),
                                loanMapper.fromGraphqlModelToModel(input.getInstallmentFrequency())
                        ).getPublicId()
                ).build();
    }
}