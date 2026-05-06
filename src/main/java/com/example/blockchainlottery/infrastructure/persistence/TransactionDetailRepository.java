package com.example.blockchainlottery.infrastructure.persistence;

import com.example.blockchainlottery.domain.TransactionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionDetailRepository extends JpaRepository<TransactionDetail, String> {
}
