package ch.uzh.ifi.hase.soprafs21.repository;

import ch.uzh.ifi.hase.soprafs21.entity.Portfolio;
import ch.uzh.ifi.hase.soprafs21.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository("portfolioRepository")
public interface PortfolioRepository extends JpaRepository <Portfolio, Long> {

}