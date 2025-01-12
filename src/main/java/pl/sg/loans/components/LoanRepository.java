package pl.sg.loans.components;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.sg.loans.entities.Loan;

import java.util.Optional;
import java.util.UUID;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    Optional<Loan> findByPublicId(UUID publicId);
}
