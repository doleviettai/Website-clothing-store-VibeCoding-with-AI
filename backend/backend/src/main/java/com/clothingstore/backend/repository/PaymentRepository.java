package com.clothingstore.backend.repository;

import com.clothingstore.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>, JpaSpecificationExecutor<Payment> {

    Optional<Payment> findByTransactionCode(String transactionCode);

    List<Payment> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
