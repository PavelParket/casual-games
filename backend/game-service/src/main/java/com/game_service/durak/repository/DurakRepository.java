package com.game_service.durak.repository;

import com.game_service.durak.domain.entity.Durak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DurakRepository extends JpaRepository<Durak, Long> {
}
