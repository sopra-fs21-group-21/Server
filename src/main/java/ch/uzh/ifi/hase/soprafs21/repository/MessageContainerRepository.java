package ch.uzh.ifi.hase.soprafs21.repository;

import ch.uzh.ifi.hase.soprafs21.entity.MessageContainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository("messageContainerRepository")
public interface MessageContainerRepository extends JpaRepository<MessageContainer, Long> {
    Optional<MessageContainer> findByPortfolioId(long id);
}
