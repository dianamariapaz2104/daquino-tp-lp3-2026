package py.edu.uc.lp3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import py.edu.uc.lp3.domain.JugadaEscenario;

@Repository
public interface JugadaEscenarioRepository extends JpaRepository<JugadaEscenario, Long> {
}
