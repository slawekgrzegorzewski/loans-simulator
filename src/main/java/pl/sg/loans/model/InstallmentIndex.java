package pl.sg.loans.model;

import org.jetbrains.annotations.NotNull;

public record InstallmentIndex(int index) {

    public static InstallmentIndex of(int index) {
        return new InstallmentIndex(index);
    }

    public static InstallmentIndex parse(@NotNull String value) {
        return new InstallmentIndex(Integer.parseInt(value));
    }

    public int number() {
        return index + 1;
    }

    public InstallmentIndex next() {
        return of(index + 1);
    }
}
