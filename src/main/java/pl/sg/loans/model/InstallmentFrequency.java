package pl.sg.loans.model;

public enum InstallmentFrequency {
    MONTHLY(12), QUARTERLY(4), YEARLY(1);

    private final int numberOfPeriodsInYear;

    InstallmentFrequency(int numberOfPeriodsInYear) {
        this.numberOfPeriodsInYear = numberOfPeriodsInYear;
    }

    public int numberOfPeriodsInYear() {
        return numberOfPeriodsInYear;
    }
}
