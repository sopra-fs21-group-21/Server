package ch.uzh.ifi.hase.soprafs21.repository;

import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("portfolioRepository")
public interface PortfolioRepository extends JpaRepository <Portfolio, Long> {
    Optional<Portfolio> findPortfolioByPortfolioCode(String portfolioCode);
    boolean existsByPortfolioName(String portfolioName);
}