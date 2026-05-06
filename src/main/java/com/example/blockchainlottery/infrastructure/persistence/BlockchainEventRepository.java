package com.example.blockchainlottery.infrastructure.persistence;

import com.example.blockchainlottery.domain.BlockchainEvent;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BlockchainEventRepository extends JpaRepository<BlockchainEvent, Long>, JpaSpecificationExecutor<BlockchainEvent> {

    Optional<BlockchainEvent> findByChainIdAndTxHashAndLogIndex(String chainId, String txHash, Long logIndex);
    Optional<BlockchainEvent> findFirstByTxHashIgnoreCaseAndLogIndex(String txHash, Long logIndex);
    void deleteByChainIdAndBlockNumberGreaterThanEqual(String chainId, Long blockNumber);

    Page<BlockchainEvent> findAllByOrderByBlockNumberDescLogIndexDesc(Pageable pageable);
}
