package com.telecom.inventory.repository;

import com.telecom.inventory.entity.Reservation;
import com.telecom.inventory.entity.ReservationStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  Optional<Reservation> findByReservationNumber(String reservationNumber);

  Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);

  List<Reservation> findByOrderIdOrderByReservedAtDesc(Long orderId);

  List<Reservation> findByResourceId(Long resourceId);
}
