package com.example.blockchainlottery.infrastructure.persistence;

import com.example.blockchainlottery.domain.ListenerState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ListenerStateRepository extends JpaRepository<ListenerState, Long> {
}
