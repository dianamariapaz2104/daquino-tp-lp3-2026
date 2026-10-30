package py.edu.uc.lp3.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import py.edu.uc.lp3.domain.Agente;

@Repository
public interface AgenteRepository extends JpaRepository<Agente, Long> {
}
