package pl.sg.loans.components;

import org.springframework.stereotype.Component;
import pl.sg.loans.model.InstallmentFrequency;

import java.time.LocalDate;

@Component
public class LoanMapper {

    public pl.sg.graphql.schema.types.Loan fromEntityToGraphqlModel(pl.sg.loans.entities.Loan loan) {
        return pl.sg.graphql.schema.types.Loan.newBuilder()
                .id(loan.getPublicId())
                .start(loan.getStart())
                .amount(loan.getAmount())
                .rate(loan.getRate())
                .numberOfInstallments(loan.getNumberOfInstallments())
                .installmentType(fromEntityToGraphqlModel(loan.getInstallmentType()))
                .installmentFrequency(fromEntityToGraphqlModel(loan.getInstallmentFrequency()))
                .build();
    }

    public pl.sg.graphql.schema.types.InstallmentType fromEntityToGraphqlModel(pl.sg.loans.model.InstallmentType installmentType) {
        return switch (installmentType) {
            case DECLINING -> pl.sg.graphql.schema.types.InstallmentType.DECLINING;
            case FIXED -> pl.sg.graphql.schema.types.InstallmentType.FIXED;
        };
    }

    public pl.sg.graphql.schema.types.InstallmentFrequency fromEntityToGraphqlModel(pl.sg.loans.model.InstallmentFrequency installmentFrequency) {
        return switch (installmentFrequency) {
            case YEARLY -> pl.sg.graphql.schema.types.InstallmentFrequency.YEARLY;
            case QUARTERLY -> pl.sg.graphql.schema.types.InstallmentFrequency.QUARTERLY;
            case MONTHLY -> pl.sg.graphql.schema.types.InstallmentFrequency.MONTHLY;
        };
    }

    public pl.sg.loans.model.Loan fromEntityToModel(pl.sg.loans.entities.Loan loan) {
        return new pl.sg.loans.model.Loan(
                loan.getStart(),
                loan.getAmount(),
                loan.getNumberOfInstallments(),
                loan.getInstallmentType(),
                loan.getInstallmentFrequency()
        );
    }

    public pl.sg.graphql.schema.types.Installment fromEntityToGraphqlModel(pl.sg.loans.model.Installment installment, LocalDate loanStart, InstallmentFrequency installmentFrequency) {
        return pl.sg.graphql.schema.types.Installment.newBuilder()
                .installmentIndex(installment.installmentIndex().index())
                .amount(installment.installment())
                .periodStart(installment.periodStart(loanStart, installmentFrequency))
                .periodEnd(installment.periodEnd(loanStart, installmentFrequency))
                .remainingCapitalAtPeriodStart(installment.remainingCapitalAtPeriodStart())
                .capital(installment.capital())
                .overpayment(installment.overpayment())
                .interest(installment.interest())
                .build();
    }

    public pl.sg.loans.model.InstallmentType fromGraphqlModelToModel(pl.sg.graphql.schema.types.InstallmentType installmentType) {
        return switch (installmentType) {
            case DECLINING -> pl.sg.loans.model.InstallmentType.DECLINING;
            case FIXED -> pl.sg.loans.model.InstallmentType.FIXED;
        };
    }

    public pl.sg.loans.model.InstallmentFrequency fromGraphqlModelToModel(pl.sg.graphql.schema.types.InstallmentFrequency installmentFrequency) {
        return switch (installmentFrequency) {
            case YEARLY -> pl.sg.loans.model.InstallmentFrequency.YEARLY;
            case QUARTERLY -> pl.sg.loans.model.InstallmentFrequency.QUARTERLY;
            case MONTHLY -> pl.sg.loans.model.InstallmentFrequency.MONTHLY;
        };
    }
}
